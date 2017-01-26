package fr.univnantes.termsuite.engines.gatherer;

import fr.univnantes.termsuite.framework.AggregateEngine;
import fr.univnantes.termsuite.framework.Parameter;

public class TermGatherer extends AggregateEngine {

	@Parameter
	private GathererOptions gathererOptions;
	
	@Override
	public void configure() {
		
		pipe("PrefixationGatherer", 
				PrefixationGatherer.class, 
				VariationType.PREFIXATION);
	
		pipe("DerivationGatherer", 
				DerivationGatherer.class, 
				VariationType.DERIVATION);
	
		pipe("MorphologicalGatherer", 
				MorphologicalGatherer.class, 
				VariationType.MORPHOLOGICAL);
			
		pipe("SyntagmaticGatherer", 
				SyntagmaticGatherer.class, 
				VariationType.SYNTAGMATIC);

		if(gathererOptions.isSemanticEnabled()) 
			pipe(SemanticGatherer.class
					, gathererOptions
					, VariationType.SEMANTIC);
		
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
