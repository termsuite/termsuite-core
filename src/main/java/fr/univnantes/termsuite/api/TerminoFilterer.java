package fr.univnantes.termsuite.api;

import fr.univnantes.termsuite.engines.cleaner.TerminoFilterOptions;
import fr.univnantes.termsuite.model.Terminology;
import fr.univnantes.termsuite.uima.TermSuitePipeline;
import fr.univnantes.termsuite.utils.PipelineUtils;

public class TerminoFilterer {

	private TerminoFilterOptions config = new TerminoFilterOptions();
	
	private Terminology termino;
	
	public static TerminoFilterer create(Terminology termino) {
		TerminoFilterer terminoFilterer = new TerminoFilterer();
		terminoFilterer.termino = termino;
		return terminoFilterer;
	}
	
	public TerminoFilterer configure(TerminoFilterOptions config) {
		this.config = config;
		return this;
	}
	
	private TerminoFilterer() {
	}


	public Terminology execute() {
		TermSuitePipeline pipeline = TermSuitePipeline
				.create(termino);
		PipelineUtils.filter(pipeline, config);
		pipeline.run();
		return termino;
	}
}
