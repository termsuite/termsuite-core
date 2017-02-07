package fr.univnantes.termsuite.framework.service;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import java.util.Collection;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Semaphore;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.inject.Inject;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import fr.univnantes.termsuite.api.TermOrdering;
import fr.univnantes.termsuite.engines.gatherer.VariationType;
import fr.univnantes.termsuite.framework.TermSuiteFactory;
import fr.univnantes.termsuite.index.Terminology;
import fr.univnantes.termsuite.model.Lang;
import fr.univnantes.termsuite.model.OccurrenceStore;
import fr.univnantes.termsuite.model.Relation;
import fr.univnantes.termsuite.model.RelationProperty;
import fr.univnantes.termsuite.model.RelationType;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.TermWord;
import fr.univnantes.termsuite.model.Word;
import fr.univnantes.termsuite.utils.TermSuiteUtils;
import fr.univnantes.termsuite.utils.TermUtils;

public class TerminologyService {
	private static final String MSG_NOT_AN_EXTENSION = "Term '%s' is no extension of term '%s'";
	private static final String MSG_TERM_NOT_FOUND = "No such term with key %s";
	private static final String MSG_REFLEXION_NOT_ALLOWED = "reflexive relations not allowed: %s --%s--> %s";
	private static final String MSG_DUPLICATE_RELATION = "Relation %s already exists in termino";

	private Terminology termino;
	
	public TerminologyService(Terminology termino) {
		super();
		this.termino = termino;
	}

	@Inject
	OccurrenceStore occurrenceStore;
	
	@Inject
	private IndexService indexService;

	private Multimap<Term, Relation> inboundRelations =  null;
	private Multimap<Term, Relation> outboundRelations =  null;
	
	private Semaphore relationMutex = new Semaphore(1);
	private Semaphore termMutex = new Semaphore(1);
	
	private synchronized void initRelations() {
		if(inboundRelations == null) {
			inboundRelations = HashMultimap.create(); 
			outboundRelations = HashMultimap.create(); 
			this.termino.getRelations().forEach(r-> {
				outboundRelations.put(r.getFrom(), r);
				inboundRelations.put(r.getTo(), r);
			});
		}
	}
	
	private Multimap<Term, Relation> getOutboundRelations() {
		initRelations();
		return outboundRelations;
	}

	private Multimap<Term, Relation> getInboundRelations() {
		initRelations();
		return inboundRelations;
	}
	

	public RelationService getRelationOrCreate(Term from, RelationType type, Term to) {
		RelationService r;
		relationMutex.acquireUninterruptibly();
		Optional<RelationService> relation = getRelation(from, type, to);
		if(relation.isPresent())
			r =  relation.get();
		else {
			Relation created = new Relation(type, from, to);
			privateAddRelation(created);
			r = asRelationService(created);
		}
		relationMutex.release();
		return r;
	}

	public Optional<RelationService> getRelation(Term from, RelationType type, Term to) {
		return outboundRelations(from).filter(rel -> rel.getTo().equals(to) && type == rel.getType()).findAny();
	}
	
	public Stream<Relation> getRelations(Term from, Term to, RelationType type, RelationType... types) {
		Stream<Relation> stream = this.getOutboundRelations().get(from)
					.stream()
					.filter(relation -> relation.getTo().equals(to));
		
		if(types.length == 0)
			return stream;
		else if(types.length == 0)
			return stream.filter(relation -> relation.getType() == types[0]);
		else {
			Set<RelationType> typeSet = Sets.newHashSet(types);
			return stream.filter(relation -> typeSet.contains(relation.getType()));
		}
	}

	
	public Stream<RelationService> relations() {
		return this.termino.getRelations()
				.stream()
				.map(this::asRelationService)
				;
	}
	
	public Stream<RelationService> variations() {
		return relations().filter(r -> r.getRelation().getType() == RelationType.VARIATION);
	}
	
	public Stream<RelationService> variations(VariationType type) {
		return variations()
				.filter(r -> r.isVariationOfType(type));
	}

	public Stream<RelationService> relations(RelationType type, RelationType... types) {
		EnumSet<RelationType> typeSet = EnumSet.of(type, types);
		return relations()
				.filter(r -> typeSet.contains(r.getRelation().getType()));
	}

	public Collection<TermService> getTerms() {
		return terms().collect(toList());
	}

	public Stream<TermService> terms() {
		return this.termino.getTerms().values()
				.stream()
				.map(this::asTermService)
				;
	}

	private ConcurrentMap<Term, TermService> termServices = new ConcurrentHashMap<>();
	public TermService asTermService(Term term) {
		return termServices.computeIfAbsent(
				term, t -> 
				new TermService(this, t));
	}

	private ConcurrentMap<Relation, RelationService> relationServices = new ConcurrentHashMap<>();

	public RelationService asRelationService(Relation relation) {
		return relationServices.computeIfAbsent(
				relation, r -> 
				new RelationService(this, r));
	}

	public Stream<RelationService> relations(Term from, Term to) {
		return outboundRelations(from)
						.filter(r-> r.getTo().equals(to));
	}


	public Stream<RelationService> inboundRelations(Term target) {
		return this.getInboundRelations()
				.get(target)
				.stream()
				.map(this::asRelationService);
	}
	
	public Stream<RelationService> inboundRelations(Term target, RelationType rType, RelationType... relationTypes) {
		Set<RelationType> rTypes = EnumSet.of(rType, relationTypes);
		return inboundRelations(target)
				.filter(r->rTypes.contains(r.getRelation().getType()));
	}
	
	public Stream<RelationService> outboundRelations(Term source) {
		return this.getOutboundRelations()
				.get(source)
				.stream()
				.map(this::asRelationService)
				;
	}

	public Stream<RelationService> outboundRelations(Term source, RelationType rType, RelationType... relationTypes) {
		Set<RelationType> rTypes = EnumSet.of(rType, relationTypes);
		return outboundRelations(source)
				.filter(r->rTypes.contains(r.getRelation().getType()))
				;
	}
	
	public Stream<RelationService> variations(Term from, Term to) {
		return relations(from,to)
				.filter(r-> r.getRelation().getType() == RelationType.VARIATION);
	}

	public synchronized Optional<RelationService> getVariation(Term from, Term to) {
		return variations(from, to).findAny();
	}

	public RelationService createVariation(VariationType variationType, Term from, Term to) {
		Preconditions.checkArgument(!from.equals(to), 
				MSG_REFLEXION_NOT_ALLOWED, from, variationType, to);
		relationMutex.acquireUninterruptibly();
		RelationService r;
		Optional<RelationService> existing = variations(from, to).findAny();
		if(!existing.isPresent()) {
			Relation relation = TermSuiteFactory.createVariation(variationType, from, to);
			relation.setProperty(variationType.getRelationProperty(), true);
			privateAddRelation(relation);
			r = asRelationService(relation);
		} else
			r = existing.get();
		relationMutex.release();
		return r;
	}

	public Stream<RelationService> relations(RelationProperty property, Object value) {
		return relations()
				.filter(r->r.getRelation().isPropertySet(property))
				.filter(r->Objects.equals(r.getRelation().get(property), value))
				;
	}

	public Stream<RelationService> variations(String fromKey, String toKey) {
		return variations(toTerm(fromKey), toTerm(toKey));
	}

	private Term toTerm(String groupingKey) {
		Preconditions.checkArgument(this.termino.getTerms().containsKey(groupingKey), MSG_TERM_NOT_FOUND, groupingKey);
		return this.termino.getTerms().get(groupingKey);
	}
	
	public TermService getTerm(String termKey) {
		TermService term = getTermUnchecked(termKey);
		Preconditions.checkNotNull(term, MSG_TERM_NOT_FOUND, termKey);
		return term;
	}

	public TermService getTermUnchecked(String termKey) {
		return termino.getTerms().containsKey(termKey) ? asTermService(termino.getTerms().get(termKey)) : null;
	}

	public Stream<RelationService> variationsFrom(Term from) {
		return outboundRelations(from)
				.filter(r-> r.getRelation().getType() == RelationType.VARIATION);
	}

	public void removeAll(Predicate<Term> predicate) {
		this.termino.getTerms().values().stream()
			.filter(predicate).collect(toSet())
			.forEach(this::removeTerm);
	}

	public long termCount() {
		return this.termino.getTerms().size();
	}

	public Stream<RelationService> extensions() {
		return relations(RelationType.HAS_EXTENSION);
	}
	
	public Stream<RelationService> extensionBases(Term term) {
		return inboundRelations(term)
				.filter(r-> r.getType() == RelationType.HAS_EXTENSION)
			;
	}
	
	public Word getWord(String lemma) {
		return this.termino.getWords().get(lemma);
	}

	public Stream<RelationService> extensions(Term from) {
		return outboundRelations(from)
				.filter(r-> r.getType() == RelationType.HAS_EXTENSION);
	}

	public long getWordAnnotationsNum() {
		return termino.getNbWordAnnotations().longValue();
	}

	public boolean containsWord(String lemma) {
		return this.termino.getWords().containsKey(lemma);
	}

	public int wordCount() {
		return this.termino.getWords().size();
	}

	public Collection<Word> getWords() {
		return this.termino.getWords().values();
	}

	public Stream<RelationService> extensions(Term from, Term to) {
		return relations(from, to).filter(r->r.getType() == RelationType.HAS_EXTENSION);
	}

	public Lang getLang() {
		return this.termino.getLang();
	}

	public boolean containsTerm(String gKey) {
		return this.termino.getTerms().containsKey(gKey);
	}
	
	public void addTerm(Term term) {
		termMutex.acquireUninterruptibly();
		Preconditions.checkArgument(
				!containsTerm(term.getGroupingKey()));
		this.termino.getTerms().put(term.getGroupingKey(), term);
		for(TermWord tw:term.getWords())
			privateAddWord(tw.getWord(), false);
		indexService.addTerm(term);
		termMutex.release();
	}

	public void addWord(Word word) {
		privateAddWord(word, true);
	}

	private void privateAddWord(Word word, boolean failIfAlredyPresent) {
		if(failIfAlredyPresent)
			Preconditions.checkArgument(
				!this.termino.getWords().containsKey(word.getLemma()));
		this.termino.getWords().put(word.getLemma(), word);
	}
	
	public void cleanOrphanWords() {
		Set<String> usedWordLemmas = Sets.newHashSet();
		for(TermService t:getTerms()) {
			for(TermWord tw:t.getTerm().getWords())
				usedWordLemmas.add(tw.getWord().getLemma());
		}
		Iterator<Entry<String, Word>> it = this.termino.getWords().entrySet().iterator();
		Entry<String, Word> entry;
		while (it.hasNext()) {
			entry = it.next();
			if(!usedWordLemmas.contains(entry.getValue().getLemma()))
				it.remove();
		}
	}
	
	public void removeTerm(TermService t) {
		removeTerm(t.getTerm());
	}
	
	public void removeTerm(Term t) {
		termMutex.acquireUninterruptibly();
		this.termino.getTerms().remove(t.getGroupingKey());
		// remove from variants
		List<Relation> toRem = Lists.newLinkedList();
		toRem.addAll(this.getOutboundRelations().get(t));
		toRem.addAll(this.getInboundRelations().get(t));
		removeRelations(toRem);
		
		/*
		 * Removes from context vectors.
		 * 
		 * We assumes that if this term has a context vector 
		 * then all others terms may have this term as co-term,
		 * thus they must be checked from removal.
		 * 
		 */
		if(t.getContext() != null) {
			for(Term o:this.termino.getTerms().values()) {
				if(o.getContext() != null)
					o.getContext().removeCoTerm(t);
			}
		}
		indexService.removeTerm(t);
		occurrenceStore.removeTerm(t);
		termMutex.release();
	}

	
	public void incrementWordAnnotationNum(int nbWordAnnotations) {
		this.termino.getNbWordAnnotations().addAndGet(nbWordAnnotations);
	}

	public void incrementSpottedTermsNum(int nbSpottedTerms) {
		this.termino.getNbSpottedTerms().addAndGet(nbSpottedTerms);
	}

	public void addRelation(Relation termVariation) {
		relationMutex.acquireUninterruptibly();
		privateAddRelation(termVariation);
		relationMutex.release();
	}

	/*
	 * Must be invoked under mutex
	 */
	private void privateAddRelation(Relation relation) {
		Preconditions.checkArgument(!relation.getFrom().equals(relation.getTo()),
				MSG_REFLEXION_NOT_ALLOWED,
				relation.getFrom(),
				relation.getType(),
				relation.getTo()
				);
		initRelations();
		Preconditions.checkArgument(
				!this.termino.getRelations().contains(relation),
				MSG_DUPLICATE_RELATION, relation);
		this.termino.getRelations().add(relation);
		this.getOutboundRelations().put(relation.getFrom(), relation);
		this.getInboundRelations().put(relation.getTo(), relation);
	}

	public void removeRelation(RelationService variation) {
		removeRelation(variation.getRelation());
	}
	
	public void removeRelations(Collection<Relation> relations) {
		relationMutex.acquireUninterruptibly();
		this.termino.getRelations().removeAll(relations);
		for(Relation relation:relations) {
			this.getOutboundRelations().remove(relation.getFrom(), relation);
			this.getInboundRelations().remove(relation.getTo(), relation);
		}
		relationMutex.release();
	}


	public void removeRelation(Relation relation) {
		relationMutex.acquireUninterruptibly();
		this.termino.getRelations().remove(relation);
		this.getOutboundRelations().remove(relation.getFrom(), relation);
		this.getInboundRelations().remove(relation.getTo(), relation);
		relationMutex.release();
	}

	public List<TermService> getTerms(TermOrdering ordering) {
		return terms()
				.map(TermService::getTerm)
				.sorted(ordering.toComparator())
				.map(this::asTermService)
				.collect(toList());
	}

	public static Comparator<TermService> toTermServiceComparator(Comparator<Term> comparator) {
		return new Comparator<TermService>() {
			@Override
			public int compare(TermService o1, TermService o2) {
				return comparator.compare(o1.getTerm(), o2.getTerm());
			}
		};
	}
	

	public Stream<TermService> terms(Comparator<Term> comparator) {
		return terms()
				.sorted(toTermServiceComparator(comparator));
	}
	
	
	public Stream<TermService> terms(TermOrdering termOrdering) {
		return terms()
				.sorted(termOrdering.toTermServiceComparator());
	}
	
	
	/**
	 * 
	 * Finds in a {@link Terminology} the biggest extension affix term of a term depending 
	 * on a base term.
	 * 
	 * For example, the term "offshore wind turbine" is an extension of 
	 * "wind turbine". The extension affix is the term "offshore".
	 * 
	 * @param base
	 * 			The base term
	 * @param extension
	 * 			The extension term
	 * @return
	 * 		the extension affix found in <code>termino</code>, <code>null</code> if none
	 * 		has been found.
	 * @throws IllegalArgumentException if <code>extension</code> id not an 
	 * 			extension of the term <code>base</code>.
	 */
	public Optional<TermService> getExtensionAffix(Term base, Term extension) {
		int index = TermUtils.getPosition(base, extension);
		if(index == -1)
			throw new IllegalStateException(String.format(MSG_NOT_AN_EXTENSION, 
					extension,
					base)
				);

		/*
		 *  true if prefix, false if suffix
		 */
		boolean isPrefix = false;
		if(index == 0)
			isPrefix = true;
		else if(index + base.getWords().size() == extension.getWords().size())
			isPrefix = false; // suffix
		else {
			/*
			 * Happens sometimes. 
			 * 
			 * base = 		'nnnn: hd spring spring spring' 
			 * extension = 	'nn: spring spring'
			 * 
			 * Do nothing.
			 */
		}
		
		if(isPrefix) 
			return findBiggestSuffix(
					extension.getWords().subList(index + base.getWords().size(), extension.getWords().size())
				);
		else
			return findBiggestPrefix(
					extension.getWords().subList(0, index)
				);
	}
	
	
	/**
	 * Finds in a {@link Terminology} the biggest prefix of a sequence of
	 * {@link TermWord}s that exists as a term.
	 * 
	 * @param words
	 * 			the initial sequence of {@link TermWord}s
	 * @return
	 * 			A {@link Term} found in <code>termino</code> that makes the
	 * 			biggest possible prefix sequence for <code>words</code>.
	 */
	public Optional<TermService> findBiggestPrefix(List<TermWord> words) {
		String gKey;
		for(int i = words.size(); i > 0 ; i--) {
			gKey = TermSuiteUtils.getGroupingKey(words.subList(0, i));
			if(containsTerm(gKey))
				return Optional.of(getTerm(gKey));
		}
		return Optional.empty();
	}
	

	/**
	 * Finds in a {@link Terminology} the biggest suffix of a sequence of
	 * {@link TermWord}s that exists as a term.
	 * 
	 * @param words
	 * 			the initial sequence of {@link TermWord}s
	 * @return
	 * 			A {@link Term} found in <code>termino</code> that makes the
	 * 			biggest possible suffix sequence for <code>words</code>.

	 */
	public Optional<TermService> findBiggestSuffix(List<TermWord> words) {
		String gKey;
		for(int i = 0; i < words.size() ; i++) {
			gKey = TermSuiteUtils.getGroupingKey(words.subList(i, words.size()));
			if(containsTerm(gKey))
				return Optional.of(getTerm(gKey));
		}
		return Optional.empty();
	}

	public Optional<TermService> getSwt(TermWord tw) {
		String groupingKey = TermUtils.toGroupingKey(tw);
		if(containsTerm(groupingKey))
			return Optional.of(getTerm(groupingKey));
		else
			return Optional.empty();
	}

	public void addRelations(Set<Relation> relations) {
		relationMutex.acquireUninterruptibly();
		for(Relation r:relations)
			privateAddRelation(r);
		relationMutex.release();
	}
}
