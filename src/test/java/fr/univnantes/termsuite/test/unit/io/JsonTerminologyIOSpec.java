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
import static org.assertj.core.api.Assertions.offset;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import fr.univnantes.termsuite.framework.TermSuiteFactory;
import fr.univnantes.termsuite.index.Terminology;
import fr.univnantes.termsuite.io.json.JsonOptions;
import fr.univnantes.termsuite.io.json.JsonTerminologyIO;
import fr.univnantes.termsuite.model.CompoundType;
import fr.univnantes.termsuite.model.ContextVector;
import fr.univnantes.termsuite.model.IndexedCorpus;
import fr.univnantes.termsuite.model.Lang;
import fr.univnantes.termsuite.model.OccurrenceStore;
import fr.univnantes.termsuite.model.Relation;
import fr.univnantes.termsuite.model.RelationProperty;
import fr.univnantes.termsuite.model.RelationType;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.TermBuilder;
import fr.univnantes.termsuite.model.Word;
import fr.univnantes.termsuite.model.WordBuilder;
import fr.univnantes.termsuite.test.unit.UnitTests;
import fr.univnantes.termsuite.test.util.TestUtil;

@SuppressWarnings("unchecked")
public class JsonTerminologyIOSpec {
	public static final String jsonFile1 = "fr/univnantes/termsuite/test/json/termino1.json";
	
	private IndexedCorpus indexedCorpus;
	private Term term1;
	private Term term2;
	private Word word1;
	private Word word2;
	private Word word3;
	
	private String json1;
	
	@Before
	public void initTermino() {
		indexedCorpus = TermSuiteFactory.createIndexedCorpus(Lang.FR,"Titi va voir Toto");
		Terminology termino = indexedCorpus.getTerminology();
		OccurrenceStore store = indexedCorpus.getOccurrenceStore();
		
		termino.setCorpusId("ccid");
		termino.setNbWordAnnotations(new AtomicLong(222));
		termino.setNbSpottedTerms(new AtomicLong(111));
		word1 = new Word("word1", "stem1");
		word2 = new Word("word2", "stem2");
		word3 = new WordBuilder()
				.setLemma("word3")
				.setStem("stem3")
				.addComponent(0, 2,"wop")
				.addComponent(2, 5,"rd3")
				.setCompoundType(CompoundType.NATIVE)
				.create();
		term1 = TermBuilder.start(termino)
			.setRank(1)
			.setFrequency(2)
			.addWord(word1, "L1", true)
			.addWord(word2, "L2", true)
			.setSpottingRule("spotRule1")
			.setSpecificity(1.1)
			.create();
		store.addOccurrence(term1, "source2", 10, 12, "coveredText 3");
		store.addOccurrence(term1, "source3", 20, 30, "coveredText 4");
		UnitTests.addTerm(termino, term1);
		String form2 = "coveredText 2";
		term2 = TermBuilder.start(termino)
				.setRank(3)
				.setFrequency(2)
				.addWord(word1, "L1", true)
				.addWord(word2, "L2", false)
				.addWord(word3, "L3", true)
				.setSpottingRule("spotRule1")
				.setSpecificity(2.2)
				.create();
		store.addOccurrence(term2, "source2", 0, 2, "coveredText 1");
		store.addOccurrence(term2, "source1", 10, 12, form2);
		store.addOccurrence(term2, "source2", 14, 20, form2);
		UnitTests.addTerm(termino, term2);
		Relation rel1 = new Relation(RelationType.VARIATION, term1, term2);
		rel1.setProperty(RelationProperty.IS_SYNTAGMATIC, true);
		rel1.setProperty(RelationProperty.VARIATION_RULES, Sets.newHashSet("variationRule1"));
		termino.getRelations().add(rel1);
		Relation rel2 = new Relation(RelationType.HAS_EXTENSION, term1, term2);
		rel2.setProperty(RelationProperty.IS_GRAPHICAL, true);
		rel2.setProperty(RelationProperty.GRAPHICAL_SIMILARITY, 0.956d);
		termino.getRelations().add(rel2);
		
		// generate context vectors
		ContextVector v = new ContextVector(term1);
		v.addEntry(term2, 21, 2.0);
		term1.setContext(v);
	}
	
	@Before
	public void initJsonTermino() {
		json1 = TestUtil.readFile(jsonFile1);
	}
	
	
	
	@Test
	public void testSaveLoadReturnWithNoVariant() throws IOException {
		Terminology termino = indexedCorpus.getTerminology();
		Relation rel = termino.getRelations().stream()
				.filter(r -> r.getFrom().equals(term1))
				.findFirst()
				.get();
		termino.getRelations().remove(rel);
		StringWriter writer = new StringWriter();
		JsonTerminologyIO.save(writer, indexedCorpus, new JsonOptions().withContexts(true).withOccurrences(true));
		String string = writer.toString();
		JsonTerminologyIO.load(new StringReader(string), new JsonOptions().withOccurrences(true));
	}

	@Test
	public void testSaveLoadReturn() throws IOException {
		StringWriter writer = new StringWriter();
		JsonTerminologyIO.save(writer, indexedCorpus, new JsonOptions().withContexts(true).withOccurrences(true));
		String string = writer.toString();
		IndexedCorpus indexCorpus2 = JsonTerminologyIO.load(new StringReader(string), new JsonOptions().withOccurrences(true));
		Terminology termino2 = indexCorpus2.getTerminology();
		
		Terminology termino = indexedCorpus.getTerminology();
		assertEquals(111, termino2.getNbSpottedTerms().intValue());
		assertEquals(222, termino2.getNbWordAnnotations().intValue());
		assertThat(termino2.getTerms().values()).hasSameElementsAs(termino.getTerms().values());
		assertThat(termino2.getWords().values()).hasSameElementsAs(termino.getWords().values());
		assertThat(termino2.getRelations()).hasSameElementsAs(termino.getRelations());
		for(Term t:termino.getTerms().values()) {
			Term t2 = termino2.getTerms().get(t.getGroupingKey());
			assertThat(indexCorpus2.getOccurrenceStore().getOccurrences(t2)).hasSameElementsAs(indexedCorpus.getOccurrenceStore().getOccurrences(t));
			assertThat(t2.getFrequency()).isEqualTo(t.getFrequency());
			assertThat(t2.getSpecificity()).isEqualTo(t.getSpecificity());
			assertThat(t2.getFrequencyNorm()).isEqualTo(t.getFrequencyNorm());
			assertThat(t2.getGeneralFrequencyNorm()).isEqualTo(t.getGeneralFrequencyNorm());
			assertThat(t2.getSpottingRule()).isEqualTo(t.getSpottingRule());
			assertThat(t2.getPattern()).isEqualTo(t.getPattern());
			assertThat(t2.getWords()).isEqualTo(t.getWords());
			assertThat(t2.getRank()).isEqualTo(t.getRank());
			if(t2.getGroupingKey().equals(term1.getGroupingKey())) {
				assertNotNull(t.getContext());
				assertNotNull(t2.getContext());
				assertThat(t2.getContext()).isEqualTo(t.getContext());
			} else if(t2.getGroupingKey().equals(term2.getGroupingKey())) {
				assertNull(t.getContext());
				assertNotNull(t2.getContext());
			} else {
				fail("should never happen");
			}

		}
		for(Word w:termino.getWords().values()) {
			Word w2 = termino2.getWords().get(w.getLemma());
			assertThat(w2.getStem()).isEqualTo(w.getStem());
			assertThat(w2.isCompound()).isEqualTo(w.isCompound());
			assertThat(w2.getCompoundType()).isEqualTo(w.getCompoundType());
			assertThat(w2.getComponents()).hasSameElementsAs(w.getComponents());
		}
	}

	@Test
	public void testExportTerminoToJsonWithoutOccurrences() throws IOException {
		StringWriter writer = new StringWriter();
		JsonTerminologyIO.save(writer, indexedCorpus, new JsonOptions().withContexts(true).withOccurrences(false));
		ObjectMapper mapper = new ObjectMapper();
		Map<String,Object> map = mapper.readValue(writer.toString(), 
			    new TypeReference<HashMap<String,Object>>(){});
		Map<String,Object> t1 = (Map<String,Object>)((List<?>)map.get("terms")).iterator().next();
		assertThat(t1.keySet()).contains("props", "words", "context");
		assertThat((Map<String,Object>)t1.get("props"))
			.containsEntry("key", "l1l2: word1 word2")
			.containsEntry("spec", 1.1)
			.containsEntry("rank", 1)
			.containsEntry("pattern", "L1 L2")
			.containsEntry("freq", 2)
			.containsEntry("rule", "spotRule1")
			;
	}

	@Test
	public void testLoadJsonTermino() throws IOException {
		IndexedCorpus indexedCorpus2 = JsonTerminologyIO.load(new StringReader(json1), new JsonOptions().withOccurrences(true));
		
		Terminology termino2 = indexedCorpus2.getTerminology();
		assertEquals("Toto va à la plage", termino2.getName());
		assertEquals("Toto va à la montagne", termino2.getCorpusId());
		assertEquals(Lang.EN, termino2.getLang());
		assertEquals(123, termino2.getNbWordAnnotations().intValue());
		assertEquals(456, termino2.getNbSpottedTerms().intValue());

		// test term rank
		assertThat(termino2.getTerms().values()).hasSize(3)
		.extracting("rank")
		.containsOnly(1, 2, 3)
		;
		
		// test terms
		assertThat(termino2.getTerms().values()).hasSize(3)
			.extracting("groupingKey")
			.containsOnly("na: word1 word2", "n: word1", "a: word2")
			;
		
		// test terms
		Term t1 = termino2.getTerms().get("na: word1 word2");
		Term t2 = termino2.getTerms().get("n: word1");
		Term t3 = termino2.getTerms().get("a: word2");
		assertThat(t1.getSpecificity()).isCloseTo(0.321d, offset(0.000001d));
		assertThat(t1.getFrequencyNorm()).isCloseTo(0.123d, offset(0.000001d));
		assertThat(t1.getGeneralFrequencyNorm()).isCloseTo(0.025d, offset(0.000001d));
		assertThat(t1.getFrequency()).isEqualTo(6);
		assertThat(termino2
			.getRelations()
			).extracting("from", "type", "to").containsOnly(
				tuple(t2, RelationType.VARIATION, t1),
				tuple(t3, RelationType.VARIATION, t1),
				tuple(t1, RelationType.VARIATION, t2)
			);
		
		
		// test words
		assertThat(termino2.getWords().values()).hasSize(2)
			.extracting("lemma", "stem")
			.containsOnly(
					tuple("word1", "stem1"), 
					tuple("word2", "stem2")
			);
		
		// test word composition
		
		Iterator<Word> iterator = termino2.getWords().values().iterator();
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
		
		assertThat(t1.getContext().getEntries())
			.hasSize(2)
			.extracting("coTerm.groupingKey", "nbCooccs", "assocRate")
			.contains(
				tuple("n: word1", 18, 1.2000000476837158d),
				tuple("a: word2", 12, 6.5d)
				);	

	}

	@Test
	public void testExportTerminoToJsonWithOccurrencesAndContext() throws IOException {
		StringWriter writer = new StringWriter();
		JsonTerminologyIO.save(writer, indexedCorpus, new JsonOptions().withContexts(true).withOccurrences(true));
		ObjectMapper mapper = new ObjectMapper();
//		System.out.println(writer.toString());
		String string = writer.toString();
		Map<String,Object> map = mapper.readValue(string, 
			    new TypeReference<HashMap<String,Object>>(){});
		assertThat(map.keySet()).hasSize(6).containsOnly(
				"metadata", 
				"words", 
				"terms", 
				"relations", 
				"input_sources",
				"occurrences");
		

		// test metadata
		Map<String,String> metadata = (LinkedHashMap<String,String>)map.get("metadata");
		assertThat(metadata).containsOnlyKeys("name", "corpus-id", "wordsNum", "spottedTermsNum", "lang", "occurrence_storage");
		
		// test input sources1
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
		LinkedHashMap<?,?> t1 = (LinkedHashMap<?,?>)termList.get(0);
		LinkedHashMap<?,?> props = (LinkedHashMap<?, ?>) t1.get("props");
		assertThat(props.get("rank")).isEqualTo(1);
		assertThat(props.get("spec")).isEqualTo(1.1);
		assertThat((List<?>)t1.get("words")).extracting("lemma", "syn", "swt").containsOnly(tuple("word1", "L1", true), tuple("word2", "L2", true));
		final Map<?,?> t1Ctxt = (Map<?,?>)t1.get("context");
		assertEquals(21, t1Ctxt.get("total_cooccs"));
		assertThat((List<?>)t1Ctxt.get("cooccs"))
			.hasSize(1)
			.extracting("co_term", "cnt", "assoc_rate")
			.contains(tuple("l1l2l3: word1 word2 word3", 21, 2.0d));
		

		LinkedHashMap<?,?> t2 = (LinkedHashMap<?,?>)termList.get(1);
		assertThat((List<?>)t2.get("words")).extracting("lemma", "syn").containsOnly(tuple("word1", "L1"), tuple("word2", "L2"), tuple("word3", "L3"));

		
		/*
		 *  Test relations
		 */
		List<?> variantList = (List<?>)map.get("relations");
		assertThat(variantList)
			.hasSize(2)
			.extracting("from", "to", "type")
			.contains(
				tuple(term1.getGroupingKey(), term2.getGroupingKey(), "var")
			);
		
		variantList.sort(new Comparator<Object>() {
			@Override
			public int compare(Object o1, Object o2) {
				Map<String,Object> props1 = (Map<String, Object>) ((Map<String,Object>)o1).get("props");
				Map<String,Object> props2 = (Map<String, Object>) ((Map<String,Object>)o2).get("props");
				return Integer.compare(props1.hashCode(), props2.hashCode());
			}
		});
		Map<?,?> firstVariant = (Map<?,?>)variantList.get(0);
		Map<?,?> secondVariant = (Map<?,?>)variantList.get(1);
		
		Map<String,Object> relation = (Map<String,Object>)firstVariant.get("props");
		assertThat(relation)
			.containsEntry("vrules", Lists.newArrayList("variationRule1"))
			.containsEntry("isSyntag", true)
			.doesNotContainKey("graphSim");
		
		assertThat((Map<String,Object>)secondVariant.get("props"))
			.containsEntry("graphSim", 0.956)
			.containsEntry("isGraph", true)
			.doesNotContainKey("vrules");
		
		List<?> occurrences = (List<?>)map.get("occurrences");
		
		assertThat(occurrences).hasSize(5).extracting("tid", "begin", "end", "file", "text")
			.containsOnly(
				tuple("l1l2l3: word1 word2 word3", 0, 2, Integer.parseInt(sources.inverse().get("source2")), "coveredText 1"),
				tuple("l1l2l3: word1 word2 word3",10, 12, Integer.parseInt(sources.inverse().get("source1")), "coveredText 2"),
				tuple("l1l2l3: word1 word2 word3",14, 20, Integer.parseInt(sources.inverse().get("source2")), "coveredText 2"),
				tuple("l1l2: word1 word2",10, 12, Integer.parseInt(sources.inverse().get("source2")), "coveredText 3"),
				tuple("l1l2: word1 word2",20, 30, Integer.parseInt(sources.inverse().get("source3")), "coveredText 4")
			);
	}
}
