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

import org.junit.Before;
import org.junit.Test;
import org.ttc.project.Fixtures;

import eu.project.ttc.models.Document;
import eu.project.ttc.models.OccurrenceType;
import eu.project.ttc.models.Term;
import eu.project.ttc.models.TermOccurrence;

public class DocumentSpec {

	private Document doc1;
	private Term term1;
	private Term term2;
	private Term term3;
	
	private TermOccurrence o1;
	private TermOccurrence o2;
	private TermOccurrence o3;
	private TermOccurrence o4;
	private TermOccurrence o5;
	private TermOccurrence o6;
	private TermOccurrence o7;

	@Before
	public void setTerms() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		this.term1 = Fixtures.term1(); 
		this.term2 = Fixtures.term2(); // single word
		this.term3 = Fixtures.term3();
		this.doc1 = Fixtures.document1();
		o1 = new TermOccurrence(term1, "o1", doc1, 4, 6);
		o1.setPrimaryOccurrence(true);
		o2 = new TermOccurrence(term2, "o2", doc1, 7, 10);
		o2.setPrimaryOccurrence(true);
		o3 = new TermOccurrence(term2, "o3", doc1, 10, 18);
		o3.setPrimaryOccurrence(true);
		o4 = new TermOccurrence(term1, "o4", doc1, 18, 25);
		o4.setPrimaryOccurrence(false);
		o5 = new TermOccurrence(term2, "o5", doc1, 25, 30);
		o5.setPrimaryOccurrence(true);
		o6 = new TermOccurrence(term3, "o6", doc1, 30, 35);
		o6.setPrimaryOccurrence(true);
		o7 = new TermOccurrence(term3, "o7", doc1, 35, 40);
		o7.setPrimaryOccurrence(false);
		
		addOcc(o1);
		addOcc(o2);
		addOcc(o3);
		addOcc(o4);
		addOcc(o5);
		addOcc(o6);
		addOcc(o7);
		
	}

	private void addOcc(TermOccurrence occ) {
		occ.getTerm().addOccurrence(occ);
		doc1.indexTermOccurrence(occ);
	}
	
	@Test
	public void testGetOccurrences() {
		// should order the occurrence list
		assertThat(doc1.getOccurrences()).extracting("coveredText").containsExactly(
			"o1","o2","o3","o4","o5","o6", "o7"
		);
	}
	
	@Test
	public void testAddOccurrence() {
		assertThat(doc1.getOccurrences()).extracting("coveredText").containsExactly(
				"o1","o2","o3","o4","o5","o6", "o7"
			);
		// Should reorder the occurrence list
		addOcc(new TermOccurrence(term1, "o8", doc1, 14, 20));
		assertThat(doc1.getOccurrences()).extracting("coveredText").containsExactly(
				"o1","o2","o3","o8", "o4","o5","o6", "o7"
			);
	}
	
	@Test
	public void testGetOccurrenceContextDoNotOverlap1() {
		assertThat(doc1.getOccurrenceContext(o4, OccurrenceType.PRIMARY, 1)).containsExactly(o3, o5);
		TermOccurrence o8 = new TermOccurrence(term2, "o8", doc1, 	20, 28);
		addOcc(o8);
		// o8 does not overlap with o4 and is returned instead of o5
		assertThat(doc1.getOccurrenceContext(o4, OccurrenceType.PRIMARY, 1)).containsExactly(o3, o5);
	}

	@Test
	public void testGetOccurrenceContextDoNotOverlap2() {
		assertThat(doc1.getOccurrenceContext(o4, OccurrenceType.PRIMARY, 1)).containsExactly(o3, o5);
		TermOccurrence o8 = new TermOccurrence(term2, "o8", doc1, 	20, 28);
		addOcc(o8);
		// o8 overlaps with o4 and is returned instead of o5
		assertThat(doc1.getOccurrenceContext(o4, OccurrenceType.PRIMARY, 1)).containsExactly(o3, o5);
	}

	@Test
	public void testGetOccurrenceContextPrimaryOnly() {
		// size 0
		assertThat(doc1.getOccurrenceContext(o1, OccurrenceType.PRIMARY, 0)).isEmpty();
		assertThat(doc1.getOccurrenceContext(o2, OccurrenceType.PRIMARY, 0)).isEmpty();
		assertThat(doc1.getOccurrenceContext(o3, OccurrenceType.PRIMARY, 0)).isEmpty();
		assertThat(doc1.getOccurrenceContext(o4, OccurrenceType.PRIMARY, 0)).isEmpty();
		assertThat(doc1.getOccurrenceContext(o5, OccurrenceType.PRIMARY, 0)).isEmpty();
		assertThat(doc1.getOccurrenceContext(o6, OccurrenceType.PRIMARY, 0)).isEmpty();

		// size 1
		assertThat(doc1.getOccurrenceContext(o1, OccurrenceType.PRIMARY, 1)).containsExactly(o2);
		assertThat(doc1.getOccurrenceContext(o2, OccurrenceType.PRIMARY, 1)).containsExactly(o1, o3);
		assertThat(doc1.getOccurrenceContext(o3, OccurrenceType.PRIMARY, 1)).containsExactly(o2, o5);
		assertThat(doc1.getOccurrenceContext(o4, OccurrenceType.PRIMARY, 1)).containsExactly(o3, o5);
		assertThat(doc1.getOccurrenceContext(o5, OccurrenceType.PRIMARY, 1)).containsExactly(o3, o6);
		assertThat(doc1.getOccurrenceContext(o6, OccurrenceType.PRIMARY, 1)).containsExactly(o5);
		assertThat(doc1.getOccurrenceContext(o7, OccurrenceType.PRIMARY, 1)).containsExactly(o6);
		

		// size 2
		assertThat(doc1.getOccurrenceContext(o1, OccurrenceType.PRIMARY, 2)).containsExactly(o2, o3);
		assertThat(doc1.getOccurrenceContext(o2, OccurrenceType.PRIMARY, 2)).containsExactly(o1, o3, o5);
		assertThat(doc1.getOccurrenceContext(o3, OccurrenceType.PRIMARY, 2)).hasSize(4).contains(o1, o2, o5, o6);
		assertThat(doc1.getOccurrenceContext(o4, OccurrenceType.PRIMARY, 2)).hasSize(4).contains(o2, o3, o5, o6);
		assertThat(doc1.getOccurrenceContext(o5, OccurrenceType.PRIMARY, 2)).hasSize(3).contains(o2, o3, o6);
		assertThat(doc1.getOccurrenceContext(o6, OccurrenceType.PRIMARY, 2)).hasSize(2).contains(o3, o5);
		assertThat(doc1.getOccurrenceContext(o7, OccurrenceType.PRIMARY, 2)).hasSize(2).contains(o5, o6);
		
		// size 3
		assertThat(doc1.getOccurrenceContext(o1, OccurrenceType.PRIMARY, 3)).hasSize(3).contains(o2, o3, o5);
		assertThat(doc1.getOccurrenceContext(o2, OccurrenceType.PRIMARY, 3)).hasSize(4).contains(o1, o3, o5, o6);
		assertThat(doc1.getOccurrenceContext(o3, OccurrenceType.PRIMARY, 3)).hasSize(4).contains(o1, o2, o5, o6);
		assertThat(doc1.getOccurrenceContext(o4, OccurrenceType.PRIMARY, 3)).hasSize(5).contains(o1, o2, o3, o5, o6);
		assertThat(doc1.getOccurrenceContext(o5, OccurrenceType.PRIMARY, 3)).hasSize(4).contains(o1, o2, o3, o6);
		assertThat(doc1.getOccurrenceContext(o6, OccurrenceType.PRIMARY, 3)).hasSize(3).contains(o2, o3, o5);
		assertThat(doc1.getOccurrenceContext(o7, OccurrenceType.PRIMARY, 3)).hasSize(3).contains(o3, o5, o6);

		// size 10
		assertThat(doc1.getOccurrenceContext(o1, OccurrenceType.PRIMARY, 13)).hasSize(4).contains(o2, o3, o5, o6);
		assertThat(doc1.getOccurrenceContext(o2, OccurrenceType.PRIMARY, 13)).hasSize(4).contains(o1, o3, o5, o6);
		assertThat(doc1.getOccurrenceContext(o3, OccurrenceType.PRIMARY, 13)).hasSize(4).contains(o1, o2, o5, o6);
		assertThat(doc1.getOccurrenceContext(o4, OccurrenceType.PRIMARY, 13)).hasSize(5).contains(o1, o2, o3, o5, o6);
		assertThat(doc1.getOccurrenceContext(o5, OccurrenceType.PRIMARY, 13)).hasSize(4).contains(o1, o2, o3, o6);
		assertThat(doc1.getOccurrenceContext(o6, OccurrenceType.PRIMARY, 13)).hasSize(4).contains(o1, o2, o3, o5);
		assertThat(doc1.getOccurrenceContext(o7,OccurrenceType.PRIMARY,  13)).hasSize(5).contains(o1, o2, o3, o5, o6);

	}

	@Test
	public void testGetOccurrenceContext() {
		// size 0
		assertThat(doc1.getOccurrenceContext(o1, OccurrenceType.SINGLE_WORD, 0)).isEmpty();
		assertThat(doc1.getOccurrenceContext(o2, OccurrenceType.SINGLE_WORD, 0)).isEmpty();
		assertThat(doc1.getOccurrenceContext(o3, OccurrenceType.SINGLE_WORD, 0)).isEmpty();
		assertThat(doc1.getOccurrenceContext(o4, OccurrenceType.SINGLE_WORD, 0)).isEmpty();
		assertThat(doc1.getOccurrenceContext(o5, OccurrenceType.SINGLE_WORD, 0)).isEmpty();
		assertThat(doc1.getOccurrenceContext(o6, OccurrenceType.SINGLE_WORD, 0)).isEmpty();

		// size 1
		assertThat(doc1.getOccurrenceContext(o1, OccurrenceType.SINGLE_WORD, 1)).containsExactly(o2);
		assertThat(doc1.getOccurrenceContext(o2, OccurrenceType.SINGLE_WORD, 1)).containsExactly(o3);
		assertThat(doc1.getOccurrenceContext(o3, OccurrenceType.SINGLE_WORD, 1)).containsExactly(o2, o5);
		assertThat(doc1.getOccurrenceContext(o4, OccurrenceType.SINGLE_WORD, 1)).containsExactly(o3, o5);
		assertThat(doc1.getOccurrenceContext(o5, OccurrenceType.SINGLE_WORD, 1)).containsExactly(o3);
		assertThat(doc1.getOccurrenceContext(o6, OccurrenceType.SINGLE_WORD, 1)).containsExactly(o5);
		assertThat(doc1.getOccurrenceContext(o7, OccurrenceType.SINGLE_WORD, 1)).containsExactly(o5);
		

		// size 2
		assertThat(doc1.getOccurrenceContext(o1, OccurrenceType.SINGLE_WORD, 2)).containsExactly(o2, o3);
		assertThat(doc1.getOccurrenceContext(o2, OccurrenceType.SINGLE_WORD, 2)).containsExactly(o3, o5);
		assertThat(doc1.getOccurrenceContext(o3, OccurrenceType.SINGLE_WORD, 2)).containsExactly(o2, o5);
		assertThat(doc1.getOccurrenceContext(o4, OccurrenceType.SINGLE_WORD, 2)).containsExactly(o3, o2, o5);
		assertThat(doc1.getOccurrenceContext(o5, OccurrenceType.SINGLE_WORD, 2)).containsExactly(o3, o2);
		assertThat(doc1.getOccurrenceContext(o6, OccurrenceType.SINGLE_WORD, 2)).containsExactly(o5, o3);
		assertThat(doc1.getOccurrenceContext(o7, OccurrenceType.SINGLE_WORD, 2)).containsExactly(o5, o3);
		
		// size 3
		assertThat(doc1.getOccurrenceContext(o1, OccurrenceType.SINGLE_WORD, 3)).containsExactly(o2, o3, o5);
		assertThat(doc1.getOccurrenceContext(o2, OccurrenceType.SINGLE_WORD, 3)).containsExactly(o3, o5);
		assertThat(doc1.getOccurrenceContext(o3, OccurrenceType.SINGLE_WORD, 3)).containsExactly(o2, o5);
		assertThat(doc1.getOccurrenceContext(o4, OccurrenceType.SINGLE_WORD, 3)).containsExactly(o3, o2, o5);
		assertThat(doc1.getOccurrenceContext(o5, OccurrenceType.SINGLE_WORD, 3)).containsExactly(o3, o2);
		assertThat(doc1.getOccurrenceContext(o6, OccurrenceType.SINGLE_WORD, 3)).containsExactly(o5, o3, o2);
		assertThat(doc1.getOccurrenceContext(o7, OccurrenceType.SINGLE_WORD, 3)).containsExactly(o5, o3, o2);

		// size 10
		assertThat(doc1.getOccurrenceContext(o1, OccurrenceType.SINGLE_WORD, 13)).containsExactly(o2, o3, o5);
		assertThat(doc1.getOccurrenceContext(o2, OccurrenceType.SINGLE_WORD, 13)).containsExactly(o3, o5);
		assertThat(doc1.getOccurrenceContext(o3, OccurrenceType.SINGLE_WORD, 13)).containsExactly(o2, o5);
		assertThat(doc1.getOccurrenceContext(o4, OccurrenceType.SINGLE_WORD, 13)).containsExactly(o3, o2, o5);
		assertThat(doc1.getOccurrenceContext(o5, OccurrenceType.SINGLE_WORD, 13)).containsExactly(o3, o2);
		assertThat(doc1.getOccurrenceContext(o6, OccurrenceType.SINGLE_WORD, 13)).containsExactly(o5, o3, o2);
		assertThat(doc1.getOccurrenceContext(o7, OccurrenceType.SINGLE_WORD, 13)).containsExactly(o5, o3, o2);

	}

}
