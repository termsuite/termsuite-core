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

import fr.univnantes.termsuite.test.termino.export.TsvExporterSpec;
import fr.univnantes.termsuite.test.unit.api.TraverserSpec;
import fr.univnantes.termsuite.test.unit.engines.ContextualizerSpec;
import fr.univnantes.termsuite.test.unit.engines.FixedExpressionSpotterSpec;
import fr.univnantes.termsuite.test.unit.engines.GraphicalVariantGathererSpec;
import fr.univnantes.termsuite.test.unit.engines.GroovyServiceSpec;
import fr.univnantes.termsuite.test.unit.engines.SuffixDerivationExceptionSetterSpec;
import fr.univnantes.termsuite.test.unit.engines.TermGathererSpec;
import fr.univnantes.termsuite.test.unit.engines.YamlRuleSetIOSpec;
import fr.univnantes.termsuite.test.unit.engines.YamlRuleSetIOSpecSynonymic;
import fr.univnantes.termsuite.test.unit.engines.morpho.SegmentationSpec;
import fr.univnantes.termsuite.test.unit.io.JsonTerminologyIOSpec;
import fr.univnantes.termsuite.test.unit.io.SegmentationParserSpec;
import fr.univnantes.termsuite.test.unit.metrics.DiacriticInsensitiveLevenshteinSpec;
import fr.univnantes.termsuite.test.unit.metrics.FastDiacriticInsensitiveLevenshteinSpec;
import fr.univnantes.termsuite.test.unit.metrics.LevenshteinSpec;
import fr.univnantes.termsuite.test.unit.metrics.SimilarityDistanceSpec;
import fr.univnantes.termsuite.test.unit.models.ContextVectorSpec;
import fr.univnantes.termsuite.test.unit.models.CrossTableSpec;
import fr.univnantes.termsuite.test.unit.models.DocumentViewSpec;
import fr.univnantes.termsuite.test.unit.models.MemoryTerminologySpec;
import fr.univnantes.termsuite.test.unit.models.TermSpec;
import fr.univnantes.termsuite.test.unit.readers.TermsuiteJsonCasSerializerDeserializerSpec;
import fr.univnantes.termsuite.test.unit.resources.PrefixTreeSpec;
import fr.univnantes.termsuite.test.unit.resources.SuffixDerivationListSpec;
import fr.univnantes.termsuite.test.unit.resources.SuffixDerivationSpec;
import fr.univnantes.termsuite.test.unit.selectors.HasSingleWordSelectorSpec;
import fr.univnantes.termsuite.test.unit.selectors.TermValueProvidersSpec;
import fr.univnantes.termsuite.test.unit.tools.TermSuitePipelineSpec;
import fr.univnantes.termsuite.test.unit.utils.OccurrenceBufferSpec;
import fr.univnantes.termsuite.test.unit.utils.StringUtilsSpec;
import fr.univnantes.termsuite.test.unit.utils.TermOccurrenceUtilsSpec;
import fr.univnantes.termsuite.test.unit.utils.TermUtilsSpec;
import fr.univnantes.termsuite.utils.test.CollectionUtilsSpec;
import fr.univnantes.termsuite.utils.test.CompoundUtilsSpec;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	GroovyServiceSpec.class, 
	YamlRuleSetIOSpec.class,
	TermGathererSpec.class,
	TermSpec.class, 
	SegmentationSpec.class, 
	DocumentViewSpec.class, 
	TermUtilsSpec.class,
	FixedExpressionSpotterSpec.class,
	OccurrenceBufferSpec.class,
	SimilarityDistanceSpec.class,
	JsonTerminologyIOSpec.class,
	ContextVectorSpec.class,
	TermOccurrenceUtilsSpec.class,
	CollectionUtilsSpec.class,
	CompoundUtilsSpec.class,
	StringUtilsSpec.class,
	PrefixTreeSpec.class,
	SegmentationParserSpec.class,
	SuffixDerivationListSpec.class,
	SuffixDerivationExceptionSetterSpec.class,
	SuffixDerivationSpec.class,
	TermValueProvidersSpec.class,
	HasSingleWordSelectorSpec.class,
	DiacriticInsensitiveLevenshteinSpec.class,
	GraphicalVariantGathererSpec.class,
	CrossTableSpec.class, 
	TermsuiteJsonCasSerializerDeserializerSpec.class,
	TsvExporterSpec.class,
	TraverserSpec.class,
	YamlRuleSetIOSpecSynonymic.class,
	ContextualizerSpec.class,
	LevenshteinSpec.class,
	FastDiacriticInsensitiveLevenshteinSpec.class,
	MemoryTerminologySpec.class,
	TermSuitePipelineSpec.class
//	TeiCollectionReaderSpec.class,
	})
public class UnitTests {

}
