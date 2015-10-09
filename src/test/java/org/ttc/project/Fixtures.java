/*******************************************************************************
 * Copyright 2015 - CNRS (Centre National de Recherche Scientifique)
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
package org.ttc.project;

import eu.project.ttc.engines.desc.Lang;
import eu.project.ttc.models.Document;
import eu.project.ttc.models.Term;
import eu.project.ttc.models.TermBuilder;
import eu.project.ttc.models.Word;
import eu.project.ttc.models.WordBuilder;
import eu.project.ttc.models.index.MemoryTermIndex;

public class Fixtures {

	/**
	 * énergie:noun
	 */
	public static Word word1() {
		Word w = WordBuilder.start()
				.setLemma("énergie")
				.setStem("énerg")
				.create();
		return w;
	}

	/**
	 * éolien:adjective
	 */
	public static Word word2() {
		Word w = WordBuilder.start()
				.setLemma("éolien")
				.setStem("éol")
				.create();
		return w;
	}

	/**
	 * accès:noun
	 */
	public static Word word3() {
		Word w = WordBuilder.start()
				.setLemma("accès")
				.setStem("acc")
				.create();
		return w;
	}

	/**
	 * radioélectrique:adjective (compound)
	 */
	public static Word word4() {
		Word w = WordBuilder.start()
				.setLemma("radioélectrique")
				.setStem("radioélectriq")
				.addComponent(0, 5, "radio")
				.addComponent(5, 15, "électrique")
				.create();
		return w;
	}

	/**
	 * de:preposition
	 */
	public static Word word5() {
		Word w = WordBuilder.start()
				.setLemma("de")
				.setStem("de")
				.create();
		return w;
	}

	/**
	 * éolien:adjective
	 */
	public static Word word6() {
		Word w = WordBuilder.start()
				.setLemma("recouvrement")
				.setStem("recouvr")
				.create();
		return w;
	}


	/**
	 * totale:adjective
	 */
	public static Word word7() {
		Word w = WordBuilder.start()
				.setLemma("totale")
				.setStem("total")
				.create();
		return w;
	}

	
	/**
	 * énergie éolien
	 * @return
	 */
	public static Term term1() {
		return new TermBuilder()
					.setId(1)
					.setGroupingKey("énergie éolien")
					.addWord(word1(), "N")
					.addWord(word2(), "A")
					.create();
	}
	
	/**
	 * radioélectrique
	 * @return
	 */
	public static Term term2() {
		return new TermBuilder()
					.setId(2)
					.setGroupingKey("radioélectrique")
					.addWord(word4(), "A")
					.create();
	}
	
	/**
	 * accès radioélectrique de recouvrement
	 * @return
	 */
	public static Term term3() {
		return new TermBuilder()
				.setId(3)
				.setGroupingKey("accès radioélectrique de recouvrement")
				.addWord(word3(), "N")
				.addWord(word4(), "A")
				.addWord(word5(), "P")
				.addWord(word6(), "N")
				.create();
	}

	/**
	 * accès radioélectrique de recouvrement
	 * @return
	 */
	public static Term term4() {
		return new TermBuilder()
				.setId(4)
				.setGroupingKey("accès radioélectrique de recouvrement total")
				.addWord(word3(), "N")
				.addWord(word4(), "A")
				.addWord(word5(), "P")
				.addWord(word6(), "N")
				.addWord(word7(), "A")
				.create();
	}
	

	/**
	 * accès radioélectrique de recouvrement
	 * @return
	 */
	public static Term term5() {
		return new TermBuilder()
				.setId(5)
				.setGroupingKey("accès radioélectrique")
				.addWord(word3(), "N")
				.addWord(word4(), "A")
				.create();
	}
	
	/**
	 * @return
	 */
	public static Document document1() {
		return new Document("url1");
	}
	
	/**
	 * @return
	 */
	public static Document document2() {
		return new Document("url2");
	}
	
	
	public static MemoryTermIndex termIndex() {
		MemoryTermIndex memoryTermIndex = new MemoryTermIndex("TermIndex", Lang.EN);
		Term term1 = term1();
		memoryTermIndex.addTerm(term1);
		Term term2 = term2();
		memoryTermIndex.addTerm(term2);
		Term term3 = term3();
		memoryTermIndex.addTerm(term3);
		Term term4 = term4();
		memoryTermIndex.addTerm(term4);
		Term term5 = term5();
		memoryTermIndex.addTerm(term5);
		return memoryTermIndex;
	}
	
	public static MemoryTermIndex termIndexWithOccurrences() {
		MemoryTermIndex memoryTermIndex = new MemoryTermIndex("TermIndexWithOccurrences", Lang.EN);
		final Document doc = document1();
		
		
		new TermBuilder(memoryTermIndex)
			.setGroupingKey("term1")
			.addWord(Fixtures.word1(), "N")
			.addOccurrence(0, 10, doc, "blabla")
			.addOccurrence(31, 40, doc, "blabla")
			.addOccurrence(61, 70, doc, "blabla")
			.createAndAddToIndex();
		
		new TermBuilder(memoryTermIndex)
			.setGroupingKey("term2")
			.addWord(Fixtures.word2(), "A")
			.addOccurrence(11, 20, doc, "blabla")
			.createAndAddToIndex();
		
		
		
		new TermBuilder(memoryTermIndex)
			.setGroupingKey("term3")
			.addWord(Fixtures.word3(), "N")
			.addOccurrence(21, 30, doc, "blabla")
			.addOccurrence(41, 50, doc, "blabla")
			.addOccurrence(51, 60, doc, "blabla")
			.createAndAddToIndex();

		return memoryTermIndex;

	}
}
