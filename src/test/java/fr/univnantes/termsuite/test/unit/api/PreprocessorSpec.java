package fr.univnantes.termsuite.test.unit.api;

import static fr.univnantes.termsuite.test.asserts.TermSuiteAssertions.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.stream.Stream;

import org.apache.uima.jcas.JCas;
import org.junit.Test;

import fr.univnantes.termsuite.api.Preprocessor;
import fr.univnantes.termsuite.api.TermSuite;
import fr.univnantes.termsuite.model.IndexedCorpus;
import fr.univnantes.termsuite.model.Lang;
import fr.univnantes.termsuite.test.func.FunctionalTests;

public class PreprocessorSpec {

	@Test
	public void testToIndexedCorpusOnTXTCorpus() {
		IndexedCorpus indexedCorpus = TermSuite.preprocessor()
			.setTaggerPath(FunctionalTests.getTaggerPath())
			.toIndexedCorpus(FunctionalTests.CORPUS1, 500000);

		assertThat(indexedCorpus.getTerminology())
			.containsTerm("n: énergie");
	}
	
	
	@Test
	public void testToIndexedCorpusOnBlankCasStream() {
		Stream<JCas> casStream = FunctionalTests.CORPUS1
				.documents()
				.map(doc -> Preprocessor.toCas(
						doc, 
						FunctionalTests.CORPUS1.readDocumentText(doc)));
		Lang lang = FunctionalTests.CORPUS1.getLang();
		
		IndexedCorpus indexedCorpus = TermSuite.preprocessor()
			.setTaggerPath(FunctionalTests.getTaggerPath())
			.toIndexedCorpus(lang, casStream, 500000);

		assertTrue(indexedCorpus.getTerminology().getTerms().size() > 0);
		
		assertThat(indexedCorpus.getTerminology())
			.containsTerm("n: énergie");
	}

}
