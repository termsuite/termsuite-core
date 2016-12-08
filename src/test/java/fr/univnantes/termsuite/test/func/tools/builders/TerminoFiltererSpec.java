package fr.univnantes.termsuite.test.func.tools.builders;

import static fr.univnantes.termsuite.test.TermSuiteAssertions.assertThat;

import java.net.MalformedURLException;

import org.junit.Before;
import org.junit.Test;

import fr.univnantes.termsuite.api.TerminoFilterConfig;
import fr.univnantes.termsuite.api.TerminoFilterer;
import fr.univnantes.termsuite.api.TerminologyIO;
import fr.univnantes.termsuite.model.TermProperty;
import fr.univnantes.termsuite.model.Terminology;
import fr.univnantes.termsuite.test.func.FunctionalTests;
import fr.univnantes.termsuite.utils.TermSuiteResourceManager;

public class TerminoFiltererSpec {

	Terminology termino1;
	@Before
	public void setup() throws MalformedURLException {
		TermSuiteResourceManager.getInstance().clear();
		termino1 = TerminologyIO.fromJson(FunctionalTests.TERM_INDEX_1.toUri().toURL());
	}
	
	@Test
	public void test1() {
		
		assertThat(termino1)
			.hasNTerms(3)
			.containsTerm("a: word2")
			.containsTerm("n: word1")
			.containsTerm("na: word1 word2")
			;
		
		TerminoFilterer.create(termino1)
			.configure(new TerminoFilterConfig().by(TermProperty.FREQUENCY).keepOverTh(6))
			.execute();

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
		
		TerminoFilterer.create(termino1)
		.configure(new TerminoFilterConfig().by(TermProperty.FREQUENCY).keepOverTh(10))
		.execute();
		
		assertThat(termino1)
		.hasNTerms(0)
		;
	}

}
