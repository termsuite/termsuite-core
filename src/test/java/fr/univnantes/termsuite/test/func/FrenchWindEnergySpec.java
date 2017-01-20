
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

import java.util.List;
import java.util.stream.Collectors;

import org.assertj.core.util.Lists;
import org.junit.Test;

import fr.univnantes.termsuite.engines.gatherer.VariationType;
import fr.univnantes.termsuite.model.CompoundType;
import fr.univnantes.termsuite.model.Lang;
import fr.univnantes.termsuite.model.RelationProperty;
import fr.univnantes.termsuite.model.RelationType;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.TermProperty;
import fr.univnantes.termsuite.model.Word;
import fr.univnantes.termsuite.test.unit.TermSuiteExtractors;

public class FrenchWindEnergySpec extends WindEnergySpec {
	
	@Override
	protected Lang getLang() {
		return Lang.FR;
	}
	
	@Override
	protected List<String> getSyntacticMatchingRules() {
		return Lists.newArrayList(
				"S-NA",
				"NA-NprefA",
				"S-Ed-NA-A",
				"S-Ed-NA-AA",
				"S-Ed-NA-PN",
				"S-Ed-NA-PAN",
				"S-Ed-NA-PACAN",
				"S-Ed-NA-PNA",
				"S-Ed-NA-CA",
				"S-Ed-NA-,ACA",
				"S-I-NA-A",
				"S-I-N(N|A)-PN",
				"S-I-NA-R",
				"S-I-NA-V",
				"S-I-NA-AC",
				"S-I-NA-A,AC",
				"S-I-NA-PNA",
				"S-I1-NPN-A",
				"S-Ed-NPN-A",
				"S-Ed-NPN-PN",
				"S-Ed-NPN-PAN",
				"S-Ed-NPN-PNA",
				"S-Ed-NPN-PACAN",
				"S-Ed-NPN-AA",
				"S-Ed-NPN-CPN",
				"S-Ed-NPN-,PNCPN",
				"S-Eg-NA-NP",
				"S-Eg-NPN-NP",
				"S-R2-NPN",
				"S-R2I-NPN-P",
				"S-R2D-NPN",
				"S-P-NAPN-A",
				"S-P-NAA-A",
				"M-S-NN",
				"M-PI-EN-P",
				"M-I-NA-CE"
				);
	}
	
	@Override
	protected List<String> getSyntacticNotMatchingRules() {
		return Lists.newArrayList(
				"M-I-NA-EC",
				"S-IEg-NPN-PN,-CPN",
				"S-I-NA-RV",
				"S-R2I2-NPN-PNP",
				"S-I2-NPN-PN,PNC",
				"S-PID-NA-P",
				"S-IEg-NA-A,-CA",
				"S-PID-NAA-P",
				"M-I2-NA");
	}
	
	@Override
	protected List<String> getRulesNotTested() {
		return Lists.newArrayList(
				"NA-NsynA",
				"NA-synNA",
				"NPN-NPsynN",
				"NPN-synNPN"
				);
	}
	
	@Test
	public void weNeoclassicalCompounds() {
		List<Word> neoclassicals = termino.getWords().stream()
			.filter(Word::isCompound)
			.filter(w -> w.getCompoundType() == CompoundType.NEOCLASSICAL).collect(Collectors.toList());
		
		assertThat(neoclassicals)
			.isNotEmpty()
			.extracting("lemma", "neoclassicalAffix.lemma")
			.contains(tuple("hydroélectrique", "eau"))
			.contains(tuple("antinucléaire", "anti"))
			.contains(tuple("aéroélastique", "air"))
			.hasSize(363);
	}

	
	@Test
	public void testTop10ByFreq() {
		assertThat(termsByProperty(termino, TermProperty.FREQUENCY, true).subList(0, 10))
			.hasSize(10)
			.extracting("groupingKey")
			.containsExactly(
					"n: puissance", "a: éolien", "n: système", "n: énergie", "n: vitesse", 
					"n: vent", "n: réseau", "n: éolienne", "n: machine", "n: figure");
	}
	
	@Test
	public void testTop10ByWR() {
		assertThat(termsByProperty(termino, TermProperty.SPECIFICITY, true).subList(0, 10))
			.hasSize(10)
			.extracting("groupingKey")
			.containsExactly(
					"n: convertisseur", 
					"n: pale", 
					"n: rotor", 
					"n: éolienne", 
					"n: réglage", 
					"npn: vitesse du vent", 
					"n: kw", 
					"na: turbine éolien", 
					"n: coefficient", 
					"n: mw"
				)
			;
	}
	
	@Test
	public void testCheckTerms() {
		assertThat(termino.getTerms().values())
			.extracting("groupingKey")
			.contains(
					"n: tourbillon"
					)
			.doesNotContain("npn: givrage de pale")
			;
	}

	@Test
	public void testTermHydroelectrique() {
		Term term = termino.getTerms().get("a: hydroélectrique");
		
		assertThat(term)
			.isCompound()
			.hasCompoundType(CompoundType.NEOCLASSICAL)
			.hasCompositionSubstrings("hydro", "électrique")
			.hasCompositionLemmas("eau", "électrique")
			;
	}


	@Test
	public void testTermElectromagnetique() {
		Term term = termino.getTerms().get("a: électromagnétique");
		
		assertThat(term)
			.isCompound()
			.hasCompoundType(CompoundType.NEOCLASSICAL)
			.hasCompositionSubstrings("électro", "magnétique");
	}

	@Test
	public void testTermVitesseDeRotation() {
		Term term = termino.getTerms().get("npn: vitesse de rotation");
		assertThat(term)
			.hasFrequency(311);
		
		assertThat(termino)
			.hasNBases(term, 2)
			.hasNVariationsOfType(term, 22, VariationType.SYNTAGMATIC)
			.getVariations(term)
			.extracting(TermSuiteExtractors.RELATION_TOGKEY_RULE_TOFREQ)
			.contains(
					tuple("napn: vitesse angulaire de rotation", "S-I1-NPN-A", 2),
					tuple("napn: vitesse nominal de rotation", "S-I1-NPN-A", 2),
					tuple("npna: vitesse de rotation correspondant", "S-Ed-NPN-A", 3),
					tuple("npnpna: vitesse de rotation du champ magnétique", "S-Ed-NPN-PNA", 2)
				);
	}

	@Test
	public void testTermEolienne() {
		assertThat(termino.getTerms().get("n: éolienne"))
				.hasFrequency(1147)
				.hasGroupingKey("n: éolienne");
	}

	@Test
	public void testTop10ByRank() {
		assertThat(termsByProperty(termino, TermProperty.RANK, false).subList(0, 10))
			.hasSize(10)
			.extracting("groupingKey")
			.containsExactly(
					"n: convertisseur", 
					"n: pale", 
					"n: rotor", 
					"n: éolienne", 
					"n: réglage", 
					"npn: vitesse du vent", 
					"n: kw", 
					"na: turbine éolien", 
					"n: coefficient", 
					"n: mw"
				)
			;
	}

	@Test
	public void testMINACEVariations() {
		assertThat(termino)
			.asTermVariationsHavingRule("M-I-NA-CE")
			.extracting("from.groupingKey", "to.groupingKey")
			.contains(
				   tuple("na: fonctionnement hypersynchrone", "naca: fonctionnement hyper et hyposynchrone")
			)
			.hasSize(1)
			;

	}

	@Test
	public void testMSNNVariations() {
		assertThat(termino)
			.hasNVariationsOfType(30, VariationType.MORPHOLOGICAL)
			.asTermVariationsHavingRule("M-S-NN")
			.extracting("from.groupingKey", "to.groupingKey")
			.contains(
				   tuple("n: microsystème", "nn: micro système"), 
//				   tuple("n: transistor-diode", "nn: transistor diode"), // terms have been merged
//				   tuple("n: france-allemagne", "nn: france allemagne"), // terms have been merged
				   tuple("n: schéma-bloc", "nn: schéma bloc"),
				   tuple("n: micro-turbines", "nn: micro turbine")
			)
			.hasSize(3)
			;
	}

		   
	
	@Test
	public void testSyntacticalVariations() {
		assertThat(termino)
			.containsVariation("npn: phase du stator", VariationType.DERIVATION, "na: phase statorique", RelationProperty.VARIATION_RULE, "S-R2D-NPN")
			.containsVariation("na: machine asynchrone", VariationType.SYNTAGMATIC, "naa: machine asynchrone auto-excitée", RelationProperty.VARIATION_RULE, "S-Ed-NA-A")
			.containsVariation("na: machine asynchrone", VariationType.SYNTAGMATIC, "napn: machine asynchrone à cage", RelationProperty.VARIATION_RULE, "S-Ed-NA-PN")
			.containsVariation("na: machine asynchrone", VariationType.SYNTAGMATIC, "napna: machine asynchrone à cage autonome", RelationProperty.VARIATION_RULE, "S-Ed-NA-PNA")
			.containsVariation("na: machine asynchrone", VariationType.SYNTAGMATIC, "napan: machine asynchrone à double alimentation",RelationProperty.VARIATION_RULE,  "S-Ed-NA-PAN")
			.containsVariation("na: machine asynchrone", VariationType.SYNTAGMATIC, "naca: machine synchrone ou asynchrone", RelationProperty.VARIATION_RULE, "S-I-NA-AC")
			;
	}

	@Test
	public void testInferedVariations() {
		assertThat(termino)
			.containsVariation("npnpn: résistance du bobinage de stator", VariationType.INFERENCE, "npna: résistance du bobinage statorique")
			.containsVariation("naa: machine synchrone classique", VariationType.INFERENCE, "naa: machine asynchrone classique");
	}

	@Test
	public void testSyntacticalVariationsWithPrefixes() {
		assertThat(termino)
		.asTermVariationsHavingRule("NA-NprefA")
		.extracting("from.groupingKey", "to.groupingKey")
		.contains(
			tuple("na: générateur synchrone", "na: générateur asynchrone"),
			tuple("na: machine synchrone", "na: machine asynchrone"),
			tuple("na: contrôle direct", "na: contrôle indirect"),
			tuple("na: mode direct", "na: mode indirect"),
			tuple("na: aspect esthétique", "na: aspect inesthétique"),
			tuple("na: option nucléaire", "na: option antinucléaire"),
			tuple("na: génératrice synchrone", "na: génératrice asynchrone"),
			tuple("na: mesure précis", "na: mesure imprécis"),
			tuple("na: circulation stationnaire", "na: circulation instationnaire")
		)
		.hasSize(27)
		;
		
	}

	@Test
	public void testSyntacticalVariationsWithDerivatesSR2DNPN() {
		assertThat(termino)
			.asTermVariationsHavingRule("S-R2D-NPN")
			.hasSize(68)
			.extracting("from.groupingKey", "to.groupingKey")
			.contains(
					tuple("npn: production de électricité", "na: production électrique"),
					tuple("npn: étude de environnement", "na: étude environnemental"),
					tuple("npn: génération de électricité", "na: génération électrique")
			)
			;
	}
	@Test
	public void testSyntacticalVariationsWithDerivatesSPIDNAP() {
		assertThat(termino)
			.asTermVariationsHavingRule("S-PID-NA-P")
			.hasSize(0)
			.extracting("from.groupingKey", "to.groupingKey")
			.contains(
//					tuple("npn: givrage de pale", "na: pale givrer"),
//					tuple("npn: profondeur de eau", "na: eau profond")
			)
			;
	}
	

	@Test
	public void testPrefixes() {
		assertThat(termino)
			.containsRelation("a: multipolaire", RelationType.IS_PREFIX_OF, "a: polaire")
			.containsRelation("n: cofinancement", RelationType.IS_PREFIX_OF, "n: financement")
			.containsRelation("a: tripale", RelationType.IS_PREFIX_OF, "n: pale")
			.containsRelation("a: bipale", RelationType.IS_PREFIX_OF, "n: pale")
			.containsRelation("a: asynchrone", RelationType.IS_PREFIX_OF, "a: synchrone")
			.containsRelation("n: déréglementation", RelationType.IS_PREFIX_OF, "n: réglementation")
			;
	}
	
	@Test
	public void testDerivations() {
		assertThat(termino)
			.containsRelation("n: hydroélectricité", RelationType.DERIVES_INTO, "a: hydroélectrique")
			.containsRelation("n: stator", RelationType.DERIVES_INTO, "a: statorique")
			.containsRelation("n: usage", RelationType.DERIVES_INTO, "n: usager")
			.containsRelation("n: support", RelationType.DERIVES_INTO, "n: supportage")
			.containsRelation("n: commerce", RelationType.DERIVES_INTO, "a: commercial")
			;
	}

}
