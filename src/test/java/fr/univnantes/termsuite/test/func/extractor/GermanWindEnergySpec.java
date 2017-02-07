
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

package fr.univnantes.termsuite.test.func.extractor;

import static fr.univnantes.termsuite.test.asserts.TermSuiteAssertions.assertThat;
import static fr.univnantes.termsuite.test.func.FunctionalTests.termsByProperty;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.assertj.core.util.Lists;
import org.junit.Test;

import fr.univnantes.termsuite.engines.gatherer.VariationType;
import fr.univnantes.termsuite.model.CompoundType;
import fr.univnantes.termsuite.model.Lang;
import fr.univnantes.termsuite.model.Relation;
import fr.univnantes.termsuite.model.RelationProperty;
import fr.univnantes.termsuite.model.RelationType;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.TermProperty;
import fr.univnantes.termsuite.model.Word;

public class GermanWindEnergySpec extends WindEnergySpec {

	@Override
	protected Lang getLang() {
		return Lang.DE;
	}

	@Override
	protected List<String> getSyntacticMatchingRules() {
		return Lists.newArrayList(
				"S-I-NN-NC",
				 "M-PI-NN-A",
				 "S-I2-NPN-A",
				 "S-Eg-NPN-A",
				 "S-I2-NPN-NC",
				 "S-Eg-AN-R",
				 "M-AN",
				 "S-Ed-AN-CN",
				 "S-Eg-NN-A",
				 "S-PR2D-NPN",
				 "M-I-EN-A|N",
				 "M-P-NN",
				 "M-PI-NN-P",
				 "S-Eg-AN-AC",
				 "M-PI-NN-AE",
				 "S-R2-NPN",
				 "S-Ed-NN-PN",
				 "S-Ed-NPN-PN",
				 "M-PI-NN-PA ",
				 "S-I-AN-CA",
				 "M-I-(N|A)N-N|A",
				 "S-Eg-NN-NC",
				 "S-PI-AN-V",
				 "M-S-(N|A)N",
				 "S-Ed-NPN-CN",
				 "S-Ed-NN-CN",
				 "S-R2-NPAN",
				 "M-S-(A|N)NN",
				 "S-I-AN-NC",
				 "S-I-AN-A",
				 "S-Eg-AN-A",
				 "S-I-NN-CN"
			);
	}


	@Override
	protected List<String> getSyntacticNotMatchingRules() {
		return Lists.newArrayList(
				"S-I-NN-A",
				"S-I1-NPN-CN",
				"S-Eg-NPN-NC",
				"M-PI1-NAN2-N2-P",
				"M-I-NN-CN",
				"M-I2-NCN-N",
				"M-PI-NNCN-N-P");
	}


	@Test
	public void testTop10ByFreq() {
		assertThat(termsByProperty(termino, TermProperty.FREQUENCY, true).subList(0, 10))
			.hasSize(10)
			.extracting("groupingKey")
			.containsExactly(
					"n: jahr",
					"r: nur",
					"n: energie",
					"a: hoch",
					"r: so",
					"n: windenergie",
					"a: groß",
					"n: windenergieanlage",
					"n: abbildung",
					"n: m");
	}
	
	@Test
	public void testTop10ByWR() {
		assertThat(termsByProperty(termino, TermProperty.SPECIFICITY, true).subList(0, 10))
			.hasSize(10)
			.extracting("groupingKey")
			.containsExactly(
					"r: nur",
					"r: so",
					"n: m",
					"n: s",
					"a: erneuerbaren",
					"a: windenergie",
					"a: i",
					"n: eeg",
					"n: t",
					"n: windenergienutzung")
			;
	}

	@Test
	public void testTop10ByRank() {
		assertThat(termsByProperty(termino, TermProperty.RANK, false).subList(0, 10))
			.hasSize(10)
			.extracting("groupingKey")
			.containsExactly(
					"r: nur",
					"r: so",
					"n: m",
					"n: s",
					"a: erneuerbaren",
					"a: windenergie",
					"a: i",
					"n: eeg",
					"n: t",
					"n: windenergienutzung"
			)
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
			.contains(tuple("elektromotor", "elektro"))
			.contains(tuple("automobil", "auto"))
			.doesNotContain(tuple("einkommen", "ein"))
			.doesNotContain(tuple("stromsparfond", "par"))
			.hasSize(594);
	}

	
	@Test
	public void testTermWindenergie() {
		assertThat(termino.getTerms().get("n: windenergie"))
				.hasFrequency(588)
				.hasGroupingKey("n: windenergie")
				.isCompound()
				.hasCompoundType(CompoundType.NATIVE)
				.hasCompositionSubstrings("wind", "energie")
				.hasCompositionLemmas("wind", "energie")
				;
	}

	@Test
	public void testTermSelbstverständlich() {
		assertThat(termino.getTerms().get("a: selbstverständlich"))
		.hasFrequency(3)
		.hasGroupingKey("a: selbstverständlich")
		.isCompound()
		.hasCompoundType(CompoundType.NATIVE)
		.hasCompositionSubstrings("selbst", "verständlich")
		.hasCompositionLemmas("selbst", "verständlich")
		;
		
	}

	@Test
	public void testTermHochwert() {
		Term term = termino.getTerms().get("a: hochwert");
		
		assertThat(term)
			.isCompound()
			.hasCompoundType(CompoundType.NATIVE)
			.hasCompositionSubstrings("hoch", "wert");
		
	}

		
	
	@Test
	public void testTermElektromagnetisch() {
		Term term = termino.getTerms().get("a: elektromagnetisch");
		
		assertThat(term)
			.isCompound()
			.hasCompoundType(CompoundType.NEOCLASSICAL)
			.hasCompositionSubstrings("elektro", "magnetisch");
	}

	
	@Test
	public void testTermWasserkraftwerke() {
		Term term = termino.getTerms().get("a: wasserkraftwerke");
		
		assertThat(term)
			.isCompound()
			.hasCompoundType(CompoundType.NATIVE)
			.hasCompositionSubstrings("wasserkraft", "werke")
			.hasCompositionLemmas("wasserkraft", "werk")
			;
	}


	@Test
	public void testTermHydrothermale() {
		Term term = termino.getTerms().get("a: hydrothermale");
		
		assertThat(term)
			.isCompound()
			.hasCompoundType(CompoundType.NEOCLASSICAL)
			.hasCompositionSubstrings("hydro", "ther", "male")
			.hasCompositionLemmas("wasser", "th", "mal")
			;
	}

	@Test
	public void testMSNorANVariations() {
		assertThat(termino)
//			.hasNVariationsOfType(1266, VariationType.MORPHOLOGICAL)
			.asTermVariationsHavingRule("M-S-(N|A)N")
			.extracting("from.groupingKey", "to.groupingKey")
			.contains(
				   tuple("n: windatlas", "an: wind atlas"),
				   tuple("n: gesamtstrom", "an: gesamt strom"),
				   tuple("n: megawatt-anlage", "nn: megawatt anlage"),
				   tuple("n: zukunftsmarkt", "nn: zukunft markt")
			)
			;
		
		@SuppressWarnings("unchecked")
		List<Relation> msnanVars = termino.getRelations().stream()
				.filter(tv -> tv.isPropertySet(RelationProperty.VARIATION_RULES))
				.filter(tv -> ((Set<String>)tv.get(RelationProperty.VARIATION_RULES)).contains("M-S-(N|A)N"))
				.collect(Collectors.toList());
		// TODO investigate why the size varies
		assertTrue("Expected size between 89 and 95, but got: " + msnanVars.size(), msnanVars.size() <= 95 && msnanVars.size() >= 89
				);
	}

		   
	
	@Test
	public void testSyntacticalVariations() {
		assertThat(termino)
			.containsVariationWithRuleName("an: staatlich umweltamt", VariationType.SYNTAGMATIC, "aan: windenergie staatlich umweltamt", "S-Eg-AN-A")
			.containsVariationWithRuleName("acan: topographisch und meteorologisch verhältnis", VariationType.SYNTAGMATIC, "an: topographisch verhältnis", "S-I-AN-CA")
			;
	}

	@Test
	public void testSyntacticalVariationsWithPrefixes() {
		// no prefixe yet in german
	}


	@Test
	public void testSyntacticalVariationsWithDerivates() {
		// no derivate yet in german
	}

	@Test
	public void testPrefixes() {
		assertThat(termino)
			.asTermVariations(RelationType.IS_PREFIX_OF)
			.hasSize(962)
			;
	}
	
	@Test
	public void testDerivations() {
		assertThat(termino)
			.asTermVariations(RelationType.DERIVES_INTO)
			.hasSize(0);
	}

	@Override
	protected List<String> getRulesNotTested() {
		return Lists.newArrayList();
	}
}
