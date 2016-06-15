package org.ttc.project.test.resources;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.uima.resource.DataResource;
import org.apache.uima.resource.ResourceInitializationException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.ttc.project.TestUtil;

import eu.project.ttc.resources.SuffixDerivationList;

public class SuffixDerivationListTestCase {
	private static final String SUFFIX_DERIVATIONS1 = "org/project/ttc/test/resources/derivational-suffixes1.txt";

	SuffixDerivationList suffixDerivations;
	
	@Before
	public void set() throws FileNotFoundException, IOException, ResourceInitializationException {
		suffixDerivations = new SuffixDerivationList();
		DataResource data1 = Mockito.mock(DataResource.class);
		InputStream resourceAsStream = TestUtil.getInputStream(SUFFIX_DERIVATIONS1);
		Mockito.when(data1.getInputStream()).thenReturn(resourceAsStream);
		suffixDerivations.load(data1);
	}
	

	@Test
	public void testGetSuffixDerivationsEmpty() {
		assertThat(suffixDerivations.getSuffixDerivations("tata")).hasSize(0);
	}

	@Test
	public void testGetSuffixDerivationsSize1() {
		assertThat(suffixDerivations.getSuffixDerivations("grossièreté"))
			.hasSize(1)
			.extracting("fromSuffix", "toSuffix")
			.contains(tuple("ièreté", "ier"));

		assertThat(suffixDerivations.getSuffixDerivations("amuserie"))
			.hasSize(3)
			.extracting("fromSuffix", "toSuffix")
			.contains(tuple("erie", "er"), tuple("ie", "a"),tuple("erie", "e"));

		assertThat(suffixDerivations.getSuffixDerivations("encablure"))
			.hasSize(2)
			.extracting("fromSuffix", "toSuffix")
			.contains(tuple("ure", "er"), tuple("ure", ""));

	}


	@Test
	public void testParsing() {
		assertThat(suffixDerivations.getDerivations().keySet()).hasSize(5)
			.contains("ièreté", "ure", "ion", "erie", "ie");
		
		assertThat(suffixDerivations.getDerivations().get("ièreté"))
			.hasSize(1)
			.extracting("fromSuffix", "toSuffix")
			.contains(tuple("ièreté", "ier"));
		
		assertThat(suffixDerivations.getDerivations().get("ion"))
			.hasSize(1)
			.extracting("fromSuffix", "toSuffix")
			.contains(tuple("ion", ""));

		assertThat(suffixDerivations.getDerivations().get("ure"))
			.hasSize(2)
			.extracting("fromSuffix", "toSuffix")
			.contains(tuple("ure", ""), tuple("ure", "er"));

		assertThat(suffixDerivations.getDerivations().get("erie"))
			.hasSize(2)
			.extracting("fromSuffix", "toSuffix")
			.contains(tuple("erie", "er"),tuple("erie", "e"));

		assertThat(suffixDerivations.getDerivations().get("ie"))
			.hasSize(1)
			.extracting("fromSuffix", "toSuffix")
			.contains(tuple("ie", "a"));
	}

}
