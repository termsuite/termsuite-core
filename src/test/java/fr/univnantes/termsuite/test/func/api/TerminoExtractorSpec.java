package fr.univnantes.termsuite.test.func.api;


import static fr.univnantes.termsuite.test.asserts.TermSuiteAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.atIndex;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import fr.univnantes.termsuite.api.PipelineListener;
import fr.univnantes.termsuite.api.TermSuite;
import fr.univnantes.termsuite.model.IndexedCorpus;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.test.func.FunctionalTests;

public class TerminoExtractorSpec {
	IndexedCorpus corpus ;
	@Before
	public void setup() {
		corpus = TermSuite.preprocessor()
				.setTaggerPath(FunctionalTests.getTaggerPath())
				.toIndexedCorpus(FunctionalTests.CORPUS1, 500000);
		
		
	}
	
	@Test
	public void testWithListener() {
		List<Double> progresses = new ArrayList<>();
		List<String> messages = new ArrayList<>();
		PipelineListener listener = new PipelineListener() {
			@Override
			public void statusUpdated(double progress, String msg) {
				progresses.add(progress);
				messages.add(msg);
			}
		};
		
		TermSuite.terminoExtractor()
			.setListener(listener)
			.execute(corpus);
		
		assertThat(progresses)
			.hasSize(34)
			.contains(1d/34, 2d/34,3d/34, 33d/34);
		
		assertThat(messages)
		.hasSize(34)
		.contains("StatEngine", atIndex(0))
		.contains("TerminologyChecker", atIndex(1))
		.contains("StopWordCleaner", atIndex(2))
		.contains("TermRanker", atIndex(33));
		

	}

	@Test
	public void testCorpus1() {
		
		TermSuite.terminoExtractor()
			.execute(corpus);
		
		assertThat(corpus.getTerminology())
			.containsTerm("n: énergie");
		
		Term t = corpus.getTerminology().getTerms().get("n: énergie");
		assertEquals(3.374554d, t.getSpecificity(), 0.0001d);
	}
}
