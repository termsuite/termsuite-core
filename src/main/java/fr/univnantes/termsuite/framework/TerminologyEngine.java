package fr.univnantes.termsuite.framework;

import java.lang.reflect.Method;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Injector;

import fr.univnantes.termsuite.utils.TermHistory;

public class TerminologyEngine {

	protected Optional<TermHistory> history = Optional.empty();
	
	public void execute() {
		for (Method method : this.getClass().getDeclaredMethods()) {
		    if (method.isAnnotationPresent(Execute.class)) {
		    	try {
					method.invoke(this);
				} catch (Exception e) {
					throw new TermSuiteFrameworkException(e);
				}
		    }
		}
	}
	
	
	protected Logger logger = null;
	protected Logger getLogger() {
		if(logger == null)
			logger = LoggerFactory.getLogger(this.getClass());
		return logger;
	}
	
	public void configure(Object... parameters) {
		
	}

	public void init(Injector injector, Object... parameters) {
		
	}
}
