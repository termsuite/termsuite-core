package fr.univnantes.termsuite.test.func.align;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import fr.univnantes.termsuite.alignment.AlignmentMethod;
import fr.univnantes.termsuite.alignment.BilingualAlignmentService;
import fr.univnantes.termsuite.alignment.TranslationCandidate;
import fr.univnantes.termsuite.api.IndexedCorpusIO;
import fr.univnantes.termsuite.api.BilingualAligner;
import fr.univnantes.termsuite.api.TermSuiteException;
import fr.univnantes.termsuite.index.Terminology;
import fr.univnantes.termsuite.model.Lang;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.test.func.FunctionalTests;

public class BilingualAlignerFrEnSpec {

	Terminology frTermino;
	Terminology enTermino;
	BilingualAlignmentService aligner;
	
	@Before
	public void setup() {
		frTermino = IndexedCorpusIO.fromJson(FunctionalTests.getTerminoWEShortPath(Lang.FR)).getTerminology();
		enTermino = IndexedCorpusIO.fromJson(FunctionalTests.getTerminoWEShortPath(Lang.EN)).getTerminology();
		
		aligner = new BilingualAligner()
				.setSourceTerminology(frTermino)
				.setTargetTerminology(enTermino)
				.setDicoPath(FunctionalTests.getDicoPath(Lang.FR, Lang.EN))
				.setDistanceCosine()
				.create();
	}
	
	
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
	
	@Test
	public void failWhenSourceTermNotExisting() {
		thrown.expect(NullPointerException.class);
		Term t1 = frTermino.getTerms().get("npna: production de ssénergie électrique");
		aligner.alignSize2(t1, 3, 2);
	}


	@Test
	public void testAddTranslationFailWhenSourceLemmaDoesNotExist() {
		thrown.expect(TermSuiteException.class);
		thrown.expectMessage("No term found in source termino with lemma: tatayoyo");
		aligner.addTranslation("tatayoyo", "titi");
	}

	@Test
	public void testAddTranslationFailWhenTargetLemmaDoesNotExist() {
		thrown.expect(TermSuiteException.class);
		thrown.expectMessage("No term found in target termino with lemma: titi");
		aligner.addTranslation("éolienne", "titi");
	}

	@Test
	public void testAlignMWTWithManualDico() {
		Term t1 = frTermino.getTerms().get("npna: production de énergie électrique");
		aligner.addTranslation(
				frTermino.getTerms().get("npn: production de énergie"), 
				enTermino.getTerms().get("nn: energy production"));
		aligner.addTranslation(
				frTermino.getTerms().get("a: électrique"), 
				enTermino.getTerms().get("a: electrical"));
		List<TranslationCandidate> results = aligner.align(t1, 3, 1);
		assertThat(results)
			.hasSize(1)
			.extracting("term.groupingKey", "method")
			.contains(
				tuple("ann: electrical energy production", AlignmentMethod.COMPOSITIONAL)
			);
	}

	@Test
	public void testTermSizeGreaterThan2() {
		Term t1 = frTermino.getTerms().get("npna: production de énergie électrique");
		List<TranslationCandidate> results = aligner.align(t1, 3, 2);
		assertThat(results)
			.hasSize(1)
			.extracting("term.groupingKey", "method")
			.contains(
				tuple("npan: production of electric power", AlignmentMethod.COMPOSITIONAL)
			);
	}


	@Test
	public void testAlignSWTWithManualDico() {
		Term eolienne = frTermino.getTerms().get("n: éolienne");
		TranslationCandidate noDicoCandidate = aligner.align(
				eolienne, 
				1, 
				1).get(0);
		
		assertThat(noDicoCandidate.getTerm().getGroupingKey()).isEqualTo("n: wind");
		assertThat(noDicoCandidate.getMethod()).isEqualTo(AlignmentMethod.DICTIONARY);
		
		aligner.addTranslation(eolienne, enTermino.getTerms().get("nn: wind turbine"));
		TranslationCandidate dicoCandidate = aligner.align(
				eolienne, 
				1, 
				1).get(0);
		
		assertThat(dicoCandidate.getTerm().getGroupingKey()).isEqualTo("nn: wind turbine");
		assertThat(dicoCandidate.getMethod()).isEqualTo(AlignmentMethod.DICTIONARY);
	}

	
	@Test
	public void testAlignerMWTComp() {
		Term t1 = frTermino.getTerms().get("na: turbine éolien");

		List<TranslationCandidate> results = aligner.alignSize2(t1, 3, 2);
		assertThat(results)
			.hasSize(1)
			.extracting("term.groupingKey", "method")
			.containsExactly(
					tuple("nn: wind turbine", AlignmentMethod.COMPOSITIONAL)
					);
	}
	

	@Test
	public void testAlignerNeoclassicalDico() {
		Term t1 = frTermino.getTerms().get("a: aérodynamique");
		List<TranslationCandidate> results = aligner.alignNeoclassical(t1, 3, 1);
		assertThat(results)
			.hasSize(3)
			.extracting("term.groupingKey", "method")
			.containsExactly(
					tuple("a: aerodynamic", AlignmentMethod.NEOCLASSICAL),
					tuple("n: aerodynamics", AlignmentMethod.NEOCLASSICAL),
					tuple("r: aerodynamically", AlignmentMethod.NEOCLASSICAL)
					);
	}

	@Test
	public void testAlignerNeoclassicalGraph() {
		Term t1 = frTermino.getTerms().get("n: géothermie");
		List<TranslationCandidate> results = aligner.alignNeoclassical(t1, 3, 1);
		assertThat(results)
			.hasSize(3)
			.extracting("term.groupingKey", "method")
			.containsExactly(
					tuple("a: geothermal", AlignmentMethod.NEOCLASSICAL),
					tuple("n: geometry", AlignmentMethod.NEOCLASSICAL),
					tuple("a: geometric", AlignmentMethod.NEOCLASSICAL)
					);
	}

	@Test
	public void testAlignerMWTSemiDist() {
		Term t1 = frTermino.getTerms().get("na: parc éolien");

		List<TranslationCandidate> results = aligner.alignSize2(t1, 3, 2);
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
		Term t1 = frTermino.getTerms().get("n: énergie");

		List<TranslationCandidate> results = aligner.alignSize2(t1, 3, 2);
		assertThat(results)
			.hasSize(3)
			.extracting("term.groupingKey", "method")
			.containsExactly(
					tuple("n: power", AlignmentMethod.DICTIONARY),
					tuple("n: energy", AlignmentMethod.DICTIONARY),
					tuple("n: motor", AlignmentMethod.DISTRIBUTIONAL)
					);
		
		
	}

}
