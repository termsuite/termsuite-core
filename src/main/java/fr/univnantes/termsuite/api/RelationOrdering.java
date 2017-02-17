package fr.univnantes.termsuite.api;

import java.util.Comparator;

import fr.univnantes.termsuite.framework.service.RelationService;
import fr.univnantes.termsuite.model.Relation;
import fr.univnantes.termsuite.model.RelationProperty;

public class RelationOrdering extends Ordering<RelationProperty, Relation, RelationOrdering> {

	public static RelationOrdering byRank() {
		return new RelationOrdering().asc(RelationProperty.VARIATION_RANK).desc(RelationProperty.VARIANT_SCORE);
	}

	public Comparator<? super RelationService> toServiceComparator() {
		final Comparator<Relation> termComp = toComparator();
		Comparator<RelationService> comparator = new Comparator<RelationService>() {
			@Override
			public int compare(RelationService o1, RelationService o2) {
				return termComp.compare(o1.getRelation(), o2.getRelation());
			}
		};
		return comparator;

	}
}
