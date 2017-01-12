package fr.univnantes.termsuite.framework;

import static java.util.stream.Collectors.toSet;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.google.common.base.Preconditions;

import fr.univnantes.termsuite.engines.gatherer.VariationType;
import fr.univnantes.termsuite.model.Lang;
import fr.univnantes.termsuite.model.OccurrenceStore;
import fr.univnantes.termsuite.model.RelationProperty;
import fr.univnantes.termsuite.model.RelationType;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.TermBuilder;
import fr.univnantes.termsuite.model.TermRelation;
import fr.univnantes.termsuite.model.Terminology;
import fr.univnantes.termsuite.model.Word;
import fr.univnantes.termsuite.utils.TermSuiteUtils;

public class TerminologyService {

	private Terminology termino;

	public TerminologyService(Terminology termino) {
		super();
		this.termino = termino;
	}
	
	public Stream<TermRelation> variations() {
		return termino.getRelations(RelationType.VARIATION);
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

	public Stream<TermRelation> relations(RelationType... types) {
		return termino.getRelations(types);
	}

	public Collection<Term> getTerms() {
		return termino.getTerms();
	}

	public Stream<Term> terms() {
		return termino.getTerms().stream();
	}

	public Stream<TermRelation> relations(Term from, Term to) {
		return outboundRelations(from)
						.filter(r-> r.getTo().equals(to));
	}


	public Stream<TermRelation> inboundRelations(Term target) {
		return this.termino.getInboundRelations()
				.get(target)
				.stream();
	}
	
	public Stream<TermRelation> inboundRelations(Term target, RelationType rType, RelationType... relationTypes) {
		Set<RelationType> rTypes = EnumSet.of(rType, relationTypes);
		return inboundRelations(target)
				.filter(r->rTypes.contains(r.getType()));
	}
	

	
	public Stream<TermRelation> outboundRelations(Term source) {
		return this.termino.getOutboundRelations()
			.get(source)
			.stream();
	}

	public Stream<TermRelation> outboundRelations(Term source, RelationType rType, RelationType... relationTypes) {
		Set<RelationType> rTypes = EnumSet.of(rType, relationTypes);
		return outboundRelations(source)
				.filter(r->rTypes.contains(r.getType()));
	}
	
	@Deprecated
	public Terminology getTerminology() {
		return termino;
	}

	public Stream<TermRelation> variations(Term from, Term to) {
		return relations(from,to)
				.filter(r-> r.getType() == RelationType.VARIATION);
	}

	public synchronized Optional<TermRelation> getVariation(Term from, Term to) {
		return variations(from, to).findAny();
	}

	public synchronized TermRelation createVariation(VariationType variationType, Term from, Term to) {
		Optional<TermRelation> existing = variations(from, to).findAny();
		if(!existing.isPresent()) {
			TermRelation relation = buildVariation(variationType, from, to);
			addRelation(relation);
			return relation;
		} else
			return existing.get();
	}

	public synchronized void addRelation(TermRelation relation) {
		termino.addRelation(relation);
	}

	public TermRelation buildVariation(VariationType variationType, Term from, Term to) {
		TermRelation relation = new TermRelation(RelationType.VARIATION, from, to);
		for(VariationType vType:VariationType.values())
			relation.setProperty(vType.getRelationProperty(), false);
		relation.setProperty(RelationProperty.VARIATION_TYPE, variationType);
		relation.setProperty(variationType.getRelationProperty(), true);
		return relation;
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

	public Term getTerm(String toKey) {
		Term to = termino.getTermByGroupingKey(toKey);
		String MSG_TERM_NOT_FOUND = "No such term with key %s";
		Preconditions.checkNotNull(to, MSG_TERM_NOT_FOUND, toKey);
		return to;
	}

	public void removeRelation(TermRelation r) {
		termino.removeRelation(r);
	}

	public void removeTerm(Term r) {
		termino.removeTerm(r);
	}

	public Stream<TermRelation> variationsFrom(Term from) {
		return outboundRelations(from)
				.filter(r-> r.getType() == RelationType.VARIATION);
	}

	public void addTerm(Term term) {
		this.termino.addTerm(term);
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
	

	public Stream<TermRelation> extensions(Term from) {
		return outboundRelations(from)
				.filter(r-> r.getType() == RelationType.HAS_EXTENSION);
	}

	public double getWordAnnotationsNum() {
		return 0;
	}

	private static final String MSG_PATTERN_EMPTY = "Pattern must not be empty";
	private static final String MSG_LEMMAS_EMPTY = "Words array must not be empty";
	private static final String MSG_NOT_SAME_LENGTH = "Pattern and words must have same length";

	public Term createOrGetTerm(String[] pattern, Word[] words) {
		Preconditions.checkArgument(pattern.length > 0, MSG_PATTERN_EMPTY);
		Preconditions.checkArgument(words.length > 0, MSG_LEMMAS_EMPTY);
		Preconditions.checkArgument(words.length == pattern.length, MSG_NOT_SAME_LENGTH);

		String termGroupingKey = TermSuiteUtils.getGroupingKey(pattern, words);
		Term term = this.termino.getTermByGroupingKey(termGroupingKey);
		if(term == null) {
			TermBuilder builder = TermBuilder.start();
			for (int i = 0; i < pattern.length; i++)
				builder.addWord(words[i], pattern[i].toLowerCase());
			builder.setFrequency(0);
			term = builder.create();
		}
		this.termino.addTerm(term);
		return term;
	}

	public Word createOrGetWord(String lemma, String stem) {
		if(termino.getWord(lemma) == null)
			termino.addWord(new Word(lemma, stem));
		return termino.getWord(lemma);
	}

	public synchronized void incrementFrequency(Term term) {
		term.setFrequency(term.getFrequency() + 1);
	}

	public OccurrenceStore getOccurrenceStore() {
		return this.termino.getOccurrenceStore();
	}

	public void incrementWordAnnotationNum(int nbSpottedTerms) {
		this.termino.incSpottedTermsNum(nbSpottedTerms);
	}

	public int wordCount() {
		return this.termino.getWords().size();
	}

	public Collection<Word> getWords() {
		return this.termino.getWords();
	}

	public Stream<TermRelation> extensions(Term from, Term to) {
		return relations(from, to).filter(r->r.getType() == RelationType.HAS_EXTENSION);
	}

	public Lang getLang() {
		return this.termino.getLang();
	}
}
