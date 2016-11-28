package fr.univnantes.termsuite.api;

import fr.univnantes.termsuite.model.TermIndex;
import fr.univnantes.termsuite.uima.TermSuitePipeline;
import fr.univnantes.termsuite.utils.PipelineUtils;

public class TerminoFilterer {

	private TerminoFilterConfig config = new TerminoFilterConfig();
	
	private TermIndex termIndex;
	
	public static TerminoFilterer create(TermIndex termIndex) {
		TerminoFilterer terminoFilterer = new TerminoFilterer();
		terminoFilterer.termIndex = termIndex;
		return terminoFilterer;
	}
	
	public TerminoFilterer configure(TerminoFilterConfig config) {
		this.config = config;
		return this;
	}
	
	private TerminoFilterer() {
	}


	public TermIndex execute() {
		TermSuitePipeline pipeline = TermSuitePipeline
				.create(termIndex);
		PipelineUtils.filter(pipeline, config);
		pipeline.run();
		return termIndex;
	}
}
