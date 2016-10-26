package eu.project.ttc.test.func.align;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import eu.project.ttc.align.AlignmentMethod;
import eu.project.ttc.align.BilingualAligner;
import eu.project.ttc.align.RequiresSize2Exception;
import eu.project.ttc.align.TranslationCandidate;
import eu.project.ttc.api.TermIndexIO;
import eu.project.ttc.engines.desc.Lang;
import eu.project.ttc.models.Term;
import eu.project.ttc.models.TermIndex;
import eu.project.ttc.test.func.FunctionalTests;
import eu.project.ttc.tools.TermSuiteAlignerBuilder;

public class BilingualAlignerFrEnSpec {

	TermIndex frTermino;
	TermIndex enTermino;
	BilingualAligner aligner;
	
	@Before
	public void setup() {
		frTermino = TermIndexIO.fromJson(FunctionalTests.getTerminoWEShortPath(Lang.FR));
		enTermino = TermIndexIO.fromJson(FunctionalTests.getTerminoWEShortPath(Lang.EN));
		aligner = TermSuiteAlignerBuilder.start()
				.setSourceTerminology(frTermino)
				.setTargetTerminology(enTermino)
				.setDicoPath(FunctionalTests.getDicoPath(Lang.FR, Lang.EN))
				.setDistanceCosine()
				.create();
	}
	
	
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
	@Test
	public void failWhenTermSizeGreaterThan2() {
		thrown.expect(RequiresSize2Exception.class);
		Term t1 = frTermino.getTermByGroupingKey("npna: production de énergie électrique");
		aligner.align(t1, 3, 2);
	}

	@Test
	public void failWhenSourceTermNotExisting() {
		thrown.expect(NullPointerException.class);
		Term t1 = frTermino.getTermByGroupingKey("npna: production de ssénergie électrique");
		aligner.align(t1, 3, 2);
	}

	@Test
	public void testAlignerMWTComp() {
		Term t1 = frTermino.getTermByGroupingKey("na: turbine éolien");

		List<TranslationCandidate> results = aligner.align(t1, 3, 2);
		assertThat(results)
			.hasSize(1)
			.extracting("term.groupingKey", "method")
			.containsExactly(
					tuple("nn: wind turbine", AlignmentMethod.COMPOSITIONAL)
					);
	}
	

	@Test
	public void testAlignerNeoclassicalDico() {
		Term t1 = frTermino.getTermByGroupingKey("a: aérodynamique");
		List<TranslationCandidate> results = aligner.alignNeoclassical(t1, 3, 1);
		assertThat(results)
			.hasSize(3)
			.extracting("term.groupingKey", "method")
			.containsExactly(
					tuple("a: aerodynamic", AlignmentMethod.NEOCLASSICAL),
					tuple("r: aerodynamically", AlignmentMethod.NEOCLASSICAL),
					tuple("n: aerodynamics", AlignmentMethod.NEOCLASSICAL)
					);
	}

	@Test
	public void testAlignerNeoclassicalGraph() {
		Term t1 = frTermino.getTermByGroupingKey("n: géothermie");
		List<TranslationCandidate> results = aligner.alignNeoclassical(t1, 3, 1);
		assertThat(results)
			.hasSize(3)
			.extracting("term.groupingKey", "method")
			.containsExactly(
					tuple("a: geothermal", AlignmentMethod.NEOCLASSICAL),
					tuple("n: geometry", AlignmentMethod.NEOCLASSICAL),
					tuple("a: geometrical", AlignmentMethod.NEOCLASSICAL)
					);
	}

	@Test
	public void testAlignerMWTSemiDist() {
		Term t1 = frTermino.getTermByGroupingKey("na: parc éolien");

		List<TranslationCandidate> results = aligner.align(t1, 3, 2);
		assertThat(results)
			.hasSize(3)
			.extracting("term.groupingKey", "method")
			.containsExactly(
					tuple("nn: wind turbine", AlignmentMethod.SEMI_DISTRIBUTIONAL),
					tuple("nn: wind power", AlignmentMethod.SEMI_DISTRIBUTIONAL),
					tuple("nn: wind energy", AlignmentMethod.SEMI_DISTRIBUTIONAL)
					);
	}
	@Test
	public void testAlignerSWT() {
		Term t1 = frTermino.getTermByGroupingKey("n: énergie");

		List<TranslationCandidate> results = aligner.align(t1, 3, 2);
		assertThat(results)
			.hasSize(3)
			.extracting("term.groupingKey", "method")
			.containsExactly(
					tuple("n: energy", AlignmentMethod.DICTIONARY),
					tuple("n: power", AlignmentMethod.DICTIONARY),
					tuple("n: turbine", AlignmentMethod.DISTRIBUTIONAL)
					);
		
		
	}

}
