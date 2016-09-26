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
package eu.project.ttc.test.unit.io;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import eu.project.ttc.api.JSONOptions;
import eu.project.ttc.engines.desc.Lang;
import eu.project.ttc.models.CompoundType;
import eu.project.ttc.models.ContextVector;
import eu.project.ttc.models.Document;
import eu.project.ttc.models.Term;
import eu.project.ttc.models.TermBuilder;
import eu.project.ttc.models.TermIndex;
import eu.project.ttc.models.VariationType;
import eu.project.ttc.models.Word;
import eu.project.ttc.models.WordBuilder;
import eu.project.ttc.models.index.JSONTermIndexIO;
import eu.project.ttc.models.index.MemoryTermIndex;
import eu.project.ttc.models.occstore.MemoryOccurrenceStore;
import eu.project.ttc.test.unit.TestUtil;

public class JSONTermIndexIOSpec {
	public static final String jsonFile1 = "org/project/ttc/test/json/termIndex1.json";
	
	private TermIndex termIndex;
	private Term term1;
	private Term term2;
	private Word word1;
	private Word word2;
	private Word word3;
	private Document doc1;
	private Document doc2;
	private Document doc3;
	
	private String json1;
	
	@Before
	public void initTermIndex() {
		termIndex = new MemoryTermIndex("Titi va voir Toto", Lang.FR, new MemoryOccurrenceStore());
		termIndex.setCorpusId("ccid");
		termIndex.setWordAnnotationsNum(222);
		termIndex.setSpottedTermsNum(111);
		doc1 = termIndex.getDocument("source1");
		doc2 = termIndex.getDocument("source2");
		doc3 = termIndex.getDocument("source3");
		word1 = new Word("word1", "stem1");
		word2 = new Word("word2", "stem2");
		word3 = WordBuilder.start()
				.setLemma("word3")
				.setStem("stem3")
				.addComponent(0, 2,"wop")
				.addComponent(2, 5,"rd3")
				.setCompoundType(CompoundType.NATIVE)
				.create();
		term1 = TermBuilder.start(termIndex)
			.setRank(1)
			.addWord(word1, "L1")
			.addWord(word2, "L2")
			.addOccurrence(10, 12, doc2, "coveredText 3")
			.addOccurrence(20, 30, doc3, "coveredText 4")
			.setSpottingRule("spotRule1")
			.setSpecificity(1.1)
			.createAndAddToIndex();
		term2 = TermBuilder.start(termIndex)
				.setRank(2)
				.addWord(word1, "L1")
				.addWord(word2, "L2")
				.addWord(word3, "L3")
				.setSpottingRule("spotRule1")
				.addOccurrence(0, 2, doc2, "coveredText 1")
				.addOccurrence(10, 12, doc1, "coveredText 2")
				.addOccurrence(14, 20, doc2, "coveredText 2")
				.setSpecificity(2.2)
				.createAndAddToIndex();
		term1.addTermVariation(term2, VariationType.SYNTACTICAL, "variationRule1");
		term1.addTermVariation(term2, VariationType.GRAPHICAL, 0.956d);
		
		// generate context vectors
		ContextVector v = new ContextVector(term1);
		v.addEntry(term2, 21, 2.0);
		term1.setContextVector(v);
	}
	
	@Before
	public void initJsonTermIndex() {
		json1 = TestUtil.readFile(jsonFile1);
	}
	
	@Test
	public void testSaveLoadReturnWithNoVariant() throws IOException {
		term1.removeTermVariation(term1.getVariations(VariationType.SYNTACTICAL).iterator().next());
		StringWriter writer = new StringWriter();
		JSONTermIndexIO.save(writer, termIndex, new JSONOptions().withContexts(true).withOccurrences(true));
		String string = writer.toString();
		JSONTermIndexIO.load(new StringReader(string), new JSONOptions().withOccurrences(true));
	}



	@Test
	public void testSaveLoadReturn() throws IOException {
		StringWriter writer = new StringWriter();
		JSONTermIndexIO.save(writer, termIndex, new JSONOptions().withContexts(true).withOccurrences(true));
		String string = writer.toString();
		TermIndex termIndex2 = JSONTermIndexIO.load(new StringReader(string), new JSONOptions().withOccurrences(true));
		
		assertEquals(111, termIndex2.getSpottedTermsNum());
		assertEquals(222, termIndex2.getWordAnnotationsNum());
		assertThat(termIndex2.getTerms()).hasSameElementsAs(termIndex.getTerms());
		assertThat(termIndex2.getWords()).hasSameElementsAs(termIndex.getWords());
		for(Term t:termIndex.getTerms()) {
			Term t2 = termIndex2.getTermByGroupingKey(t.getGroupingKey());
			assertThat(t2.getOccurrences()).hasSameElementsAs(t.getOccurrences());
			assertThat(t2.getVariations()).hasSameElementsAs(t.getVariations());
			assertThat(t2.getBases()).hasSameElementsAs(t.getBases());
			assertThat(t2.getForms()).hasSameElementsAs(t.getForms());
			assertThat(t2.getFrequency()).isEqualTo(t.getFrequency());
			assertThat(t2.getSpecificity()).isEqualTo(t.getSpecificity());
			assertThat(t2.getFrequencyNorm()).isEqualTo(t.getFrequencyNorm());
			assertThat(t2.getGeneralFrequencyNorm()).isEqualTo(t.getGeneralFrequencyNorm());
			assertThat(t2.getSpottingRule()).isEqualTo(t.getSpottingRule());
			assertThat(t2.getPattern()).isEqualTo(t.getPattern());
			assertThat(t2.getWords()).isEqualTo(t.getWords());
			assertThat(t2.getRank()).isEqualTo(t.getRank());
			if(t2.getId() == term1.getId()) {
				assertTrue(t.isContextVectorComputed());
				assertTrue(t2.isContextVectorComputed());
				assertThat(t2.getContextVector()).isEqualTo(t.getContextVector());
			} else if(t2.getId() == term2.getId()) {
				assertFalse(t.isContextVectorComputed());
				assertFalse(t2.isContextVectorComputed());
			} else {
				fail("should never happen");
			}

		}
		for(Word w:termIndex.getWords()) {
			Word w2 = termIndex2.getWord(w.getLemma());
			assertThat(w2.getStem()).isEqualTo(w.getStem());
			assertThat(w2.isCompound()).isEqualTo(w.isCompound());
			assertThat(w2.getCompoundType()).isEqualTo(w.getCompoundType());
			assertThat(w2.getComponents()).hasSameElementsAs(w.getComponents());
		}
	}

	@Test
	public void testExportTermIndexToJsonWithoutOccurrences() throws IOException {
		StringWriter writer = new StringWriter();
		JSONTermIndexIO.save(writer, termIndex, new JSONOptions().withContexts(true).withOccurrences(false));
		ObjectMapper mapper = new ObjectMapper();
		Map<String,Object> map = mapper.readValue(writer.toString(), 
			    new TypeReference<HashMap<String,Object>>(){});
		@SuppressWarnings("unchecked")
		Map<String,Object> t1 = (Map<String,Object>)((List<?>)map.get("terms")).iterator().next();
		assertThat(t1.keySet()).contains("id", "key").doesNotContain("occurrences");
	}

	@Test
	public void testLoadJsonTermIndex() throws IOException {
		TermIndex termIndex = JSONTermIndexIO.load(new StringReader(json1), new JSONOptions().withOccurrences(true));
		
		assertEquals("Toto va à la plage", termIndex.getName());
		assertEquals("Toto va à la montagne", termIndex.getCorpusId());
		assertEquals(Lang.EN, termIndex.getLang());
		assertEquals(123, termIndex.getWordAnnotationsNum());
		assertEquals(456, termIndex.getSpottedTermsNum());

		// test term rank
		assertThat(termIndex.getTerms()).hasSize(3)
		.extracting("rank")
		.containsOnly(1, 2, 3)
		;

		
		// test terms
		assertThat(termIndex.getTerms()).hasSize(3)
			.extracting("groupingKey")
			.containsOnly("na: word1 word2", "n: word1", "a: word2")
			;
		
		// test terms
		Term t1 = termIndex.getTermByGroupingKey("na: word1 word2");
		Term t2 = termIndex.getTermByGroupingKey("n: word1");
		Term t3 = termIndex.getTermByGroupingKey("a: word2");
		assertThat(t1.getId()).isEqualTo(1);
		assertThat(t1.getSpecificity()).isCloseTo(0.321d, offset(0.000001d));
		assertThat(t1.getFrequencyNorm()).isCloseTo(0.123d, offset(0.000001d));
		assertThat(t1.getGeneralFrequencyNorm()).isCloseTo(0.025d, offset(0.000001d));
		assertThat(t1.getFrequency()).isEqualTo(6);
		assertThat(t1.getVariations(VariationType.GRAPHICAL)).extracting("variant").containsOnly(t2);
		assertThat(t1.getVariations(VariationType.SYNTACTICAL)).hasSize(0);
		assertThat(t1.getBases())
			.hasSize(2)
			.extracting("base")
			.containsOnly(t2, t3);	
		
		
		// test words
		assertThat(termIndex.getWords()).hasSize(2)
			.extracting("lemma", "stem")
			.containsOnly(
					tuple("word1", "stem1"), 
					tuple("word2", "stem2")
			);
		
		// test word composition
		
		Iterator<Word> iterator = termIndex.getWords().iterator();
		Word w1 = iterator.next();
		assertFalse(w1.isCompound());
		assertThat(w1.getComponents()).hasSize(0);
		Word w2 = iterator.next();
		assertTrue(w2.isCompound());
		assertThat(w2.getComponents())
			.extracting("lemma", "begin", "end")
			.containsOnly(
					tuple("wor", 0, 3), 
					tuple("d3", 3, 5)
			);

		
		assertThat(t1.getContextVector().getEntries())
			.hasSize(2)
			.extracting("coTerm.id", "nbCooccs", "assocRate")
			.contains(
				tuple(2, 18, 1.2000000476837158d),
				tuple(3, 12, 6.5d)
				);	

	}

	@Test
	public void testExportTermIndexToJsonWithOccurrencesAndContext() throws IOException {
		StringWriter writer = new StringWriter();
		JSONTermIndexIO.save(writer, termIndex, new JSONOptions().withContexts(true).withOccurrences(true));
		ObjectMapper mapper = new ObjectMapper();
//		System.out.println(writer.toString());
		Map<String,Object> map = mapper.readValue(writer.toString(), 
			    new TypeReference<HashMap<String,Object>>(){});
		assertThat(map.keySet()).hasSize(5).containsOnly("metadata", "words", "terms", "variations", "input_sources");
		

		// test metadata
		Map<String,String> metadata = (LinkedHashMap<String,String>)map.get("metadata");
		assertThat(metadata).containsOnlyKeys("name", "corpus-id", "wordsNum", "spottedTermsNum", "lang", "occurrence_storage");
		
		// test input sources1
		@SuppressWarnings("unchecked")
		Map<String,String> inputSources = (LinkedHashMap<String,String>)map.get("input_sources");
		assertThat(inputSources).containsOnlyKeys("1", "2", "3");
		assertThat(inputSources.values()).containsOnly("source1", "source2", "source3");

		// test words
		List<?> wordList = (List<?>)map.get("words");
		assertThat(wordList).hasSize(3).extracting("lemma").containsOnly("word1", "word2", "word3");
		
		
		LinkedHashMap<?,?> w3 = null;
		for(Object wl:wordList) {
			if(((LinkedHashMap<?, ?>)wl).get("lemma").equals("word3"))
				w3 = (LinkedHashMap<?,?>)wl;
		}
		
		assertEquals("word3", w3.get("lemma"));
		assertEquals("stem3", w3.get("stem"));
		assertEquals("NATIVE", w3.get("compound_type"));
		List<?> components = (List<?>)w3.get("components");
		assertThat(components).hasSize(2).extracting("lemma", "begin", "end").contains(tuple("wop", 0, 2), tuple("rd3", 2, 5));
		
		// test terms
		BiMap<String, String> sources = HashBiMap.create(inputSources);
		List<?> termList = (List<?>)map.get("terms");
		assertThat(termList).hasSize(2).extracting("id").containsOnly(term1.getId(), term2.getId());
		LinkedHashMap<?,?> t1 = (LinkedHashMap<?,?>)termList.get(0);
		assertThat(t1.get("rank")).isEqualTo(1);
		assertThat(t1.get("spec")).isEqualTo(1.1);
		assertThat((List<?>)t1.get("words")).extracting("lemma", "syn").containsOnly(tuple("word1", "L1"), tuple("word2", "L2"));
		assertThat((List<?>)t1.get("occurrences")).hasSize(2).extracting("begin", "end", "file", "text").containsOnly(
				tuple(10, 12, Integer.parseInt(sources.inverse().get("source2")), "coveredText 3"),
				tuple(20, 30, Integer.parseInt(sources.inverse().get("source3")), "coveredText 4")
				);
		final Map<?,?> t1Ctxt = (Map<?,?>)t1.get("context");
		assertEquals(21, t1Ctxt.get("total_cooccs"));
		assertThat((List<?>)t1Ctxt.get("cooccs"))
			.hasSize(1)
			.extracting("co_term", "cnt", "assoc_rate")
			.contains(tuple("l1l2l3: word1 word2 word3", 21, 2.0d));
		

		LinkedHashMap<?,?> t2 = (LinkedHashMap<?,?>)termList.get(1);
		assertThat((List<?>)t2.get("occurrences")).hasSize(3).extracting("begin", "end", "file", "text").containsOnly(
				tuple(0, 2, Integer.parseInt(sources.inverse().get("source2")), "coveredText 1"),
				tuple(10, 12, Integer.parseInt(sources.inverse().get("source1")), "coveredText 2"),
				tuple(14, 20, Integer.parseInt(sources.inverse().get("source2")), "coveredText 2")
			);
		assertThat((List<?>)t2.get("words")).extracting("lemma", "syn").containsOnly(tuple("word1", "L1"), tuple("word2", "L2"), tuple("word3", "L3"));

		
		// test syntactic variants
		List<?> variantList = (List<?>)map.get("variations");
		assertThat(variantList).hasSize(2)
			.extracting("base", "variant", "info", "type")
			.contains(
					tuple(term1.getGroupingKey(), term2.getGroupingKey(), "variationRule1", "syn"),
					tuple(term1.getGroupingKey(), term2.getGroupingKey(), "0.956", "graph")
				);
	}
}
