/*******************************************************************************
 * Copyright 2015 - CNRS (Centre National de Recherche Scientifique)
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
package org.ttc.project.test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.Assert.fail;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.ttc.project.Fixtures;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import eu.project.ttc.models.OccurrenceType;
import eu.project.ttc.models.Term;
import eu.project.ttc.models.TermClass;
import eu.project.ttc.models.TermIndex;
import eu.project.ttc.models.VariationType;
import eu.project.ttc.models.index.TermIndexes;
import eu.project.ttc.models.index.TermValueProvider;
import eu.project.ttc.models.index.TermValueProviders;

public class TermSpec {

	private Term term1;
	private Term term2;
	private Term term3;
	private Term term4;
	private Term term5;

	@Before
	public void setTerms() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		this.term1 = Fixtures.term1();
		this.term2 = Fixtures.term2();
		this.term3 = Fixtures.term3();
		this.term4 = Fixtures.term4();
		this.term5 = Fixtures.term5();
		
	}

	private Term termWithContext1;
	private Term termWithContext2;
	private Term termWithContext3;
	
	@Before
	public void initContexts() {
		TermIndex termIndex = Fixtures.termIndexWithOccurrences();
		termIndex.createOccurrenceIndex();
		termWithContext1 = termIndex.getTermByGroupingKey("n: énergie");
		termWithContext2 = termIndex.getTermByGroupingKey("a: éolien");
		termWithContext3 = termIndex.getTermByGroupingKey("n: accès");
		
	}
	
	private void initTermClasses() {
		TermClass termClass1 = new TermClass(termWithContext1, ImmutableSet.of(termWithContext1));
		TermClass termClass2 = new TermClass(termWithContext2, ImmutableSet.of(termWithContext2, termWithContext3));
		termWithContext1.setTermClass(termClass1);
		termWithContext2.setTermClass(termClass2);
		termWithContext3.setTermClass(termClass2);
	}
	
	@Test
	public void testGetLemmaStemKeys() {
		TermValueProvider provider = TermValueProviders.get(TermIndexes.WORD_COUPLE_LEMMA_STEM);
		Assert.assertEquals(
				ImmutableList.of("energie+eol"),
				provider.getClasses(term1));
		Assert.assertEquals(
				ImmutableList.of(),
				provider.getClasses(term2));
		Assert.assertEquals(
				ImmutableList.of("acces+radioelectriq", "acces+recouvr", "radioelectrique+recouvr"), 
				provider.getClasses(term3));
	}

	@Test
	public void computeContextVectorScope1() {
		termWithContext1.computeContextVector(OccurrenceType.SINGLE_WORD, 1, 1, false);
		termWithContext2.computeContextVector(OccurrenceType.SINGLE_WORD, 1, 1, false);
		termWithContext3.computeContextVector(OccurrenceType.SINGLE_WORD, 1, 1, false);
		
		// T1 T2 T3 T1 T3 T3 T1

		assertThat(termWithContext1.getContextVector().getEntries())
			.hasSize(2)
			.extracting("coTerm.groupingKey", "nbCooccs", "assocRate")
			.contains(tuple("a: éolien", 1, 0d), tuple("n: accès", 3, 0d));

		assertThat(termWithContext2.getContextVector().getEntries())
			.hasSize(2)
			.extracting("coTerm.groupingKey", "nbCooccs", "assocRate")
			.contains(tuple("n: énergie", 1, 0d), tuple("n: accès", 1, 0d));

		assertThat(termWithContext3.getContextVector().getEntries())
			.hasSize(2)
			.extracting("coTerm.groupingKey", "nbCooccs", "assocRate")
			.contains(tuple("n: énergie", 3, 0d), tuple("a: éolien", 1, 0d));
	}

	@Test
	public void computeContextVectorScope3() {
		termWithContext1.computeContextVector(OccurrenceType.SINGLE_WORD, 3, 1, false);
		termWithContext2.computeContextVector(OccurrenceType.SINGLE_WORD, 3, 1, false);
		termWithContext3.computeContextVector(OccurrenceType.SINGLE_WORD, 3, 1, false);
		
		// T1 T2 T3 T1 T3 T3 T1

		assertThat(termWithContext1.getContextVector().getEntries())
			.hasSize(2)
			.extracting("coTerm.groupingKey", "nbCooccs", "assocRate")
			.contains(tuple("a: éolien", 2, 0d), tuple("n: accès", 6, 0d));
	
		assertThat(termWithContext2.getContextVector().getEntries())
			.hasSize(2)
			.extracting("coTerm.groupingKey", "nbCooccs", "assocRate")
			.contains(tuple("n: énergie", 2, 0d), tuple("n: accès", 2, 0d));
	
		assertThat(termWithContext3.getContextVector().getEntries())
			.hasSize(2)
			.extracting("coTerm.groupingKey", "nbCooccs", "assocRate")
			.contains(tuple("n: énergie", 6, 0d), tuple("a: éolien", 2, 0d));
	}


	@Test
	public void computeContextVectorWithTermClassesRaiseErrorIfNoTermClass() {
		try {
			termWithContext1.computeContextVector(OccurrenceType.SINGLE_WORD, 3, 1, true);
			fail("should raise error");
		} catch(IllegalStateException e) {
			// ok
		} catch(Exception e) {
			fail("Unexpected exception");
		}
		initTermClasses();
		// should not raise error
		termWithContext1.computeContextVector(OccurrenceType.SINGLE_WORD, 3, 1, true);
	}

	

		
	@Test
	public void computeContextVectorWithTermClasses() {
		initTermClasses();
		termWithContext1.computeContextVector(OccurrenceType.SINGLE_WORD, 3, 1, true);
		termWithContext2.computeContextVector(OccurrenceType.SINGLE_WORD, 3, 1, true);
		termWithContext3.computeContextVector(OccurrenceType.SINGLE_WORD, 3, 1, true);

		assertThat(termWithContext1.getContextVector().getEntries())
			.hasSize(1)
			.extracting("coTerm.groupingKey", "nbCooccs", "assocRate")
			.contains(tuple("a: éolien", 8, 0d));
	
		assertThat(termWithContext2.getContextVector().getEntries())
			.hasSize(1)
			.extracting("coTerm.groupingKey", "nbCooccs", "assocRate")
			.contains(tuple("n: énergie", 2, 0d));
	
		assertThat(termWithContext3.getContextVector().getEntries())
			.hasSize(1)
			.extracting("coTerm.groupingKey", "nbCooccs", "assocRate")
			.contains(tuple("n: énergie", 6, 0d));
	}

	@Test
	public void testAddTermVariation() {
		assertThat(this.term5.getVariations()).hasSize(0);
		assertThat(this.term5.getBases()).hasSize(0);
		assertThat(this.term3.getVariations()).hasSize(0);
		assertThat(this.term3.getBases()).hasSize(0);
		assertThat(this.term4.getVariations()).hasSize(0);
		assertThat(this.term4.getBases()).hasSize(0);
		
		term5.addTermVariation(term3, VariationType.SYNTACTICAL, "Tata");
		assertThat(this.term5.getVariations()).hasSize(1);
		assertThat(this.term5.getBases()).hasSize(0);
		assertThat(this.term3.getVariations()).hasSize(0);
		assertThat(this.term3.getBases()).hasSize(1);
		assertThat(this.term3.getBases()).extracting("info").containsExactly("Tata");
		
		term5.addTermVariation(term4, VariationType.SYNTACTICAL, "Tata");
		assertThat(this.term5.getVariations()).hasSize(2);
		assertThat(this.term5.getBases()).hasSize(0);
		assertThat(this.term3.getVariations()).hasSize(0);
		assertThat(this.term3.getBases()).hasSize(1);
		assertThat(this.term4.getVariations()).hasSize(0);
		assertThat(this.term4.getBases()).hasSize(1);
		assertThat(this.term5.getVariations()).extracting("info").containsExactly("Tata","Tata");
		
		term5.addTermVariation(term3, VariationType.SYNTACTICAL, "Tata");
		assertThat(this.term5.getVariations()).hasSize(2);
		assertThat(this.term5.getBases()).hasSize(0);
		assertThat(this.term3.getVariations()).hasSize(0);
		assertThat(this.term3.getBases()).hasSize(1);
		assertThat(this.term4.getVariations()).hasSize(0);
		assertThat(this.term4.getBases()).hasSize(1);
		assertThat(this.term5.getVariations()).extracting("info").containsExactly("Tata","Tata");
		
	}
	
	@Test
	public void getVariationPaths() {
		assertThat(term5.getVariationPaths(0)).isEmpty();

		term5.addTermVariation(term3, VariationType.SYNTACTICAL, "Tata");
		assertThat(term5.getVariationPaths(0)).isEmpty();
		assertThat(term5.getVariationPaths(1)).hasSize(1).extracting("variant").contains(term3);
		assertThat(term5.getVariationPaths(10)).hasSize(1).extracting("variant").contains(term3);
		
		term3.addTermVariation(term4, VariationType.SYNTACTICAL, "Toto");
		assertThat(term5.getVariationPaths(0)).isEmpty();
		assertThat(term5.getVariationPaths(1)).hasSize(1).extracting("variant").contains(term3);
		assertThat(term5.getVariationPaths(2)).hasSize(2).extracting("variant").contains(term3, term4);
		assertThat(term5.getVariationPaths(10)).hasSize(2).extracting("variant").contains(term3, term4);
		
		term4.addTermVariation(term5, VariationType.SYNTACTICAL, "Toto");
		assertThat(term5.getVariationPaths(0)).isEmpty();
		assertThat(term5.getVariationPaths(1)).hasSize(1).extracting("variant").contains(term3);
		assertThat(term5.getVariationPaths(2)).hasSize(2).extracting("variant").contains(term3, term4);
		assertThat(term5.getVariationPaths(3)).hasSize(3).extracting("variant").contains(term3, term4, term5);
		// handles cycles
		assertThat(term5.getVariationPaths(10)).hasSize(3).extracting("variant").contains(term3, term4, term5);
	}
	
	@Test
	public void testGetLemmaKeys() {
		TermValueProvider provider = TermValueProviders.get(TermIndexes.WORD_LEMMA);
		Assert.assertEquals(
				ImmutableList.of("énergie", "éolien"),
				provider.getClasses(term1));
		Assert.assertEquals(
				ImmutableList.of("radio","électrique"),
				provider.getClasses(term2));
		Assert.assertEquals(
				ImmutableList.of("accès", "radio", "électrique", "de", "recouvrement"), 
				provider.getClasses(term3));
	}
}
