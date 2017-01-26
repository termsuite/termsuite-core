package fr.univnantes.termsuite.framework;

import javax.inject.Inject;
import javax.inject.Named;

import fr.univnantes.termsuite.framework.service.PipelineService;

public abstract class Engine {
	/*
	 * Injected at engine initialization
	 */
	@Named("engineName")
	private String engineName;
	
	@Inject
	protected PipelineService pipelineService;
	
	public String getEngineName() {
		return engineName;
	}
}
