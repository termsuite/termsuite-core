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
package fr.univnantes.termsuite.test.unit.engines.contextualizer;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

import fr.univnantes.termsuite.engines.contextualizer.DocumentView;
import fr.univnantes.termsuite.framework.TermSuiteFactory;
import fr.univnantes.termsuite.index.Terminology;
import fr.univnantes.termsuite.model.Document;
import fr.univnantes.termsuite.model.Lang;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.TermOccurrence;
import fr.univnantes.termsuite.test.unit.Fixtures;

public class DocumentViewSpec {

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
	
	private DocumentView documentView;
	
	
	@Before
	public void setTerms() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		Terminology termino = TermSuiteFactory.createIndexedCorpus(Lang.FR, "").getTerminology();
		this.term1 = Fixtures.term1(termino);
		this.term2 = Fixtures.term2(termino);
		this.term3 = Fixtures.term3(termino);
		this.doc1 = Fixtures.document1();
		o1 = new TermOccurrence(term1, "o1", doc1, 4, 6);
		o2 = new TermOccurrence(term2, "o2", doc1, 7, 10);
		o3 = new TermOccurrence(term2, "o3", doc1, 10, 18);
		o4 = new TermOccurrence(term1, "o4", doc1, 18, 25);
		o5 = new TermOccurrence(term2, "o5", doc1, 25, 30);
		o6 = new TermOccurrence(term3, "o6", doc1, 30, 35);
		o7 = new TermOccurrence(term3, "o7", doc1, 35, 40);
		
		documentView = new DocumentView();
		
		addOcc(o1);
		addOcc(o2);
		addOcc(o3);
		addOcc(o4);
		addOcc(o5);
		addOcc(o6);
		addOcc(o7);
		
	}

	private void addOcc(TermOccurrence occ) {
		documentView.indexTermOccurrence(occ);
	}
	
	@Test
	public void testGetOccurrences() {
		// should order the occurrence list
		assertThat(documentView.getOccurrences()).extracting("coveredText").containsExactly(
			"o1","o2","o3","o4","o5","o6", "o7"
		);
	}
	
	@Test
	public void testAddOccurrence() {
		assertThat(documentView.getOccurrences()).extracting("coveredText").containsExactly(
				"o1","o2","o3","o4","o5","o6", "o7"
			);
		// Should reorder the occurrence list
		addOcc(new TermOccurrence(term1, "o8", doc1, 14, 20));
		assertThat(documentView.getOccurrences()).extracting("coveredText").containsExactly(
				"o1","o2","o3","o8", "o4","o5","o6", "o7"
			);
	}
	
	@Test
	public void testGetOccurrenceContextDoNotOverlap1() {
		assertThat(documentView.getOccurrenceContext(o4,  1)).containsExactly(o3, o5);
		TermOccurrence o8 = new TermOccurrence(term2, "o8", doc1, 	20, 28);
		addOcc(o8);
		// o8 does not overlap with o4 and is returned instead of o5
		assertThat(documentView.getOccurrenceContext(o4,  1)).containsExactly(o3, o5);
	}

	@Test
	public void testGetOccurrenceContextDoNotOverlap2() {
		assertThat(documentView.getOccurrenceContext(o4,  1)).containsExactly(o3, o5);
		TermOccurrence o8 = new TermOccurrence(term2, "o8", doc1, 	20, 28);
		addOcc(o8);
		// o8 overlaps with o4 and is returned instead of o5
		assertThat(documentView.getOccurrenceContext(o4,  1)).containsExactly(o3, o5);
	}

	@Test
	public void testGetOccurrenceContext() {
		// size 0
		assertThat(documentView.getOccurrenceContext(o1,  0)).isEmpty();
		assertThat(documentView.getOccurrenceContext(o2,  0)).isEmpty();
		assertThat(documentView.getOccurrenceContext(o3,  0)).isEmpty();
		assertThat(documentView.getOccurrenceContext(o4,  0)).isEmpty();
		assertThat(documentView.getOccurrenceContext(o5,  0)).isEmpty();
		assertThat(documentView.getOccurrenceContext(o6,  0)).isEmpty();

		// size 1
		assertThat(documentView.getOccurrenceContext(o1,  1)).containsExactly(o2);
		assertThat(documentView.getOccurrenceContext(o2,  1)).containsExactly(o3);
		assertThat(documentView.getOccurrenceContext(o3,  1)).containsExactly(o2, o5);
		assertThat(documentView.getOccurrenceContext(o4,  1)).containsExactly(o3, o5);
		assertThat(documentView.getOccurrenceContext(o5,  1)).containsExactly(o3);
		assertThat(documentView.getOccurrenceContext(o6,  1)).containsExactly(o5);
		assertThat(documentView.getOccurrenceContext(o7,  1)).containsExactly(o5);
		

		// size 2
		assertThat(documentView.getOccurrenceContext(o1,  2)).containsExactly(o2, o3);
		assertThat(documentView.getOccurrenceContext(o2,  2)).containsExactly(o3, o5);
		assertThat(documentView.getOccurrenceContext(o3,  2)).containsExactly(o2, o5);
		assertThat(documentView.getOccurrenceContext(o4,  2)).containsExactly(o3, o2, o5);
		assertThat(documentView.getOccurrenceContext(o5,  2)).containsExactly(o3, o2);
		assertThat(documentView.getOccurrenceContext(o6,  2)).containsExactly(o5, o3);
		assertThat(documentView.getOccurrenceContext(o7,  2)).containsExactly(o5, o3);
		
		// size 3
		assertThat(documentView.getOccurrenceContext(o1,  3)).containsExactly(o2, o3, o5);
		assertThat(documentView.getOccurrenceContext(o2,  3)).containsExactly(o3, o5);
		assertThat(documentView.getOccurrenceContext(o3,  3)).containsExactly(o2, o5);
		assertThat(documentView.getOccurrenceContext(o4,  3)).containsExactly(o3, o2, o5);
		assertThat(documentView.getOccurrenceContext(o5,  3)).containsExactly(o3, o2);
		assertThat(documentView.getOccurrenceContext(o6,  3)).containsExactly(o5, o3, o2);
		assertThat(documentView.getOccurrenceContext(o7,  3)).containsExactly(o5, o3, o2);

		// size 10
		assertThat(documentView.getOccurrenceContext(o1,  13)).containsExactly(o2, o3, o5);
		assertThat(documentView.getOccurrenceContext(o2,  13)).containsExactly(o3, o5);
		assertThat(documentView.getOccurrenceContext(o3,  13)).containsExactly(o2, o5);
		assertThat(documentView.getOccurrenceContext(o4,  13)).containsExactly(o3, o2, o5);
		assertThat(documentView.getOccurrenceContext(o5,  13)).containsExactly(o3, o2);
		assertThat(documentView.getOccurrenceContext(o6,  13)).containsExactly(o5, o3, o2);
		assertThat(documentView.getOccurrenceContext(o7,  13)).containsExactly(o5, o3, o2);

	}

}
