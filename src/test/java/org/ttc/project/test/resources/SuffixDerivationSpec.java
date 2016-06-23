package org.ttc.project.test.resources;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.hamcrest.core.StringContains;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import eu.project.ttc.models.TermWord;
import eu.project.ttc.resources.SuffixDerivation;

public class SuffixDerivationSpec {
	
	SuffixDerivation suffixDerivation;
	
	@Before
	public void setUp() {
		suffixDerivation = new SuffixDerivation("N", "A", "ièreté", "ier");
	}
	
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void testGetType() {
		assertEquals("A N", suffixDerivation.getType());
	}

	@Test
	public void testShouldRaiseErrorSinceIfCannotDerivateOnLemma() {
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage(StringContains.containsString("Cannot operate the derivation"));
		suffixDerivation.getBaseForm(TermWord.create("tata", "N"));
	}

	@Test
	public void testShouldRaiseErrorSinceIfCannotDerivateOnLabel() {
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage(StringContains.containsString("Cannot operate the derivation"));
		suffixDerivation.getBaseForm(TermWord.create("grossièreté", "A"));
	}

	@Test
	public void testDerivate() {
		assertEquals(
				TermWord.create("grossier", "A"), 
				suffixDerivation.getBaseForm(TermWord.create("grossièreté", "N")));
	}

	@Test
	public void testCanDerivate() {
		assertFalse(suffixDerivation.isKnownDerivate(TermWord.create("tata", "A")));
		assertFalse(suffixDerivation.isKnownDerivate(TermWord.create("grossièreté", "A")));
		assertTrue(suffixDerivation.isKnownDerivate(TermWord.create("grossièreté", "N")));
	}


}
