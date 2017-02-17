package fr.univnantes.termsuite.test.unit.api;

import static fr.univnantes.termsuite.test.asserts.TermSuiteAssertions.assertThat;

import org.junit.Test;

import fr.univnantes.termsuite.api.TermSuite;
import fr.univnantes.termsuite.model.IndexedCorpus;
import fr.univnantes.termsuite.test.func.FunctionalTests;

public class PreprocessorSpec {

	@Test
	public void testCorpus1() {
		IndexedCorpus indexedCorpus = TermSuite.preprocessor()
			.setTaggerPath(FunctionalTests.getTaggerPath())
			.toIndexedCorpus(FunctionalTests.CORPUS1, 500000);

		assertThat(indexedCorpus.getTerminology())
			.containsTerm("n: énergie");
	}
}
