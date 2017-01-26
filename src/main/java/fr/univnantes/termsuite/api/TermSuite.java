package fr.univnantes.termsuite.api;

import com.google.inject.Guice;
import com.google.inject.Injector;

import fr.univnantes.termsuite.framework.modules.TermSuiteModule;
import fr.univnantes.termsuite.framework.service.LanguageService;
import fr.univnantes.termsuite.model.Lang;

public class TermSuite {
	

	public static TerminoExtractor terminoExtractor() {
		return new TerminoExtractor();
	}


	public static ExtractorOptions getDefaultExtractorConfig(Lang lang) {
		return getLanguageService().getDefaultExtractorConfig(lang);
	}
	
	public static LanguageService getLanguageService() {
		return termSuiteInjector().getInstance(LanguageService.class);
	}
	
	public static Preprocessor preprocessor() {
		return termSuiteInjector().getInstance(Preprocessor.class);
	}

	private static Injector termSuiteInjector() {
		return Guice.createInjector(new TermSuiteModule());
	}

}
