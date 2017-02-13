package fr.univnantes.termsuite.tools;

import java.io.IOException;
import java.nio.file.Path;

import fr.univnantes.termsuite.api.ExtractorOptions;
import fr.univnantes.termsuite.api.TermSuite;
import fr.univnantes.termsuite.framework.TermSuiteFactory;
import fr.univnantes.termsuite.model.IndexedCorpus;
import fr.univnantes.termsuite.model.Lang;
import fr.univnantes.termsuite.model.TextCorpus;
import fr.univnantes.termsuite.test.func.FunctionalTests;

public class GenerateTerminology {
	public static void main(String[] args) throws IOException {
		runPipeline(FunctionalTests.getCorpusWEShort(Lang.FR), FunctionalTests.getTerminoWEShortPath(Lang.FR));
		runPipeline(FunctionalTests.getCorpusWEShort(Lang.EN), FunctionalTests.getTerminoWEShortPath(Lang.EN));
		runPipeline(FunctionalTests.getCorpusWEShort(Lang.DE), FunctionalTests.getTerminoWEShortPath(Lang.DE));
	}

	public static void runPipeline(TextCorpus txtCorpus, Path jsonExportPath) throws IOException {
		IndexedCorpus corpus = TermSuite.preprocessor()
				.setTaggerPath(FunctionalTests.getTaggerPath())
				.toIndexedCorpus(txtCorpus, 5000000);
			
		ExtractorOptions extractorOptions = TermSuite.getDefaultExtractorConfig(txtCorpus.getLang());
		extractorOptions.getPostProcessorConfig().setEnabled(false);
		extractorOptions.getGathererConfig().setMergerEnabled(false);
		extractorOptions.getContextualizerOptions().setEnabled(true);
		TermSuite.terminoExtractor()
					.setOptions(extractorOptions)
					.execute(corpus);
		
		TermSuiteFactory.createJsonExporter()
				.export(corpus, jsonExportPath);
	}
}
