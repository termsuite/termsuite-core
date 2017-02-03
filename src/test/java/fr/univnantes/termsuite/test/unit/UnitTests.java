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
package fr.univnantes.termsuite.test.unit;

import static java.util.stream.Collectors.toSet;

import java.lang.reflect.Field;
import java.util.Set;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import com.google.common.base.Preconditions;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

import fr.univnantes.termsuite.engines.SimpleEngine;
import fr.univnantes.termsuite.framework.Engine;
import fr.univnantes.termsuite.framework.EngineDescription;
import fr.univnantes.termsuite.framework.EngineInjector;
import fr.univnantes.termsuite.framework.TermSuiteFactory;
import fr.univnantes.termsuite.framework.modules.IndexedCorpusModule;
import fr.univnantes.termsuite.framework.modules.ResourceModule;
import fr.univnantes.termsuite.framework.pipeline.EngineRunner;
import fr.univnantes.termsuite.framework.service.TerminologyService;
import fr.univnantes.termsuite.index.Terminology;
import fr.univnantes.termsuite.model.IndexedCorpus;
import fr.univnantes.termsuite.model.Lang;
import fr.univnantes.termsuite.model.Relation;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.TermBuilder;
import fr.univnantes.termsuite.model.TermWord;
import fr.univnantes.termsuite.model.Word;
import fr.univnantes.termsuite.model.occurrences.EmptyOccurrenceStore;
import fr.univnantes.termsuite.test.unit.api.ExtractorConfigIOSpec;
import fr.univnantes.termsuite.test.unit.api.PreprocessorSpec;
import fr.univnantes.termsuite.test.unit.api.ResourceConfigSpec;
import fr.univnantes.termsuite.test.unit.api.TerminoExtractorSpec;
import fr.univnantes.termsuite.test.unit.engines.YamlRuleSetIOSpec;
import fr.univnantes.termsuite.test.unit.engines.YamlRuleSetIOSynonymicSpec;
import fr.univnantes.termsuite.test.unit.engines.contextualizer.ContextualizerSpec;
import fr.univnantes.termsuite.test.unit.engines.contextualizer.ContextualizerSpec2;
import fr.univnantes.termsuite.test.unit.engines.contextualizer.DocumentViewSpec;
import fr.univnantes.termsuite.test.unit.engines.gatherer.GraphicalVariantGathererSpec;
import fr.univnantes.termsuite.test.unit.engines.gatherer.GroovyServiceSpec;
import fr.univnantes.termsuite.test.unit.engines.gatherer.RelationPairsBasedGathererSpec;
import fr.univnantes.termsuite.test.unit.engines.gatherer.TermGathererSpec;
import fr.univnantes.termsuite.test.unit.engines.postproc.IndependanceScorerSpec;
import fr.univnantes.termsuite.test.unit.engines.postproc.VariantScorerSpec;
import fr.univnantes.termsuite.test.unit.engines.splitter.ManualSuffixDerivationDetecterSpec;
import fr.univnantes.termsuite.test.unit.engines.splitter.SegmentationSpec;
import fr.univnantes.termsuite.test.unit.export.TsvExporterSpec;
import fr.univnantes.termsuite.test.unit.framework.TermSuiteResourceManagerSpec;
import fr.univnantes.termsuite.test.unit.framework.service.TerminologyServiceSpec;
import fr.univnantes.termsuite.test.unit.io.JsonTerminologyIOSpec;
import fr.univnantes.termsuite.test.unit.io.SegmentationParserSpec;
import fr.univnantes.termsuite.test.unit.metrics.DiacriticInsensitiveLevenshteinSpec;
import fr.univnantes.termsuite.test.unit.metrics.FastDiacriticInsensitiveLevenshteinSpec;
import fr.univnantes.termsuite.test.unit.metrics.LevenshteinSpec;
import fr.univnantes.termsuite.test.unit.metrics.SimilarityDistanceSpec;
import fr.univnantes.termsuite.test.unit.models.ContextVectorSpec;
import fr.univnantes.termsuite.test.unit.models.TermValueProvidersSpec;
import fr.univnantes.termsuite.test.unit.readers.TermsuiteJsonCasSerializerDeserializerSpec;
import fr.univnantes.termsuite.test.unit.resources.PrefixTreeSpec;
import fr.univnantes.termsuite.test.unit.resources.SuffixDerivationListSpec;
import fr.univnantes.termsuite.test.unit.resources.SuffixDerivationSpec;
import fr.univnantes.termsuite.test.unit.uima.engines.FixedExpressionSpotterSpec;
import fr.univnantes.termsuite.test.unit.utils.CollectionUtilsSpec;
import fr.univnantes.termsuite.test.unit.utils.CompoundUtilsSpec;
import fr.univnantes.termsuite.test.unit.utils.FileUtilsSpec;
import fr.univnantes.termsuite.test.unit.utils.OccurrenceBufferSpec;
import fr.univnantes.termsuite.test.unit.utils.StringUtilsSpec;
import fr.univnantes.termsuite.test.unit.utils.TermOccurrenceUtilsSpec;
import fr.univnantes.termsuite.test.unit.utils.TermUtilsSpec;

@RunWith(Suite.class)
@Suite.SuiteClasses({

	/*
	 * API
	 */
	ExtractorConfigIOSpec.class,
	TerminoExtractorSpec.class,
	PreprocessorSpec.class,
	ResourceConfigSpec.class,
	
	/*
	 * Engines
	 */
	ContextualizerSpec.class,
	ContextualizerSpec2.class, 
	DocumentViewSpec.class,
	GraphicalVariantGathererSpec.class,
	GroovyServiceSpec.class, 
	TermGathererSpec.class,
	RelationPairsBasedGathererSpec.class,
	IndependanceScorerSpec.class,
	VariantScorerSpec.class,
	SegmentationSpec.class, 
	ManualSuffixDerivationDetecterSpec.class,
	YamlRuleSetIOSpec.class,
	YamlRuleSetIOSynonymicSpec.class,
	

	
	/*
	 * Framework
	 */
	TermSuiteResourceManagerSpec.class,
	TerminologyServiceSpec.class,

	/*
	 * IO
	 */
	JsonTerminologyIOSpec.class,
	SegmentationParserSpec.class,

	/*
	 * Metrics
	 */
	DiacriticInsensitiveLevenshteinSpec.class,
	FastDiacriticInsensitiveLevenshteinSpec.class,
	LevenshteinSpec.class,
	SimilarityDistanceSpec.class,

	/*
	 * Export
	 */
	TsvExporterSpec.class,
	
	/*
	 * Models
	 */
	ContextVectorSpec.class,
	TermValueProvidersSpec.class,

	/*
	 * Readers
	 */
	TermsuiteJsonCasSerializerDeserializerSpec.class,
	
	/*
	 * Resources
	 */
	PrefixTreeSpec.class,
	SuffixDerivationListSpec.class,
	SuffixDerivationSpec.class,

	/*
	 * UIMA Engines
	 */
	FixedExpressionSpotterSpec.class,

	/*
	 * Utils
	 */
	FileUtilsSpec.class,
	TermUtilsSpec.class,
	StringUtilsSpec.class,
	OccurrenceBufferSpec.class,
	TermOccurrenceUtilsSpec.class,
	TermUtilsSpec.class,
	CollectionUtilsSpec.class,
	CompoundUtilsSpec.class,
	
	})
public class UnitTests {
	
	public static MockResourceModule mockResourceModule() {
		return new MockResourceModule();
	}

	public static  EngineRunner createEngineRunner(IndexedCorpus corpus, Class<? extends Engine> cls,
			Module resourceModule, Object... parameters) {
		return createEngineRunner(cls, extractorInjector(corpus, resourceModule), parameters);
	}

	public static  EngineRunner createEngineRunner(Class<? extends Engine> cls, Injector injector, Object... parameters) {
		EngineDescription description = new EngineDescription(cls.getSimpleName(), cls, parameters);
		return TermSuiteFactory.createEngineRunner(description, injector, null);
	}

	public static Injector extractorInjector(IndexedCorpus corpus) {
		return extractorInjector(corpus, new ResourceModule());
	}

	public static Injector extractorInjector(IndexedCorpus corpus, ResourceModule resourceModule) {
		Injector injector = Guice.createInjector(resourceModule, new IndexedCorpusModule(corpus));
		return injector;
	}

	public static Injector extractorInjector(IndexedCorpus corpus, Module resourceModule) {
		Injector injector = Guice.createInjector(resourceModule, new IndexedCorpusModule(corpus));
		return injector;
	}


	public static <T extends SimpleEngine> T createSimpleEngine(IndexedCorpus corpus, Class<T> cls, Object... parameters) {
		Injector guiceInjector = TermSuiteFactory.createExtractorInjector(corpus, null, null);
		EngineInjector engineInjector = new EngineInjector(cls, guiceInjector);
		
		T engine;
		try {
			engine = guiceInjector.getInstance(cls);
			EngineDescription description = new EngineDescription(cls.getSimpleName(), cls, parameters);
			engineInjector.injectName(engine, cls.getSimpleName());
			engineInjector.injectParameters(engine, description.getParameters());
			engineInjector.injectResources(engine);
			engineInjector.injectIndexes(engine);

			return engine;
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
	}

	public static Term addTerm(Terminology terminology, Term term) {
		Preconditions.checkArgument(!terminology.getTerms().containsKey(term.getGroupingKey()), "Term %s already in termino", term);
		Set<Word> words = term.getWords().stream().map(TermWord::getWord).collect(toSet());
		for(Word word:words)
			if(!terminology.getWords().containsKey(word.getLemma()))
				terminology.getWords().put(word.getLemma(), word);
		terminology.getTerms().put(term.getGroupingKey(), term);
		return term;
		
	}
	public static Term addTerm(Terminology terminology, String gKey) {
		Term term = new TermBuilder().setGroupingKey(gKey).create();
		terminology.getTerms().put(gKey, term);
		return term;
	}

	public static void addRelation(Terminology terminology, Relation r) {
		terminology.getOutboundRelations().put(r.getFrom(), r);
	}

	public static TerminologyService getTerminologyService(IndexedCorpus corpus) {
		return extractorInjector(corpus).getInstance(TerminologyService.class);
	}

	public static Term createTerm(String gKey) {
		return new TermBuilder().setGroupingKey(gKey).create();
	}

	public static void setField(Object instance, String fieldName, Object value) {
		try {
			
			Class<? extends Object> cls = instance.getClass();
			while(cls != null) {
				for(Field field:cls.getDeclaredFields()) {
					if(field.getName().equals(fieldName)) {
						field.setAccessible(true);
						field.set(instance, value);
						return;
					}
				}
				cls = cls.getSuperclass();
			}
			throw new RuntimeException(String.format("No such field %s for class %s", fieldName, instance.getClass().getName()));

			
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static <T extends SimpleEngine> void injectIndexes(
			T engine, 
			String string, 
			Terminology terminology) {
		
		IndexedCorpus indexedCorpus = TermSuiteFactory.createIndexedCorpus(
				terminology, 
				new EmptyOccurrenceStore(Lang.EN));
		new EngineInjector(engine.getClass(), extractorInjector(indexedCorpus)).injectIndexes(engine);
	}

}
