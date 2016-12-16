package fr.univnantes.termsuite.framework;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.google.common.base.Preconditions;

import fr.univnantes.termsuite.engines.gatherer.VariationType;
import fr.univnantes.termsuite.model.RelationProperty;
import fr.univnantes.termsuite.model.RelationType;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.TermRelation;
import fr.univnantes.termsuite.model.Terminology;

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

	public Stream<TermRelation> outboundRelations(Term source) {
		return this.termino.getOutboundRelation()
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
}
