package fr.univnantes.termsuite.test.func.tools.builders;

import static fr.univnantes.termsuite.test.asserts.TermSuiteAssertions.assertThat;

import java.net.MalformedURLException;

import org.junit.Before;
import org.junit.Test;

import fr.univnantes.termsuite.api.IndexedCorpusIO;
import fr.univnantes.termsuite.engines.cleaner.TerminoFilterOptions;
import fr.univnantes.termsuite.engines.cleaner.TerminologyCleaner;
import fr.univnantes.termsuite.framework.service.TerminologyService;
import fr.univnantes.termsuite.model.IndexedCorpus;
import fr.univnantes.termsuite.model.TermProperty;
import fr.univnantes.termsuite.test.func.FunctionalTests;

public class TerminoCleanerSpec {

	IndexedCorpus corpus1;
	@Before
	public void setup() throws MalformedURLException {
		corpus1 = IndexedCorpusIO.fromJson(FunctionalTests.TERMINOLOGY_1.toUri().toURL());
	}
	
	@Test
	public void test1() {
		
		assertThat(corpus1)
			.hasNTerms(3)
			.containsTerm("a: word2")
			.containsTerm("n: word1")
			.containsTerm("na: word1 word2")
			;
		
		new TerminologyCleaner()
			.setOptions(new TerminoFilterOptions().by(TermProperty.FREQUENCY).keepOverTh(6))
			.clean(new TerminologyService(corpus1));

		assertThat(corpus1)
			.hasNTerms(1)
			.containsTerm("na: word1 word2")
			;
	}

	@Test
	public void test2() {
		
		assertThat(corpus1)
		.hasNTerms(3)
		.containsTerm("a: word2")
		.containsTerm("n: word1")
		.containsTerm("na: word1 word2")
		;
		
		new TerminologyCleaner()
		.setOptions(new TerminoFilterOptions().by(TermProperty.FREQUENCY).keepOverTh(10))
		.clean(new TerminologyService(corpus1));

		assertThat(corpus1)
		.hasNTerms(0)
		;
	}

}
