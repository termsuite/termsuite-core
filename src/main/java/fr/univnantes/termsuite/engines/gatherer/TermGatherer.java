package fr.univnantes.termsuite.engines.gatherer;

import fr.univnantes.termsuite.framework.AggregateTerminologyEngine;
import fr.univnantes.termsuite.framework.Parameter;
import fr.univnantes.termsuite.model.termino.TermIndexes;

public class TermGatherer extends AggregateTerminologyEngine {

	@Parameter
	private GathererOptions gathererOptions;
	
	@Override
	public void configure() {
		
		pipe(VariationTypeGatherer.class, 
				VariationType.PREFIXATION, 
				TermIndexes.PREFIXATION_LEMMAS, 
				true);
	
		pipe(VariationTypeGatherer.class, 
				VariationType.DERIVATION, 
				TermIndexes.DERIVATION_LEMMAS, 
				true);
	
		pipe(VariationTypeGatherer.class, 
				VariationType.MORPHOLOGICAL, 
				TermIndexes.ALLCOMP_PAIRS, 
				false);
			
		pipe(VariationTypeGatherer.class, 
				VariationType.SYNTAGMATIC, 
				TermIndexes.ALLCOMP_PAIRS, 
				true);

		if(gathererOptions.isSemanticEnabled()) 
			pipe(SemanticGatherer.class, 
					VariationType.SEMANTIC);
		
		/*
		 * Gathers extensions of morpho, derivative, prefix, and semantic variants
		 */
		pipe(ExtensionVariantGatherer.class);
		
		if(gathererOptions.isGraphicalEnabled()) 
			pipe(GraphicalGatherer.class, gathererOptions);
		
		
		/*
		 * Merging terms
		 */
		if(gathererOptions.isTermMergerEnabled()) 
			pipe(TermMerger.class);
	}
}
