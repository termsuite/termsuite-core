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
package eu.project.ttc.test.unit.models;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableList;

import eu.project.ttc.models.OccurrenceType;
import eu.project.ttc.models.Term;
import eu.project.ttc.models.TermIndex;
import eu.project.ttc.models.VariationType;
import eu.project.ttc.models.index.TermIndexes;
import eu.project.ttc.models.index.TermValueProvider;
import eu.project.ttc.models.index.TermValueProviders;
import eu.project.ttc.test.unit.Fixtures;

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
	
	private TermIndex termIndex;
	
	@Before
	public void initContexts() {
		termIndex = Fixtures.termIndexWithOccurrences();
		termIndex.createOccurrenceIndex();
		termWithContext1 = termIndex.getTermByGroupingKey("n: énergie");
		termWithContext2 = termIndex.getTermByGroupingKey("a: éolien");
		termWithContext3 = termIndex.getTermByGroupingKey("n: accès");
		
	}
	
	@Test
	public void testGetLemmaStemKeys() {
		TermValueProvider provider = TermValueProviders.get(TermIndexes.WORD_COUPLE_LEMMA_STEM);
		Assert.assertEquals(
				ImmutableList.of("energie+eol"),
				provider.getClasses(termIndex, term1));
		Assert.assertEquals(
				ImmutableList.of(),
				provider.getClasses(termIndex, term2));
		Assert.assertEquals(
				ImmutableList.of("acces+radioelectriq", "acces+recouvr", "radioelectrique+recouvr"), 
				provider.getClasses(termIndex, term3));
	}

	@Test
	public void computeContextVectorScope1() {
		termWithContext1.computeContextVector(OccurrenceType.SINGLE_WORD, 1, 1);
		termWithContext2.computeContextVector(OccurrenceType.SINGLE_WORD, 1, 1);
		termWithContext3.computeContextVector(OccurrenceType.SINGLE_WORD, 1, 1);
		
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
		termWithContext1.computeContextVector(OccurrenceType.SINGLE_WORD, 3, 1);
		termWithContext2.computeContextVector(OccurrenceType.SINGLE_WORD, 3, 1);
		termWithContext3.computeContextVector(OccurrenceType.SINGLE_WORD, 3, 1);
		
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
	public void testAddTermVariation() {
		assertThat(termIndex.getOutboundTermVariations(this.term5)).hasSize(0);
		assertThat(termIndex.getInboundTermVariations(this.term5)).hasSize(0);
		assertThat(termIndex.getOutboundTermVariations(this.term3)).hasSize(0);
		assertThat(termIndex.getInboundTermVariations(this.term3)).hasSize(0);
		assertThat(termIndex.getOutboundTermVariations(this.term4)).hasSize(0);
		assertThat(termIndex.getInboundTermVariations(this.term4)).hasSize(0);
		
		termIndex.addTermVariation(term5, term3, VariationType.SYNTACTICAL, "Tata");
		assertThat(termIndex.getOutboundTermVariations(this.term5)).hasSize(1);
		assertThat(termIndex.getInboundTermVariations(this.term5)).hasSize(0);
		assertThat(termIndex.getOutboundTermVariations(this.term3)).hasSize(0);
		assertThat(termIndex.getInboundTermVariations(this.term3)).hasSize(1);
		assertThat(termIndex.getInboundTermVariations(this.term3)).extracting("info").containsExactly("Tata");
		
		termIndex.addTermVariation(term5, term4, VariationType.SYNTACTICAL, "Tata");
		assertThat(termIndex.getOutboundTermVariations(this.term5)).hasSize(2);
		assertThat(termIndex.getInboundTermVariations(this.term5)).hasSize(0);
		assertThat(termIndex.getOutboundTermVariations(this.term3)).hasSize(0);
		assertThat(termIndex.getInboundTermVariations(this.term3)).hasSize(1);
		assertThat(termIndex.getOutboundTermVariations(this.term4)).hasSize(0);
		assertThat(termIndex.getInboundTermVariations(this.term4)).hasSize(1);
		assertThat(termIndex.getOutboundTermVariations(this.term5)).extracting("info").containsExactly("Tata","Tata");
		
		termIndex.addTermVariation(term5, term3, VariationType.SYNTACTICAL, "Tata");
		assertThat(termIndex.getOutboundTermVariations(this.term5)).hasSize(2);
		assertThat(termIndex.getInboundTermVariations(this.term5)).hasSize(0);
		assertThat(termIndex.getOutboundTermVariations(this.term3)).hasSize(0);
		assertThat(termIndex.getInboundTermVariations(this.term3)).hasSize(1);
		assertThat(termIndex.getOutboundTermVariations(this.term4)).hasSize(0);
		assertThat(termIndex.getInboundTermVariations(this.term4)).hasSize(1);
		assertThat(termIndex.getOutboundTermVariations(this.term5)).extracting("info").containsExactly("Tata","Tata");
	}
		
	@Test
	public void testGetLemmaKeys() {
		TermValueProvider provider = TermValueProviders.get(TermIndexes.WORD_LEMMA);
		
		assertThat(provider.getClasses(termIndex, term1))
			.hasSize(2)
			.contains("énergie", "éolien");
		
		assertThat(provider.getClasses(termIndex, term2))
			.hasSize(1)
			.contains("radioélectrique");
	
		assertThat(provider.getClasses(termIndex, term3))
			.hasSize(4)
			.contains("accès", "radioélectrique", "de", "recouvrement");
	}

}
