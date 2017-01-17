package fr.univnantes.termsuite.test.unit.api;


import static fr.univnantes.termsuite.test.TermSuiteAssertions.assertThat;

import org.junit.Before;
import org.junit.Test;

import fr.univnantes.termsuite.api.TermSuite;
import fr.univnantes.termsuite.model.Terminology;
import fr.univnantes.termsuite.test.func.FunctionalTests;

public class TerminoExtractorSpec {
	
	@Before
	public void setup() {
		
	}
	
	@Test
	public void testCorpus1() {
		Terminology terminology = TermSuite.preprocessor()
			.setTaggerPath(FunctionalTests.getTaggerPath())
			.toTerminology(FunctionalTests.CORPUS1, true);
		
		TermSuite.terminoExtractor()
			.execute(terminology);
		
		assertThat(terminology)
			.containsTerm("n: énergie");
	}
}
