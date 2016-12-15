
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

package fr.univnantes.termsuite.utils.test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.Assert.fail;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableList;

import fr.univnantes.termsuite.engines.splitter.CompoundUtils;
import fr.univnantes.termsuite.model.Component;
import fr.univnantes.termsuite.model.Word;
import fr.univnantes.termsuite.model.WordBuilder;

public class CompoundUtilsSpec {

	Word w_ab;
	Word w_abcd;
	Word w_abcdef;
	Word w_abcdefgh;
	Component ab, cd, ef, gh;
	
	@Before
	public void setUp() {
		w_abcdefgh = WordBuilder.start()
				.setLemma("abcdefgg")
				.addComponent(0, 2, "ab", "aa")
				.addComponent(2, 4, "cd", "cc")
				.addComponent(4, 6, "ef", "ee")
				.addComponent(6, 8, "gg", "gg")
				.create();
		ab = w_abcdefgh.getComponents().get(0);
		cd = w_abcdefgh.getComponents().get(1);
		ef = w_abcdefgh.getComponents().get(2);
		gh = w_abcdefgh.getComponents().get(3);
		
		w_ab = WordBuilder.start()
				.setLemma("aa")
				.addComponent(0, 2, "aa", "aa")
				.create();
		w_abcd = WordBuilder.start()
				.setLemma("abcc")
				.addComponent(0, 2, "ab", "aa")
				.addComponent(2, 4, "cc", "cc")
				.create();

		w_abcdef = WordBuilder.start()
				.setLemma("abcdee")
				.addComponent(0, 2, "ab", "aa")
				.addComponent(2, 4, "cd", "cc")
				.addComponent(4, 6, "ee", "ee")
				.create();

	}
	
	@Test
	public void testMerge() {
		/* nbComponents = 1*/
		Component m_ab = CompoundUtils.merge(w_abcdefgh, ImmutableList.of(ab));
		assertThat(m_ab.getBegin()).isEqualTo(0);
		assertThat(m_ab.getEnd()).isEqualTo(2);
		assertThat(m_ab.getLemma()).isEqualTo("aa");
		
		Component m_cd = CompoundUtils.merge(w_abcdefgh, ImmutableList.of(cd));
		assertThat(m_cd.getBegin()).isEqualTo(2);
		assertThat(m_cd.getEnd()).isEqualTo(4);
		assertThat(m_cd.getLemma()).isEqualTo("cc");

		Component m_ef = CompoundUtils.merge(w_abcdefgh, ImmutableList.of(ef));
		assertThat(m_ef.getBegin()).isEqualTo(4);
		assertThat(m_ef.getEnd()).isEqualTo(6);
		assertThat(m_ef.getLemma()).isEqualTo("ee");
		
		Component m_gh = CompoundUtils.merge(w_abcdefgh, ImmutableList.of(gh));
		assertThat(m_gh.getBegin()).isEqualTo(6);
		assertThat(m_gh.getEnd()).isEqualTo(8);
		assertThat(m_gh.getLemma()).isEqualTo("gg");

		
		/* nbComponents = 2*/
		Component m_abcd = CompoundUtils.merge(w_abcdefgh, ImmutableList.of(ab, cd));
		assertThat(m_abcd.getBegin()).isEqualTo(0);
		assertThat(m_abcd.getEnd()).isEqualTo(4);
		assertThat(m_abcd.getLemma()).isEqualTo("abcc");
		
		Component m_cdef = CompoundUtils.merge(w_abcdefgh, ImmutableList.of(cd, ef));
		assertThat(m_cdef.getBegin()).isEqualTo(2);
		assertThat(m_cdef.getEnd()).isEqualTo(6);
		assertThat(m_cdef.getLemma()).isEqualTo("cdee");

		Component m_efgh = CompoundUtils.merge(w_abcdefgh, ImmutableList.of(ef, gh));
		assertThat(m_efgh.getBegin()).isEqualTo(4);
		assertThat(m_efgh.getEnd()).isEqualTo(8);
		assertThat(m_efgh.getLemma()).isEqualTo("efgg");

		
		/* nbComponents = 3*/
		Component m_abcdef = CompoundUtils.merge(w_abcdefgh, ImmutableList.of(ab, cd, ef));
		assertThat(m_abcdef.getBegin()).isEqualTo(0);
		assertThat(m_abcdef.getEnd()).isEqualTo(6);
		assertThat(m_abcdef.getLemma()).isEqualTo("abcdee");
		
		Component m_cdefgh = CompoundUtils.merge(w_abcdefgh, ImmutableList.of(cd, ef, gh));
		assertThat(m_cdefgh.getBegin()).isEqualTo(2);
		assertThat(m_cdefgh.getEnd()).isEqualTo(8);
		assertThat(m_cdefgh.getLemma()).isEqualTo("cdefgg");

		
		/* nbComponents = 4*/
		Component m_abcdefgh = CompoundUtils.merge(w_abcdefgh, ImmutableList.of(ab, cd, ef, gh));
		assertThat(m_abcdefgh.getBegin()).isEqualTo(0);
		assertThat(m_abcdefgh.getEnd()).isEqualTo(8);
		assertThat(m_abcdefgh.getLemma()).isEqualTo("abcdefgg");
	}
	
	
	@Test
	public void testHorizontalAxis() {
		Word horizontalAxis = WordBuilder.start()
				.setLemma("horizontal-axis")
				.addComponent(0, 7, "horizon", "horizon")
				.addComponent(7, 10, "tal", "t")
				.addComponent(11, 15, "axis", "axis")
				.create();
		
		assertThat(CompoundUtils.innerComponentPairs(horizontalAxis))
			.extracting("element1.lemma", "element2.lemma")
			.contains(
					tuple("horizon","t"),
					tuple("horizon","axis"),
					tuple("t","axis"),
					tuple("horizont","axis"),
					tuple("horizon","tal-axis")
				)
			.hasSize(5)
			;

	}

	@Test
	public void testMergeWithEmptyList() {
		try {
			CompoundUtils.merge(w_abcdefgh, new ArrayList<Component>());
			fail("Should have thrown Exception");
		} catch(Exception e) {
			  assertThat(e)
			  	.isInstanceOf(IllegalArgumentException.class)
			  	.hasMessage("Cannot merge an empty set of component");
		}
	}
	
	@Test
	public void testMergeWithNonConsecutiveComponents() {
		Word w = WordBuilder.start()
				.setLemma("homme-grenouille")
				.addComponent(0, 5, "homme")
				.addComponent(6, 16, "grenouille")
				.create();

		Component merged = CompoundUtils.merge(w, w.getComponents());
		assertThat(merged.getBegin()).isEqualTo(0);
		assertThat(merged.getEnd()).isEqualTo(16);
		assertThat(merged.getLemma()).isEqualTo("homme-grenouille");

		
	}


	@Test
	public void testMergeWithOverlappingComponents() {
		try {
			CompoundUtils.merge(w_abcdefgh, ImmutableList.of(ab, new Component(1, 3, "titi")));
			fail("Should have thrown Exception");
		} catch(Exception e) {
			  assertThat(e)
			  	.isInstanceOf(IllegalArgumentException.class)
			  	.hasMessage("Cannot merge two components if they overlap. Got [0,2] followed by [1,3].");
		}
	}

	@Test
	public void testMergeWithBadComponentIndexes() {
		/* Case 1, the bad component is the last one */
		try {
			CompoundUtils.merge(w_abcdefgh, ImmutableList.of(ef, new Component(6, 9, "tata")));
			fail("Should have thrown Exception");
		} catch(Exception e) {
			  assertThat(e)
			  	.isInstanceOf(IllegalArgumentException.class)
			  	.hasMessage("Component tata does not belong to word Word{lemma=abcdefgg, isCompound=true} (length=8), because offsets [6,9] are too big.");
		}
		
		/* Case 2, the bad component is not the last one */
		try {
			CompoundUtils.merge(w_abcdefgh, ImmutableList.of(cd, new Component(4, 9, "toto", "toto"), new Component(9, 12, "tata")));
			fail("Should have thrown Exception");
		} catch(Exception e) {
			 assertThat(e)
			  	.isInstanceOf(IllegalArgumentException.class)
			  	.hasMessage("Component toto does not belong to word Word{lemma=abcdefgg, isCompound=true} (length=8), because offsets [4,9] are too big.");
		}
	}


	@Test
	public void testAllSizeComponents() {
		/* 1 component only */
		assertThat(CompoundUtils.allSizeComponents(w_ab))
			.hasSize(1)
			.extracting("begin", "end", "lemma")
			.contains(tuple(0,2,"aa"));

		/* 2 components */
		assertThat(CompoundUtils.allSizeComponents(w_abcd))
			.hasSize(3)
			.extracting("begin", "end", "lemma")
			.contains(
					tuple(0,2,"aa"),
					tuple(2,4,"cc"),
					tuple(0,4,"abcc")
					);

		/* 4 components */
		assertThat(CompoundUtils.allSizeComponents(w_abcdefgh))
		.hasSize(10)
		.extracting("begin", "end", "lemma")
		.contains(
				tuple(0,2,"aa"),
				tuple(2,4,"cc"),
				tuple(4,6,"ee"),
				tuple(6,8,"gg"),
				tuple(0,4,"abcc"),
				tuple(2,6,"cdee"),
				tuple(4,8,"efgg"),
				tuple(0,6,"abcdee"),
				tuple(2,8,"cdefgg"),
				tuple(0,8,"abcdefgg")
				);
	}
	
	
	@Test
	public void testInnerContiguousComponentPairs() {
		assertThat(CompoundUtils.innerContiguousComponentPairs(w_ab))
			.hasSize(0);

		assertThat(CompoundUtils.innerContiguousComponentPairs(w_abcd))
			.hasSize(1)
			.extracting("element1.lemma", "element2.lemma")
			.contains(
				tuple("aa","cc"));
		
		assertThat(CompoundUtils.innerContiguousComponentPairs(w_abcdef))
			.hasSize(2)
			.extracting("element1.lemma", "element2.lemma")
			.contains(
				tuple("aa","cdee"),
				tuple("abcc","ee"));
		
		assertThat(CompoundUtils.innerContiguousComponentPairs(w_abcdefgh))
			.hasSize(3)
			.extracting("element1.lemma", "element2.lemma")
			.contains(
				tuple("aa","cdefgg"),
				tuple("abcc","efgg"),
				tuple("abcdee","gg"));

	}

	@Test
	public void testGetPossibleComponentsAt() {
		/*
		 * ab
		 */
		assertThat(CompoundUtils.getPossibleComponentsAt(w_ab, 0))
			.hasSize(0);

		/*
		 * abcd
		 */
		assertThat(CompoundUtils.getPossibleComponentsAt(w_abcd, 0))
			.extracting("lemma")
			.hasSize(1)
			.contains("aa");
		
		assertThat(CompoundUtils.getPossibleComponentsAt(w_abcd, 1))
			.extracting("lemma")
			.hasSize(1)
			.contains("cc");

		assertThat(CompoundUtils.getPossibleComponentsAt(w_abcd, 2))
			.hasSize(0);

		
		/*
		 * abcdef
		 */
		assertThat(CompoundUtils.getPossibleComponentsAt(w_abcdef, 0))
			.extracting("lemma")
			.hasSize(2)
			.contains("aa", "abcc");
		
		assertThat(CompoundUtils.getPossibleComponentsAt(w_abcdef, 1))
			.extracting("lemma")
			.hasSize(3)
			.contains("cc", "cdee", "ee");
		
		assertThat(CompoundUtils.getPossibleComponentsAt(w_abcdef, 2))
			.extracting("lemma")
			.hasSize(1)
			.contains("ee");


		assertThat(CompoundUtils.getPossibleComponentsAt(w_abcdef, 3))
			.hasSize(0);


		/*
		 * abcdefgh
		 */
		assertThat(CompoundUtils.getPossibleComponentsAt(w_abcdefgh, 0))
			.extracting("lemma")
			.hasSize(3)
			.contains("aa", "abcc", "abcdee");
		
		assertThat(CompoundUtils.getPossibleComponentsAt(w_abcdefgh, 1))
			.extracting("lemma")
			.hasSize(6)
			.contains("cc", "cdee", "cdefgg", "ee", "efgg", "gg");
		
		assertThat(CompoundUtils.getPossibleComponentsAt(w_abcdefgh, 2))
			.extracting("lemma")
			.hasSize(3)
			.contains("ee", "efgg", "gg");

		assertThat(CompoundUtils.getPossibleComponentsAt(w_abcdefgh, 3))
			.extracting("lemma")
			.hasSize(1)
			.contains("gg");


		assertThat(CompoundUtils.getPossibleComponentsAt(w_abcdefgh, 4))
			.hasSize(0);

	}
		
	@Test
	public void testInnerComponentPairs() {
		assertThat(CompoundUtils.innerComponentPairs(w_ab))
			.hasSize(0);
		assertThat(CompoundUtils.innerComponentPairs(w_abcd))
			.hasSize(1)
			.extracting("element1.lemma", "element2.lemma")
			.contains(tuple("aa","cc"));
		assertThat(CompoundUtils.innerComponentPairs(w_abcdef))
			.hasSize(5)
			.extracting("element1.lemma", "element2.lemma")
			.contains(
					tuple("aa","cc"),
					tuple("aa","ee"),
					tuple("cc","ee"),
					tuple("aa","cdee"),
					tuple("abcc","ee")
				);
		assertThat(CompoundUtils.innerComponentPairs(w_abcdefgh))
			.hasSize(15)
			.extracting("element1.lemma", "element2.lemma")
			.contains(
					tuple("aa","cc"),
					tuple("aa","ee"),
					tuple("aa","gg"),
					tuple("aa","cdee"),
					tuple("aa","efgg"),
					tuple("aa","cdefgg"),
					tuple("abcc","ee"),
					tuple("abcc","gg"),
					tuple("abcc","efgg"),
					tuple("abcdee","gg"),
					tuple("cc","ee"),
					tuple("cc","gg"),
					tuple("cc","efgg"),
					tuple("cdee","gg"),
					tuple("ee","gg")
				);
	}

}
