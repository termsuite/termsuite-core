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
package eu.project.ttc.test.unit.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

import eu.project.ttc.models.Document;
import eu.project.ttc.models.Form;
import eu.project.ttc.models.Term;
import eu.project.ttc.models.TermOccurrence;
import eu.project.ttc.test.unit.Fixtures;
import eu.project.ttc.utils.TermOccurrenceUtils;

public class TermOccurrenceUtilsSpec {

	
	TermOccurrence o1;
	TermOccurrence o2;
	TermOccurrence o3;
	TermOccurrence o4;
	TermOccurrence o5;
	
	@Before
	public void setup() {
		final Document document1 = Fixtures.document1();
		final Term term1 = Fixtures.term1();
		final Term term2 = Fixtures.term2();
		final Term term3 = Fixtures.term3();
		term1.setFrequencyNorm(0.1);
		term2.setFrequencyNorm(0.2);
		term3.setFrequencyNorm(0.3);
		Form form1 = new Form("blabla1");
		o1 = new TermOccurrence(term1, form1, document1, 10, 20);
		Form form2 = new Form("blabla2");
		o2 = new TermOccurrence(term2, form2, document1, 20, 30);
		Form form3 = new Form("blabla3");
		o3 = new TermOccurrence(term1, form3, document1, 10, 40);
		Form form4 = new Form("blabla4");
		o4 = new TermOccurrence(term3, form4, document1, 30, 50);
		Form form5 = new Form("blabla5");
		o5 = new TermOccurrence(term2, form5, document1, 40, 60);
	}
	
	@Test
	public void testAreOffsetsOverlapping() {
		assertFalse(TermOccurrenceUtils.areOffsetsOverlapping(o1, o2));
		assertFalse(TermOccurrenceUtils.areOffsetsOverlapping(o2, o1));
		assertTrue(TermOccurrenceUtils.areOffsetsOverlapping(o1, o3));
		assertTrue(TermOccurrenceUtils.areOffsetsOverlapping(o3, o1));
		assertFalse(TermOccurrenceUtils.areOffsetsOverlapping(o1, o4));
		assertFalse(TermOccurrenceUtils.areOffsetsOverlapping(o4, o1));
		assertFalse(TermOccurrenceUtils.areOffsetsOverlapping(o1, o4));
		assertFalse(TermOccurrenceUtils.areOffsetsOverlapping(o5, o1));

		assertTrue(TermOccurrenceUtils.areOffsetsOverlapping(o2, o3));
		assertTrue(TermOccurrenceUtils.areOffsetsOverlapping(o3, o2));
		assertFalse(TermOccurrenceUtils.areOffsetsOverlapping(o2, o4));
		assertFalse(TermOccurrenceUtils.areOffsetsOverlapping(o4, o2));
		assertFalse(TermOccurrenceUtils.areOffsetsOverlapping(o2, o4));
		assertFalse(TermOccurrenceUtils.areOffsetsOverlapping(o5, o2));

		
		assertTrue(TermOccurrenceUtils.areOffsetsOverlapping(o3, o4));
		assertTrue(TermOccurrenceUtils.areOffsetsOverlapping(o4, o3));
		assertFalse(TermOccurrenceUtils.areOffsetsOverlapping(o3, o5));
		assertFalse(TermOccurrenceUtils.areOffsetsOverlapping(o5, o3));

		assertTrue(TermOccurrenceUtils.areOffsetsOverlapping(o5, o4));
		assertTrue(TermOccurrenceUtils.areOffsetsOverlapping(o5, o4));

	}
	
	@Test
	public void testHasOverlappingOffsets() {
		assertTrue(TermOccurrenceUtils.hasOverlappingOffsets(o1, Lists.newArrayList(o2, o3, o4, o5)));
		assertFalse(TermOccurrenceUtils.hasOverlappingOffsets(o1, Lists.newArrayList(o2, o4, o5)));

		assertTrue(TermOccurrenceUtils.hasOverlappingOffsets(o2, Lists.newArrayList(o1, o3, o4, o5)));
		assertFalse(TermOccurrenceUtils.hasOverlappingOffsets(o2, Lists.newArrayList(o1, o4, o5)));

		assertTrue(TermOccurrenceUtils.hasOverlappingOffsets(o3, Lists.newArrayList(o1, o2, o4, o5)));
		assertTrue(TermOccurrenceUtils.hasOverlappingOffsets(o3, Lists.newArrayList(o1, o2, o5)));
		assertTrue(TermOccurrenceUtils.hasOverlappingOffsets(o3, Lists.newArrayList(o1, o5)));
		assertTrue(TermOccurrenceUtils.hasOverlappingOffsets(o3, Lists.newArrayList(o1, o4, o5)));
		assertTrue(TermOccurrenceUtils.hasOverlappingOffsets(o3, Lists.newArrayList(o2, o4, o5)));
		assertFalse(TermOccurrenceUtils.hasOverlappingOffsets(o3, Lists.newArrayList(o5)));

		assertTrue(TermOccurrenceUtils.hasOverlappingOffsets(o4, Lists.newArrayList(o1, o2, o3,  o5)));
		assertTrue(TermOccurrenceUtils.hasOverlappingOffsets(o4, Lists.newArrayList(o1, o2, o5)));
		assertFalse(TermOccurrenceUtils.hasOverlappingOffsets(o4, Lists.newArrayList(o1, o2)));

		assertTrue(TermOccurrenceUtils.hasOverlappingOffsets(o5, Lists.newArrayList(o1, o2, o3, o4)));
		assertFalse(TermOccurrenceUtils.hasOverlappingOffsets(o5, Lists.newArrayList(o1, o2, o3)));

	}
	
	@Test
	public void testOccurrenceChunkIterator1() {
		List<TermOccurrence> occurrences = Lists.newArrayList(o1, o2, o3, o4, o5);
		List<List<TermOccurrence>> chunks = Lists.newArrayList(TermOccurrenceUtils.occurrenceChunkIterator(occurrences));
		assertThat(chunks).hasSize(1);
		assertThat(chunks.get(0)).hasSize(5)
			.containsExactly(o3,o1,o2,o4,o5);
	}
	
	@Test
	public void testOccurrenceChunkIterator2() {
		TermOccurrence o6 = new TermOccurrence(Fixtures.term1(), new Form("blabla6"), Fixtures.document1(), 100, 200);
		TermOccurrence o7 = new TermOccurrence(Fixtures.term2(), new Form("blabla7"), Fixtures.document1(), 150, 220);
		
		List<TermOccurrence> occurrences = Lists.newArrayList(o1, o2, o3, o4, o5, o6, o7);
		List<List<TermOccurrence>> chunks = Lists.newArrayList(TermOccurrenceUtils.occurrenceChunkIterator(occurrences));
		assertThat(chunks).hasSize(2);
		assertThat(chunks.get(0)).hasSize(5)
			.containsExactly(o3,o1,o2,o4,o5);

		assertThat(chunks.get(1)).hasSize(2)
		.containsExactly(o6,o7);
	}
	
	
}
