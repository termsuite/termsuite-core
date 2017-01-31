package fr.univnantes.termsuite.engines.postproc;

import org.apache.commons.lang.mutable.MutableInt;

import fr.univnantes.termsuite.engines.SimpleEngine;
import fr.univnantes.termsuite.model.RelationProperty;
import fr.univnantes.termsuite.model.RelationType;

public class VariationRanker extends SimpleEngine {

	@Override
	public void execute() {
		terminology.getTerms().forEach(t-> {
			final MutableInt vrank = new MutableInt(0);
			terminology.outboundRelations(t, RelationType.VARIATION)
				.sorted(RelationProperty.VARIANT_SCORE.getComparator(true))
				.forEach(rel -> {
					vrank.increment();
					rel.setProperty(RelationProperty.VARIATION_RANK, vrank.intValue());
				});
		});
	}
}
