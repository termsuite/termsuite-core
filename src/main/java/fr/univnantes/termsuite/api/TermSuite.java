package fr.univnantes.termsuite.api;

import com.google.inject.Guice;
import com.google.inject.Injector;

import fr.univnantes.termsuite.framework.EngineDescription;
import fr.univnantes.termsuite.framework.TermSuiteModule;
import fr.univnantes.termsuite.framework.TerminologyEngine;
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

	public static Preprocessor preprocessor() {
		return injector().getInstance(Preprocessor.class);
	}

	public static Injector injector() {
		return Guice.createInjector(new TermSuiteModule());
	}

	public static Terminology createPersitentTerminology(String storeUrl, Lang lang, String name) {
		OccurrenceStore store = new XodusOccurrenceStore(lang, storeUrl);
		return new MemoryTerminology(name, lang, store);

	}

	public static EngineDescription createEngineDescription(
			Class<? extends TerminologyEngine> engineClass,
			Object... parameters) {
		return new EngineDescription(engineClass, parameters);
	}
}
