
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

package fr.univnantes.termsuite.test.unit.io;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.Assert.assertEquals;

import org.apache.uima.resource.ResourceInitializationException;
import org.hamcrest.core.StringContains;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import fr.univnantes.julestar.uima.resources.ResourceFormatException;
import fr.univnantes.termsuite.engines.morpho.Segmentation;
import fr.univnantes.termsuite.uima.resources.io.SegmentationParser;

public class SegmentationParserSpec {

	String s1 = "[re][inscription]";
	String s2 = "[re:toto][inscription:tata]";
	String s3 = "[homme:toto]-[grenouille]";
	String s4 = "bla[homme:toto]blabla[grenouille]blablabla";
	String s5 = "  [homme : toto]- [ grenouille] ";

	

	SegmentationParser parser;
	
	@Before
	public void setUp() {
		parser = new SegmentationParser();
	}

	@Test
	public void testGetTargetString() throws ResourceInitializationException {
		assertEquals("reinscription", parser.parse(s1).getString());
		assertEquals("reinscription", parser.parse(s2).getString());
		assertEquals("homme-grenouille", parser.parse(s3).getString());
		assertEquals("blahommeblablagrenouilleblablabla", parser.parse(s4).getString());
	}

	@Test
	public void testParseWithHyphen() throws ResourceInitializationException {
		Segmentation seg3 = parser.parse(s3);
		assertThat(seg3.getSegments()).hasSize(2)
			.extracting("begin", "end", "lemma", "substring")
			.containsExactly(
					tuple(0, 5, "toto", "homme"),
					tuple(6, 16, "grenouille", "grenouille")
			);
		
	}
	
	@Test
	public void testParseWithUnsegmentedText() throws ResourceInitializationException {
		Segmentation seg4 = parser.parse(s4);
		assertThat(seg4.getSegments()).hasSize(2)
			.extracting("begin", "end", "lemma", "substring")
			.containsExactly(
					tuple(3, 8, "toto", "homme"),
					tuple(14, 24, "grenouille", "grenouille")
			);
	}
	
	
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
	String se1 = "[homme:toto][ ]"; // error, empty not allowed

	@Test
	public void shouldRaiseErrorOnEmptySegment() throws ResourceInitializationException {
	    thrown.expect(ResourceFormatException.class);
	    thrown.expectMessage(StringContains.containsString("Empty segment not allowed"));
	    parser.parse(se1);
	}

	String se2 = "[homme:]-[grenouille]"; // bad segment format;
	@Test
	public void shouldRaiseErrorOnEndSegmentWithCol() throws ResourceInitializationException {
	    thrown.expect(ResourceFormatException.class);
	    thrown.expectMessage(StringContains.containsString("Cannot end segment with \":\""));
	    parser.parse(se2);
	}

	String se3 = "[homme:ho]-[:grenouille]"; // bad segment format;
	@Test
	public void shouldRaiseErrorOnStartSegmentWithCol() throws ResourceInitializationException {
	    thrown.expect(ResourceFormatException.class);
	    thrown.expectMessage(StringContains.containsString("Cannot start segment with \":\""));
	    parser.parse(se3);
	}

	String se4 = "[homme:ho-[hoih:grenouille]"; // illegal character [;
	@Test
	public void shouldRaiseErrorOnIllegalOpeningBracket() throws ResourceInitializationException {
	    thrown.expect(ResourceFormatException.class);
	    thrown.expectMessage(StringContains.containsString("Illegal character \"[\""));
	    parser.parse(se4);
	}

	String se5 = "[homme:ho]]-[jpo:grenouille]"; // illegal character ] ;
	@Test
	public void shouldRaiseErrorOnIllegalClosingBracket() throws ResourceInitializationException {
	    thrown.expect(ResourceFormatException.class);
	    thrown.expectMessage(StringContains.containsString("Illegal character \"]\""));
	    parser.parse(se5);
	}

	String se6 = "[homme:ho]-[hoih:grenouille"; // expecting ];
	@Test
	public void shouldRaiseErrorOnMissingClosingBracket() throws ResourceInitializationException {
	    thrown.expect(ResourceFormatException.class);
	    thrown.expectMessage(StringContains.containsString("Expected \"]\""));
	    parser.parse(se6);
	}


	@Test
	public void testParseWithoutLemma() throws ResourceInitializationException {
		Segmentation seg1 = parser.parse(s1);
		assertThat(seg1.getSegments()).hasSize(2)
			.extracting("begin", "end", "lemma", "substring")
			.containsExactly(
					tuple(0, 2, "re", "re"),
					tuple(2, 13, "inscription", "inscription")
			);
	}

	@Test
	public void testParseWithLemma() throws ResourceInitializationException {
		Segmentation seg2 = parser.parse(s2);
		assertThat(seg2.getSegments()).hasSize(2)
			.extracting("begin", "end", "lemma", "substring")
			.containsExactly(
					tuple(0, 2, "toto", "re"),
					tuple(2, 13, "tata", "inscription")
			);
	}


	@Test
	public void testParseEmpty() throws ResourceInitializationException {
		assertEquals(0, parser.parse("[]").size());
		assertEquals(0, parser.parse("   [  ]  ").size());
	}

}
