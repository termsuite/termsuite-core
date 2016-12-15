package fr.univnantes.termsuite.framework;

import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Stream;

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
}
