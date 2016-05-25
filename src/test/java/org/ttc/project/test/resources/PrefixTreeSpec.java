package org.ttc.project.test.resources;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.uima.resource.DataResource;
import org.apache.uima.resource.ResourceInitializationException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.ttc.project.TestUtil;

import eu.project.ttc.resources.PrefixTree;

public class PrefixTreeSpec {

	
	private static final String PREFIX_TREE_1 = "org/project/ttc/test/resources/prefixes1.txt";
	private static final String PREFIX_TREE_2 = "org/project/ttc/test/resources/prefixes2.txt";

	PrefixTree prefixTree1;
	PrefixTree prefixTree2;
	
	DataResource data1;
	DataResource data2;
	
	@Before
	public void set() throws FileNotFoundException, IOException {
		prefixTree1 = new PrefixTree();
		data1 = Mockito.mock(DataResource.class);
		InputStream resourceAsStream = TestUtil.getInputStream(PREFIX_TREE_1);
		Mockito.when(data1.getInputStream()).thenReturn(resourceAsStream);
		prefixTree2 = new PrefixTree();
		data2 = Mockito.mock(DataResource.class);
		Mockito.when(data2.getInputStream()).thenReturn(TestUtil.getInputStream(PREFIX_TREE_2));

	}
	

	@Test
	public void testGetPrefix() throws ResourceInitializationException {
		prefixTree2.load(data2);
		
		assertNull(prefixTree2.getPrefix(""));
		assertEquals("a", prefixTree2.getPrefix("ab"));
		assertEquals("a", prefixTree2.getPrefix("ac"));
		assertEquals("a", prefixTree2.getPrefix("a"));
		assertEquals("a", prefixTree2.getPrefix("an"));
		assertEquals("a", prefixTree2.getPrefix("ant"));
		assertEquals("anti", prefixTree2.getPrefix("anti"));
		assertEquals("anti", prefixTree2.getPrefix("antic"));
	}

	
	@Test
	public void testLoad() throws ResourceInitializationException {
		prefixTree1.load(data1);
		prefixTree1.load(data2);
	}
	
}
