
/*******************************************************************************
 * Copyright 2015-2016 - CNRS (Centre National de Recherche Scientifique)
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *******************************************************************************/

package fr.univnantes.termsuite.test.unit.resources;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.uima.resource.DataResource;
import org.apache.uima.resource.ResourceInitializationException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import fr.univnantes.termsuite.test.unit.TestUtil;
import fr.univnantes.termsuite.uima.resources.preproc.PrefixTree;

public class PrefixTreeSpec {

	
	private static final String PREFIX_TREE_1 = "fr/univnantes/termsuite/test/resources/prefixes1.txt";
	private static final String PREFIX_TREE_2 = "fr/univnantes/termsuite/test/resources/prefixes2.txt";

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
