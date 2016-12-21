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
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import fr.univnantes.termsuite.api.JsonOptions;
import fr.univnantes.termsuite.engines.gatherer.VariationType;
import fr.univnantes.termsuite.model.CompoundType;
import fr.univnantes.termsuite.model.ContextVector;
import fr.univnantes.termsuite.model.Lang;
import fr.univnantes.termsuite.model.RelationProperty;
import fr.univnantes.termsuite.model.RelationType;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.TermBuilder;
import fr.univnantes.termsuite.model.TermRelation;
import fr.univnantes.termsuite.model.Terminology;
import fr.univnantes.termsuite.model.Word;
import fr.univnantes.termsuite.model.WordBuilder;
import fr.univnantes.termsuite.model.occurrences.MemoryOccurrenceStore;
import fr.univnantes.termsuite.model.termino.JsonTerminologyIO;
import fr.univnantes.termsuite.model.termino.MemoryTerminology;
import fr.univnantes.termsuite.test.unit.TestUtil;

@SuppressWarnings("unchecked")
public class JsonTerminologyIOSpec {
	public static final String jsonFile1 = "fr/univnantes/termsuite/test/json/termino1.json";
	
	private Terminology termino;
	private Term term1;
	private Term term2;
	private Word word1;
	private Word word2;
	private Word word3;
	
	private String json1;
	
	@Before
	public void initTermino() {
		termino = new MemoryTerminology("Titi va voir Toto", Lang.FR, new MemoryOccurrenceStore(Lang.FR));
		termino.setCorpusId("ccid");
		termino.setWordAnnotationsNum(222);
		termino.setSpottedTermsNum(111);
		word1 = new Word("word1", "stem1");
		word2 = new Word("word2", "stem2");
		word3 = WordBuilder.start()
				.setLemma("word3")
				.setStem("stem3")
				.addComponent(0, 2,"wop")
				.addComponent(2, 5,"rd3")
				.setCompoundType(CompoundType.NATIVE)
				.create();
		term1 = TermBuilder.start(termino)
			.setRank(1)
			.addWord(word1, "L1", true)
			.addWord(word2, "L2", true)
			.addOccurrence(10, 12, "source2", "coveredText 3")
			.addOccurrence(20, 30, "source3", "coveredText 4")
			.setSpottingRule("spotRule1")
			.setSpecificity(1.1)
			.createAndAddToIndex();
		String form2 = "coveredText 2";
		term2 = TermBuilder.start(termino)
				.setRank(2)
				.addWord(word1, "L1", true)
				.addWord(word2, "L2", false)
				.addWord(word3, "L3", true)
				.setSpottingRule("spotRule1")
				.addOccurrence(0, 2, "source2", "coveredText 1")
				.addOccurrence(10, 12, "source1", form2)
				.addOccurrence(14, 20, "source2", form2)
				.setSpecificity(2.2)
				.createAndAddToIndex();
		TermRelation rel1 = new TermRelation(RelationType.VARIATION, term1, term2);
		rel1.setProperty(RelationProperty.VARIATION_TYPE, VariationType.SYNTAGMATIC);
		rel1.setProperty(RelationProperty.VARIATION_RULE, "variationRule1");
		termino.addRelation(rel1);
		TermRelation rel2 = new TermRelation(RelationType.VARIATION, term1, term2);
		rel2.setProperty(RelationProperty.VARIATION_TYPE, VariationType.GRAPHICAL);
		rel2.setProperty(RelationProperty.GRAPHICAL_SIMILARITY, 0.956d);
		termino.addRelation(rel2);
		
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
		termino.removeRelation(termino.getOutboundRelations(term1, RelationType.VARIATION).iterator().next());
		StringWriter writer = new StringWriter();
		JsonTerminologyIO.save(writer, termino, new JsonOptions().withContexts(true).withOccurrences(true));
		String string = writer.toString();
		JsonTerminologyIO.load(new StringReader(string), new JsonOptions().withOccurrences(true));
	}



	@Test
	public void testSaveLoadReturn() throws IOException {
		StringWriter writer = new StringWriter();
		JsonTerminologyIO.save(writer, termino, new JsonOptions().withContexts(true).withOccurrences(true));
		String string = writer.toString();
		Terminology termino2 = JsonTerminologyIO.load(new StringReader(string), new JsonOptions().withOccurrences(true));
		
		assertEquals(111, termino2.getSpottedTermsNum());
		assertEquals(222, termino2.getWordAnnotationsNum());
		assertThat(termino2.getTerms()).hasSameElementsAs(termino.getTerms());
		assertThat(termino2.getWords()).hasSameElementsAs(termino.getWords());
		for(Term t:termino.getTerms()) {
			Term t2 = termino2.getTermByGroupingKey(t.getGroupingKey());
			assertThat(termino2.getOccurrenceStore().getOccurrences(t2)).hasSameElementsAs(termino.getOccurrenceStore().getOccurrences(t));
			assertThat(termino2.getOutboundRelations(t2)).hasSameElementsAs(termino.getOutboundRelations(t));
			assertThat(termino2.getInboundRelations(t2)).hasSameElementsAs(termino.getInboundRelations(t));
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
				assertNull(t2.getContext());
			} else {
				fail("should never happen");
			}

		}
		for(Word w:termino.getWords()) {
			Word w2 = termino2.getWord(w.getLemma());
			assertThat(w2.getStem()).isEqualTo(w.getStem());
			assertThat(w2.isCompound()).isEqualTo(w.isCompound());
			assertThat(w2.getCompoundType()).isEqualTo(w.getCompoundType());
			assertThat(w2.getComponents()).hasSameElementsAs(w.getComponents());
		}
	}

	@Test
	public void testExportTerminoToJsonWithoutOccurrences() throws IOException {
		StringWriter writer = new StringWriter();
		JsonTerminologyIO.save(writer, termino, new JsonOptions().withContexts(true).withOccurrences(false));
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
		Terminology termino = JsonTerminologyIO.load(new StringReader(json1), new JsonOptions().withOccurrences(true));
		
		assertEquals("Toto va à la plage", termino.getName());
		assertEquals("Toto va à la montagne", termino.getCorpusId());
		assertEquals(Lang.EN, termino.getLang());
		assertEquals(123, termino.getWordAnnotationsNum());
		assertEquals(456, termino.getSpottedTermsNum());

		// test term rank
		assertThat(termino.getTerms()).hasSize(3)
		.extracting("rank")
		.containsOnly(1, 2, 3)
		;

		
		// test terms
		assertThat(termino.getTerms()).hasSize(3)
			.extracting("groupingKey")
			.containsOnly("na: word1 word2", "n: word1", "a: word2")
			;
		
		// test terms
		Term t1 = termino.getTermByGroupingKey("na: word1 word2");
		Term t2 = termino.getTermByGroupingKey("n: word1");
		Term t3 = termino.getTermByGroupingKey("a: word2");
		assertThat(t1.getSpecificity()).isCloseTo(0.321d, offset(0.000001d));
		assertThat(t1.getFrequencyNorm()).isCloseTo(0.123d, offset(0.000001d));
		assertThat(t1.getGeneralFrequencyNorm()).isCloseTo(0.025d, offset(0.000001d));
		assertThat(t1.getFrequency()).isEqualTo(6);
		assertThat(termino
				.getOutboundRelations(t1, RelationType.VARIATION)
				.stream()
				.filter(tv -> tv.get(RelationProperty.VARIATION_TYPE) == VariationType.GRAPHICAL)
				.collect(Collectors.toList())
				).extracting("to").containsOnly(t2);
		assertThat(termino.getOutboundRelations(t1, RelationType.VARIATION)
				.stream()
				.filter(tv -> tv.get(RelationProperty.VARIATION_TYPE) == VariationType.SYNTAGMATIC)
				.collect(Collectors.toList())
				).hasSize(0);
		assertThat(termino.getInboundRelations(t1))
			.hasSize(2)
			.extracting("from")
			.containsOnly(t2, t3);	
		
		
		// test words
		assertThat(termino.getWords()).hasSize(2)
			.extracting("lemma", "stem")
			.containsOnly(
					tuple("word1", "stem1"), 
					tuple("word2", "stem2")
			);
		
		// test word composition
		
		Iterator<Word> iterator = termino.getWords().iterator();
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
		JsonTerminologyIO.save(writer, termino, new JsonOptions().withContexts(true).withOccurrences(true));
		ObjectMapper mapper = new ObjectMapper();
//		System.out.println(writer.toString());
		Map<String,Object> map = mapper.readValue(writer.toString(), 
			    new TypeReference<HashMap<String,Object>>(){});
		assertThat(map.keySet()).hasSize(5).containsOnly("metadata", "words", "terms", "relations", "input_sources");
		

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
		List<?> variantList = (List<?>)map.get("relations");
		assertThat(variantList)
			.extracting("from", "to", "type")
			.contains(
				tuple(term1.getGroupingKey(), term2.getGroupingKey(), "var"),
				tuple(term1.getGroupingKey(), term2.getGroupingKey(), "var")
			);
		
		assertThat((Map<String,Object>)((Map<?,?>)variantList.get(0)).get("props"))
			.containsEntry("vrule", "variationRule1")
			.containsEntry("vtype", "syn")
			.doesNotContainKey("graphSim");

		assertThat((Map<String,Object>)((Map<?,?>)variantList.get(1)).get("props"))
			.containsEntry("graphSim", 0.956)
			.containsEntry("vtype", "graph")
			.doesNotContainKey("vrule");
	}
}
