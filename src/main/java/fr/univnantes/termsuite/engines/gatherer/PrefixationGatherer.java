package fr.univnantes.termsuite.engines.gatherer;

import fr.univnantes.termsuite.model.RelationType;

public class PrefixationGatherer extends RelationPairBasedGatherer {
	
	@Override
	protected RelationType getRelType() {
		return RelationType.IS_PREFIX_OF;
	}
}
