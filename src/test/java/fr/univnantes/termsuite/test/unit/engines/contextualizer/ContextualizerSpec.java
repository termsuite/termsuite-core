package fr.univnantes.termsuite.test.unit.engines.contextualizer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import org.junit.Before;
import org.junit.Test;

import fr.univnantes.termsuite.api.TermSuite;
import fr.univnantes.termsuite.engines.contextualizer.Contextualizer;
import fr.univnantes.termsuite.engines.contextualizer.ContextualizerOptions;
import fr.univnantes.termsuite.framework.TermSuiteFactory;
import fr.univnantes.termsuite.index.Terminology;
import fr.univnantes.termsuite.model.IndexedCorpus;
import fr.univnantes.termsuite.model.Lang;
import fr.univnantes.termsuite.model.OccurrenceStore;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.TermBuilder;
import fr.univnantes.termsuite.model.Word;
import fr.univnantes.termsuite.test.unit.UnitTests;

public class ContextualizerSpec {

	private Word w1, w2, w3;
	private Term t1, t2, t3;
	private Contextualizer contextualizer;
	private OccurrenceStore store;
	private ContextualizerOptions options;
	
	@Before
	public void setup() {
		options = TermSuite.getDefaultExtractorConfig(Lang.EN).getContextualizerOptions();
		IndexedCorpus corpus = terminoWithOccurrences();
		contextualizer = UnitTests.createSimpleEngine(corpus, Contextualizer.class, options);
	}
	
	
	public IndexedCorpus terminoWithOccurrences() {
		IndexedCorpus indexedCorpus = TermSuiteFactory.createIndexedCorpus(Lang.FR, "");
		Terminology memoryTermino = indexedCorpus.getTerminology();
		store = indexedCorpus.getOccurrenceStore();
		
		String form = "blabla";
		
		w1 = new Word("w1", "w1");
		w2 = new Word("w2", "w2");
		w3 = new Word("w3", "w3");
		
		t1 = TermBuilder.start(memoryTermino).setGroupingKey("t1").addWord(w1, "N").create();
		UnitTests.addTerm(memoryTermino, t1);
		store.addOccurrence(t1, "url", 0, 10, form);
		store.addOccurrence(t1, "url", 31, 40, form);
		store.addOccurrence(t1, "url", 61, 70, form);
		
		t2 = TermBuilder.start(memoryTermino).setGroupingKey("t2").addWord(w2, "A").create();
		UnitTests.addTerm(memoryTermino, t2);
		store.addOccurrence(t2, "url", 11, 20, form);
		
		t3 = TermBuilder.start(memoryTermino).setGroupingKey("t3").addWord(w3, "N").create();
		UnitTests.addTerm(memoryTermino, t3);
		store.addOccurrence(t3, "url", 21, 30, form);
		store.addOccurrence(t3, "url", 41, 50, form);
		store.addOccurrence(t3, "url", 51, 60, form);
	
		return indexedCorpus;
	
	}

	
	@Test
	public void computeContextVectorScope1() {
		options.setScope(1);
		options.setMinimumCooccFrequencyThreshold(1);
		contextualizer.execute();
		
		// T1 T2 T3 T1 T3 T3 T1
		assertThat(t3.getContext().getEntries())
			.extracting("coTerm.groupingKey", "nbCooccs")
			.contains(tuple("t1", 3), tuple("t2", 1))
			.hasSize(2)
			;

		assertThat(t2.getContext().getEntries())
			.extracting("coTerm.groupingKey", "nbCooccs")
			.contains(tuple("t1", 1), tuple("t3", 1))
			.hasSize(2)
			;
		
		assertThat(t1.getContext().getEntries())
			.extracting("coTerm.groupingKey", "nbCooccs")
			.contains(tuple("t2", 1), tuple("t3", 3))
			.hasSize(2)
			;


	}

	@Test
	public void computeContextVectorScope3() {
		options.setScope(3);
		contextualizer.execute();

		// T1 T2 T3 T1 T3 T3 T1

		assertThat(t1.getContext().getEntries())
			.hasSize(2)
			.extracting("coTerm.groupingKey", "nbCooccs")
			.contains(tuple("t2", 2), tuple("t3", 6));
	
		assertThat(t2.getContext().getEntries())
			.hasSize(2)
			.extracting("coTerm.groupingKey", "nbCooccs")
			.contains(tuple("t1", 2), tuple("t3", 2));
	
		assertThat(t3.getContext().getEntries())
			.hasSize(2)
			.extracting("coTerm.groupingKey", "nbCooccs")
			.contains(tuple("t1", 6), tuple("t2", 2));
	}
}
