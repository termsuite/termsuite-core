package fr.univnantes.termsuite.api;

import fr.univnantes.termsuite.model.RelationProperty;
import fr.univnantes.termsuite.model.Relation;

public class RelationOrdering extends Ordering<RelationProperty, Relation, RelationOrdering>{

	public static RelationOrdering byRank() {
		return new RelationOrdering()
				.asc(RelationProperty.VARIATION_RANK)
				.desc(RelationProperty.VARIANT_SCORE)
				;
	}
}
