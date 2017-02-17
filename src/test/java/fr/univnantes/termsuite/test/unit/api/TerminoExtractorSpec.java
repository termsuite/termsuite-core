package fr.univnantes.termsuite.test.unit.api;


import static fr.univnantes.termsuite.test.asserts.TermSuiteAssertions.assertThat;
import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import fr.univnantes.termsuite.api.TermSuite;
import fr.univnantes.termsuite.model.IndexedCorpus;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.test.func.FunctionalTests;

public class TerminoExtractorSpec {
	
	@Before
	public void setup() {
		
	}
	
	@Test
	public void testCorpus1() {
		IndexedCorpus corpus = TermSuite.preprocessor()
			.setTaggerPath(FunctionalTests.getTaggerPath())
			.toIndexedCorpus(FunctionalTests.CORPUS1, 500000);
		
		TermSuite.terminoExtractor()
			.execute(corpus);
		
		assertThat(corpus.getTerminology())
			.containsTerm("n: énergie");
		
		Term t = corpus.getTerminology().getTerms().get("n: énergie");
		assertEquals(3.374554d, t.getSpecificity(), 0.0001d);
	}
}
