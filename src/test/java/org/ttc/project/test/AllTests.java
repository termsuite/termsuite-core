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
package org.ttc.project.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.ttc.project.test.engines.SyntacticTermGathererSpec;
import org.ttc.project.test.engines.TermClassifierSpec;
import org.ttc.project.test.io.SegmentationParserSpec;
import org.ttc.project.test.resources.PrefixTreeSpec;
import org.ttc.project.test.resources.SuffixDerivationListSpec;
import org.ttc.project.test.resources.SuffixDerivationSpec;
import org.ttc.project.test.selectors.HasSingleWordSelectorSpec;
import org.ttc.project.test.selectors.TermClassProvidersSpec;
import org.ttc.project.test.variants.VariantRuleSpec;
import org.ttc.project.test.variants.VariantRuleYamlIOSpec;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	VariantRuleSpec.class, 
	VariantRuleYamlIOSpec.class,
	SyntacticTermGathererSpec.class,
	TermSpec.class, 
	SegmentationSpec.class, 
	DocumentSpec.class, 
	TermUtilsSpec.class,
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
	CrossTableSpec.class
	})
public class AllTests {

}
