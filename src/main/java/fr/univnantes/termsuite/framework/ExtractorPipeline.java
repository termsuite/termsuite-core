package fr.univnantes.termsuite.framework;

import javax.inject.Inject;

import com.google.inject.Injector;

public class ExtractorPipeline {

	@Inject
	Injector injector;
	
	public void run(Class<? extends TerminologyEngine> cls) {
		injector.getInstance(cls);
	}
}
