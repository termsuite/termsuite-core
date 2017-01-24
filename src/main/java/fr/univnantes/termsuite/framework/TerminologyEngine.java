package fr.univnantes.termsuite.framework;

import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Named;

import fr.univnantes.termsuite.framework.service.TerminologyService;
import fr.univnantes.termsuite.utils.TermHistory;

public abstract class TerminologyEngine {
	/*
	 * Injected at engine initialization
	 */
	@Named("engineName")
	private String engineName;

	@Inject 
	protected TerminologyService terminology;

	protected Optional<TermHistory> history = Optional.empty();
	
	
	public abstract void execute();
	
	public String getEngineName() {
		return engineName;
	}
}
