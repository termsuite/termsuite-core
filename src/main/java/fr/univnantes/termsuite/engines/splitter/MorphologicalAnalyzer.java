package fr.univnantes.termsuite.engines.splitter;

import javax.inject.Inject;

import fr.univnantes.termsuite.framework.AggregateTerminologyEngine;
import fr.univnantes.termsuite.framework.TerminologyPipeline;

public class MorphologicalAnalyzer extends AggregateTerminologyEngine {

	@Inject
	private MorphologicalOptions options;

	@Override
	public void configurePipeline(TerminologyPipeline pipeline) {
		if(options.isPrefixSplitterEnabled()) {
			pipeline.pipeEngine(PrefixSplitter.class);
			pipeline.pipeEngine(ManualPrefixSetter.class);
		}
		
		pipeline.pipeEngine(ManualSplitter.class);
		
		if(options.isDerivationDetecterEnabled()) {
			pipeline.pipeEngine(SuffixDerivationDetecter.class);
			pipeline.pipeEngine(ManualSuffixDerivationDetecter.class);
		}
		
		if(options.isNativeSplittingEnabled()) 
			pipeline.pipeEngine(NativeSplitter.class, options);
	}
}
