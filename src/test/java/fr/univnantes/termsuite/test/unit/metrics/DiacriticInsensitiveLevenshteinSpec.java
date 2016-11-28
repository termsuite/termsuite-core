
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

package fr.univnantes.termsuite.test.unit.metrics;

import static org.junit.Assert.assertEquals;

import java.util.Locale;

import org.junit.Before;
import org.junit.Test;

import fr.univnantes.termsuite.metrics.DiacriticInsensitiveLevenshtein;
import fr.univnantes.termsuite.metrics.EditDistance;

public class DiacriticInsensitiveLevenshteinSpec {

	EditDistance dFrCaseSensitive;
	EditDistance dFrCaseInsensitive;
	
	@Before
	public void setup() {
		dFrCaseSensitive = new DiacriticInsensitiveLevenshtein(Locale.FRENCH);
		dFrCaseInsensitive = new DiacriticInsensitiveLevenshtein(Locale.FRENCH);
	}
	
	@Test
	public void testAccent() {
		assertEquals(0.0, dFrCaseSensitive.compute("a", "a"), 0.0001d);
		assertEquals(0.0, dFrCaseInsensitive.compute("a", "à"), 0.0001d);
		assertEquals(0.0, dFrCaseSensitive.compute("a", "à"), 0.0001d);
		assertEquals(0.0, dFrCaseInsensitive.compute("a", "a"), 0.0001d);
	}

	@Test
	public void testCap() {
		assertEquals(0.0, dFrCaseInsensitive.compute("a", "A"), 0.0001d);
		assertEquals(0.0, dFrCaseSensitive.compute("a", "A"), 0.0001d);

	}
 }
