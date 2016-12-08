package fr.univnantes.termsuite.api;

import fr.univnantes.termsuite.model.Terminology;
import fr.univnantes.termsuite.uima.TermSuitePipeline;
import fr.univnantes.termsuite.utils.PipelineUtils;

public class TerminoFilterer {

	private TerminoFilterConfig config = new TerminoFilterConfig();
	
	private Terminology termIndex;
	
	public static TerminoFilterer create(Terminology termIndex) {
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


	public Terminology execute() {
		TermSuitePipeline pipeline = TermSuitePipeline
				.create(termIndex);
		PipelineUtils.filter(pipeline, config);
		pipeline.run();
		return termIndex;
	}
}
