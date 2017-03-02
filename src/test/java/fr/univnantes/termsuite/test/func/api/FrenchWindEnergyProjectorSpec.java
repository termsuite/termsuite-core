package fr.univnantes.termsuite.test.func.api;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

import fr.univnantes.termsuite.api.ExtractorOptions;
import fr.univnantes.termsuite.api.TermSuite;
import fr.univnantes.termsuite.model.Document;
import fr.univnantes.termsuite.model.IndexedCorpus;
import fr.univnantes.termsuite.model.Lang;
import fr.univnantes.termsuite.projection.DocumentProjectionService;
import fr.univnantes.termsuite.test.func.FunctionalTests;

public class FrenchWindEnergyProjectorSpec {
	private static IndexedCorpus corpus;

	@BeforeClass
	public static void setup() {
		corpus = TermSuite.preprocessor()
			.setPreprocessedCorpusCache(FunctionalTests.getCachedWindEnergyPreprocessedCorpusFile(Lang.FR))
			.setTaggerPath(FunctionalTests.getTaggerPath())
			.toIndexedCorpus(FunctionalTests.getCorpusWE(Lang.FR), 5000000);
		ExtractorOptions extractorOptions = TermSuite.getDefaultExtractorConfig(Lang.FR);
		extractorOptions.getPostProcessorConfig().setEnabled(false);
		extractorOptions.getGathererConfig().setMergerEnabled(false);
		extractorOptions.getGathererConfig().setSemanticEnabled(false);
		TermSuite.terminoExtractor()
				.setOptions(extractorOptions)
				.execute(corpus);
	}


	@Test
	public void testFileMobile1() {
		DocumentProjectionService s = getProjection(FunctionalTests.getMobileTechnologyDocument(Lang.FR, "file-1.txt"));
		assertEquals(0.06, s.getProjectionScore(TermSuite.toService(corpus)), 0.01d);
	}

	@Test
	public void testFileMobile2() {
		DocumentProjectionService s = getProjection(FunctionalTests.getMobileTechnologyDocument(Lang.FR, "file-2.txt"));
		assertEquals(0.07, s.getProjectionScore(TermSuite.toService(corpus)), 0.01d);
	}

	
	@Test
	public void testFileMobile3() {
		DocumentProjectionService s = getProjection(FunctionalTests.getMobileTechnologyDocument(Lang.FR, "file-3.txt"));
		assertEquals(0.05, s.getProjectionScore(TermSuite.toService(corpus)), 0.01d);
	}

	@Test
	public void testFile3() {
		DocumentProjectionService s = getProjection(FunctionalTests.getWindEnergyDocument(Lang.FR, "file-3.txt"));
		assertEquals(0.61, s.getProjectionScore(TermSuite.toService(corpus)), 0.01d);
	}

	
	@Test
	public void testFile2() {
		DocumentProjectionService s = getProjection(FunctionalTests.getWindEnergyDocument(Lang.FR, "file-2.txt"));
		assertEquals(0.84, s.getProjectionScore(TermSuite.toService(corpus)), 0.01d);
	}

	@Test
	public void testFile1() {
		DocumentProjectionService s = getProjection(FunctionalTests.getWindEnergyDocument(Lang.FR, "file-1.txt"));
		assertEquals(1, s.getProjectionScore(TermSuite.toService(corpus)), 0.01d);
	}

	public DocumentProjectionService getProjection(Document doc2) {
		DocumentProjectionService s;
		try {
			s = TermSuite.preprocessor()
				.setTaggerPath(FunctionalTests.getTaggerPath())
				.asService(Lang.FR)
				.toProjectionService(doc2, Files.toString(new File(doc2.getUrl()), Charsets.UTF_8));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return s;
	}
	
}
