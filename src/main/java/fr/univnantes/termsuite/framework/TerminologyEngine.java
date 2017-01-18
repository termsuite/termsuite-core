package fr.univnantes.termsuite.framework;

import java.util.Optional;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Injector;

import fr.univnantes.termsuite.framework.service.TerminologyService;
import fr.univnantes.termsuite.utils.TermHistory;

public abstract class TerminologyEngine {

	protected Optional<TermHistory> history = Optional.empty();
	
	protected Logger logger = null;
	
	@Inject 
	protected TerminologyService terminology;
	
	protected Logger getLogger() {
		if(logger == null)
			logger = LoggerFactory.getLogger(this.getClass());
		return logger;
	}
	
	public void configure(Object... parameters) {
		
	}

	public void init(Injector injector, Object... parameters) {
		
	}
	
	public abstract void execute();
}
