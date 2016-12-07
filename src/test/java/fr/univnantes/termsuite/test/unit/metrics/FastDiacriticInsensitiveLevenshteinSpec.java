
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
import fr.univnantes.termsuite.metrics.FastDiacriticInsensitiveLevenshtein;

public class FastDiacriticInsensitiveLevenshteinSpec {

	EditDistance caseSensitive;
	EditDistance caseInsensitive;
	
	@Before
	public void setup() {
		caseSensitive = new FastDiacriticInsensitiveLevenshtein(true);
		caseInsensitive = new FastDiacriticInsensitiveLevenshtein(false);
	}
	
	@Test
	public void testMinimumSimilarity() {
		assertEquals(0.5, caseSensitive.computeNormalized("abcdefghij", "azbzczdzezfzgzhzizjz"), 0.0001d);
		
		assertEquals(0, caseSensitive.computeNormalized("abcdefghij", "azbzczdzezfzgzhzizjz", 0.9), 0.0001d);

		assertEquals(1, caseSensitive.computeNormalized("abcdefghij", "abcdefghij", 0.8), 0.0001d);
		assertEquals(10.0/11, caseSensitive.computeNormalized("abcdefghij", "azbcdefghij", 0.8), 0.0001d);
		assertEquals(10.0/12, caseSensitive.computeNormalized("abcdefghij", "azbzcdefghij", 0.8), 0.0001d);
		assertEquals(10.0/13, caseSensitive.computeNormalized("abcdefghij", "azbzczdefghij", 0.8), 0.0001d);
		assertEquals(0, caseSensitive.computeNormalized("abcdefghij", "azbzczdzefghij", 0.8), 0.0001d);
	}

	
	@Test
	public void testAccent() {
		assertEquals(0, caseSensitive.compute("a", "a"));
		assertEquals(0, caseSensitive.compute("a", "à"));
		assertEquals(0, caseSensitive.compute("aàbbccddeè", "aàbbccddeé"));
		assertEquals(1.0, caseSensitive.computeNormalized("a", "a"), 0.0001d);
		assertEquals(1.0, caseSensitive.computeNormalized("a", "à"), 0.0001d);
		assertEquals(1.0, caseSensitive.computeNormalized("aàbbccddeè", "aàbbccddeé"), 0.00001d);

	}

	@Test
	public void testCap() {
		assertEquals(1, caseSensitive.compute("a", "A"));
		assertEquals(3, caseSensitive.compute("abc", "AAC"));
		assertEquals(0, caseSensitive.computeNormalized("a", "A"), 0.0001d);
		assertEquals(0, caseSensitive.computeNormalized("abc", "AAC"), 0.0001d);
		
		assertEquals(0, caseInsensitive.compute("a", "A"));
		assertEquals(1, caseInsensitive.compute("abc", "AAC"));
		assertEquals(1.0, caseInsensitive.computeNormalized("a", "A"), 0.0001d);
		assertEquals(0.666666, caseInsensitive.computeNormalized("abc", "AAC"), 0.0001d);
		

		
	}
 }
