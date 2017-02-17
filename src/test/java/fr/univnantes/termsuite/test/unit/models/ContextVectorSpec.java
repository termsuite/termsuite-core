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
package fr.univnantes.termsuite.test.unit.models;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import org.junit.Before;
import org.junit.Test;

import fr.univnantes.termsuite.framework.TermSuiteFactory;
import fr.univnantes.termsuite.index.Terminology;
import fr.univnantes.termsuite.model.ContextVector;
import fr.univnantes.termsuite.model.Document;
import fr.univnantes.termsuite.model.Lang;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.TermOccurrence;
import fr.univnantes.termsuite.test.mock.Fixtures;

public class ContextVectorSpec {

	private ContextVector vector;
	private Document doc;
	private Term term1, term2, term3;
	
	@Before
	public void setTerms() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		Terminology terminology = TermSuiteFactory.createIndexedCorpus(Lang.FR, "").getTerminology();

		term1 = Fixtures.term1(terminology);
		term2 = Fixtures.term2(terminology);
		term3 = Fixtures.term3(terminology);
		this.vector = new ContextVector(term1);
		this.doc = new Document(Lang.FR, "doc1");
		this.vector.addCooccurrence(new TermOccurrence(term1, "text1", doc, 10, 15));
		this.vector.addCooccurrence(new TermOccurrence(term3, "text2", doc, 30, 45));
		this.vector.addCooccurrence(new TermOccurrence(term2, "text2", doc, 50, 65));
		this.vector.addCooccurrence(new TermOccurrence(term1, "text1", doc, 70, 90));
		this.vector.addCooccurrence(new TermOccurrence(term3, "text1", doc, 100, 115));
		this.vector.addCooccurrence(new TermOccurrence(term1, "text2", doc, 200, 215));
	}
	
	@Test
	public void testGetEntries() {
		assertThat(this.vector.getEntries()).extracting("coTerm", "nbCooccs", "assocRate").containsExactly(
				tuple(term1, 3, 0d),
				tuple(term3, 2, 0d),
				tuple(term2, 1, 0d)
			);
		this.vector.addCooccurrence(new TermOccurrence(term1, "text2", doc, 64, 65));
		assertThat(this.vector.getEntries()).extracting("coTerm", "nbCooccs", "assocRate").containsExactly(
				tuple(term1, 4, 0d),
				tuple(term3, 2, 0d),
				tuple(term2, 1, 0d)
			);
	}
}
