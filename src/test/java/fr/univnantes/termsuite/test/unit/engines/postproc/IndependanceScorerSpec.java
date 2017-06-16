package fr.univnantes.termsuite.test.unit.engines.postproc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

import fr.univnantes.termsuite.engines.postproc.IndependanceScorer;
import fr.univnantes.termsuite.framework.TermSuiteFactory;
import fr.univnantes.termsuite.framework.service.RelationService;
import fr.univnantes.termsuite.framework.service.TermService;
import fr.univnantes.termsuite.model.IndexedCorpus;
import fr.univnantes.termsuite.model.Lang;
import fr.univnantes.termsuite.model.Relation;
import fr.univnantes.termsuite.model.RelationType;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.TermBuilder;
import fr.univnantes.termsuite.test.unit.UnitTests;

public class IndependanceScorerSpec {

	IndependanceScorer scorer;
	
	private TermService t_a;
	private TermService t_ab;
	private TermService t_xa;
	private TermService t_at;
	private TermService t_xab;
	private TermService t_xat;

	RelationService a_ab, a_at, a_xa, a_xab, a_xat, ab_xab, xa_xab, xa_xat, at_xat;

	IndexedCorpus indexedCorpus;

	/*
	 * Fixture graph
	 *    
	 *        a(10)
	 *      /   |   \
	 *     /    |    \
	 *  ab(3)  xa(3) at(2)
	 *     \   /  \  /
	 * 		\ /    \/
	 *     xab(1) xat(1)
	 */
	@Before
	public void setup() {
		indexedCorpus = TermSuiteFactory.createIndexedCorpus(Lang.FR, "");

		t_a = addTerm("a", 10);
		t_ab = addTerm("ab", 3);
		t_xa = addTerm("xa", 3);
		t_at = addTerm("at", 2);
		t_xab = addTerm("xab", 1);
		t_xat = addTerm("xat", 1);
		
		a_ab = addExtension(t_a, t_ab);
		a_at = addExtension(t_a, t_at);
		a_xa = addExtension(t_a, t_xa);
		a_xab = addExtension(t_a, t_xab);
		a_xat = addExtension(t_a, t_xat);
		ab_xab = addExtension(t_ab, t_xab);
		xa_xab = addExtension(t_xa, t_xab);
		xa_xat = addExtension(t_xa, t_xat);
		at_xat = addExtension(t_at, t_xat);
		
		scorer = UnitTests.createSimpleEngine(indexedCorpus, IndependanceScorer.class);
	}
	
	private  RelationService addExtension(TermService t1, TermService t2) {
		Relation termRelation = new Relation(RelationType.HAS_EXTENSION, t1.getTerm(), t2.getTerm());
		indexedCorpus.getTerminology().getRelations().add(termRelation);
		return new RelationService(UnitTests.getTerminologyService(indexedCorpus), termRelation);
	}

	private TermService addTerm(String gKey, int frequency) {
		TermBuilder builder = TermBuilder.start(indexedCorpus.getTerminology())
				.setGroupingKey(gKey)
				.setFrequency(frequency);
		
		/*
		 * Needs to set the correct num of words
		 * because IndependanceScorer#setDepths
		 * checks if term is swt at init.
		 */
		for(char c:gKey.toCharArray())
			builder.addWord(Character.toString(c), Character.toString(c), "Label", true);
				
		Term term = builder.create();
		UnitTests.addTerm(indexedCorpus.getTerminology(), term);
		return new TermService(UnitTests.getTerminologyService(indexedCorpus), null, term);
	}
	
	@Test
	public void testGetExtensions() {
		assertThat(scorer.getExtensions(t_xat)).isEmpty();
		assertThat(scorer.getExtensions(t_xab)).isEmpty();

		assertThat(scorer.getExtensions(t_xa))
			.containsOnly(t_xab, t_xat);
		assertThat(scorer.getExtensions(t_at))
			.containsOnly(t_xat);
		assertThat(scorer.getExtensions(t_ab))
			.containsOnly(t_xab);
		assertThat(scorer.getExtensions(t_a))
			.containsOnly(t_xa, t_at, t_ab, t_xab, t_xat);
	}

	@Test
	public void testGetBases() {
		assertThat(scorer.getBases(t_a))
		 	.isEmpty();
		assertThat(scorer.getBases(t_xab))
			.containsOnly(t_xa, t_ab, t_a);
		assertThat(scorer.getBases(t_xat))
			.containsOnly(t_xa, t_at, t_a);
		assertThat(scorer.getBases(t_xa))
			.containsOnly(t_a);
		assertThat(scorer.getBases(t_ab))
			.containsOnly(t_a);
		assertThat(scorer.getBases(t_at))
			.containsOnly(t_a);

	}

	@Test
	public void testSetIndependence() {
		scorer.execute();
		assertIndFreq(1, t_xab);
		assertIndFreq(1, t_xat);
		assertIndFreq(2, t_ab);
		assertIndFreq(1, t_xa);
		assertIndFreq(1, t_at);
		assertIndFreq(4, t_a);
		
		assertInd(1.0, t_xab);
		assertInd(1.0, t_xat);
		assertInd(0.6666666, t_ab);
		assertInd(0.3333333, t_xa);
		assertInd(0.5, t_at);
		assertInd(.4, t_a);
	}



	private void assertInd(double ifreq, TermService t) {
		assertEquals(ifreq, t.getIndependance(), 0.0001d);

	}
	private void assertIndFreq(int ifreq, TermService t) {
		assertEquals(ifreq, t.getIndependantFrequency().intValue());
	}

	@Test
	public void testSetDepths() {
		int maxDepth = scorer.setDepths().intValue();
		assertDepth(1, t_a);
		assertDepth(2, t_xa);
		assertDepth(2, t_ab);
		assertDepth(2, t_at);
		assertDepth(3, t_xab);
		assertDepth(3, t_xat);
		assertEquals(3, maxDepth);
	}

	private void assertDepth(int expected, TermService t) {
		assertEquals(expected, t.getDepth().intValue());
		
	}

	@Test
	public void testCheckNoCycleInExtensions() {
		// should be find
		scorer.checkNoCycleInExtensions();
		
		// add a size-3 to size-3 extension
		addExtension(t_xab, t_xat);
		try {
			scorer.checkNoCycleInExtensions();
		} catch(IllegalStateException e) {
			// ok, expected exception
		} catch(Exception e) {
			fail("Expected IllegalStateException, but got " + e.getClass().getName());
		}
	}
}
