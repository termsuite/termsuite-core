
/*******************************************************************************
 * Copyright 2015-2016 - CNRS (Centre National de Recherche Scientifique)
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *******************************************************************************/

package eu.project.ttc.test.func;

import static eu.project.ttc.test.TermSuiteAssertions.assertThat;
import static eu.project.ttc.test.func.FunctionalTests.termsByProperty;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.util.List;
import java.util.stream.Collectors;

import org.assertj.core.util.Lists;
import org.junit.Test;

import eu.project.ttc.engines.cleaner.TermProperty;
import eu.project.ttc.engines.desc.Lang;
import eu.project.ttc.models.CompoundType;
import eu.project.ttc.models.Term;
import eu.project.ttc.models.VariationType;
import eu.project.ttc.models.Word;

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
				"S-R2I-NPN-P",
				"ANN-prefANN",
				"AAN-AprefAN",
				"S-R2D-NN1",
				"M-I2-(A|N)N-E",
				"M-R3I1-ANNN",
				"AN-prefAN"
			);
	}


	@Override
	protected List<String> getSyntacticNotMatchingRules() {
		return Lists.newArrayList(
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
		assertThat(termsByProperty(termIndex, TermProperty.SPECIFICITY, true).subList(0, 10))
			.hasSize(10)
			.extracting("groupingKey")
			.containsExactly(
					"nn: wind turbine", "n: rotor", "nn: wind energy", "nn: wind speed", "nn: wind power", 
					"an: offshore wind", "n: m/s", "n: airfoil", "n: voltage", "n: coefficient"
					)
			;
	}


	@Test
	public void testTermElectromagnec() {
		Term term = termIndex.getTermByGroupingKey("a: electromagnetic");
		
		assertThat(term)
			.isCompound()
			.hasCompoundType(CompoundType.NEOCLASSICAL)
			.hasCompositionSubstrings("electro", "magnetic");
	}

	@Test
	public void testTermHydroelectric() {
		Term term = termIndex.getTermByGroupingKey("a: hydroelectric");
		
		assertThat(term)
			.isCompound()
			.hasCompoundType(CompoundType.NEOCLASSICAL)
			.hasCompositionSubstrings("hydro", "electric")
			.hasCompositionLemmas("water", "electric")
			;
	}

	@Test
	public void weNeoclassicalCompounds() {
		List<Word> neoclassicals = termIndex.getWords().stream()
			.filter(Word::isCompound)
			.filter(w -> w.getCompoundType() == CompoundType.NEOCLASSICAL).collect(Collectors.toList());
		
		assertThat(neoclassicals)
			.isNotEmpty()
			.extracting("lemma", "neoclassicalAffix.lemma")
			.contains(tuple("hydroelectric", "water"))
			.hasSize(769);
	}

	
	@Test
	public void testTermHighSpeed() {
		Term term = termIndex.getTermByGroupingKey("a: high-speed");
		assertThat(term)
			.hasFrequency(6);
		
		assertThat(termIndex)
			.hasNBases(term, 1) // a: highspeed
			.hasAtLeastNBasesOfType(term, 1, VariationType.GRAPHICAL)
			.hasNVariationsOfType(term, 4, VariationType.MORPHOLOGICAL)
			.hasNVariationsOfType(term, 0, VariationType.SYNTACTICAL)
			.getVariations(term)
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
//			.hasNVariationsOfType(1266, VariationType.MORPHOLOGICAL)
			.asTermVariationsHavingObject("M-S-NN")
			.hasSize(128)
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
		assertThat(termIndex)
		.asTermVariationsHavingObject("AN-prefAN")
		.extracting("base.groupingKey", "variant.groupingKey")
		.contains(
				tuple("an: national regulation", "an: international regulation"),
				tuple("an: finite number", "an: infinite number"),
				tuple("an: conventional horizontal-axis", "an: unconventional horizontal-axis"),
				tuple("an: favourable conservation", "an: unfavourable conservation"),
				tuple("an: national standard", "an: international standard"),
				tuple("an: transient time", "an: subtransient time"),
				tuple("an: rotational motion", "an: irrotational motion"),
				tuple("an: sound emission", "an: infrasound emission"),
				tuple("an: national wind", "an: international wind"),
				tuple("an: sufficient time", "an: insufficient time"),
				tuple("an: direct employment", "an: indirect employment"),
				tuple("an: audible sound", "an: subaudible sound"),
				tuple("an: geographical region", "an: biogeographical region"),
				tuple("an: national level", "an: supranational level"),
				tuple("an: direct impact", "an: indirect impact"),
				tuple("an: twisted blade", "an: untwisted blade"),
				tuple("an: direct benefit", "an: indirect benefit"),
				tuple("an: significant effect", "an: insignificant effect"),
				tuple("an: national energy", "an: international energy"),
				tuple("an: sufficient evidence", "an: insufficient evidence"),
				tuple("an: sufficient information", "an: insufficient information"),
				tuple("an: dominant sound", "an: predominant sound"),
				tuple("an: active power", "an: reactive power"),
				tuple("an: national commitment", "an: international commitment"),
				tuple("an: significant area", "an: insignificant area"),
				tuple("an: sound level", "an: infrasound level"),
				tuple("an: audible level", "an: inaudible level"),
				tuple("an: proper installation", "an: improper installation"),
				tuple("an: national agency", "an: international agency"),
				tuple("an: limited amount", "an: unlimited amount"),
				tuple("an: suitable site", "an: unsuitable site"),
				tuple("an: national institute", "an: international institute")
		)
		.hasSize(32)
		;
		
	}


	@Test
	public void testSyntacticalVariationsWithDerivates() {
		assertThat(termIndex)
			.asTermVariationsHavingObject("S-R2D-NN1")
			.extracting("base.groupingKey", "variant.groupingKey")
			.contains(
				tuple("nn: rotation speed", "an: rotational speed"),
				tuple("nn: azimuth position", "an: azimuthal position"),
				tuple("nn: gas cylinder", "an: gaseous cylinder"),
				tuple("nn: environment research", "an: environmental research"),
				tuple("nn: operation phase", "an: operational phase"),
				tuple("nn: experiment field", "an: experimental field"),
				tuple("nn: operation cost", "an: operational cost"),
				tuple("nn: environment condition", "an: environmental condition"),
				tuple("nn: environment report", "an: environmental report"),
				tuple("nn: environment protection", "an: environmental protection"),
				tuple("nn: experiment datum", "an: experimental datum"),
				tuple("nn: magnet field", "an: magnetic field"),
				tuple("nn: addition power", "an: additional power"),
				tuple("nn: government agency", "an: governmental agency"),
				tuple("nn: territory planning", "an: territorial planning"),
				tuple("nn: axis wind", "an: axial wind"),
				tuple("nn: season change", "an: seasonal change"),
				tuple("nn: industry noise", "an: industrial noise"),
				tuple("nn: axis direction", "an: axial direction")
			)
			.hasSize(19)
		;
	}

	@Test
	public void testPrefixes() {
		assertThat(termIndex)
			.containsVariation("n: postconstruction", VariationType.IS_PREFIX_OF, "n: construction")
			.containsVariation("n: microgeneration", VariationType.IS_PREFIX_OF, "n: generation")
			.containsVariation("a: subtransient", VariationType.IS_PREFIX_OF, "a: transient")
			.containsVariation("n: incompetence", VariationType.IS_PREFIX_OF, "n: competence")
			.containsVariation("a: subacoustic", VariationType.IS_PREFIX_OF, "a: acoustic")
			;
	}
	
	@Test
	public void testDerivations() {
		assertThat(termIndex)
			.containsVariation("n: photograph", VariationType.DERIVES_INTO, "a: photographic")
			.containsVariation("n: ethic", VariationType.DERIVES_INTO, "a: ethical")
			.containsVariation("n: institution", VariationType.DERIVES_INTO, "a: institutional")
			.containsVariation("n: industry", VariationType.DERIVES_INTO, "a: industrial")
			;
	}


}
