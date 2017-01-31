package fr.univnantes.termsuite.framework.service;

import static java.util.stream.Collectors.toSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.inject.Inject;

import com.google.common.base.Preconditions;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Sets;

import fr.univnantes.termsuite.engines.gatherer.VariationType;
import fr.univnantes.termsuite.index.Terminology;
import fr.univnantes.termsuite.model.Lang;
import fr.univnantes.termsuite.model.OccurrenceStore;
import fr.univnantes.termsuite.model.RelationProperty;
import fr.univnantes.termsuite.model.RelationType;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.TermProperty;
import fr.univnantes.termsuite.model.TermRelation;
import fr.univnantes.termsuite.model.TermWord;
import fr.univnantes.termsuite.model.Word;
import fr.univnantes.termsuite.utils.TermUtils;

public class TerminologyService {
	
	private static final String MSG_TERM_NOT_FOUND = "No such term with key %s";

	@Inject
	private Terminology termino;

	@Inject
	OccurrenceStore occurrenceStore;
	
	@Inject
	private IndexService indexService;

	private Multimap<Term, TermRelation> inboundVariations =  null;
	
	private Semaphore relationMutex = new Semaphore(1);
	private Semaphore inboundMutex = new Semaphore(1);

	
	private Multimap<Term, TermRelation> getInboundRelations() {
		inboundMutex.acquireUninterruptibly();
		if(inboundVariations == null) {
			LinkedListMultimap<Term, TermRelation> map = LinkedListMultimap.create();
			this.termino.getOutboundRelations().values().forEach(r-> {
				map.put(r.getTo(), r);
			});
			inboundVariations = Multimaps.synchronizedListMultimap(map);
		}
		inboundMutex.release();
		return inboundVariations;
	}
	
	public Stream<TermRelation> getRelations(Term from, Term to, RelationType... types) {
		Stream<TermRelation> stream = this.termino.getOutboundRelations().get(from)
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

	
	public Stream<TermRelation> relations() {
		return termino.getOutboundRelations().values().stream();
	}
	
	public Stream<TermRelation> variations() {
		return relations().filter(r -> r.getType() == RelationType.VARIATION);
	}
	
	public Stream<TermRelation> variations(VariationType type) {
		Predicate<? super TermRelation> havingVariationType = havingProperty(RelationProperty.VARIATION_TYPE);
		return variations()
				.filter(havingVariationType)
				.filter(r -> r.get(RelationProperty.VARIATION_TYPE) == type);
	}

	private Predicate<? super TermRelation> havingProperty(RelationProperty property) {
		return new Predicate<TermRelation>() {
			@Override
			public boolean test(TermRelation t) {
				return t.isPropertySet(property);
			}
		};
	}

	public Stream<TermRelation> relations(RelationType type, RelationType... types) {
		EnumSet<RelationType> typeSet = EnumSet.of(type, types);
		return relations()
				.filter(r -> typeSet.contains(r.getType()));
	}

	public Collection<Term> getTerms() {
		return termino.getTerms().values();
	}

	public Stream<Term> terms() {
		return termino.getTerms().values().stream();
	}

	public Stream<TermRelation> relations(Term from, Term to) {
		return outboundRelations(from)
						.filter(r-> r.getTo().equals(to));
	}


	public Stream<TermRelation> inboundRelations(Term target) {
		return this.getInboundRelations()
				.get(target)
				.stream();
	}
	
	public Stream<TermRelation> inboundRelations(Term target, RelationType rType, RelationType... relationTypes) {
		Set<RelationType> rTypes = EnumSet.of(rType, relationTypes);
		return inboundRelations(target)
				.filter(r->rTypes.contains(r.getType()));
	}
	
	public Stream<TermRelation> outboundRelations(Term source) {
		return this.termino.getOutboundRelations().get(source)
			.stream();
	}

	public Stream<TermRelation> outboundRelations(Term source, RelationType rType, RelationType... relationTypes) {
		Set<RelationType> rTypes = EnumSet.of(rType, relationTypes);
		return outboundRelations(source)
				.filter(r->rTypes.contains(r.getType()));
	}
	
	public Stream<TermRelation> variations(Term from, Term to) {
		return relations(from,to)
				.filter(r-> r.getType() == RelationType.VARIATION);
	}

	public synchronized Optional<TermRelation> getVariation(Term from, Term to) {
		return variations(from, to).findAny();
	}

	public TermRelation createVariation(VariationType variationType, Term from, Term to) {
		relationMutex.acquireUninterruptibly();
		TermRelation r;
		Optional<TermRelation> existing = variations(from, to).findAny();
		if(!existing.isPresent()) {
			TermRelation relation = new TermRelation(RelationType.VARIATION, from, to);
			for(VariationType vType:VariationType.values())
				relation.setProperty(vType.getRelationProperty(), false);
			relation.setProperty(RelationProperty.VARIATION_TYPE, variationType);
			relation.setProperty(variationType.getRelationProperty(), true);
			privateAddRelation(relation);
			r = relation;
		} else
			r = existing.get();
		relationMutex.release();
		return r;
	}

	public Stream<TermRelation> relations(RelationProperty property, Object value) {
		return relations()
				.filter(r->r.isPropertySet(property))
				.filter(r->Objects.equals(r.get(property), value))
				;
	}

	public Stream<TermRelation> variations(String fromKey, String toKey) {
		return variations(getTerm(fromKey), getTerm(toKey));
	}

	public Term getTerm(String termKey) {
		Term term = getTermUnchecked(termKey);
		Preconditions.checkNotNull(term, MSG_TERM_NOT_FOUND, termKey);
		return term;
	}

	public Term getTermUnchecked(String termKey) {
		return termino.getTerms().get(termKey);
	}

	public Stream<TermRelation> variationsFrom(Term from) {
		return outboundRelations(from)
				.filter(r-> r.getType() == RelationType.VARIATION);
	}

	public void removeAll(Predicate<Term> predicate) {
		terms().filter(predicate).collect(toSet()).forEach(this::removeTerm);
	}

	public long termCount() {
		return this.termino.getTerms().size();
	}

	public Stream<TermRelation> extensions() {
		return relations(RelationType.HAS_EXTENSION);
	}
	
	public Stream<TermRelation> extensionBases(Term term) {
		return inboundRelations(term)
				.filter(r-> r.getType() == RelationType.HAS_EXTENSION)
			;
	}
	
	public Word getWord(String lemma) {
		return this.termino.getWords().get(lemma);
	}

	public Stream<TermRelation> extensions(Term from) {
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

	public Stream<TermRelation> extensions(Term from, Term to) {
		return relations(from, to).filter(r->r.getType() == RelationType.HAS_EXTENSION);
	}

	public Lang getLang() {
		return this.termino.getLang();
	}

	public boolean containsTerm(String gKey) {
		return this.termino.getTerms().containsKey(gKey);
	}
	
	public void addTerm(Term term) {
		Preconditions.checkArgument(
				!containsTerm(term.getGroupingKey()));
		this.termino.getTerms().put(term.getGroupingKey(), term);
		for(TermWord tw:term.getWords()) {
			privateAddWord(tw.getWord(), false);
			if(!tw.isSwt())
				// try to set swt manually
				tw.setSwt(this.termino.getTerms().containsKey(TermUtils.toGroupingKey(tw)));
		}
		indexService.addTerm(term);
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
		for(Term t:getTerms()) {
			for(TermWord tw:t.getWords())
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
	
	public void removeTerm(Term t) {
		
		this.termino.getTerms().remove(t.getGroupingKey());
		// remove from variants
		List<TermRelation> toRem = Lists.newLinkedList();
		toRem.addAll(this.termino.getOutboundRelations().get(t));
		toRem.addAll(this.getInboundRelations().get(t));
		toRem.forEach(this::removeRelation);
		
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
	}

	
	public void incrementWordAnnotationNum(int nbWordAnnotations) {
		this.termino.getNbWordAnnotations().addAndGet(nbWordAnnotations);
	}

	public void incrementSpottedTermsNum(int nbSpottedTerms) {
		this.termino.getNbSpottedTerms().addAndGet(nbSpottedTerms);
	}

	public void addRelation(TermRelation termVariation) {
		relationMutex.acquireUninterruptibly();
		privateAddRelation(termVariation);
		relationMutex.release();
	}

	
	/*
	 * Must be invoked under mutex
	 */
	private void privateAddRelation(TermRelation termVariation) {
		/*
		 * Do not delete: First synchronize inbound with outbound
		 */
		this.getInboundRelations();
		
		/*
		 * Then add the relation in both
		 */
		this.termino.getOutboundRelations().put(termVariation.getFrom(), termVariation);
		this.getInboundRelations().put(termVariation.getTo(), termVariation);
	}

	public void removeRelation(TermRelation variation) {
		relationMutex.acquireUninterruptibly();
		this.termino.getOutboundRelations().remove(variation.getFrom(), variation);
		this.getInboundRelations().remove(variation.getTo(), variation);
		relationMutex.release();
	}

	public List<Term> getTermsBy(TermProperty property, boolean desc) {
		List<Term> terms = new ArrayList<>(this.termino.getTerms().values());
		Collections.sort(terms, property.getComparator(desc));
		return terms;
	}
}
