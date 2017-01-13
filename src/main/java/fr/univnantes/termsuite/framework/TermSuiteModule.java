package fr.univnantes.termsuite.framework;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

import fr.univnantes.termsuite.framework.service.CorpusService;
import fr.univnantes.termsuite.framework.service.PreprocessorService;

public class TermSuiteModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(CorpusService.class).in(Singleton.class);
		bind(PreprocessorService.class).in(Singleton.class);
	}

}
