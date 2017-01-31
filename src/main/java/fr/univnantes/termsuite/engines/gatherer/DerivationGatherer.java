package fr.univnantes.termsuite.engines.gatherer;

import fr.univnantes.termsuite.model.RelationType;

public class DerivationGatherer extends RelationPairBasedGatherer {
	@Override
	protected RelationType getRelType() {
		return RelationType.DERIVES_INTO;
	}
}
