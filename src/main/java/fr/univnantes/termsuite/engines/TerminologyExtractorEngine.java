package fr.univnantes.termsuite.engines;

import fr.univnantes.termsuite.api.ExtractorOptions;
import fr.univnantes.termsuite.engines.cleaner.TerminologyCleaner;
import fr.univnantes.termsuite.engines.gatherer.TermGatherer;
import fr.univnantes.termsuite.engines.postproc.TermPostProcessor;
import fr.univnantes.termsuite.engines.postproc.TermRanker;
import fr.univnantes.termsuite.engines.prepare.Preparator;
import fr.univnantes.termsuite.engines.splitter.MorphologicalAnalyzer;
import fr.univnantes.termsuite.framework.AggregateTerminologyEngine;
import fr.univnantes.termsuite.framework.Parameter;

public class TerminologyExtractorEngine extends AggregateTerminologyEngine {
	
	@Parameter
	private ExtractorOptions config;
	
	@Override
	public void configure() {
		pipe(Preparator.class);
		
		if(config.getPreFilterConfig().isEnabled()) 
			pipe("Pre-gathering filter", TerminologyCleaner.class, config.getPreFilterConfig());

		if(config.getMorphologicalConfig().isEnabled()) 
			pipe(MorphologicalAnalyzer.class, config.getMorphologicalConfig());
		
		pipe(TermGatherer.class, config.getGathererConfig());

		if(config.getPostProcessorConfig().isEnabled())
			pipe(TermPostProcessor.class, config.getPostProcessorConfig());
		
		if(config.getPostFilterConfig().isEnabled()) 
			pipe("Post-gathering filter", TerminologyCleaner.class, config.getPostFilterConfig());
	
		pipe(TermRanker.class, this.config.getRankingConfig());
	}
}
