
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

package fr.univnantes.termsuite.test.func;

import static fr.univnantes.termsuite.test.TermSuiteAssertions.assertThat;
import static fr.univnantes.termsuite.test.func.FunctionalTests.termsByProperty;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.assertj.core.util.Lists;
import org.junit.Test;

import fr.univnantes.termsuite.engines.gatherer.VariationType;
import fr.univnantes.termsuite.framework.Relations;
import fr.univnantes.termsuite.framework.service.TerminologyService;
import fr.univnantes.termsuite.index.TermIndexType;
import fr.univnantes.termsuite.index.TermIndexValueProvider;
import fr.univnantes.termsuite.model.CompoundType;
import fr.univnantes.termsuite.model.Lang;
import fr.univnantes.termsuite.model.RelationProperty;
import fr.univnantes.termsuite.model.RelationType;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.TermProperty;
import fr.univnantes.termsuite.model.Relation;
import fr.univnantes.termsuite.model.Word;
import fr.univnantes.termsuite.test.unit.TermSuiteExtractors;
import fr.univnantes.termsuite.test.unit.UnitTests;

public class EnglishWindEnergySpec extends WindEnergySpec {
	

	@Override
	protected Lang getLang() {
		return Lang.EN;
	}


	@Override
	protected List<String> getSyntacticMatchingRules() {
		return Lists.newArrayList(
				"M-S-NN",
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
				"S-I1-NPN-CN",
				"S-PEg-NN-NP", "S-PID-AN-P", "S-R2D-NN");
	}

	@Override
	protected List<String> getRulesNotTested() {
		return Lists.newArrayList(
				"AN-synAN",
				"AN-AsynN",
				"NN-NsynN",
				"NN-synNN",
				"NPN-NPsynN",
				"NPN-synNPN"
				);
	}

	

	@Test
	public void testTop10ByFreq() {
		assertThat(termsByProperty(termino, TermProperty.FREQUENCY, true).subList(0, 10))
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
		assertThat(termsByProperty(termino, TermProperty.SPECIFICITY, true).subList(0, 10))
			.hasSize(10)
			.extracting("groupingKey")
			.containsExactly(
					"nn: wind turbine", "n: rotor", "nn: wind energy", "nn: wind speed", "nn: wind power", 
					"an: offshore wind", "n: m/s", "n: airfoil", "n: voltage", "n: coefficient"
					)
			;
	}


	
	@Test
	public void testInferedVariations() {
		assertThat(termino)
			.containsVariation(
					"nnn: horizontal-axis wind turbine", VariationType.INFERENCE, "annn: horizontal axis wind turbine")
			.containsVariation(
				"annn: conventional horizontal-axis wind turbine", VariationType.INFERENCE, "annn: unconventional horizontal-axis wind turbine");

	}

	@Test
	public void testInferenceOnHorizontalAxis() {
		Optional<Relation> rel1 = UnitTests.getTerminologyService(corpus)
				.variations("nnn: horizontal-axis wind turbine", "annn: horizontal axis wind turbine")
				.findFirst();

		assertTrue("Relation not found", rel1.isPresent());
		assertThat(rel1.get().getProperties())
			.containsEntry(RelationProperty.IS_INFERED, true)
			.containsEntry(RelationProperty.IS_MORPHOLOGICAL, true)
			.containsEntry(RelationProperty.IS_DERIVATION, false)
			.containsEntry(RelationProperty.IS_PREFIXATION, false)
			.containsEntry(RelationProperty.IS_SYNTAGMATIC, false)
			.containsEntry(RelationProperty.IS_GRAPHICAL, true)
			;
	}


	@Test
	public void testNumberOfInferedVariations() {
		TerminologyService service = UnitTests.getTerminologyService(corpus);
		assertThat(service.relations()
				.filter(Relations.IS_INFERENCE)
				.collect(Collectors.toSet())).hasSize(510);
		assertThat(service.relations()
				.filter(Relations.IS_INFERENCE)
				.filter(Relations.IS_GRAPHICAL)
				.collect(Collectors.toSet())).hasSize(176);
		assertThat(service.relations()
				.filter(Relations.IS_INFERENCE)
				.filter(Relations.IS_SEMANTIC)
				.collect(Collectors.toSet())).hasSize(0);
		assertThat(service.relations()
				.filter(Relations.IS_INFERENCE)
				.filter(Relations.IS_PREFIXATION)
				.collect(Collectors.toSet())).hasSize(9);
		assertThat(service.relations()
				.filter(Relations.IS_INFERENCE)
				.filter(Relations.IS_DERIVATION)
				.collect(Collectors.toSet())).hasSize(1);
		assertThat(service.relations()
				.filter(Relations.IS_INFERENCE)
				.filter(Relations.IS_MORPHOLOGICAL)
				.collect(Collectors.toSet())).hasSize(500);
		assertThat(service.relations()
				.filter(Relations.IS_INFERENCE)
				.filter(Relations.IS_SYNTAGMATIC)
				.collect(Collectors.toSet())).hasSize(0);
	}

	@Test
	public void testMorphologicalVariations() {
		Stream<Relation> variationsTypedMorpho = UnitTests.getTerminologyService(corpus)
				.variations(VariationType.MORPHOLOGICAL);
		assertThat(variationsTypedMorpho.collect(Collectors.toList())).hasSize(458);

		Stream<Relation> variationsTaggedMorpho = UnitTests.getTerminologyService(corpus)
				.relations(RelationProperty.IS_MORPHOLOGICAL, true);
		assertThat(variationsTaggedMorpho.collect(Collectors.toList())).hasSize(958);
	}


	@Test
	public void testTermElectromagnec() {
		Term term = termino.getTerms().get("a: electromagnetic");
		
		assertThat(term)
			.isCompound()
			.hasCompoundType(CompoundType.NEOCLASSICAL)
			.hasCompositionSubstrings("electro", "magnetic");
	}

	@Test
	public void testSWTFlagSet() {
		Term term = termino.getTerms().get("npn: power of wind");
		assertTrue(term.getWords().get(0).isSwt());
		assertFalse(term.getWords().get(1).isSwt());
		assertTrue(term.getWords().get(2).isSwt());
	}

	@Test
	public void testTermHorizontalAxis() throws InstantiationException, IllegalAccessException {
		Term morph = termino.getTerms().get("n: horizontal-axis");
		Term syntag = termino.getTerms().get("an: horizontal axis");

		assertThat(morph)
			.isCompound();

		assertThat(morph.getWords().get(0).getWord().getComponents())
			.extracting("lemma", "begin", "end")
			.containsExactly(
					tuple("horizontal", 0,10),
					tuple("axis", 11,15)
				);

		TermIndexValueProvider allCompProvider = TermIndexType.ALLCOMP_PAIRS.getProviderClass().newInstance();
		assertThat(allCompProvider.getClasses(morph))
			.containsExactly("axis+horizontal");

		assertThat(TermIndexType.ALLCOMP_PAIRS.getProviderClass().newInstance().getClasses(syntag))
			.contains("axis+horizontal");
		
		assertThat(termino)
			.containsVariation("n: horizontal-axis", VariationType.MORPHOLOGICAL, "an: horizontal axis");
	}
	
	@Test
	public void testTermHydroelectric() {
		Term term = termino.getTerms().get("a: hydroelectric");
		
		assertThat(term)
			.isCompound()
			.hasCompoundType(CompoundType.NEOCLASSICAL)
			.hasCompositionSubstrings("hydro", "electric")
			.hasCompositionLemmas("water", "electric")
			;
	}

	@Test
	public void weNeoclassicalCompounds() {
		List<Word> neoclassicals = termino.getWords().values().stream()
			.filter(Word::isCompound)
			.filter(w -> w.getCompoundType() == CompoundType.NEOCLASSICAL).collect(Collectors.toList());
		
		assertThat(neoclassicals)
			.isNotEmpty()
			.extracting("lemma", "neoclassicalAffix.lemma")
			.hasSize(696)
			.contains(tuple("hydroelectric", "water"))
			;
	}

	
	@Test
	public void testTermHighSpeed() {
		Term term = termino.getTerms().get("a: high-speed");
		assertThat(term)
			.hasFrequency(6);
		
		assertThat(termino)
			.hasNVariationsOfType(term, 4, VariationType.MORPHOLOGICAL)
			.hasNVariationsOfType(term, 0, VariationType.SYNTAGMATIC)
			.hasNVariationsOfType(term, 0, VariationType.SEMANTIC)
			.getVariations(term)
			.extracting(TermSuiteExtractors.RELATION_TOGKEY_RULE_TOFREQ)
			.contains(
					tuple("aan: high revolving speed", "M-I-AN-N|A|R", 1),
					tuple("aan: high rotational speed", "M-I-AN-N|A|R", 3),
					tuple("an: high speed", "M-SD-(N|A)N", 7),
					tuple("ann: high wind speed", "M-I-AN-N|A|R", 6)
				);
	}

	@Test
	public void testWindTurbine() {
		assertThat(termino.getTerms().get("nn: wind turbine"))
				.hasFrequency(1852)
				.hasGroupingKey("nn: wind turbine");
	}

	@Test
	public void testTop10ByRank() {
		assertThat(termsByProperty(termino, TermProperty.RANK, false).subList(0, 10))
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
		assertThat(termino)
//			.hasNVariationsOfType(1266, VariationType.MORPHOLOGICAL)
			.asTermVariationsHavingRule("M-S-NN")
			.extracting("from.groupingKey", "to.groupingKey")
			.contains(
				   tuple("n: baseline", "nn: base line"), 
				   tuple("n: groundwater", "nn: ground water"), 
				   tuple("n: spreadsheet", "nn: spread sheet"), 
				   tuple("n: gearbox", "nn: gear box")
			)
			.hasSize(193)
			;
	}

		   
	
	@Test
	public void testSyntacticalVariations() {
		assertThat(termino)
			.containsVariation("nn: wind turbine", VariationType.SYNTAGMATIC, "nnn: wind regime turbine", RelationProperty.VARIATION_RULE, "S-I-NN-(N|A)")
			.containsVariation("an: low frequency", VariationType.SYNTAGMATIC, "aan: low audible frequency", RelationProperty.VARIATION_RULE, "S-I-AN-A")
			.containsVariation("nn: wind generator", VariationType.SYNTAGMATIC, "nnn: wind turbine generator", RelationProperty.VARIATION_RULE, "S-I-NN-(N|A)")
			;
	}

	@Test
	public void testSyntacticalVariationsWithPrefixes() {
		assertThat(termino)
		.asTermVariationsHavingRule("AN-prefAN")
		.extracting("from.groupingKey", "to.groupingKey")
		.contains(
				tuple("an: national regulation", "an: international regulation"),
				tuple("an: finite number", "an: infinite number"),
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
		.hasSize(33)
		;
		
	}


	@Test
	public void testSyntacticalVariationsWithDerivates() {
		assertThat(termino)
			.asTermVariationsHavingRule("S-R2D-NN1")
			.extracting("from.groupingKey", "to.groupingKey")
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
		assertThat(termino)
			.containsRelation("n: postconstruction", RelationType.IS_PREFIX_OF, "n: construction")
			.containsRelation("n: microgeneration", RelationType.IS_PREFIX_OF, "n: generation")
			.containsRelation("a: subtransient", RelationType.IS_PREFIX_OF, "a: transient")
			.containsRelation("n: incompetence", RelationType.IS_PREFIX_OF, "n: competence")
			.containsRelation("a: subacoustic", RelationType.IS_PREFIX_OF, "a: acoustic")
			;
	}
	
	@Test
	public void testDerivations() {
		assertThat(termino)
			.containsRelation("n: photograph", RelationType.DERIVES_INTO, "a: photographic")
			.containsRelation("n: ethic", RelationType.DERIVES_INTO, "a: ethical")
			.containsRelation("n: institution", RelationType.DERIVES_INTO, "a: institutional")
			.containsRelation("n: industry", RelationType.DERIVES_INTO, "a: industrial")
			;
	}


}
