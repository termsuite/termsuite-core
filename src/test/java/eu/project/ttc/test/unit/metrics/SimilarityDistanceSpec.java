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
package eu.project.ttc.test.unit.metrics;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

import eu.project.ttc.metrics.Cosine;
import eu.project.ttc.metrics.Jaccard;
import eu.project.ttc.models.ContextVector;
import eu.project.ttc.models.Term;
import eu.project.ttc.test.unit.Fixtures;

public class SimilarityDistanceSpec {

	private Term term1;
	private Term term2;
	private Term term3;
	private Term term4;
	
	private ContextVector v1;
	private ContextVector v2;
	private ContextVector v3;
	private ContextVector v4;
	private ContextVector v5;
	private ContextVector v6;
	private ContextVector v7;

	private Cosine cosine;
	private Jaccard jaccard;
	
	@Before
	public void setTerms() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		this.term1 = Fixtures.term1();
		this.term2 = Fixtures.term2();
		this.term3 = Fixtures.term3();
		this.term4 = Fixtures.term4();
		this.v1 = new ContextVector();
		this.v1.addEntry(term1, 1234, 1d);
		
		this.v2 = new ContextVector();
		this.v2.addEntry(term2, 1234, 1d);
		this.v3 = new ContextVector();
		this.v3.addEntry(term3, 1234, 1d);
		this.v4 = new ContextVector();
		this.v4.addEntry(term1, 1234, 2d);
		this.v4.addEntry(term2, 1234, 1d);
		this.v5 = new ContextVector();
		this.v5.addEntry(term1, 1234, 1d);
		this.v5.addEntry(term3, 1234, 3d);
		this.v6 = new ContextVector();
		this.v6.addEntry(term1, 1234, 1d);
		this.v6.addEntry(term2, 1234, 1d);
		this.v7 = new ContextVector();
		this.v7.addEntry(term2, 1234, 1d);
		this.v7.addEntry(term4, 1234, 3d);
		cosine = new Cosine();
		jaccard = new Jaccard();
	}
	
	@Test
	public void testJaccard() {
		// jac = 1 for same vectors
		jac(v1, v1, 1d);
		jac(v2, v2, 1d);
		jac(v3, v3, 1d);
		jac(v4, v4, 1d);
		jac(v5, v5, 1d);
		jac(v6, v6, 1d);
		jac(v7, v7, 1d);

		// v1
		jac(v1, v2, 0d);
		jac(v1, v3, 0d);
		jac(v1, v4, (1d + 0d) / (2d + 1d));
		jac(v1, v5, (1d + 0d) / (1d + 3d));
		jac(v1, v6, (1d + 0d) / (1d + 1d));
		jac(v1, v7, 0d);
		
		// v2
		jac(v2, v3, 0d);
		jac(v2, v4, (1d + 0d) / (2d + 1d));
		jac(v2, v5, 0d);
		jac(v2, v6, (1d + 0d) / (1d + 1d));
		jac(v2, v7, (1d + 0d) / (3d + 1d));
		
		// v3
		jac(v3, v4, 0d);
		jac(v3, v5, (1d + 0d) / (1d + 3d));
		jac(v3, v6, 0d);
		jac(v3, v7, 0d);

		// v4
		jac(v4, v5, (1d + 0d + 0d) / (2d + 1d + 3d));
		jac(v4, v6, (1d + 1d) / (2d + 1d));
		jac(v4, v7, (0d + 1d + 0d) / (2d + 3d + 1d));
		
		// v5
		jac(v5, v6, (1d + 0d + 0d) / (1d + 1d + 3d));
		jac(v5, v7, 0d);
		
		// v6
		jac(v6, v7, (0d + 1d + 0d) / (1d + 1d + 3d));
	}

	
	@Test
	public void testCosine() {
		// cos = 1 for same vectors
		cos(v1, v1, 1d);
		cos(v2, v2, 1d);
		cos(v3, v3, 1d);
		cos(v4, v4, 1d);
		cos(v5, v5, 1d);
		cos(v6, v6, 1d);
		cos(v7, v7, 1d);

		// v1
		cos(v1, v2, 0d);
		cos(v1, v3, 0d);
		cos(v1, v4, 1d*2d / Math.sqrt(1*1 * (2*2 + 1*1)));
		cos(v1, v5, 1d*1d / Math.sqrt(1*1 * (1*1 + 3*3)));
		cos(v1, v6, 1d*1d / Math.sqrt(1*1 * (1*1 + 1*1)));
		cos(v1, v7, 0d);
		
		// v2
		cos(v2, v3, 0d);
		cos(v2, v4, 1d*1d / Math.sqrt(1*1 * (2*2 + 1*1)));
		cos(v2, v5, 0d);
		cos(v2, v6, 1d*1d / Math.sqrt(1*1 * (1*1 + 1*1)));
		cos(v2, v7, 1d*1d / Math.sqrt(1*1 * (3*3 + 1*1)));
		
		// v3
		cos(v3, v4, 0d);
		cos(v3, v5, 1d*3d / Math.sqrt(1*1 * (3*3 + 1*1)));
		cos(v3, v6, 0d);
		cos(v3, v7, 0d);

		// v4
		cos(v4, v5, 2d*1d / Math.sqrt((2*2 + 1*1)* (1*1 + 3*3)));
		cos(v4, v6, (2d*1d  + 1d*1d)/ Math.sqrt((2*2 + 1*1)* (1*1 + 1*1)));
		cos(v4, v7, 1d*1d / Math.sqrt((2*2 + 1*1)* (1*1 + 3*3)));
		
		// v5
		cos(v5, v6, 1d*1d / Math.sqrt((1*1 + 3*3) * (1*1 + 1*1)));
		cos(v5, v7, 0d);
		
		// v6
		cos(v6, v7, 1d*1d / Math.sqrt((1*1 + 1*1) * (3*3 + 1*1)));
		
	}

	private void cos(ContextVector v1, ContextVector v2, Number expected) {
		assertThat(cosine.getValue(v1, v2)).isEqualTo(expected);
		assertThat(cosine.getValue(v2, v1)).isEqualTo(expected);
		assertThat(cosine.getValue(v2, v1)).isBetween(0d, 1d);
	}

	private void jac(ContextVector v1, ContextVector v2, Number expected) {
		assertThat(jaccard.getValue(v1, v2)).isEqualTo(expected);
		assertThat(jaccard.getValue(v2, v1)).isEqualTo(expected);
		assertThat(jaccard.getValue(v2, v1)).isBetween(0d, 1d);
	}



}
