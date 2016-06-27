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
import eu.project.ttc.models.TermIndex;
import eu.project.ttc.models.Word;
import eu.project.ttc.models.WordBuilder;
import eu.project.ttc.models.index.MemoryTermIndex;
import eu.project.ttc.models.occstore.MemoryOccurrenceStore;

public class Fixtures {

	/**
	 * énergie
	 */
	public static Word word1() {
		Word w = WordBuilder.start()
				.setLemma("énergie")
				.setStem("énerg")
				.create();
		return w;
	}

	/**
	 * éolien
	 */
	public static Word word2() {
		Word w = WordBuilder.start()
				.setLemma("éolien")
				.setStem("éol")
				.create();
		return w;
	}

	/**
	 * accès
	 */
	public static Word word3() {
		Word w = WordBuilder.start()
				.setLemma("accès")
				.setStem("acc")
				.create();
		return w;
	}

	/**
	 * radioélectrique (compound)
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
	 * de
	 */
	public static Word word5() {
		Word w = WordBuilder.start()
				.setLemma("de")
				.setStem("de")
				.create();
		return w;
	}

	/**
	 * recouvrement
	 */
	public static Word word6() {
		Word w = WordBuilder.start()
				.setLemma("recouvrement")
				.setStem("recouvr")
				.create();
		return w;
	}


	/**
	 * total
	 */
	public static Word word7() {
		Word w = WordBuilder.start()
				.setLemma("total")
				.setStem("total")
				.create();
		return w;
	}


	/**
	 * na: énergie éolien
	 * @return
	 */
	public static Term term1(TermIndex termIndex) {
		return TermBuilder.start(termIndex)
					.setId(1)
					.setGroupingKey("na: énergie éolien")
					.addWord(word1(), "N")
					.addWord(word2(), "A")
					.createAndAddToIndex();
	}
	
	/**
	 * a: radioélectrique
	 * @return
	 */
	public static Term term2(TermIndex termIndex) {
		return TermBuilder.start(termIndex)
					.setId(2)
					.setGroupingKey("a: radioélectrique")
					.addWord(word4(), "A")
					.createAndAddToIndex();
	}
	
	/**
	 * napn: accès radioélectrique de recouvrement
	 * @return
	 */
	public static Term term3(TermIndex termIndex) {
		return TermBuilder.start(termIndex)
				.setId(3)
				.setGroupingKey("napn: accès radioélectrique de recouvrement")
				.addWord(word3(), "N")
				.addWord(word4(), "A")
				.addWord(word5(), "P")
				.addWord(word6(), "N")
				.createAndAddToIndex();
	}

	/**
	 * napna: accès radioélectrique de recouvrement total
	 * @return
	 */
	public static Term term4(TermIndex termIndex) {
		return TermBuilder.start(termIndex)
				.setId(4)
				.setGroupingKey("napna: accès radioélectrique de recouvrement total")
				.addWord(word3(), "N")
				.addWord(word4(), "A")
				.addWord(word5(), "P")
				.addWord(word6(), "N")
				.addWord(word7(), "A")
				.createAndAddToIndex();
	}
	

	/**
	 * na: accès radioélectrique
	 * @return
	 */
	public static Term term5(TermIndex termIndex) {
		return TermBuilder.start(termIndex)
				.setId(5)
				.setGroupingKey("na: accès radioélectrique")
				.addWord(word3(), "N")
				.addWord(word4(), "A")
				.createAndAddToIndex();
	}


	/**
	 * a: total
	 * @return
	 */
	public static Term term7(TermIndex termIndex) {
		return TermBuilder.start(termIndex)
				.setId(7)
				.setGroupingKey("a: total")
				.addWord(word7(), "A")
				.createAndAddToIndex();
	}
	
	/**
	 * n: accès
	 * @return
	 */
	public static Term term8(TermIndex termIndex) {
		return TermBuilder.start(termIndex)
				.setId(8)
				.setGroupingKey("n: accès")
				.addWord(word3(), "N")
				.createAndAddToIndex();
	}
	
	/**
	 * n: recouvrement
	 * @return
	 */
	public static Term term9(TermIndex termIndex) {
		return TermBuilder.start(termIndex)
				.setId(9)
				.setGroupingKey("n: recouvrement")
				.addWord(word6(), "N")
				.createAndAddToIndex();
	}

	/**
	 * n: énergie
	 * @return
	 */
	public static Term term10(TermIndex termIndex) {
		return TermBuilder.start(termIndex)
				.setId(10)
				.setGroupingKey("n: énergie")
				.addWord(word1(), "N")
				.createAndAddToIndex();
	}

	/**
	 * a: éolien
	 * @return
	 */
	public static Term term11(TermIndex termIndex) {
		return TermBuilder.start(termIndex)
				.setId(11)
				.setGroupingKey("a: éolien")
				.addWord(word2(), "A")
				.createAndAddToIndex();
	}

	/**
	 * na: recouvrement total
	 * @return
	 */
	public static Term term12(TermIndex termIndex) {
		return TermBuilder.start(termIndex)
				.setId(12)
				.setGroupingKey("na: recouvrement total")
				.addWord(word6(), "N")
				.addWord(word7(), "A")
				.createAndAddToIndex();
	}

	public static Term term1() {return term1(emptyTermIndex());}
	public static Term term2() {return term2(emptyTermIndex());}
	public static Term term3() {return term3(emptyTermIndex());}
	public static Term term4() {return term4(emptyTermIndex());}
	public static Term term5() {return term5(emptyTermIndex());}
	public static Term term7() {return term7(emptyTermIndex());}
	public static Term term8() {return term8(emptyTermIndex());}
	public static Term term9() {return term9(emptyTermIndex());}
	public static Term term10() {return term10(emptyTermIndex());}
	public static Term term11() {return term11(emptyTermIndex());}
	public static Term term12() {return term12(emptyTermIndex());}
	
	/**
	 * @return
	 */
	public static Document document1() {
		return new Document(101, "url1");
	}
	
	/**
	 * @return
	 */
	public static Document document2() {
		return new Document(102, "url2");
	}
	
	public static MemoryTermIndex emptyTermIndex() {
		return new MemoryTermIndex("EmptyTermIndex", Lang.EN, new MemoryOccurrenceStore());
	}

		
	public static MemoryTermIndex termIndex() {
		MemoryTermIndex memoryTermIndex = new MemoryTermIndex("TermIndex", Lang.EN, new MemoryOccurrenceStore());
//		term1(memoryTermIndex);
//		term2(memoryTermIndex);
//		term3(memoryTermIndex);
//		term4(memoryTermIndex);
//		term5(memoryTermIndex);
		return memoryTermIndex;
	}
	
	public static MemoryTermIndex termIndexWithOccurrences() {
		MemoryTermIndex memoryTermIndex = new MemoryTermIndex("TermIndexWithOccurrences", Lang.EN, new MemoryOccurrenceStore());
		final Document doc = document1();
		
		
		TermBuilder.start(memoryTermIndex)
			.setGroupingKey("n: énergie")
			.addWord(Fixtures.word1(), "N")
			.addOccurrence(0, 10, doc, "blabla")
			.addOccurrence(31, 40, doc, "blabla")
			.addOccurrence(61, 70, doc, "blabla")
			.createAndAddToIndex();
		
		TermBuilder.start(memoryTermIndex)
			.setGroupingKey("a: éolien")
			.addWord(Fixtures.word2(), "A")
			.addOccurrence(11, 20, doc, "blabla")
			.createAndAddToIndex();
		
		
		
		TermBuilder.start(memoryTermIndex)
			.setGroupingKey("n: accès")
			.addWord(Fixtures.word3(), "N")
			.addOccurrence(21, 30, doc, "blabla")
			.addOccurrence(41, 50, doc, "blabla")
			.addOccurrence(51, 60, doc, "blabla")
			.createAndAddToIndex();

		return memoryTermIndex;

	}
}
