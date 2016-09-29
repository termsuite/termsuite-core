package eu.project.ttc.test.func.tools.builders;

import static eu.project.ttc.test.TermSuiteAssertions.assertThat;

import java.net.MalformedURLException;

import org.junit.Before;
import org.junit.Test;

import eu.project.ttc.api.TermIndexIO;
import eu.project.ttc.api.TerminoFilterConfig;
import eu.project.ttc.api.TerminoFilterer;
import eu.project.ttc.engines.cleaner.TermProperty;
import eu.project.ttc.models.TermIndex;
import eu.project.ttc.test.func.FunctionalTests;
import eu.project.ttc.tools.TermSuiteResourceManager;

public class TerminoFiltererSpec {

	TermIndex termIndex1;
	@Before
	public void setup() throws MalformedURLException {
		TermSuiteResourceManager.getInstance().clear();
		termIndex1 = TermIndexIO.fromJson(FunctionalTests.TERM_INDEX_1.toUri().toURL());
	}
	
	@Test
	public void test1() {
		
		assertThat(termIndex1)
			.hasSize(3)
			.containsTerm("a: word2")
			.containsTerm("n: word1")
			.containsTerm("na: word1 word2")
			;
		
		TerminoFilterer.create(termIndex1)
			.configure(new TerminoFilterConfig().by(TermProperty.FREQUENCY).keepOverTh(6))
			.execute();

		assertThat(termIndex1)
			.hasSize(1)
			.containsTerm("na: word1 word2")
			;
	}

	@Test
	public void test2() {
		
		assertThat(termIndex1)
		.hasSize(3)
		.containsTerm("a: word2")
		.containsTerm("n: word1")
		.containsTerm("na: word1 word2")
		;
		
		TerminoFilterer.create(termIndex1)
		.configure(new TerminoFilterConfig().by(TermProperty.FREQUENCY).keepOverTh(10))
		.execute();
		
		assertThat(termIndex1)
		.hasSize(0)
		;
	}

}
