package fr.univnantes.termsuite.engines.postproc;

import org.apache.commons.lang.mutable.MutableInt;

import fr.univnantes.termsuite.framework.Execute;
import fr.univnantes.termsuite.framework.TerminologyEngine;
import fr.univnantes.termsuite.framework.TerminologyService;
import fr.univnantes.termsuite.model.RelationProperty;
import fr.univnantes.termsuite.model.RelationType;

public class VariationRanker extends TerminologyEngine {

	@Execute
	public void rankVariations(TerminologyService termino) {
		termino.getTerms().forEach(t-> {
			final MutableInt vrank = new MutableInt(0);
			termino.outboundRelations(t, RelationType.VARIATION)
				.sorted(RelationProperty.VARIANT_SCORE.getComparator(true))
				.forEach(rel -> {
					vrank.increment();
					rel.setProperty(RelationProperty.VARIATION_RANK, vrank.intValue());
				});
		});
	}
}
