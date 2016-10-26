package eu.project.ttc.test.func.align;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import eu.project.ttc.align.AlignmentMethod;
import eu.project.ttc.align.BilingualAligner;
import eu.project.ttc.align.TranslationCandidate;
import eu.project.ttc.api.TermIndexIO;
import eu.project.ttc.engines.desc.Lang;
import eu.project.ttc.models.Term;
import eu.project.ttc.models.TermIndex;
import eu.project.ttc.test.func.FunctionalTests;
import eu.project.ttc.tools.TermSuiteAlignerBuilder;

public class BilingualAlignerDeEnSpec {

	TermIndex deTermino;
	TermIndex enTermino;
	BilingualAligner aligner;
	
	@Before
	public void setup() {
		deTermino = TermIndexIO.fromJson(FunctionalTests.getTerminoWEShortPath(Lang.DE));
		enTermino = TermIndexIO.fromJson(FunctionalTests.getTerminoWEShortPath(Lang.EN));
		aligner = TermSuiteAlignerBuilder.start()
				.setSourceTerminology(deTermino)
				.setTargetTerminology(enTermino)
				.setDicoPath(FunctionalTests.getDicoPath(Lang.DE, Lang.EN))
				.setDistanceCosine()
				.create();
	}
	

	@Test
	public void testAlignerMWTComp() {
		Term t1 = deTermino.getTermByGroupingKey("n: windenergie");

		List<TranslationCandidate> results = aligner.align(t1, 3, 2);
		assertThat(results)
			.extracting("term.groupingKey", "method")
			.contains(
					tuple("nn: wind energy", AlignmentMethod.COMPOSITIONAL)
					)
			.hasSize(3)
			;
	}
	


}
