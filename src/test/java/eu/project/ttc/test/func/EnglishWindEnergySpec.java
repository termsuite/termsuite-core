package eu.project.ttc.test.func;

import static eu.project.ttc.test.TermSuiteAssertions.assertThat;
import static eu.project.ttc.test.func.FunctionalTests.termsByProperty;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.Assert.fail;

import java.util.List;

import org.assertj.core.util.Lists;
import org.junit.Test;

import eu.project.ttc.engines.cleaner.TermProperty;
import eu.project.ttc.engines.desc.Lang;
import eu.project.ttc.models.VariationType;

public class EnglishWindEnergySpec extends WindEnergySpec {
	

	@Override
	protected Lang getLang() {
		return Lang.EN;
	}


	@Override
	protected List<String> getSyntacticMatchingRules() {
		return Lists.newArrayList(
				"M-S-NN",
				"M-S-(A|N)NN",
				"M-I-EN-N|A",
				"M-I-NN-CA",
				"M-R2I-ANN",
				"M-ID-AN-CA",
				"M-PI-NN-P",
				"M-I-NN-N",
				"M-I-AN-N|A|R",
				"S-Ed-NN-PN",
				"S-Ed-NN-N",
				"S-Ed-NPN-CPN",
				"S-Ed-AN-PN",
				"S-Eg-NPN-A",
				"S-Eg-NPN-NC",
				"S-Eg-AN-(A|N)",
				"S-Eg-AN-R",
				"S-Eg-AN-AC",
				"S-EgD-NNN-A",
				"S-EgD-(A|N)N-A|N",
				"S-EgD-NN-R",
				"S-EgD2-(A|N)N-A|N",
				"S-I-AN-A",
				"S-I-AN-CA",
				"S-I-AN-(N|A)N|AA",
				"S-I-NN-(N|A)",
				"S-I1-NPN-PNC",
				"S-I2-NPN-A",
				"S-I2-ANN-N",
				"S-P-AAN-A",
				"S-P-ANN-N",
				"S-PEg-NN-NC",
				"S-PI-NN-PN",
				"S-PI-NN-CNP",
				"S-R1Eg-AN-N",
				"S-PI-AN-V",
				"S-PI-NN-P",
				"M-SD-(N|A)N",
				"S-R2I-NPN-P"
			);
	}


	@Override
	protected List<String> getSyntacticNotMatchingRules() {
		return Lists.newArrayList(
				"M-I2-(A|N)N-E",
				"M-R3I1-ANNN",
				"M-IPR2-NPN",
				"S-I1-NPN-CN",
				"S-PEg-NN-NP");
	}


	@Test
	public void testTop10ByFreq() {
		assertThat(termsByProperty(termIndex, TermProperty.FREQUENCY, true).subList(0, 10))
			.hasSize(10)
			.extracting("groupingKey")
			.containsExactly(
					"n: wind", "n: turbine", "nn: wind turbine", "n: power", 
					"n: energy", "n: blade", "n: project", "n: rotor", 
					"n: system", "n: figure"
					);
	}
	
	@Test
	public void testTop10ByWR() {
		assertThat(termsByProperty(termIndex, TermProperty.WR, true).subList(0, 10))
			.hasSize(10)
			.extracting("groupingKey")
			.containsExactly(
					"nn: wind turbine", "n: rotor", "nn: wind energy", "nn: wind speed", "nn: wind power", 
					"an: offshore wind", "n: m/s", "n: airfoil", "n: voltage", "n: coefficient"
					)
			;
	}


	@Test
	public void testTermHighSpeed() {
		assertThat(termIndex.getTermByGroupingKey("a: high-speed"))
			.hasFrequency(6)
			.hasNBases(0)
			.hasNVariationsOfType(4, VariationType.MORPHOLOGICAL)
			.hasNVariationsOfType(0, VariationType.SYNTACTICAL)
			.getVariations()
			.extracting("variant.groupingKey", "info", "variant.frequency")
			.contains(
					tuple("aan: high revolving speed", "M-I-AN-N|A|R", 1),
					tuple("aan: high rotational speed", "M-I-AN-N|A|R", 3),
					tuple("an: high speed", "M-SD-(N|A)N", 7),
					tuple("ann: high wind speed", "M-I-AN-N|A|R", 6)
				);
	}

	@Test
	public void testWindTurbine() {
		assertThat(termIndex.getTermByGroupingKey("nn: wind turbine"))
				.hasFrequency(1852)
				.hasGroupingKey("nn: wind turbine");
	}

	@Test
	public void testTop10ByRank() {
		assertThat(termsByProperty(termIndex, TermProperty.RANK, false).subList(0, 10))
			.hasSize(10)
			.extracting("groupingKey")
			.containsExactly(
					"nn: wind turbine", "n: rotor", "nn: wind energy", "nn: wind speed", "nn: wind power", 
					"an: offshore wind", "n: m/s", "n: airfoil", "n: voltage", "n: coefficient"
			)
			;
	}

	@Test
	public void testMSNNVariations() {
		assertThat(termIndex)
			.hasNVariationsOfType(1266, VariationType.MORPHOLOGICAL)
			.asTermVariationsHavingObject("M-S-NN")
			.hasSize(130)
			.extracting("base.groupingKey", "variant.groupingKey")
			.contains(
				   tuple("n: baseline", "nn: base line"), 
				   tuple("n: groundwater", "nn: ground water"), 
				   tuple("n: spreadsheet", "nn: spread sheet"), 
				   tuple("n: gearbox", "nn: gear box")
			)
			;
	}

		   
	
	@Test
	public void testSyntacticalVariations() {
		assertThat(termIndex)
			.containsVariation("nn: wind turbine", VariationType.SYNTACTICAL, "nnn: wind regime turbine", "S-I-NN-(N|A)")
			.containsVariation("an: low frequency", VariationType.SYNTACTICAL, "aan: low audible frequency", "S-I-AN-A")
			.containsVariation("nn: wind generator", VariationType.SYNTACTICAL, "nnn: wind turbine generator", "S-I-NN-(N|A)")
			;
	}

	@Test
	public void testSyntacticalVariationsWithPrefixes() {
		fail("No prefix yet in english resources");

//		assertThat(termIndex)
//		.asTermVariationsHavingObject("NA-NprefA")
//		.extracting("base.groupingKey", "variant.groupingKey")
//		.contains(
//			tuple("na: générateur synchrone", "na: générateur asynchrone"),
//			tuple("na: machine synchrone", "na: machine asynchrone"),
//			tuple("na: contrôle direct", "na: contrôle indirect"),
//			tuple("na: mode direct", "na: mode indirect"),
//			tuple("na: aspect esthétique", "na: aspect inesthétique"),
//			tuple("na: option nucléaire", "na: option antinucléaire"),
//			tuple("na: génératrice synchrone", "na: génératrice asynchrone"),
//			tuple("na: mesure précis", "na: mesure imprécis"),
//			tuple("na: circulation stationnaire", "na: circulation instationnaire")
//		)
//		.hasSize(26)
//		;
		
	}


	@Test
	public void testSyntacticalVariationsWithDerivates() {
		fail("No prefix yet in english resources");
//		assertThat(termIndex)
//			.asTermVariationsHavingObject("S-R2D-NPN")
//			.hasSize(77)
//			.extracting("base.groupingKey", "variant.groupingKey")
//			.contains(
//					tuple("npn: production de électricité", "na: production électrique"),
//					tuple("npn: étude de environnement", "na: étude environnemental"),
//					tuple("npn: génération de électricité", "na: génération électrique")
//			)
//			;
	}

	@Test
	public void testPrefixes() {
		fail("No prefix yet in english resources");
//		assertThat(termIndex)
//			.containsVariation("a: multipolaire", VariationType.IS_PREFIX_OF, "a: polaire")
//			.containsVariation("n: cofinancement", VariationType.IS_PREFIX_OF, "n: financement")
//			.containsVariation("a: tripale", VariationType.IS_PREFIX_OF, "n: pale")
//			.containsVariation("a: bipale", VariationType.IS_PREFIX_OF, "n: pale")
//			.containsVariation("a: asynchrone", VariationType.IS_PREFIX_OF, "a: synchrone")
//			.containsVariation("n: déréglementation", VariationType.IS_PREFIX_OF, "n: réglementation")
//			;
	}
	
	@Test
	public void testDerivations() {
		fail("No derivation yet in english resources");
//		assertThat(termIndex)
//			.containsVariation("n: hydroélectricité", VariationType.DERIVES_INTO, "a: hydroélectrique")
//			.containsVariation("n: stator", VariationType.DERIVES_INTO, "a: statorique")
//			.containsVariation("n: usage", VariationType.DERIVES_INTO, "n: usager")
//			.containsVariation("n: support", VariationType.DERIVES_INTO, "n: supportage")
//			.containsVariation("n: commerce", VariationType.DERIVES_INTO, "a: commercial")
//			;
	}


}
