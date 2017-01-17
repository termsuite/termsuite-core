package fr.univnantes.termsuite.framework;

import com.google.inject.Inject;
import com.google.inject.Injector;

public class EngineRunner {

	@Inject
	Injector injector;
	
	public <T extends AggregateTerminologyEngine> TerminologyEngine createEngine(
			Class<T> engineCls, 
			Object... parameters) {
		T aggregateEngine = injector.getInstance(engineCls);
		aggregateEngine.configure();
		
		
		return new TerminologyEngine() {
			@Override
			public void execute() {
				
			}
		};
	}
}
