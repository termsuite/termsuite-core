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
		this.term1 = this.termIndex.getTermByGroupingKey(Fixtures.term1().getGroupingKey());
		this.term2 = this.termIndex.getTermByGroupingKey(Fixtures.term2().getGroupingKey());
		this.term3 = this.termIndex.getTermByGroupingKey(Fixtures.term3().getGroupingKey());
		this.term4 = this.termIndex.getTermByGroupingKey(Fixtures.term4().getGroupingKey());
		this.term5 = this.termIndex.getTermByGroupingKey(Fixtures.term5().getGroupingKey());

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

		term5.addSyntacticVariant(term3, "NA-NAPN");
		term3.addSyntacticVariant(term4, "NA-NAPN2");
		ae.collectionProcessComplete();
		assertThat(this.termIndex.getTerms()).hasSize(5).contains(term1, term2, term5);
		assertThat(this.termIndex.getTermClasses()).hasSize(3).extracting("head").contains(term1, term2, term5);

	}

}
