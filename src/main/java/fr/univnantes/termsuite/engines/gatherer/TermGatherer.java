package fr.univnantes.termsuite.engines.gatherer;

import javax.inject.Inject;

import fr.univnantes.termsuite.framework.AggregateTerminologyEngine;
import fr.univnantes.termsuite.framework.TerminologyPipeline;
import fr.univnantes.termsuite.model.termino.TermIndexes;

public class TermGatherer extends AggregateTerminologyEngine {
	@Inject
	private GathererOptions gathererOptions = new GathererOptions();
	
	@Override
	public void configurePipeline(TerminologyPipeline pipeline) {
		
		if(gathererOptions.isPrefixationGathererEnabled()) 
			pipeline.pipeEngine(VariationTypeGatherer.class, 
					VariationType.PREFIXATION, 
					TermIndexes.PREFIXATION_LEMMAS, 
					true);
		
		if(gathererOptions.isDerivationGathererEnabled()) 
			pipeline.pipeEngine(VariationTypeGatherer.class, 
					VariationType.DERIVATION, 
					TermIndexes.DERIVATION_LEMMAS, 
					true);
		
		if(gathererOptions.isMorphologicalGathererEnabled()) 
			pipeline.pipeEngine(VariationTypeGatherer.class, 
					VariationType.MORPHOLOGICAL, 
					TermIndexes.ALLCOMP_PAIRS, 
					false);
			
		pipeline.pipeEngine(VariationTypeGatherer.class, 
				VariationType.SYNTAGMATIC, 
				TermIndexes.ALLCOMP_PAIRS, 
				true);

		if(gathererOptions.isSemanticGathererEnabled()) 
			pipeline.pipeEngine(SemanticGatherer.class, 
					VariationType.SEMANTIC);
		
		/*
		 * Gathers extensions of morpho, derivative, prefix, and semantic variants
		 */
		pipeline.pipeEngine(ExtensionVariantGatherer.class);
		
		if(gathererOptions.isGraphicalGathererEnabled()) 
			pipeline.pipeEngine(GraphicalGatherer.class, gathererOptions);
		
		
		/*
		 * Merging terms
		 */
		pipeline.pipeEngine(TermMerger.class);
		
	}
}
