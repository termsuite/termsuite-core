package eu.project.ttc.metrics.test;

import static org.junit.Assert.assertEquals;

import java.util.Locale;

import org.junit.Before;
import org.junit.Test;

import eu.project.ttc.metrics.DiacriticInsensitiveLevenshtein;
import eu.project.ttc.metrics.EditDistance;

public class DiacriticInsensitiveLevenshteinSpec {

	EditDistance dFrCaseSensitive;
	EditDistance dFrCaseInsensitive;
	
	@Before
	public void setup() {
		dFrCaseSensitive = new DiacriticInsensitiveLevenshtein(Locale.FRENCH);
		dFrCaseInsensitive = new DiacriticInsensitiveLevenshtein(Locale.FRENCH);
	}
	
	@Test
	public void testAccent() {
		assertEquals(0.0, dFrCaseSensitive.compute("a", "a"), 0.0001d);
		assertEquals(0.0, dFrCaseInsensitive.compute("a", "à"), 0.0001d);
		assertEquals(0.0, dFrCaseSensitive.compute("a", "à"), 0.0001d);
		assertEquals(0.0, dFrCaseInsensitive.compute("a", "a"), 0.0001d);
	}

	@Test
	public void testCap() {
		assertEquals(0.0, dFrCaseInsensitive.compute("a", "A"), 0.0001d);
		assertEquals(0.0, dFrCaseSensitive.compute("a", "A"), 0.0001d);

	}
 }
