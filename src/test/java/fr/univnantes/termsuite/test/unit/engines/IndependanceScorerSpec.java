package fr.univnantes.termsuite.test.unit.engines;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

import fr.univnantes.termsuite.engines.postproc.IndependanceScorer;
import fr.univnantes.termsuite.framework.TerminologyService;
import fr.univnantes.termsuite.model.Lang;
import fr.univnantes.termsuite.model.RelationType;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.TermBuilder;
import fr.univnantes.termsuite.model.TermProperty;
import fr.univnantes.termsuite.model.TermRelation;
import fr.univnantes.termsuite.model.Terminology;
import fr.univnantes.termsuite.model.occurrences.EmptyOccurrenceStore;
import fr.univnantes.termsuite.model.termino.MemoryTerminology;

public class IndependanceScorerSpec {

	IndependanceScorer scorer;
	
	private Term t_a;
	private Term t_ab;
	private Term t_xa;
	private Term t_at;
	private Term t_xab;
	private Term t_xat;

	TermRelation a_ab, a_at, a_xa, a_xab, a_xat, ab_xab, xa_xab, xa_xat, at_xat;

	Terminology terminology;
	TerminologyService terminologyService;

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
		scorer = new IndependanceScorer();
		terminology = new MemoryTerminology(
				"Tata", 
				Lang.FR, 
				new EmptyOccurrenceStore(Lang.FR));

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
		
		terminologyService = new TerminologyService(terminology);
	}
	
	private  TermRelation addExtension(Term t1, Term t2) {
		TermRelation termRelation = new TermRelation(RelationType.HAS_EXTENSION, t1, t2);
		terminology.addRelation(termRelation);
		return termRelation;
	}

	private Term addTerm(String gKey, int frequency) {
		TermBuilder builder = TermBuilder.start(terminology)
				.setGroupingKey(gKey)
				.setFrequency(frequency);
		
		/*
		 * Needs to set the correct num of words
		 * because IndependanceScorer#setDepths
		 * checks if term is swt at init.
		 */
		for(char c:gKey.toCharArray())
			builder.addWord(Character.toString(c), Character.toString(c), "Label", true);
				
		return builder.
			createAndAddToTerminology();
	}
	
	@Test
	public void testGetExtensions() {
		assertThat(scorer.getExtensions(terminologyService, t_xat)).isEmpty();
		assertThat(scorer.getExtensions(terminologyService, t_xab)).isEmpty();

		assertThat(scorer.getExtensions(terminologyService, t_xa))
			.containsOnly(t_xab, t_xat);
		assertThat(scorer.getExtensions(terminologyService, t_at))
			.containsOnly(t_xat);
		assertThat(scorer.getExtensions(terminologyService, t_ab))
			.containsOnly(t_xab);
		assertThat(scorer.getExtensions(terminologyService, t_a))
			.containsOnly(t_xa, t_at, t_ab, t_xab, t_xat);
	}

	@Test
	public void testGetBases() {
		assertThat(scorer.getBases(terminologyService, t_a))
		 	.isEmpty();
		assertThat(scorer.getBases(terminologyService, t_xab))
			.containsOnly(t_xa, t_ab, t_a);
		assertThat(scorer.getBases(terminologyService, t_xat))
			.containsOnly(t_xa, t_at, t_a);
		assertThat(scorer.getBases(terminologyService, t_xa))
			.containsOnly(t_a);
		assertThat(scorer.getBases(terminologyService, t_ab))
			.containsOnly(t_a);
		assertThat(scorer.getBases(terminologyService, t_at))
			.containsOnly(t_a);

	}

	@Test
	public void testSetIndependence() {
		scorer.setIndependance(terminologyService);
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



	private void assertInd(double ifreq, Term t) {
		assertEquals(ifreq, t.getPropertyDoubleValue(TermProperty.INDEPENDANCE), 0.0001d);

	}
	private void assertIndFreq(int ifreq, Term t) {
		assertEquals(ifreq, t.get(TermProperty.INDEPENDANT_FREQUENCY));
	}

	@Test
	public void testSetDepths() {
		int maxDepth = scorer.setDepths(terminologyService).intValue();
		assertDepth(1, t_a);
		assertDepth(2, t_xa);
		assertDepth(2, t_ab);
		assertDepth(2, t_at);
		assertDepth(3, t_xab);
		assertDepth(3, t_xat);
		assertEquals(3, maxDepth);
	}

	private void assertDepth(int expected, Term t) {
		assertEquals(expected, t.get(TermProperty.DEPTH));
		
	}

	@Test
	public void testCheckNoCycleInExtensions() {
		// should be find
		scorer.checkNoCycleInExtensions(terminologyService);
		
		// add a size-3 to size-3 extension
		addExtension(t_xab, t_xat);
		try {
			scorer.checkNoCycleInExtensions(terminologyService);
		} catch(IllegalStateException e) {
			// ok, expected exception
		} catch(Exception e) {
			fail("Expected IllegalStateException, but got " + e.getClass().getName());
		}

		
	}

	
}
