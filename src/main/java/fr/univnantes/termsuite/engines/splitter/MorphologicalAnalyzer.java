package fr.univnantes.termsuite.engines.splitter;

import fr.univnantes.termsuite.framework.AggregateTerminologyEngine;
import fr.univnantes.termsuite.framework.Parameter;

public class MorphologicalAnalyzer extends AggregateTerminologyEngine {

	@Parameter
	private MorphologicalOptions options;

	@Override
	public void configure() {
		if(options.isPrefixSplitterEnabled()) {
			pipe(PrefixSplitter.class);
			pipe(ManualPrefixSetter.class);
		}
		
		pipe(ManualSplitter.class);
		
		if(options.isDerivativesDetecterEnabled()) {
			pipe(SuffixDerivationDetecter.class);
			pipe(ManualSuffixDerivationDetecter.class);
		}
		
		if(options.isNativeSplittingEnabled()) 
			pipe(NativeSplitter.class, options);
	}
}
