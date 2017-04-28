package fr.univnantes.termsuite.tools;

import java.io.IOException;
import java.nio.file.Path;

import fr.univnantes.termsuite.api.ExtractorOptions;
import fr.univnantes.termsuite.api.TXTCorpus;
import fr.univnantes.termsuite.api.TermSuite;
import fr.univnantes.termsuite.framework.TermSuiteFactory;
import fr.univnantes.termsuite.model.IndexedCorpus;
import fr.univnantes.termsuite.model.Lang;
import fr.univnantes.termsuite.test.func.FunctionalTests;

public class GenerateTerminology {
	public static void main(String[] args) throws IOException {
		runPipeline(FunctionalTests.getCorpusWEShort(Lang.FR), FunctionalTests.getTerminoWEShortPath(Lang.FR));
		runPipeline(FunctionalTests.getCorpusWEShort(Lang.EN), FunctionalTests.getTerminoWEShortPath(Lang.EN));
		runPipeline(FunctionalTests.getCorpusWEShort(Lang.DE), FunctionalTests.getTerminoWEShortPath(Lang.DE));
	}

	public static void runPipeline(TXTCorpus txtCorpus, Path jsonExportPath) throws IOException {
		/*
		 * 1. Preprocess
		 */
		IndexedCorpus corpus = TermSuite.preprocessor()
				.setTaggerPath(FunctionalTests.getTaggerPath())
				.exportAnnotationsToTSV(FunctionalTests.getTsvPreprocessedCorpusWEShortPath(txtCorpus.getLang()))
				.exportAnnotationsToJSON(FunctionalTests.getJsonPreprocessedCorpusWEShortPath(txtCorpus.getLang()))
				.exportAnnotationsToXMI(FunctionalTests.getXmiPreprocessedCorpusWEShortPath(txtCorpus.getLang()))
				.toIndexedCorpus(txtCorpus, 5000000);
		
		/*
		 * 2. Export termino before extraction
		 */
		Path preprocessedTerminoPath = FunctionalTests
				.getPreprocessedCorpusWEShortPathAsTermino(txtCorpus.getLang());
		TermSuiteFactory.createJsonExporter()
			.export(corpus, preprocessedTerminoPath);
		
		
		ExtractorOptions extractorOptions = TermSuite.getDefaultExtractorConfig(txtCorpus.getLang());
		extractorOptions.getPostProcessorConfig().setEnabled(false);
		extractorOptions.getGathererConfig().setMergerEnabled(false);
		extractorOptions.getContextualizerOptions().setEnabled(true);
		
		/*
		 * 3. Extract termino
		 */
		TermSuite.terminoExtractor()
					.setOptions(extractorOptions)
					.execute(corpus);
		
		/*
		 * 4. Export etxracted termnio
		 */
		TermSuiteFactory.createJsonExporter()
				.export(corpus, jsonExportPath);
	}
}
