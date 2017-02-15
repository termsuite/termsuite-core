package fr.univnantes.termsuite.api;

import com.google.inject.Guice;
import com.google.inject.Injector;

import fr.univnantes.termsuite.framework.TermSuiteFactory;
import fr.univnantes.termsuite.framework.modules.ImporterModule;
import fr.univnantes.termsuite.framework.modules.IndexedCorpusModule;
import fr.univnantes.termsuite.framework.modules.TermSuiteModule;
import fr.univnantes.termsuite.framework.service.CorpusService;
import fr.univnantes.termsuite.framework.service.LanguageService;
import fr.univnantes.termsuite.framework.service.ImporterService;
import fr.univnantes.termsuite.framework.service.TerminologyService;
import fr.univnantes.termsuite.model.IndexedCorpus;
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
	
	/**
	 * 
	 * Creates a new {@link IndexedCorpus} and import a {@link PreprocessedCorpus} 
	 * into it.
	 * 
	 * @param preprocessedCorpus
	 * 			The preprocessed corpus to be imported
	 * @param maxSize
	 * 			The maximun number of terms to keep in memory during import
	 * @return
	 * 			The created {@link IndexedCorpus}
	 * 
	 * @see ImporterService
	 */
	public static IndexedCorpus toIndexedCorpus(PreprocessedCorpus preprocessedCorpus, int maxSize) {
		String name = "Indexed corpus " + preprocessedCorpus.getRootDirectory().getFileName().toString();
		IndexedCorpus indexedCorpus = TermSuiteFactory.createIndexedCorpus(
				preprocessedCorpus.getLang(), 
				name);
		importCorpus(preprocessedCorpus, indexedCorpus, maxSize);
		return indexedCorpus;
	}

	/**
	 * 
	 * Imports a {@link PreprocessedCorpus} into an existing {@link IndexedCorpus}.
	 * 
	 * @param preprocessedCorpus
	 * 			The {@link PreprocessedCorpus}
	 * @param indexedCorpus
	 * 			The {@link IndexedCorpus}
	 * @param maxSize
	 * 			The maximun number of terms to keep in memory during import
	 * 			
	 */
	public static void importCorpus(PreprocessedCorpus preprocessedCorpus, IndexedCorpus indexedCorpus, int maxSize) {
		CorpusService corpusService = getCorpusService();
		ImporterService importer = terminologyImporter(indexedCorpus, maxSize);
		corpusService.cases(preprocessedCorpus).forEach(importer::importCas);
	}
	
	public static CorpusService getCorpusService() {
		return termSuiteInjector().getInstance(CorpusService.class);
	}
	public static Preprocessor preprocessor() {
		return termSuiteInjector().getInstance(Preprocessor.class);
	}

	public static ImporterService terminologyImporter(IndexedCorpus indexedCorpus, int maxSize) {
		return importerInjector(indexedCorpus, maxSize)
				.getInstance(ImporterService.class);
	}

	public static Injector importerInjector(IndexedCorpus indexedCorpus, int maxSize) {
		return Guice.createInjector(new ImporterModule(indexedCorpus, maxSize));
	}

	private static Injector termSuiteInjector() {
		return Guice.createInjector(new TermSuiteModule());
	}

	public static TerminologyService toService(IndexedCorpus corpus) {
		return Guice.createInjector(new IndexedCorpusModule(corpus)).getInstance(TerminologyService.class);
	}

}
