
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

package eu.project.ttc.test.unit.engines;

import java.io.FileInputStream;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.ExternalResourceFactory;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ExternalResourceDescription;
import org.junit.Before;
import org.junit.Test;

import eu.project.ttc.test.TermSuiteAssertions;
import fr.univnantes.termsuite.types.FixedExpression;
import fr.univnantes.termsuite.types.TermOccAnnotation;
import fr.univnantes.termsuite.types.WordAnnotation;
import fr.univnantes.termsuite.uima.engines.preproc.FixedExpressionSpotter;
import fr.univnantes.termsuite.uima.readers.TermSuiteJsonCasDeserializer;
import fr.univnantes.termsuite.uima.resources.preproc.FixedExpressionResource;

public class FixedExpressionSpotterSpec {

	private static final String CAS_URL = "src/test/resources/org/project/ttc/test/termsuite/json/cas/french-cas1.json";
	JCas cas;
	
	@Before
	public void set() throws Exception {
		cas = JCasFactory.createJCas();
		TermSuiteJsonCasDeserializer.deserialize(new FileInputStream(CAS_URL), cas.getCas());
	}
	
	private AnalysisEngine makeAE(boolean removeWordAnnotationFromCas, boolean removeTermOccAnnotationFromCas) throws Exception {
		AnalysisEngineDescription aeDesc = AnalysisEngineFactory.createEngineDescription(
				FixedExpressionSpotter.class,
				FixedExpressionSpotter.FIXED_EXPRESSION_MAX_SIZE, 5,
				FixedExpressionSpotter.REMOVE_WORD_ANNOTATIONS_FROM_CAS, removeWordAnnotationFromCas,
				FixedExpressionSpotter.REMOVE_TERM_OCC_ANNOTATIONS_FROM_CAS, removeTermOccAnnotationFromCas
			);
		
		/*
		 * The term index resource
		 */
		ExternalResourceDescription fixedExpressionDesc = ExternalResourceFactory.createExternalResourceDescription(
				FixedExpressionResource.FIXED_EXPRESSION_RESOURCE,
				FixedExpressionResource.class, 
				"file:org/project/ttc/test/resources/french-fixed-expressions.txt"
		);
		ExternalResourceFactory.bindResource(aeDesc, fixedExpressionDesc);

		AnalysisEngine ae = AnalysisEngineFactory.createEngine(aeDesc);
		return ae;
	}
	
	@Test
	public void testNoRemove() throws Exception {
		AnalysisEngine ae = makeAE(false, false);
		TermSuiteAssertions.assertThat(cas)
				.doesNotContainAnnotation(FixedExpression.class)
				.containsAnnotation(WordAnnotation.class, 0, 8)
				.containsAnnotation(WordAnnotation.class, 9, 11)
				.containsAnnotation(WordAnnotation.class, 12, 20)
				.containsAnnotation(WordAnnotation.class, 21, 24)
				.containsAnnotation(WordAnnotation.class, 25, 28)
				.containsAnnotation(WordAnnotation.class, 29, 36)
				.containsAnnotation(WordAnnotation.class, 37, 39)
				.containsAnnotation(WordAnnotation.class, 39,42)
				.containsAnnotation(TermOccAnnotation.class, 0, 20)
				.containsAnnotation(TermOccAnnotation.class, 0, 8)
				.containsAnnotation(TermOccAnnotation.class, 12, 20)
				.containsAnnotation(TermOccAnnotation.class, 29, 42)
				.containsAnnotation(TermOccAnnotation.class, 29, 36)
				.containsAnnotation(TermOccAnnotation.class, 39, 42)
		;

		ae.process(cas);
		TermSuiteAssertions.assertThat(cas)
				.containsAnnotation(FixedExpression.class,29 , 42)
				.containsAnnotation(WordAnnotation.class, 0, 8)
				.containsAnnotation(WordAnnotation.class, 9, 11)
				.containsAnnotation(WordAnnotation.class, 12, 20)
				.containsAnnotation(WordAnnotation.class, 21, 24)
				.containsAnnotation(WordAnnotation.class, 25, 28)
				.containsAnnotation(WordAnnotation.class, 29, 36)
				.containsAnnotation(WordAnnotation.class, 37, 39)
				.containsAnnotation(WordAnnotation.class, 39,42)
				.containsAnnotation(TermOccAnnotation.class, 0, 20)
				.containsAnnotation(TermOccAnnotation.class, 0, 8)
				.containsAnnotation(TermOccAnnotation.class, 12, 20)
				.containsAnnotation(TermOccAnnotation.class, 29, 42)
				.containsAnnotation(TermOccAnnotation.class, 29, 36)
				.containsAnnotation(TermOccAnnotation.class, 39, 42)
		;
	}

	@Test
	public void testRemoveTermOcc() throws Exception {
		AnalysisEngine ae = makeAE(false, true);
		TermSuiteAssertions.assertThat(cas)
				.doesNotContainAnnotation(FixedExpression.class)
				.containsAnnotation(WordAnnotation.class, 0, 8)
				.containsAnnotation(WordAnnotation.class, 9, 11)
				.containsAnnotation(WordAnnotation.class, 12, 20)
				.containsAnnotation(WordAnnotation.class, 21, 24)
				.containsAnnotation(WordAnnotation.class, 25, 28)
				.containsAnnotation(WordAnnotation.class, 29, 36)
				.containsAnnotation(WordAnnotation.class, 37, 39)
				.containsAnnotation(WordAnnotation.class, 39,42)
				.containsAnnotation(TermOccAnnotation.class, 0, 20)
				.containsAnnotation(TermOccAnnotation.class, 0, 8)
				.containsAnnotation(TermOccAnnotation.class, 12, 20)
				.containsAnnotation(TermOccAnnotation.class, 29, 42)
				.containsAnnotation(TermOccAnnotation.class, 29, 36)
				.containsAnnotation(TermOccAnnotation.class, 39, 42)
		;

		ae.process(cas);
		TermSuiteAssertions.assertThat(cas)
				.containsAnnotation(FixedExpression.class, 29, 42)
				.containsAnnotation(WordAnnotation.class, 0, 8)
				.containsAnnotation(WordAnnotation.class, 9, 11)
				.containsAnnotation(WordAnnotation.class, 12, 20)
				.containsAnnotation(WordAnnotation.class, 21, 24)
				.containsAnnotation(WordAnnotation.class, 25, 28)
				.containsAnnotation(WordAnnotation.class, 29, 36)
				.containsAnnotation(WordAnnotation.class, 37, 39)
				.containsAnnotation(WordAnnotation.class, 39,42)
				.containsAnnotation(TermOccAnnotation.class, 0, 20)
				.containsAnnotation(TermOccAnnotation.class, 0, 8)
				.containsAnnotation(TermOccAnnotation.class, 12, 20)
				.containsAnnotation(TermOccAnnotation.class, 29, 42)
				.doesNotContainAnnotation(TermOccAnnotation.class, 29, 36)
				.doesNotContainAnnotation(TermOccAnnotation.class, 39, 42)
		;
	}

	@Test
	public void testRemoveWord() throws Exception {
		AnalysisEngine ae = makeAE(true, false);
		TermSuiteAssertions.assertThat(cas)
				.doesNotContainAnnotation(FixedExpression.class)
				.containsAnnotation(WordAnnotation.class, 0, 8)
				.containsAnnotation(WordAnnotation.class, 9, 11)
				.containsAnnotation(WordAnnotation.class, 12, 20)
				.containsAnnotation(WordAnnotation.class, 21, 24)
				.containsAnnotation(WordAnnotation.class, 25, 28)
				.containsAnnotation(WordAnnotation.class, 29, 36)
				.containsAnnotation(WordAnnotation.class, 37, 39)
				.containsAnnotation(WordAnnotation.class, 39,42)
				.containsAnnotation(TermOccAnnotation.class, 0, 20)
				.containsAnnotation(TermOccAnnotation.class, 0, 8)
				.containsAnnotation(TermOccAnnotation.class, 12, 20)
				.containsAnnotation(TermOccAnnotation.class, 29, 42)
				.containsAnnotation(TermOccAnnotation.class, 29, 36)
				.containsAnnotation(TermOccAnnotation.class, 39, 42)
		;

		ae.process(cas);
		TermSuiteAssertions.assertThat(cas)
				.containsAnnotation(FixedExpression.class, 29, 42)
				.containsAnnotation(WordAnnotation.class, 0, 8)
				.containsAnnotation(WordAnnotation.class, 9, 11)
				.containsAnnotation(WordAnnotation.class, 12, 20)
				.containsAnnotation(WordAnnotation.class, 21, 24)
				.containsAnnotation(WordAnnotation.class, 25, 28)
				.doesNotContainAnnotation(WordAnnotation.class, 29, 36)
				.doesNotContainAnnotation(WordAnnotation.class, 37, 39)
				.doesNotContainAnnotation(WordAnnotation.class, 39,42)
				.containsAnnotation(TermOccAnnotation.class, 0, 20)
				.containsAnnotation(TermOccAnnotation.class, 0, 8)
				.containsAnnotation(TermOccAnnotation.class, 12, 20)
				.containsAnnotation(TermOccAnnotation.class, 29, 42)
				.containsAnnotation(TermOccAnnotation.class, 29, 36)
				.containsAnnotation(TermOccAnnotation.class, 39, 42)
		;

	}

	@Test
	public void testRemoveTermOccAndWord() throws Exception {
		AnalysisEngine ae = makeAE(true, true);
		TermSuiteAssertions.assertThat(cas)
				.doesNotContainAnnotation(FixedExpression.class)
				.containsAnnotation(WordAnnotation.class, 0, 8)
				.containsAnnotation(WordAnnotation.class, 9, 11)
				.containsAnnotation(WordAnnotation.class, 12, 20)
				.containsAnnotation(WordAnnotation.class, 21, 24)
				.containsAnnotation(WordAnnotation.class, 25, 28)
				.containsAnnotation(WordAnnotation.class, 29, 36)
				.containsAnnotation(WordAnnotation.class, 37, 39)
				.containsAnnotation(WordAnnotation.class, 39,42)
				.containsAnnotation(TermOccAnnotation.class, 0, 20)
				.containsAnnotation(TermOccAnnotation.class, 0, 8)
				.containsAnnotation(TermOccAnnotation.class, 12, 20)
				.containsAnnotation(TermOccAnnotation.class, 29, 42)
				.containsAnnotation(TermOccAnnotation.class, 29, 36)
				.containsAnnotation(TermOccAnnotation.class, 39, 42)
		;

		ae.process(cas);
		TermSuiteAssertions.assertThat(cas)
				.containsAnnotation(FixedExpression.class, 29, 42)
				.containsAnnotation(WordAnnotation.class, 0, 8)
				.containsAnnotation(WordAnnotation.class, 9, 11)
				.containsAnnotation(WordAnnotation.class, 12, 20)
				.containsAnnotation(WordAnnotation.class, 21, 24)
				.containsAnnotation(WordAnnotation.class, 25, 28)
				.doesNotContainAnnotation(WordAnnotation.class, 29, 36)
				.doesNotContainAnnotation(WordAnnotation.class, 37, 39)
				.doesNotContainAnnotation(WordAnnotation.class, 39,42)
				.containsAnnotation(TermOccAnnotation.class, 0, 20)
				.containsAnnotation(TermOccAnnotation.class, 0, 8)
				.containsAnnotation(TermOccAnnotation.class, 12, 20)
				.containsAnnotation(TermOccAnnotation.class, 29, 42)
				.doesNotContainAnnotation(TermOccAnnotation.class, 29, 36)
				.doesNotContainAnnotation(TermOccAnnotation.class, 39, 42)
		;

	}
}
