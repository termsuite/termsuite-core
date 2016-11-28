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
package eu.project.ttc.test.unit.models;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import eu.project.ttc.test.unit.Fixtures;
import fr.univnantes.termsuite.engines.Contextualizer;
import fr.univnantes.termsuite.metrics.LogLikelihood;
import fr.univnantes.termsuite.metrics.MutualInformation;
import fr.univnantes.termsuite.model.CrossTable;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.TermIndex;

public class CrossTableSpec {

	private TermIndex termIndex;
	private CrossTable crossTable;

	private Term t1;
	private Term t2;
	private Term t3;
	
	@Before
	public void init() {

		this.termIndex = Fixtures.termIndexWithOccurrences();
		
		t1 = this.termIndex.getTermByGroupingKey("n: énergie");
		t2 = this.termIndex.getTermByGroupingKey("a: éolien");
		t3 = this.termIndex.getTermByGroupingKey("n: accès");
		
		// T1 T2 T3 T1 T3 T3 T1
		new Contextualizer(termIndex)
				.setScope(1)
				.contextualize();
		
		this.crossTable = new CrossTable(this.termIndex);
	}
	
	@Test
	public void testA() {
		// T1 T2 T3 T1 T3 T3 T1

		// the number of times T1 occurs with T2
		this.crossTable.computeRate(new LogLikelihood(), t1, t2);
		assertEquals(1d, this.crossTable.getLastA(), 0.001);
		this.crossTable.computeRate(new LogLikelihood(), t2, t1);
		assertEquals(1d, this.crossTable.getLastA(), 0.001);

		// the number of times T1 occurs with T3
		this.crossTable.computeRate(new LogLikelihood(), t1, t3);
		assertEquals(3d, this.crossTable.getLastA(), 0.001);
		this.crossTable.computeRate(new LogLikelihood(), t3, t1);
		assertEquals(3d, this.crossTable.getLastA(), 0.001);

		// the number of times T2 occurs with T3
		this.crossTable.computeRate(new LogLikelihood(), t2, t3);
		assertEquals(1d, this.crossTable.getLastA(), 0.001);
		this.crossTable.computeRate(new LogLikelihood(), t3, t2);
		assertEquals(1d, this.crossTable.getLastA(), 0.001);

	}

	@Test
	public void testB() {
		// T1 T2 T3 T1 T3 T3 T1

		// the number of times T1 occurs without T2
		this.crossTable.computeRate(new LogLikelihood(), t1, t2);
		assertEquals(3d, this.crossTable.getLastB(), 0.001);
		
		// the number of times T2 occurs without T1
		this.crossTable.computeRate(new LogLikelihood(), t2, t1);
		assertEquals(1d, this.crossTable.getLastB(), 0.001);
		
		// the number of times T1 occurs without T3
		this.crossTable.computeRate(new LogLikelihood(), t1, t3);
		assertEquals(1d, this.crossTable.getLastB(), 0.001);

		// the number of times T3 occurs without T1
		this.crossTable.computeRate(new LogLikelihood(), t3, t1);
		assertEquals(1d, this.crossTable.getLastB(), 0.001);

		// the number of times T2 occurs without T3
		this.crossTable.computeRate(new LogLikelihood(), t2, t3);
		assertEquals(1d, this.crossTable.getLastB(), 0.001);

		// the number of times T3 occurs without T2
		this.crossTable.computeRate(new LogLikelihood(), t3, t2);
		assertEquals(3d, this.crossTable.getLastB(), 0.001);
	}

	@Test
	public void testC() {
		// T1 T2 T3 T1 T3 T3 T1

		// the number of times T2 is a co-occurrence but not with T1
		this.crossTable.computeRate(new LogLikelihood(), t1, t2);
		assertEquals(1d, this.crossTable.getLastC(), 0.001);

		// the number of times T1 is a co-occurrence but not with T2
		this.crossTable.computeRate(new LogLikelihood(), t2, t1);
		assertEquals(3d, this.crossTable.getLastC(), 0.001);

		// the number of times T3 is a co-occurrence but not with T1
		this.crossTable.computeRate(new LogLikelihood(), t1, t3);
		assertEquals(1d, this.crossTable.getLastC(), 0.001);

		// the number of times T1 is a co-occurrence but not with T3
		this.crossTable.computeRate(new LogLikelihood(), t3, t1);
		assertEquals(1d, this.crossTable.getLastC(), 0.001);

		// the number of times T3 is a co-occurrence but not with T2
		this.crossTable.computeRate(new LogLikelihood(), t2, t3);
		assertEquals(3d, this.crossTable.getLastC(), 0.001);

		// the number of times T2 is a co-occurrence but not with T3
		this.crossTable.computeRate(new LogLikelihood(), t3, t2);
		assertEquals(1d, this.crossTable.getLastC(), 0.001);
}

	@Test
	public void testD() {
		// T1 T2 T3 T1 T3 T3 T1

		// the number of times neither T1 nor T2 is the co-occurrence of something else
		this.crossTable.computeRate(new LogLikelihood(), t1, t2);
		assertEquals(5d, this.crossTable.getLastD(), 0.001);
		this.crossTable.computeRate(new LogLikelihood(), t2, t1);
		assertEquals(5d, this.crossTable.getLastD(), 0.001);

		// the number of times neither T1 nor T3 is the co-occurrence of something else
		this.crossTable.computeRate(new LogLikelihood(), t1, t3);
		assertEquals(5d, this.crossTable.getLastD(), 0.001);
		this.crossTable.computeRate(new LogLikelihood(), t3, t1);
		assertEquals(5d, this.crossTable.getLastD(), 0.001);

		// the number of times neither T2 nor T3 is the co-occurrence of something else
		this.crossTable.computeRate(new LogLikelihood(), t2, t3);
		assertEquals(5d, this.crossTable.getLastD(), 0.001);
		this.crossTable.computeRate(new LogLikelihood(), t3, t2);
		assertEquals(5d, this.crossTable.getLastD(), 0.001);
	}

	@Test
	public void testComputeRateLogLikelihood() {
		logLikelihood(0.051, t1, t2);
		logLikelihood(0.051, t2, t1);
		logLikelihood(1.777, t1, t3);
		logLikelihood(1.777, t3, t1);
		logLikelihood(0.051, t2, t3);
		logLikelihood(0.051, t3, t2);
	}

	private void logLikelihood(double expected, Term t12, Term t22) {
		assertEquals(expected, this.crossTable.computeRate(new LogLikelihood(), t12, t22), .001);
	}

	private void mutualInfo(double expected, Term t12, Term t22) {
		assertEquals(expected, this.crossTable.computeRate(new MutualInformation(), t12, t22), .001);
	}

	@Test
	public void testComputeRateMutualInfo() {
		mutualInfo(0.223, t1, t2);
		mutualInfo(0.223, t2, t1);
		mutualInfo(0.629, t1, t3);
		mutualInfo(0.629, t3, t1);
		mutualInfo(0.223, t2, t3);
		mutualInfo(0.223, t3, t2);
	}
}
