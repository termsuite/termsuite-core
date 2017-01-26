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

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

import fr.univnantes.termsuite.SimpleEngine;
import fr.univnantes.termsuite.framework.Engine;
import fr.univnantes.termsuite.framework.EngineDescription;
import fr.univnantes.termsuite.framework.EngineInjector;
import fr.univnantes.termsuite.framework.TermSuiteFactory;
import fr.univnantes.termsuite.framework.modules.ExtractorModule;
import fr.univnantes.termsuite.framework.modules.ResourceModule;
import fr.univnantes.termsuite.framework.pipeline.EngineRunner;
import fr.univnantes.termsuite.model.Terminology;
import fr.univnantes.termsuite.test.unit.api.ExtractorConfigIOSpec;
import fr.univnantes.termsuite.test.unit.api.PreprocessorSpec;
import fr.univnantes.termsuite.test.unit.api.ResourceConfigSpec;
import fr.univnantes.termsuite.test.unit.api.TerminoExtractorSpec;
import fr.univnantes.termsuite.test.unit.api.TraverserSpec;
import fr.univnantes.termsuite.test.unit.engines.YamlRuleSetIOSpec;
import fr.univnantes.termsuite.test.unit.engines.YamlRuleSetIOSynonymicSpec;
import fr.univnantes.termsuite.test.unit.engines.contextualizer.ContextualizerSpec;
import fr.univnantes.termsuite.test.unit.engines.contextualizer.ContextualizerSpec2;
import fr.univnantes.termsuite.test.unit.engines.contextualizer.DocumentViewSpec;
import fr.univnantes.termsuite.test.unit.engines.gatherer.GraphicalVariantGathererSpec;
import fr.univnantes.termsuite.test.unit.engines.gatherer.GroovyServiceSpec;
import fr.univnantes.termsuite.test.unit.engines.gatherer.TermGathererSpec;
import fr.univnantes.termsuite.test.unit.engines.postproc.IndependanceScorerSpec;
import fr.univnantes.termsuite.test.unit.engines.postproc.VariantScorerSpec;
import fr.univnantes.termsuite.test.unit.engines.splitter.SegmentationSpec;
import fr.univnantes.termsuite.test.unit.engines.splitter.SuffixDerivationExceptionSetterSpec;
import fr.univnantes.termsuite.test.unit.export.TsvExporterSpec;
import fr.univnantes.termsuite.test.unit.framework.TermSuiteResourceManagerSpec;
import fr.univnantes.termsuite.test.unit.io.JsonTerminologyIOSpec;
import fr.univnantes.termsuite.test.unit.io.SegmentationParserSpec;
import fr.univnantes.termsuite.test.unit.metrics.DiacriticInsensitiveLevenshteinSpec;
import fr.univnantes.termsuite.test.unit.metrics.FastDiacriticInsensitiveLevenshteinSpec;
import fr.univnantes.termsuite.test.unit.metrics.LevenshteinSpec;
import fr.univnantes.termsuite.test.unit.metrics.SimilarityDistanceSpec;
import fr.univnantes.termsuite.test.unit.models.ContextVectorSpec;
import fr.univnantes.termsuite.test.unit.models.MemoryTerminologySpec;
import fr.univnantes.termsuite.test.unit.models.TermSpec;
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
	TraverserSpec.class,
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
	IndependanceScorerSpec.class,
	VariantScorerSpec.class,
	SegmentationSpec.class, 
	SuffixDerivationExceptionSetterSpec.class,
	YamlRuleSetIOSpec.class,
	YamlRuleSetIOSynonymicSpec.class,

	
	/*
	 * Framework
	 */
	TermSuiteResourceManagerSpec.class,

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
	MemoryTerminologySpec.class,
	TermSpec.class,
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

	public static  EngineRunner createEngineRunner(Terminology terminology, Class<? extends Engine> cls, Object... parameters) {
		return createEngineRunner(terminology, cls, new ResourceModule(), parameters);
	}

	public static  EngineRunner createEngineRunner(Terminology terminology, Class<? extends Engine> cls,
			Module resourceModule, Object... parameters) {
		return createEngineRunner(cls, extractorInjector(terminology, resourceModule), parameters);
	}

	public static  EngineRunner createEngineRunner(Class<? extends Engine> cls, Injector injector, Object... parameters) {
		EngineDescription description = new EngineDescription(cls.getSimpleName(), cls, parameters);
		return TermSuiteFactory.createEngineRunner(description, injector, null);
	}

	public static Injector extractorInjector(Terminology terminology) {
		Injector injector = Guice.createInjector(new ResourceModule(), new ExtractorModule(terminology));
		return injector;
	}

	public static Injector extractorInjector(Terminology terminology, Module resourceModule) {
		Injector injector = Guice.createInjector(resourceModule, new ExtractorModule(terminology));
		return injector;
	}

	public static <T extends SimpleEngine> T createSimpleEngine(Terminology terminology, Class<T> cls, Object... parameters) {
		Injector guiceInjector = TermSuiteFactory.createExtractorInjector(terminology, null, null);
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

}
