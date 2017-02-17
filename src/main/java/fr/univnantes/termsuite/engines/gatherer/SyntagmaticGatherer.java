package fr.univnantes.termsuite.engines.gatherer;

import fr.univnantes.termsuite.framework.Index;
import fr.univnantes.termsuite.index.TermIndex;
import fr.univnantes.termsuite.index.TermIndexType;

public class SyntagmaticGatherer extends VariationTypeGatherer {
	@Index(type=TermIndexType.ALLCOMP_PAIRS)
	TermIndex termIndex;

	@Override
	protected TermIndex getTermIndex() {
		return termIndex;
	}
}
