package fr.univnantes.termsuite.framework;

import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.univnantes.termsuite.framework.service.TerminologyService;
import fr.univnantes.termsuite.utils.TermHistory;

public abstract class TerminologyEngine {

	
	/*
	 * Injected at engine initialization
	 */
	@Named("engineName")
	private String engineName;

	protected Optional<TermHistory> history = Optional.empty();
	
	protected Logger logger = null;
	
	@Inject 
	protected TerminologyService terminology;
	
	protected Logger getLogger() {
		if(logger == null)
			logger = LoggerFactory.getLogger(this.getClass());
		return logger;
	}
	
	public abstract void execute();
	
	public String getEngineName() {
		return engineName;
	}
	
	
	
	
	
	
}
