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

package org.ttc.project.test.engines;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Comparator;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.resource.ResourceInitializationException;
import org.junit.Before;
import org.junit.Test;
import org.ttc.project.Fixtures;
import org.ttc.project.TestUtil;

import eu.project.ttc.engines.TermClassifier;
import eu.project.ttc.engines.cleaner.TermProperty;
import eu.project.ttc.models.Term;
import eu.project.ttc.models.VariationType;
import eu.project.ttc.models.index.MemoryTermIndex;

public class TermClassifierSpec {
	private MemoryTermIndex termIndex;
	private Term term1;
	private Term term2;
	private Term term3;
	private Term term4;
	private Term term5;
	Comparator<Term> reverseComp = TermProperty.FREQUENCY.getComparator(true);
	Comparator<Term> comp = TermProperty.FREQUENCY.getComparator(false);
	
	private AnalysisEngine ae;
	
	
	@Before
	public void set() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, ResourceInitializationException {
		this.termIndex = Fixtures.termIndex();
		this.term1 = Fixtures.term1(termIndex);
		this.term2 = Fixtures.term2(termIndex);
		this.term3 = Fixtures.term3(termIndex);
		this.term4 = Fixtures.term4(termIndex);
		this.term5 = Fixtures.term5(termIndex);

		this.term1.setFrequency(5);
		this.term2.setFrequency(4);
		this.term5.setFrequency(3);
		this.term3.setFrequency(2);
		this.term4.setFrequency(1);
		
		
		ae = TestUtil.createAE(
				this.termIndex,
				TermClassifier.class, 
				TermClassifier.CLASSIFYING_PROPERTY, TermProperty.FREQUENCY
			);

	}
	
	
	
	@Test
	public void testClassify() throws AnalysisEngineProcessException {
		assertThat(this.termIndex.getTerms()).hasSize(5).contains(term1, term2, term3, term4, term5);

		term5.addTermVariation(term3, VariationType.SYNTACTICAL, "NA-NAPN");
		term3.addTermVariation(term4, VariationType.SYNTACTICAL, "NA-NAPN2");
		ae.collectionProcessComplete();
		assertThat(this.termIndex.getTerms()).hasSize(5).contains(term1, term2, term5);
		assertThat(this.termIndex.getTermClasses()).hasSize(3).extracting("head").contains(term1, term2, term5);

	}

}
