package fr.univnantes.termsuite.test.unit.engines;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import org.junit.Before;
import org.junit.Test;

import fr.univnantes.termsuite.engines.contextualizer.Contextualizer;
import fr.univnantes.termsuite.engines.contextualizer.ContextualizerOptions;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.TermIndex;
import fr.univnantes.termsuite.test.unit.Fixtures;

public class ContextualizerSpec {

	private Term termWithContext1;
	private Term termWithContext2;
	private Term termWithContext3;
	private Contextualizer contextualizer;
	private TermIndex termIndex;
	
	
	@Before
	public void setup() {
		termIndex = Fixtures.termIndexWithOccurrences();
		termWithContext1 = termIndex.getTermByGroupingKey("n: énergie");
		termWithContext2 = termIndex.getTermByGroupingKey("a: éolien");
		termWithContext3 = termIndex.getTermByGroupingKey("n: accès");
		contextualizer = new Contextualizer();
	}
	
	@Test
	public void computeContextVectorScope1() {
		contextualizer.setOptions(new ContextualizerOptions().setScope(1)).contextualize(termIndex);
		
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
		contextualizer.setOptions(new ContextualizerOptions().setScope(3)).contextualize(termIndex);
		
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
