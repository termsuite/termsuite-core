
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

package eu.project.ttc.test.unit.resources;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.hamcrest.core.StringContains;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import fr.univnantes.termsuite.model.TermWord;
import fr.univnantes.termsuite.resources.SuffixDerivation;

public class SuffixDerivationSpec {
	
	SuffixDerivation suffixDerivation;
	
	@Before
	public void setUp() {
		suffixDerivation = new SuffixDerivation("N", "A", "ièreté", "ier");
	}
	
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void testGetType() {
		assertEquals("A N", suffixDerivation.getType());
	}

	@Test
	public void testShouldRaiseErrorSinceIfCannotDerivateOnLemma() {
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage(StringContains.containsString("Cannot operate the derivation"));
		suffixDerivation.getBaseForm(TermWord.create("tata", "N"));
	}

	@Test
	public void testShouldRaiseErrorSinceIfCannotDerivateOnLabel() {
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage(StringContains.containsString("Cannot operate the derivation"));
		suffixDerivation.getBaseForm(TermWord.create("grossièreté", "A"));
	}

	@Test
	public void testDerivate() {
		assertEquals(
				TermWord.create("grossier", "A"), 
				suffixDerivation.getBaseForm(TermWord.create("grossièreté", "N")));
	}

	@Test
	public void testCanDerivate() {
		assertFalse(suffixDerivation.isKnownDerivate(TermWord.create("tata", "A")));
		assertFalse(suffixDerivation.isKnownDerivate(TermWord.create("grossièreté", "A")));
		assertTrue(suffixDerivation.isKnownDerivate(TermWord.create("grossièreté", "N")));
	}


}
