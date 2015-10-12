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

import org.junit.Before;
import org.junit.Test;
import org.ttc.project.Fixtures;

import com.google.common.collect.ImmutableSet;

import eu.project.ttc.engines.desc.Lang;
import eu.project.ttc.models.Document;
import eu.project.ttc.models.Term;
import eu.project.ttc.models.TermBuilder;
import eu.project.ttc.models.TermIndex;
import eu.project.ttc.models.index.MemoryTermIndex;

public class TermClassSpec {

	private Term term1;
	private Term term2;
	private Term term3;
	private TermIndex termIndex;

	@Before
	public void setTerms() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		termIndex = new MemoryTermIndex("TermIndexWithOccurrences", Lang.EN);
		
		final Document doc = Fixtures.document1();
		
		term1 = TermBuilder.start(termIndex)
			.setGroupingKey("term1")
			.addWord(Fixtures.word1(), "N")
			.addOccurrence(0, 10, doc, "t1")
			.addOccurrence(31, 40, doc, "t1")
			.addOccurrence(61, 70, doc, "t1") // included in t3
			.createAndAddToIndex();
		
		term2 = TermBuilder.start(termIndex)
			.setGroupingKey("term2")
			.addWord(Fixtures.word2(), "A")
			.addOccurrence(9, 15, doc, "t2") // overlaps with term1
			.addOccurrence(20, 25, doc, "t2") 
			.createAndAddToIndex();
		
		
		
		term3 = TermBuilder.start(termIndex)
			.setGroupingKey("term3")
			.addWord(Fixtures.word3(), "N")
			.addOccurrence(12, 18, doc, "t3") // overlaps with t2, but taken anyway
			.addOccurrence(35, 50, doc, "t3") // overlaps with t1
			.addOccurrence(61, 80, doc, "t3")
			.createAndAddToIndex();
	}
	
	@Test
	public void testGetOccurrences() {
		termIndex.classifyTerms(term1, ImmutableSet.of(term1, term2, term3));
		assertThat(term1.getTermClass().getOccurrences()).hasSize(5).extracting("begin", "end", "coveredText")
			.contains(
					tuple(0,10, "t1"),
					tuple(12,18, "t3"),
					tuple(20,25, "t2"),
					tuple(31,40, "t1"),
					tuple(61,80, "t3")
			);
		
	}

}
