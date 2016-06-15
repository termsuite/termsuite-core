package org.ttc.project.test.resources;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.hamcrest.core.StringContains;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import eu.project.ttc.resources.SuffixDerivation;

public class SuffixDerivationTestCase {
	
	SuffixDerivation suffixDerivation;
	
	@Before
	public void setUp() {
		suffixDerivation = new SuffixDerivation("ièreté", "ier");
	}
	
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
	@Test
	public void testShouldRaiseErrorSinceIfCannotDerivate() {
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage(StringContains.containsString("Cannot operate the derivation"));
		suffixDerivation.derivate("tata");
	}

	@Test
	public void testDerivate() {
		assertEquals("grossier", suffixDerivation.derivate("grossièreté"));
	}

	@Test
	public void testCanDerivate() {
		assertFalse(suffixDerivation.canDerivate("tata"));
		assertTrue(suffixDerivation.canDerivate("grossièreté"));
	}


}
