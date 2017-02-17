package fr.univnantes.termsuite.test.func.api;

import static fr.univnantes.termsuite.test.asserts.TermSuiteAssertions.assertThat;

import java.net.MalformedURLException;

import org.junit.Before;
import org.junit.Test;

import fr.univnantes.termsuite.api.IndexedCorpusIO;
import fr.univnantes.termsuite.api.TermSuite;
import fr.univnantes.termsuite.model.IndexedCorpus;
import fr.univnantes.termsuite.model.TermProperty;
import fr.univnantes.termsuite.test.func.FunctionalTests;

public class TerminoFiltererSpec {

	IndexedCorpus corpus1;
	@Before
	public void setup() throws MalformedURLException {
		corpus1 = IndexedCorpusIO.fromJson(FunctionalTests.TERMINOLOGY_1.toUri().toURL());
	}
	
	@Test
	public void test1() {
		
		assertThat(corpus1.getTerminology())
			.hasNTerms(3)
			.containsTerm("a: word2")
			.containsTerm("n: word1")
			.containsTerm("na: word1 word2")
			;
		
		TermSuite.terminologyFilterer()
			.by(TermProperty.FREQUENCY)
			.keepOverTh(6)
			.filter(corpus1);
		

		assertThat(corpus1.getTerminology())
			.hasNTerms(1)
			.containsTerm("na: word1 word2")
			;
	}

	@Test
	public void test2() {
		
		assertThat(corpus1.getTerminology())
		.hasNTerms(3)
		.containsTerm("a: word2")
		.containsTerm("n: word1")
		.containsTerm("na: word1 word2")
		;

		
		TermSuite.terminologyFilterer()
			.by(TermProperty.FREQUENCY)
			.keepOverTh(10)
			.filter(corpus1);

		assertThat(corpus1.getTerminology())
		.hasNTerms(0)
		;
	}

}
