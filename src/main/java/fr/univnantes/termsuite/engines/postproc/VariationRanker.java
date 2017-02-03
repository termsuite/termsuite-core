package fr.univnantes.termsuite.engines.postproc;

import java.util.concurrent.atomic.AtomicInteger;

import fr.univnantes.termsuite.engines.SimpleEngine;
import fr.univnantes.termsuite.framework.service.RelationService;
import fr.univnantes.termsuite.model.RelationProperty;

public class VariationRanker extends SimpleEngine {

	@Override
	public void execute() {
		terminology.getTerms().forEach(t-> {
			final AtomicInteger vrank = new AtomicInteger(0);
			t.variations()
				.map(RelationService::getRelation)
				.sorted(RelationProperty.VARIANT_SCORE.getComparator(true))
				.forEach(rel -> {
					rel.setProperty(RelationProperty.VARIATION_RANK, vrank.incrementAndGet());
				});
		});
	}
}
