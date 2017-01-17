package fr.univnantes.termsuite.engines.gatherer;

import javax.inject.Inject;

import fr.univnantes.termsuite.framework.AggregateTerminologyEngine;
import fr.univnantes.termsuite.model.termino.TermIndexes;

public class TermGatherer extends AggregateTerminologyEngine {

	@Inject
	private GathererOptions gathererOptions = new GathererOptions();
	
	@Override
	public void configure() {
		
		if(gathererOptions.isPrefixationGathererEnabled()) 
			pipe(VariationTypeGatherer.class, 
					VariationType.PREFIXATION, 
					TermIndexes.PREFIXATION_LEMMAS, 
					true);
		
		if(gathererOptions.isDerivationGathererEnabled()) 
			pipe(VariationTypeGatherer.class, 
					VariationType.DERIVATION, 
					TermIndexes.DERIVATION_LEMMAS, 
					true);
		
		if(gathererOptions.isMorphologicalGathererEnabled()) 
			pipe(VariationTypeGatherer.class, 
					VariationType.MORPHOLOGICAL, 
					TermIndexes.ALLCOMP_PAIRS, 
					false);
			
		pipe(VariationTypeGatherer.class, 
				VariationType.SYNTAGMATIC, 
				TermIndexes.ALLCOMP_PAIRS, 
				true);

		if(gathererOptions.isSemanticGathererEnabled()) 
			pipe(SemanticGatherer.class, 
					VariationType.SEMANTIC);
		
		/*
		 * Gathers extensions of morpho, derivative, prefix, and semantic variants
		 */
		pipe(ExtensionVariantGatherer.class);
		
		if(gathererOptions.isGraphicalGathererEnabled()) 
			pipe(GraphicalGatherer.class, gathererOptions);
		
		
		/*
		 * Merging terms
		 */
		pipe(TermMerger.class);
	}
}
