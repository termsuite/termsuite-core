package eu.project.ttc.test.unit.selectors;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import eu.project.ttc.engines.desc.Lang;
import eu.project.ttc.models.Term;
import eu.project.ttc.models.TermIndex;
import eu.project.ttc.models.VariationType;
import eu.project.ttc.models.index.MemoryTermIndex;
import eu.project.ttc.models.index.selectors.HasSingleWordVariationSelector;
import eu.project.ttc.models.index.selectors.TermSelector;
import eu.project.ttc.models.occstore.MemoryOccurrenceStore;
import eu.project.ttc.test.unit.TermFactory;

public class HasSingleWordSelectorSpec {
	
	private Term term1;
	private Term term2;
	private Term term3;
	private Term term4;
	private Term term5;
	private Term term6;
	private Term term7;
	
	private TermIndex termIndex ;
	
	@Before
	public void init() {
		termIndex = new MemoryTermIndex("Test", Lang.FR, new MemoryOccurrenceStore());
		populateTermIndex(new TermFactory(termIndex));
	}
	
	private void populateTermIndex(TermFactory termFactory) {
		
		this.term1 = termFactory.create("N:machine|machin", "A:synchrone|synchro");
		this.term2 = termFactory.create("A:synchrone|synchron");
		this.term3 = termFactory.create("A:asynchrone|asynchron");
		
		this.term4 = termFactory.create("N:machine|machin", "A:statorique|statoric");
		this.term5 = termFactory.create("N:machine|machin", "P:de|de", "N:stator|stator");
		this.term6 = termFactory.create("A:statorique|statoric");
		this.term7 = termFactory.create("N:stator|stator");
		
		termFactory.addPrefix(this.term3, this.term2);
		termFactory.addDerivesInto("N A", this.term7, this.term6);
	}

	
	@Test
	public void testPrefix() {
		TermSelector selector = new HasSingleWordVariationSelector(VariationType.IS_PREFIX_OF);
		assertTrue(selector.select(termIndex, term1));
		assertTrue(selector.select(termIndex, term2));
		assertTrue(selector.select(termIndex, term3));
		assertFalse(selector.select(termIndex, term4));
		assertFalse(selector.select(termIndex, term5));
		assertFalse(selector.select(termIndex, term6));
		assertFalse(selector.select(termIndex, term7));
	}

	@Test
	public void testDerivation() {
		TermSelector selector = new HasSingleWordVariationSelector(VariationType.DERIVES_INTO);
		assertFalse(selector.select(termIndex, term1));
		assertFalse(selector.select(termIndex, term2));
		assertFalse(selector.select(termIndex, term3));
		assertTrue(selector.select(termIndex, term4));
		assertTrue(selector.select(termIndex, term5));
		assertTrue(selector.select(termIndex, term6));
		assertTrue(selector.select(termIndex, term7));
	}

}
