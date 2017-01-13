package fr.univnantes.termsuite.framework;

import com.google.inject.Injector;

import fr.univnantes.termsuite.api.TermSuite;
import fr.univnantes.termsuite.api.TerminologyExtractorOptions;
import fr.univnantes.termsuite.engines.TerminologyExtractorEngine;

public class TermSuiteFramework {
	
	public static TerminologyPipeline createPipeline() {
		return new TerminologyPipeline();
	}

	public static void runEngine(Class<? extends TerminologyEngine> cls, Object... parameters) {
		TerminologyPipeline pipeline = createPipeline();
		Injector injector = TermSuite.injector();
		TerminologyEngine engine = injector.getInstance(cls);
		engine.init(injector, parameters);
	}

	public static void runExtractionEngine(TerminologyExtractorOptions config) {
		runEngine(TerminologyExtractorEngine.class, config);
	}

	public static <T> T createEngine(Class<T> cls) {
		try {
			Injector injector = TermSuite.injector();
			T engine = cls.newInstance();
			injector.injectMembers(engine);
			return engine;
		} catch(Exception e) {
			throw new TermSuiteFrameworkException(e);
		}
	}
}
