package fr.univnantes.termsuite.test.unit.engines.contextualizer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import org.junit.Before;
import org.junit.Test;

import fr.univnantes.termsuite.api.TermSuite;
import fr.univnantes.termsuite.engines.contextualizer.Contextualizer;
import fr.univnantes.termsuite.engines.contextualizer.ContextualizerOptions;
import fr.univnantes.termsuite.model.Lang;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.Terminology;
import fr.univnantes.termsuite.test.unit.Fixtures;
import fr.univnantes.termsuite.test.unit.UnitTests;

public class ContextualizerSpec {

	private Term termWithContext1;
	private Term termWithContext2;
	private Term termWithContext3;
	private Contextualizer contextualizer;
	private Terminology termino;
	private ContextualizerOptions options;
	
	@Before
	public void setup() {
		options = TermSuite.getDefaultExtractorConfig(Lang.EN).getContextualizerOptions();
		termino = Fixtures.terminoWithOccurrences();
		termWithContext1 = termino.getTerms().get("n: énergie");
		termWithContext2 = termino.getTerms().get("a: éolien");
		termWithContext3 = termino.getTerms().get("n: accès");
		contextualizer = UnitTests.createSimpleEngine(termino, Contextualizer.class, options);
	}
	
	@Test
	public void computeContextVectorScope1() {
		options.setScope(1);
		options.setMinimumCooccFrequencyThreshold(1);
		contextualizer.execute();
		
		// T1 T2 T3 T1 T3 T3 T1

		assertThat(termWithContext1.getContext().getEntries())
			.hasSize(2)
			.extracting("coTerm.groupingKey", "nbCooccs")
			.contains(tuple("a: éolien", 1), tuple("n: accès", 3));

		assertThat(termWithContext2.getContext().getEntries())
			.hasSize(2)
			.extracting("coTerm.groupingKey", "nbCooccs")
			.contains(tuple("n: énergie", 1), tuple("n: accès", 1));

		assertThat(termWithContext3.getContext().getEntries())
			.hasSize(2)
			.extracting("coTerm.groupingKey", "nbCooccs")
			.contains(tuple("n: énergie", 3), tuple("a: éolien", 1));
	}

	@Test
	public void computeContextVectorScope3() {
		options.setScope(3);
		contextualizer.execute();

		// T1 T2 T3 T1 T3 T3 T1

		assertThat(termWithContext1.getContext().getEntries())
			.hasSize(2)
			.extracting("coTerm.groupingKey", "nbCooccs")
			.contains(tuple("a: éolien", 2), tuple("n: accès", 6));
	
		assertThat(termWithContext2.getContext().getEntries())
			.hasSize(2)
			.extracting("coTerm.groupingKey", "nbCooccs")
			.contains(tuple("n: énergie", 2), tuple("n: accès", 2));
	
		assertThat(termWithContext3.getContext().getEntries())
			.hasSize(2)
			.extracting("coTerm.groupingKey", "nbCooccs")
			.contains(tuple("n: énergie", 6), tuple("a: éolien", 2));
	}
}
