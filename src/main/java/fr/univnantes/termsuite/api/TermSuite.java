package fr.univnantes.termsuite.api;

import com.google.inject.Guice;
import com.google.inject.Injector;

import fr.univnantes.termsuite.engines.prepare.ExtensionDetecter;
import fr.univnantes.termsuite.framework.EngineDescription;
import fr.univnantes.termsuite.framework.TerminologyEngine;
import fr.univnantes.termsuite.framework.modules.ExtractorModule;
import fr.univnantes.termsuite.framework.modules.ResourceModule;
import fr.univnantes.termsuite.framework.modules.TermSuiteModule;
import fr.univnantes.termsuite.framework.service.LanguageService;
import fr.univnantes.termsuite.model.Lang;
import fr.univnantes.termsuite.model.OccurrenceStore;
import fr.univnantes.termsuite.model.Terminology;
import fr.univnantes.termsuite.model.occurrences.EmptyOccurrenceStore;
import fr.univnantes.termsuite.model.occurrences.MemoryOccurrenceStore;
import fr.univnantes.termsuite.model.occurrences.XodusOccurrenceStore;
import fr.univnantes.termsuite.model.termino.MemoryTerminology;

public class TermSuite {
	
	public static Terminology createTerminology(Lang lang, String name, boolean withOccurrences) {
		OccurrenceStore store = withOccurrences ? 
				new MemoryOccurrenceStore(lang)
				: new EmptyOccurrenceStore(lang);
		return new MemoryTerminology(name, lang, store);
	}

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

	public static Terminology createPersitentTerminology(String storeUrl, Lang lang, String name) {
		OccurrenceStore store = new XodusOccurrenceStore(lang, storeUrl);
		return new MemoryTerminology(name, lang, store);

	}

	public static EngineDescription createEngineDescription(
			Class<? extends TerminologyEngine> engineClass,
			Object... parameters) {
		return new EngineDescription(engineClass.getSimpleName(), engineClass, parameters);
	}

	public static ExtensionDetecter createEngine(Class<ExtensionDetecter> class1, Terminology termino) {
		Guice.createInjector(new ResourceModule(), new ExtractorModule(termino));
		return null;
	}
}
