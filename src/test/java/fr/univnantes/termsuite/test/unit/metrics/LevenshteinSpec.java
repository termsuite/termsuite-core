
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

import org.junit.Before;
import org.junit.Test;

import fr.univnantes.termsuite.metrics.EditDistance;
import fr.univnantes.termsuite.metrics.Levenshtein;

public class LevenshteinSpec {

	EditDistance dist;
	
	@Before
	public void setup() {
		dist = new Levenshtein();
	}
	
	@Test
	public void testDistanceEqual() {
		assertEquals(0, dist.compute("abcdefghij", "abcdefghij"));
	}

	@Test
	public void testDistanceInsert() {
		assertEquals(1, dist.compute("abcdefghij", "abcdefzghij"));
	}

	@Test
	public void testDistanceSubstitution() {
		assertEquals(1, dist.compute("abcdefghij", "azcdefghij"));
	}

	@Test
	public void testDistanceRemove() {
		assertEquals(1, dist.compute("abcdefghij", "abcdeghij"));
	}
	
	@Test
	public void testDistanceWithMax() {
		assertEquals(9, dist.compute("abcdefghij", "azbzczdzezfzgzhzizj"));
		assertEquals(19, dist.compute("abcdefghij", "azbzczdzezfzgzhzizj", 3));

		assertEquals(0, dist.compute("abcdefghij", "abcdefghij"));
		assertEquals(1, dist.compute("abcdefghij", "azbcdefghij", 2));
		assertEquals(2, dist.compute("abcdefghij", "azbzcdefghij", 2));
		assertEquals(13, dist.compute("abcdefghij", "azbzczdefghij", 2));
}


	
	@Test
	public void testAccent() {
		assertEquals(0, dist.compute("a", "a"));
		assertEquals(1, dist.compute("a", "à"));
	}

	@Test
	public void testCap() {
		assertEquals(1, dist.compute("a", "A"));

	}
 }
