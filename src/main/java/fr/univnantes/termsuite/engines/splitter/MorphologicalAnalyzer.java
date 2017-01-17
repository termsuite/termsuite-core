package fr.univnantes.termsuite.engines.splitter;

import javax.inject.Inject;

import fr.univnantes.termsuite.framework.AggregateTerminologyEngine;

public class MorphologicalAnalyzer extends AggregateTerminologyEngine {

	@Inject
	private MorphologicalOptions options;

	@Override
	public void configure() {
		if(options.isPrefixSplitterEnabled()) {
			pipe(PrefixSplitter.class);
			pipe(ManualPrefixSetter.class);
		}
		
		pipe(ManualSplitter.class);
		
		if(options.isDerivationDetecterEnabled()) {
			pipe(SuffixDerivationDetecter.class);
			pipe(ManualSuffixDerivationDetecter.class);
		}
		
		if(options.isNativeSplittingEnabled()) 
			pipe(NativeSplitter.class, options);
	}
}
