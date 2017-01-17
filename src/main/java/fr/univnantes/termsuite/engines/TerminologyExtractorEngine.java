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

public class TerminologyExtractorEngine extends AggregateTerminologyEngine {
	
	@Inject
	private TerminologyExtractorOptions config;
	
	@Override
	public void configure() {
		pipe(Preparator.class);
		
		if(config.isPreFilterEnabled()) 
			pipe(TerminologyCleaner.class, config.getPreFilterConfig());

		if(config.isMorphologicalAnalysisEnabled()) 
			pipe(MorphologicalAnalyzer.class, config.getMorphologicalConfig());
		
		if(config.isGathererEnabled()) 
			pipe(TermGatherer.class, config.getGathererConfig());

		if(config.isPostProcessorEnabled())
			pipe(TermPostProcessor.class, config.getPostProcessorConfig());
		
		if(config.isPostFilterEnabled()) 
			pipe(TerminologyCleaner.class, config.getPostFilterConfig());
	
		pipe(TermRanker.class, this.config.getRankingConfig());
	}
}
