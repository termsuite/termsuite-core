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

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.assertj.core.groups.Tuple;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

import fr.univnantes.termsuite.api.TermSuite;
import fr.univnantes.termsuite.engines.contextualizer.ContextData;
import fr.univnantes.termsuite.engines.contextualizer.Contextualizer;
import fr.univnantes.termsuite.engines.contextualizer.ContextualizerOptions;
import fr.univnantes.termsuite.engines.contextualizer.CrossTable;
import fr.univnantes.termsuite.engines.contextualizer.LogLikelihood;
import fr.univnantes.termsuite.engines.contextualizer.MutualInformation;
import fr.univnantes.termsuite.model.Lang;
import fr.univnantes.termsuite.model.OccurrenceStore;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.TermBuilder;
import fr.univnantes.termsuite.model.TermOccurrence;
import fr.univnantes.termsuite.model.Terminology;
import fr.univnantes.termsuite.model.Word;
import fr.univnantes.termsuite.model.occurrences.MemoryOccurrenceStore;
import fr.univnantes.termsuite.model.termino.MemoryTerminology;
import fr.univnantes.termsuite.test.unit.UnitTests;

public class ContextualizerSpec2 {

	private OccurrenceStore occurrenceStore;
	private Terminology termino;
	private CrossTable crossTable;
	private Contextualizer contextualizer;
	private Term t1;
	private Term t2;
	private Term t3;
	
	@Before
	public void init() {

		occurrenceStore = new MemoryOccurrenceStore(Lang.FR);
		this.termino = new MemoryTerminology("e", Lang.FR, occurrenceStore);
		
		t1 = addTerm("T1");
		addOcc(t1, 0, 10);
		addOcc(t1, 31, 40);
		addOcc(t1, 61, 70);
		
		t2 = addTerm("T2");
		addOcc(t2, 11, 20);
		
		t3 = addTerm("T3");
		addOcc(t3, 21, 30);
		addOcc(t3, 41, 50);
		addOcc(t3, 51, 60);

		
		// T1 T2 T3 T1 T3 T3 T1
		ContextualizerOptions options = TermSuite.getDefaultExtractorConfig(Lang.FR).getContextualizerOptions();
		options.setMinimumCooccFrequencyThreshold(1);
		options.setScope(1);
		contextualizer = UnitTests.createEngine(
				termino, 
				Contextualizer.class, 
				options);
		
		contextualizer.computeDocumentViews();
		contextualizer.setContexts();
		crossTable = contextualizer.computeCrossTable();
	}

	public void addOcc(Term t, int begin, int end) {
		occurrenceStore.addOccurrence(t, "doc", begin, end, t.getGroupingKey().toLowerCase());
	}

	public Term addTerm(String gKey) {
		Word word = new Word(gKey, gKey);
		Term term = TermBuilder.start().setGroupingKey(gKey).addWord(word, "N", true).create();
		termino.addTerm(term);
		return term;
	}
	
	@Test
	public void testContextIterator() {
		// T1 T2 T3 T1 T3 T3 T1
		contextualizer.computeDocumentViews();

		assertThat(contexts(t1, 1))
			.contains(
					tuple("t2"),
					tuple("t3", "t3"),
					tuple("t3")
				);

		assertThat(contexts(t2, 1))
			.contains(
				tuple("t1", "t3")
			);

		assertThat(contexts(t3, 1))
			.contains(
				tuple("t2", "t1"),
				tuple("t1", "t3"),
				tuple("t3", "t1")
			);
	
}
	
	private List<Tuple> contexts(Term t, int scope) {
		Iterator<Iterator<TermOccurrence>> contextIterator = contextualizer.contextIterator(t, scope);
		List<Tuple> tuples = new ArrayList<>();
		for(Iterator<TermOccurrence> context:Lists.newArrayList(contextIterator)) {
			List<String> occurrences = 
					Lists.newArrayList(context)
					 .stream()
					 .map(TermOccurrence::getCoveredText)
					 .collect(toList());
			tuples.add(tuple(occurrences.toArray()));
		}
		return tuples;
	}

	@Test
	public void testA() {
		// T1 T2 T3 T1 T3 T3 T1
		ContextData data;
		
		// the number of times T1 occurs with T2
		data = this.contextualizer.computeContextData(this.crossTable, t1, t2);
		assertEquals(1d, data.getA(), 0.001);
		data = this.contextualizer.computeContextData(this.crossTable, t2, t1);
		assertEquals(1d, data.getA(), 0.001);

		// the number of times T1 occurs with T3
		data = this.contextualizer.computeContextData(this.crossTable, t1, t3);
		assertEquals(3d, data.getA(), 0.001);
		data = this.contextualizer.computeContextData(this.crossTable, t3, t1);
		assertEquals(3d, data.getA(), 0.001);

		// the number of times T2 occurs with T3
		data = this.contextualizer.computeContextData(this.crossTable, t2, t3);
		assertEquals(1d, data.getA(), 0.001);
		data = this.contextualizer.computeContextData(this.crossTable, t3, t2);
		assertEquals(1d, data.getA(), 0.001);
	}

	@Test
	public void testB() {
		// T1 T2 T3 T1 T3 T3 T1
		ContextData data;

		// the number of times T1 occurs without T2
		data = this.contextualizer.computeContextData(this.crossTable, t1, t2);
		assertEquals(3d, data.getB(), 0.001);
		
		// the number of times T2 occurs without T1
		data = this.contextualizer.computeContextData(this.crossTable, t2, t1);
		assertEquals(1d, data.getB(), 0.001);
		
		// the number of times T1 occurs without T3
		data = this.contextualizer.computeContextData(this.crossTable, t1, t3);
		assertEquals(1d, data.getB(), 0.001);

		// the number of times T3 occurs without T1
		data = this.contextualizer.computeContextData(this.crossTable, t3, t1);
		assertEquals(1d, data.getB(), 0.001);

		// the number of times T2 occurs without T3
				this.contextualizer.computeContextData(this.crossTable, t2, t3);
		assertEquals(1d, data.getB(), 0.001);

		// the number of times T3 occurs without T2
		data = this.contextualizer.computeContextData(this.crossTable, t3, t2);
		assertEquals(3d, data.getB(), 0.001);
	}

	@Test
	public void testC() {
		// T1 T2 T3 T1 T3 T3 T1
		ContextData data;

		// the number of times T2 is a co-occurrence but not with T1
		data = this.contextualizer.computeContextData(this.crossTable, t1, t2);
		assertEquals(1d, data.getC(), 0.001);

		// the number of times T1 is a co-occurrence but not with T2
		data = this.contextualizer.computeContextData(this.crossTable, t2, t1);
		assertEquals(3d, data.getC(), 0.001);

		// the number of times T3 is a co-occurrence but not with T1
		data = this.contextualizer.computeContextData(this.crossTable, t1, t3);
		assertEquals(1d, data.getC(), 0.001);

		// the number of times T1 is a co-occurrence but not with T3
		data = this.contextualizer.computeContextData(this.crossTable, t3, t1);
		assertEquals(1d, data.getC(), 0.001);

		// the number of times T3 is a co-occurrence but not with T2
		data = this.contextualizer.computeContextData(this.crossTable, t2, t3);
		assertEquals(3d, data.getC(), 0.001);

		// the number of times T2 is a co-occurrence but not with T3
		data = this.contextualizer.computeContextData(this.crossTable, t3, t2);
		assertEquals(1d, data.getC(), 0.001);
}

	@Test
	public void testD() {
		// T1 T2 T3 T1 T3 T3 T1
		ContextData data;

		// the number of times neither T1 nor T2 is the co-occurrence of something else
		data = this.contextualizer.computeContextData(this.crossTable, t1, t2);
		assertEquals(5d, data.getD(), 0.001);
		data = this.contextualizer.computeContextData(this.crossTable, t2, t1);
		assertEquals(5d, data.getD(), 0.001);

		// the number of times neither T1 nor T3 is the co-occurrence of something else
		data = this.contextualizer.computeContextData(this.crossTable, t1, t3);
		assertEquals(5d, data.getD(), 0.001);
		data = this.contextualizer.computeContextData(this.crossTable, t3, t1);
		assertEquals(5d, data.getD(), 0.001);

		// the number of times neither T2 nor T3 is the co-occurrence of something else
		data = this.contextualizer.computeContextData(this.crossTable, t2, t3);
		assertEquals(5d, data.getD(), 0.001);
		data = this.contextualizer.computeContextData(this.crossTable, t3, t2);
		assertEquals(5d, data.getD(), 0.001);
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
		ContextData data = this.contextualizer.computeContextData(this.crossTable, t12, t22);
		assertEquals(expected, new LogLikelihood().getValue(data), .001);
	}

	private void mutualInfo(double expected, Term t12, Term t22) {
		ContextData data = this.contextualizer.computeContextData(this.crossTable, t12, t22);
		assertEquals(expected, new MutualInformation().getValue(data), .001);
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
