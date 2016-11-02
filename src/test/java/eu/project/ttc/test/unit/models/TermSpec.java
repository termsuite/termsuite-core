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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableList;

import eu.project.ttc.models.RelationType;
import eu.project.ttc.models.Term;
import eu.project.ttc.models.TermIndex;
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

	
	private TermIndex termIndex;
	
	@Before
	public void initContexts() {
		termIndex = Fixtures.termIndexWithOccurrences();
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
	public void testAddTermVariation() {
		assertThat(termIndex.getOutboundRelations(this.term5)).hasSize(0);
		assertThat(termIndex.getInboundTermRelations(this.term5)).hasSize(0);
		assertThat(termIndex.getOutboundRelations(this.term3)).hasSize(0);
		assertThat(termIndex.getInboundTermRelations(this.term3)).hasSize(0);
		assertThat(termIndex.getOutboundRelations(this.term4)).hasSize(0);
		assertThat(termIndex.getInboundTermRelations(this.term4)).hasSize(0);
		
		termIndex.addRelation(term5, term3, RelationType.SYNTACTICAL, "Tata");
		assertThat(termIndex.getOutboundRelations(this.term5)).hasSize(1);
		assertThat(termIndex.getInboundTermRelations(this.term5)).hasSize(0);
		assertThat(termIndex.getOutboundRelations(this.term3)).hasSize(0);
		assertThat(termIndex.getInboundTermRelations(this.term3)).hasSize(1);
		assertThat(termIndex.getInboundTermRelations(this.term3)).extracting("info").containsExactly("Tata");
		
		termIndex.addRelation(term5, term4, RelationType.SYNTACTICAL, "Tata");
		assertThat(termIndex.getOutboundRelations(this.term5)).hasSize(2);
		assertThat(termIndex.getInboundTermRelations(this.term5)).hasSize(0);
		assertThat(termIndex.getOutboundRelations(this.term3)).hasSize(0);
		assertThat(termIndex.getInboundTermRelations(this.term3)).hasSize(1);
		assertThat(termIndex.getOutboundRelations(this.term4)).hasSize(0);
		assertThat(termIndex.getInboundTermRelations(this.term4)).hasSize(1);
		assertThat(termIndex.getOutboundRelations(this.term5)).extracting("info").containsExactly("Tata","Tata");
		
		termIndex.addRelation(term5, term3, RelationType.SYNTACTICAL, "Tata");
		assertThat(termIndex.getOutboundRelations(this.term5)).hasSize(2);
		assertThat(termIndex.getInboundTermRelations(this.term5)).hasSize(0);
		assertThat(termIndex.getOutboundRelations(this.term3)).hasSize(0);
		assertThat(termIndex.getInboundTermRelations(this.term3)).hasSize(1);
		assertThat(termIndex.getOutboundRelations(this.term4)).hasSize(0);
		assertThat(termIndex.getInboundTermRelations(this.term4)).hasSize(1);
		assertThat(termIndex.getOutboundRelations(this.term5)).extracting("info").containsExactly("Tata","Tata");
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
