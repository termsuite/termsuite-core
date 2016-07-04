package eu.project.ttc.test.unit.engines;

import static org.junit.Assert.fail;

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

import eu.project.ttc.engines.FixedExpressionSpotter;
import eu.project.ttc.readers.TermSuiteJsonCasDeserializer;
import eu.project.ttc.resources.FixedExpressionResource;
import eu.project.ttc.test.TermSuiteAssertions;
import eu.project.ttc.types.FixedExpression;
import eu.project.ttc.types.TermOccAnnotation;
import eu.project.ttc.types.WordAnnotation;

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
