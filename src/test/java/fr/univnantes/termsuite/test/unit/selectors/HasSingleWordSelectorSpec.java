
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

package fr.univnantes.termsuite.test.unit.selectors;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import fr.univnantes.termsuite.model.Lang;
import fr.univnantes.termsuite.model.RelationType;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.Terminology;
import fr.univnantes.termsuite.model.occurrences.MemoryOccurrenceStore;
import fr.univnantes.termsuite.model.termino.HasSingleWordVariationSelector;
import fr.univnantes.termsuite.model.termino.MemoryTerminology;
import fr.univnantes.termsuite.model.termino.TermSelector;
import fr.univnantes.termsuite.test.unit.TermFactory;

public class HasSingleWordSelectorSpec {
	
	private Term term1;
	private Term term2;
	private Term term3;
	private Term term4;
	private Term term5;
	private Term term6;
	private Term term7;
	
	private Terminology termino ;
	
	@Before
	public void init() {
		termino = new MemoryTerminology("Test", Lang.FR, new MemoryOccurrenceStore(Lang.FR));
		populateTermino(new TermFactory(termino));
	}
	
	private void populateTermino(TermFactory termFactory) {
		
		this.term1 = termFactory.create("N:machine|machin", "A:synchrone|synchro");
		this.term2 = termFactory.create("A:synchrone|synchron");
		this.term3 = termFactory.create("A:asynchrone|asynchron");
		
		this.term4 = termFactory.create("N:machine|machin", "A:statorique|statoric");
		this.term5 = termFactory.create("N:machine|machin", "P:de|de", "N:stator|stator");
		this.term6 = termFactory.create("A:statorique|statoric");
		this.term7 = termFactory.create("N:stator|stator");
		
		termFactory.addPrefix(this.term3, this.term2);
		termFactory.addDerivesInto("N A", this.term7, this.term6);
	}

	
	@Test
	public void testPrefix() {
		TermSelector selector = new HasSingleWordVariationSelector(RelationType.IS_PREFIX_OF);
		assertTrue(selector.select(termino, term1));
		assertTrue(selector.select(termino, term2));
		assertTrue(selector.select(termino, term3));
		assertFalse(selector.select(termino, term4));
		assertFalse(selector.select(termino, term5));
		assertFalse(selector.select(termino, term6));
		assertFalse(selector.select(termino, term7));
	}

	@Test
	public void testDerivation() {
		TermSelector selector = new HasSingleWordVariationSelector(RelationType.DERIVES_INTO);
		assertFalse(selector.select(termino, term1));
		assertFalse(selector.select(termino, term2));
		assertFalse(selector.select(termino, term3));
		assertTrue(selector.select(termino, term4));
		assertTrue(selector.select(termino, term5));
		assertTrue(selector.select(termino, term6));
		assertTrue(selector.select(termino, term7));
	}

}
