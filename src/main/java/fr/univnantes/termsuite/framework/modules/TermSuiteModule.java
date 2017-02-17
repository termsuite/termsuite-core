package fr.univnantes.termsuite.framework.modules;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

import fr.univnantes.termsuite.framework.service.CorpusService;
import fr.univnantes.termsuite.framework.service.LanguageService;

public class TermSuiteModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(CorpusService.class).in(Singleton.class);
		bind(LanguageService.class).in(Singleton.class);
	}
}
