package org.ttc.project.test.resources;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.uima.resource.DataResource;
import org.apache.uima.resource.ResourceInitializationException;
import org.assertj.core.api.iterable.Extractor;
import org.assertj.core.groups.Tuple;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.ttc.project.TestUtil;

import eu.project.ttc.resources.SuffixDerivation;
import eu.project.ttc.resources.SuffixDerivationList;
import eu.project.ttc.resources.SuffixDerivationList.SuffixDerivationEntry;

public class SuffixDerivationListSpec {
	private static final String SUFFIX_DERIVATIONS1 = "org/project/ttc/test/resources/derivational-suffixes1.txt";
	private static final String SUFFIX_DERIVATIONS_ERR1 = "org/project/ttc/test/resources/derivational-suffixes-err-2-columns.txt";
	private static final String SUFFIX_DERIVATIONS_ERR2 = "org/project/ttc/test/resources/derivational-suffixes-err-pattern-size-3.txt";

	SuffixDerivationList suffixDerivations1;


	@Before
	public void set() throws FileNotFoundException, IOException, ResourceInitializationException {
		suffixDerivations1 = mockDerivationList(SUFFIX_DERIVATIONS1);
	}


	private SuffixDerivationList mockDerivationList(String path) throws IOException, ResourceInitializationException {
		DataResource data;
		SuffixDerivationList suffixDerivationList = new SuffixDerivationList();
		data = Mockito.mock(DataResource.class);
		InputStream resourceAsStream = TestUtil.getInputStream(path);
		Mockito.when(data.getInputStream()).thenReturn(resourceAsStream);
		suffixDerivationList.load(data);
		return suffixDerivationList;
	}
	

	@Test
	public void testGetSuffixDerivationsEmpty() {
		assertThat(suffixDerivations1.getDerivationsFromDerivateForm("tata", "N")).hasSize(0);
	}

	@Test
	public void testGetSuffixDerivationsSize1() {
		assertThat(suffixDerivations1.getDerivationsFromDerivateForm("grossièreté", "N"))
			.hasSize(1)
			.extracting(derivExtractor)
			.contains(tuple("N", "A", "ièreté", "ier"));

		assertThat(suffixDerivations1.getDerivationsFromDerivateForm("amuserie", "N"))
			.hasSize(4)
			.extracting(derivExtractor)
			.contains(
					tuple("N", "N", "ie", "a"),
					tuple("N", "A", "erie", "e"),
					tuple("N", "A", "erie", "er"), 
					tuple("N", "N", "erie", "er")
					);

		assertThat(suffixDerivations1.getDerivationsFromDerivateForm("encablure", "N"))
			.hasSize(2)
			.extracting(derivExtractor)
			.contains(
					tuple("N", "A", "ure", "er"), 
					tuple("N", "A", "ure", ""));

	}

	Extractor<SuffixDerivation, Tuple> derivExtractor = new Extractor<SuffixDerivation, Tuple>() {
		@Override
		public Tuple extract(SuffixDerivation input) {
			return tuple(
					input.getFromPattern(),
					input.getToPattern(),
					input.getDerivateSuffix(),
					input.getRegularSuffix()
					);
		}
	};

	@Test
	public void testParsing() {
		assertThat(suffixDerivations1.getDerivations().get(new SuffixDerivationEntry("erie", "N")))
		.hasSize(3)
		.extracting(derivExtractor)
		.contains(
				tuple("N", "A", "erie", "er"),
				tuple("N", "N", "erie", "er"),
				tuple("N", "A", "erie", "e"));

		assertThat(suffixDerivations1.getDerivations().keySet()).hasSize(5)
			.extracting("suffix", "label")
			.containsOnly(
					tuple("ièreté", "N"),
					tuple("ure", "N"),
					tuple("ion", "N"),
					tuple("erie", "N"),
					tuple("ie", "N"));
		
		assertThat(suffixDerivations1.getDerivations().get(new SuffixDerivationEntry("ièreté", "N")))
			.hasSize(1)
			.extracting(derivExtractor)
			.contains(tuple("N", "A", "ièreté", "ier"));
		
		assertThat(suffixDerivations1.getDerivations().get(new SuffixDerivationEntry("ion", "N")))
			.hasSize(1)
			.extracting(derivExtractor)
			.contains(tuple("N", "A", "ion", ""));

		assertThat(suffixDerivations1.getDerivations().get(new SuffixDerivationEntry("ure", "N")))
			.hasSize(2)
			.extracting(derivExtractor)
			.contains(
					tuple("N", "A", "ure", ""), 
					tuple("N", "A", "ure", "er"));


		assertThat(suffixDerivations1.getDerivations().get(new SuffixDerivationEntry("ie", "N")))
			.hasSize(1)
			.extracting(derivExtractor)
			.contains(tuple("N", "N", "ie", "a"));
	}

	/*
	 * Format Errors
	 * 
	 */
	
	@Test
	public void testShouldRaiseErrorOnSize3Labels() throws ResourceInitializationException, IOException {
		try {
			mockDerivationList(SUFFIX_DERIVATIONS_ERR2);
			fail("Should have thrown ResourceInitializationException");
		} catch(ResourceInitializationException e) {
			assertEquals(IllegalArgumentException.class, 
					e.getCause().getClass());
			assertThat(e.getCause().getMessage())
				.contains("Derivation pattern must be of size 2 at line 1");
		} catch(Exception e) {
			fail("Unexpected exception thrown");
		}
	}
	
	@Test
	public void testShouldRaiseErrorOn2Columns() throws ResourceInitializationException, IOException {
		try {
			mockDerivationList(SUFFIX_DERIVATIONS_ERR1);
			fail("Should have thrown ResourceInitializationException");
		} catch(ResourceInitializationException e) {
			assertEquals(IllegalArgumentException.class, 
					e.getCause().getClass());
			assertThat(e.getCause().getMessage())
			.contains("Row must have 3 columns at line 1");
		} catch(Exception e) {
			fail("Unexpected exception thrown");
		}

	}

}
