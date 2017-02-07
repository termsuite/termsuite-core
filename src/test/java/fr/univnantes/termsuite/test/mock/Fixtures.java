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
package fr.univnantes.termsuite.test.mock;

import fr.univnantes.termsuite.index.Terminology;
import fr.univnantes.termsuite.model.Document;
import fr.univnantes.termsuite.model.Lang;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.TermBuilder;
import fr.univnantes.termsuite.model.Word;
import fr.univnantes.termsuite.model.WordBuilder;
import fr.univnantes.termsuite.test.unit.UnitTests;

public class Fixtures {

	/**
	 * énergie
	 */
	public static Word word1() {
		Word w = new WordBuilder()
				.setLemma("énergie")
				.setStem("énerg")
				.create();
		return w;
	}

	/**
	 * éolien
	 */
	public static Word word2() {
		Word w = new WordBuilder()
				.setLemma("éolien")
				.setStem("éol")
				.create();
		return w;
	}

	/**
	 * accès
	 */
	public static Word word3() {
		Word w = new WordBuilder()
				.setLemma("accès")
				.setStem("acc")
				.create();
		return w;
	}

	/**
	 * radioélectrique (compound)
	 */
	public static Word word4() {
		Word w = new WordBuilder()
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
		Word w = new WordBuilder()
				.setLemma("de")
				.setStem("de")
				.create();
		return w;
	}

	/**
	 * recouvrement
	 */
	public static Word word6() {
		Word w = new WordBuilder()
				.setLemma("recouvrement")
				.setStem("recouvr")
				.create();
		return w;
	}


	/**
	 * total
	 */
	public static Word word7() {
		Word w = new WordBuilder()
				.setLemma("total")
				.setStem("total")
				.create();
		return w;
	}


	/**
	 * na: énergie éolien
	 * @return
	 */
	public static Term term1(Terminology termino) {
		Term term = TermBuilder.start(termino)
					.setGroupingKey("na: énergie éolien")
					.addWord(word1(), "N", true)
					.addWord(word2(), "A", true)
					.create();
		return addTerm(termino, term);
	}
	
	/**
	 * a: radioélectrique
	 * @return
	 */
	public static Term term2(Terminology termino) {
		Term term =  TermBuilder.start(termino)
					.setGroupingKey("a: radioélectrique")
					.addWord(word4(), "A", true)
					.create();
		return addTerm(termino, term);
	}
	
	/**
	 * napn: accès radioélectrique de recouvrement
	 * @return
	 */
	public static Term term3(Terminology termino) {
		Term term =  TermBuilder.start(termino)
				.setGroupingKey("napn: accès radioélectrique de recouvrement")
				.addWord(word3(), "N", true)
				.addWord(word4(), "A", true)
				.addWord(word5(), "P")
				.addWord(word6(), "N", true)
				.create();
		return addTerm(termino, term);
	}

	/**
	 * napna: accès radioélectrique de recouvrement total
	 * @return
	 */
	public static Term term4(Terminology termino) {
		Term term =  TermBuilder.start(termino)
				.setGroupingKey("napna: accès radioélectrique de recouvrement total")
				.addWord(word3(), "N", true)
				.addWord(word4(), "A", true)
				.addWord(word5(), "P")
				.addWord(word6(), "N", true)
				.addWord(word7(), "A", true)
				.create();
		return addTerm(termino, term);
	}
	

	/**
	 * na: accès radioélectrique
	 * @return
	 */
	public static Term term5(Terminology termino) {
		Term term =  TermBuilder.start(termino)
				.setGroupingKey("na: accès radioélectrique")
				.addWord(word3(), "N", true)
				.addWord(word4(), "A", true)
				.create();
		return addTerm(termino, term);
	}


	/**
	 * a: total
	 * @return
	 */
	public static Term term7(Terminology termino) {
		Term term =  TermBuilder.start(termino)
				.setGroupingKey("a: total")
				.addWord(word7(), "A", true)
				.create();
		return addTerm(termino, term);
	}
	
	/**
	 * n: accès
	 * @return
	 */
	public static Term term8(Terminology termino) {
		Term term =  TermBuilder.start(termino)
				.setGroupingKey("n: accès")
				.addWord(word3(), "N", true)
				.create();
		return addTerm(termino, term);
	}
	
	/**
	 * n: recouvrement
	 * @return
	 */
	public static Term term9(Terminology termino) {
		Term term =  TermBuilder.start(termino)
				.setGroupingKey("n: recouvrement")
				.addWord(word6(), "N", true)
				.create();
		return addTerm(termino, term);
	}

	/**
	 * n: énergie
	 * @return
	 */
	public static Term term10(Terminology termino) {
		Term term =  TermBuilder.start(termino)
				.setGroupingKey("n: énergie")
				.addWord(word1(), "N", true)
				.create();
		return addTerm(termino, term);
	}

	public static Term addTerm(Terminology termino, Term term) {
		UnitTests.addTerm(termino, term);
		return term;
	}

	/**
	 * a: éolien
	 * @return
	 */
	public static Term term11(Terminology termino) {
		Term term =  TermBuilder.start(termino)
				.setGroupingKey("a: éolien")
				.addWord(word2(), "A", true)
				.create();
		return addTerm(termino, term);
	}

	/**
	 * na: recouvrement total
	 * @return
	 */
	public static Term term12(Terminology termino) {
		
		Term term = TermBuilder.start(termino)
				.setGroupingKey("na: recouvrement total")
				.addWord(word6(), "N", true)
				.addWord(word7(), "A", true)
				.create();
		return addTerm(termino, term);
	}

//	public static Term term1() {return term1(emptyTermino());}
//	public static Term term2() {return term2(emptyTermino());}
//	public static Term term3() {return term3(emptyTermino());}
//	public static Term term4() {return term4(emptyTermino());}
//	public static Term term5() {return term5(emptyTermino());}
//	public static Term term7() {return term7(emptyTermino());}
//	public static Term term8() {return term8(emptyTermino());}
//	public static Term term9() {return term9(emptyTermino());}
//	public static Term term10() {return term10(emptyTermino());}
//	public static Term term11() {return term11(emptyTermino());}
//	public static Term term12() {return term12(emptyTermino());}
	
	/**
	 * @return
	 */
	public static Document document1() {
		return new Document(Lang.EN, "url1");
	}
	
	/**
	 * @return
	 */
	public static Document document2() {
		return new Document(Lang.EN, "url2");
	}
//	
//	public static Terminology emptyTermino() {
//		return new Terminology("EmptyTermino", Lang.EN);
//	}
//
		
}
