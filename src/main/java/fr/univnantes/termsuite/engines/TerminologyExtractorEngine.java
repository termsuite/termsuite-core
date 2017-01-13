package fr.univnantes.termsuite.engines;

import javax.inject.Inject;

import fr.univnantes.termsuite.api.TerminologyExtractorOptions;
import fr.univnantes.termsuite.engines.cleaner.TerminologyCleaner;
import fr.univnantes.termsuite.engines.gatherer.TermGatherer;
import fr.univnantes.termsuite.engines.postproc.TermPostProcessor;
import fr.univnantes.termsuite.engines.postproc.TermRanker;
import fr.univnantes.termsuite.engines.prepare.Preparator;
import fr.univnantes.termsuite.engines.splitter.MorphologicalAnalyzer;
import fr.univnantes.termsuite.framework.AggregateTerminologyEngine;
import fr.univnantes.termsuite.framework.TerminologyPipeline;

public class TerminologyExtractorEngine extends AggregateTerminologyEngine {
	
	@Inject
	private TerminologyExtractorOptions config;
	
	@Override
	public void configurePipeline(TerminologyPipeline pipeline) {
		pipeline.pipeEngine(Preparator.class);
		
		if(config.isPreFilterEnabled()) 
			pipeline.pipeEngine(TerminologyCleaner.class, config.getPreFilterConfig());
		

		if(config.isMorphologicalAnalysisEnabled()) 
			pipeline.pipeEngine(MorphologicalAnalyzer.class, config.getMorphologicalConfig());
		
		if(config.isGathererEnabled()) 
			pipeline.pipeEngine(TermGatherer.class, config.getGathererConfig());

		if(config.isPostProcessorEnabled())
			pipeline.pipeEngine(TermPostProcessor.class, config.getPostProcessorConfig());
		
		if(config.isPostFilterEnabled()) 
			pipeline.pipeEngine(TerminologyCleaner.class, config.getPostFilterConfig());
	
		pipeline.pipeEngine(TermRanker.class, this.config.getRankingConfig());
	}
}
