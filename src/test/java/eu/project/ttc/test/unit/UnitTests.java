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
package eu.project.ttc.test.unit;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import eu.project.ttc.test.unit.engines.FixedExpressionSpotterSpec;
import eu.project.ttc.test.unit.engines.GraphicalVariantGathererSpec;
import eu.project.ttc.test.unit.engines.SyntacticTermGathererSpec;
import eu.project.ttc.test.unit.engines.TermClassifierSpec;
import eu.project.ttc.test.unit.engines.morpho.SegmentationSpec;
import eu.project.ttc.test.unit.io.JSONTermIndexIOSpec;
import eu.project.ttc.test.unit.io.SegmentationParserSpec;
import eu.project.ttc.test.unit.metrics.DiacriticInsensitiveLevenshteinSpec;
import eu.project.ttc.test.unit.metrics.SimilarityDistanceSpec;
import eu.project.ttc.test.unit.models.ContextVectorSpec;
import eu.project.ttc.test.unit.models.CrossTableSpec;
import eu.project.ttc.test.unit.models.DocumentSpec;
import eu.project.ttc.test.unit.models.TermClassSpec;
import eu.project.ttc.test.unit.models.TermSpec;
import eu.project.ttc.test.unit.readers.TermsuiteJsonCasSerializerDeserializerSpec;
import eu.project.ttc.test.unit.resources.PrefixTreeSpec;
import eu.project.ttc.test.unit.resources.SuffixDerivationListSpec;
import eu.project.ttc.test.unit.resources.SuffixDerivationSpec;
import eu.project.ttc.test.unit.selectors.HasSingleWordSelectorSpec;
import eu.project.ttc.test.unit.selectors.TermClassProvidersSpec;
import eu.project.ttc.test.unit.utils.OccurrenceBufferSpec;
import eu.project.ttc.test.unit.utils.StringUtilsSpec;
import eu.project.ttc.test.unit.utils.TermOccurrenceUtilsSpec;
import eu.project.ttc.test.unit.utils.TermUtilsSpec;
import eu.project.ttc.test.unit.variants.VariantRuleSpec;
import eu.project.ttc.test.unit.variants.VariantRuleYamlIOSpec;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	VariantRuleSpec.class, 
	VariantRuleYamlIOSpec.class,
	SyntacticTermGathererSpec.class,
	TermSpec.class, 
	SegmentationSpec.class, 
	DocumentSpec.class, 
	TermUtilsSpec.class,
	FixedExpressionSpotterSpec.class,
	TermClassifierSpec.class,
	OccurrenceBufferSpec.class,
	SimilarityDistanceSpec.class,
	JSONTermIndexIOSpec.class,
	ContextVectorSpec.class,
	TermOccurrenceUtilsSpec.class,
	TermClassSpec.class,
	StringUtilsSpec.class,
	PrefixTreeSpec.class,
	SegmentationParserSpec.class,
	SuffixDerivationListSpec.class,
	SuffixDerivationSpec.class,
	TermClassProvidersSpec.class,
	HasSingleWordSelectorSpec.class,
	DiacriticInsensitiveLevenshteinSpec.class,
	GraphicalVariantGathererSpec.class,
	CrossTableSpec.class, 
	TermsuiteJsonCasSerializerDeserializerSpec.class
//	TeiCollectionReaderSpec.class,
	})
public class UnitTests {

}
