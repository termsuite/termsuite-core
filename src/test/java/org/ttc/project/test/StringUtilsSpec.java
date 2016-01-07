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

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import eu.project.ttc.models.TermIndex;
import eu.project.ttc.utils.StringUtils;

public class StringUtilsSpec {
	
	private TermIndex termIndex;
	
	@Before
	public void setUp() {
		
	}

	@Test
	public void nbDigitSequences() {
		assertEquals(0, StringUtils.nbDigitSequences("ergqsdrgsqergeq"));
		assertEquals(1, StringUtils.nbDigitSequences("er232gqsdrgsqergeq"));
		assertEquals(1, StringUtils.nbDigitSequences("er232gqsdrgsqergeq"));
		assertEquals(2, StringUtils.nbDigitSequences("er232gqsdrg2sqergeq"));
	}
	
	@Test
	public void getOrthographicScore() {
		assertEquals(1d, StringUtils.getOrthographicScore("eqergeq"), 0.00001);
		assertEquals(1d/1.8, StringUtils.getOrthographicScore("123eqergeq"), 0.00001);
		assertEquals(1d/1.8, StringUtils.getOrthographicScore("eqergeq456"), 0.00001);
		assertEquals(0.85, StringUtils.getOrthographicScore("3eqergeq"), 0.00001);
		assertEquals(0.85, StringUtils.getOrthographicScore("eqergeq4"), 0.00001);
		assertEquals(1d/1.8, StringUtils.getOrthographicScore("eq2ergeq"), 0.00001);
		assertEquals(1d/1.8, StringUtils.getOrthographicScore("eq25ergeq"), 0.00001);
		assertEquals(1d/(1.8*1.8), StringUtils.getOrthographicScore("eq25erge2q"), 0.00001);
		assertEquals(0.70d/(1.8), StringUtils.getOrthographicScore("e25"), 0.00001);
		assertEquals(0.70d/1.8, StringUtils.getOrthographicScore("e2a"), 0.00001);
		assertEquals(0.95d/1.8/2, StringUtils.getOrthographicScore("e2a*"), 0.00001);

	}
	
	@Test
	public void nbDigits() {
		assertEquals(0, StringUtils.nbDigits("eqergeq"));
		assertEquals(1, StringUtils.nbDigits("eqerge2q"));
		assertEquals(2, StringUtils.nbDigits("eq34ergeq"));
		assertEquals(2, StringUtils.nbDigits("eqe2rg4eq"));
	}


}
