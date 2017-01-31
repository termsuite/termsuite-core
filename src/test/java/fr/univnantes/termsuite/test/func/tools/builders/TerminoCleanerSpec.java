package fr.univnantes.termsuite.test.func.tools.builders;

import static fr.univnantes.termsuite.test.TermSuiteAssertions.assertThat;

import java.net.MalformedURLException;

import org.junit.Before;
import org.junit.Test;

import fr.univnantes.termsuite.api.IndexedCorpusIO;
import fr.univnantes.termsuite.engines.cleaner.TerminoFilterOptions;
import fr.univnantes.termsuite.engines.cleaner.TerminologyCleaner;
import fr.univnantes.termsuite.framework.service.TerminologyService;
import fr.univnantes.termsuite.index.Terminology;
import fr.univnantes.termsuite.model.TermProperty;
import fr.univnantes.termsuite.test.func.FunctionalTests;

public class TerminoCleanerSpec {

	Terminology termino1;
	@Before
	public void setup() throws MalformedURLException {
		termino1 = IndexedCorpusIO.fromJson(FunctionalTests.TERMINOLOGY_1.toUri().toURL());
	}
	
	@Test
	public void test1() {
		
		assertThat(termino1)
			.hasNTerms(3)
			.containsTerm("a: word2")
			.containsTerm("n: word1")
			.containsTerm("na: word1 word2")
			;
		
		new TerminologyCleaner()
			.setOptions(new TerminoFilterOptions().by(TermProperty.FREQUENCY).keepOverTh(6))
			.clean(new TerminologyService(termino1));

		assertThat(termino1)
			.hasNTerms(1)
			.containsTerm("na: word1 word2")
			;
	}

	@Test
	public void test2() {
		
		assertThat(termino1)
		.hasNTerms(3)
		.containsTerm("a: word2")
		.containsTerm("n: word1")
		.containsTerm("na: word1 word2")
		;
		
		new TerminologyCleaner()
		.setOptions(new TerminoFilterOptions().by(TermProperty.FREQUENCY).keepOverTh(10))
		.clean(new TerminologyService(termino1));

		assertThat(termino1)
		.hasNTerms(0)
		;
	}

}
