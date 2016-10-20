
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

package eu.project.ttc.test.func;

import static eu.project.ttc.test.TermSuiteAssertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.PrintStream;
import java.util.List;

import org.assertj.core.api.iterable.Extractor;
import org.assertj.core.groups.Tuple;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import eu.project.ttc.engines.cleaner.TermProperty;
import eu.project.ttc.engines.desc.Lang;
import eu.project.ttc.engines.desc.TermSuiteCollection;
import eu.project.ttc.models.Term;
import eu.project.ttc.models.TermIndex;
import eu.project.ttc.models.VariationType;
import eu.project.ttc.tools.TermSuitePipeline;
import eu.project.ttc.tools.TermSuiteResourceManager;
import eu.project.ttc.tools.utils.ControlFilesGenerator;
import eu.project.ttc.utils.TermIndexUtils;

public abstract class WindEnergySpec {

	protected TermIndex termIndex = null;
	protected Lang lang;
	protected List<String> syntacticMatchingRules = Lists.newArrayList();
	protected List<String> syntacticNotMatchingRules = Lists.newArrayList();

	
	public WindEnergySpec() {
		super();
		this.lang = getLang();
		this.syntacticMatchingRules = getSyntacticMatchingRules();
		this.syntacticNotMatchingRules = getSyntacticNotMatchingRules();
	}
	
	protected abstract Lang getLang();
	protected abstract List<String> getSyntacticMatchingRules();
	protected abstract List<String> getSyntacticNotMatchingRules();


	protected void expectNotMatchingRules(String... rules) {
		for(String rule:rules)
			syntacticNotMatchingRules.add(rule);
	}


	protected void expectMatchingRules(String... rules) {
		for(String rule:rules)
			syntacticMatchingRules.add(rule);		
	}
	
	private static final LoadingCache<Lang, TermIndex> TERM_INDEX_CACHE = CacheBuilder.newBuilder()
				.maximumSize(1)
				.build(new CacheLoader<Lang, TermIndex>() {
					@Override
					public TermIndex load(Lang lang) throws Exception {
						return runPipeline(lang);
					}
				});
	
	@Before
	public void setup() {
		this.termIndex = TERM_INDEX_CACHE.getUnchecked(lang);
	}

	protected static TermIndex runPipeline(Lang lang) {
		TermSuiteResourceManager manager = TermSuiteResourceManager.getInstance();
		manager.clear();
		
		TermSuitePipeline pipeline = TermSuitePipeline.create(lang.getCode())
			.setCollection(TermSuiteCollection.TXT, FunctionalTests.getCorpusWEPath(lang), "UTF-8")
			.aeWordTokenizer()
			.setTreeTaggerHome(FunctionalTests.getTaggerPath())
			.aeTreeTagger()
			.aeUrlFilter()
			.aeStemmer()
			.aeRegexSpotter()
			.aeStopWordsFilter()
			.aeSpecificityComputer()
			.aeCompostSplitter()
			.aePrefixSplitter()
			.aeSuffixDerivationDetector()
			.aeSyntacticVariantGatherer()
			.aeGraphicalVariantGatherer()
			.aeExtensionDetector()
			.aeRanker(TermProperty.SPECIFICITY, true)
			.run();
			
		return pipeline.getTermIndex();
	}

	@Test
	public void weControlSyntacticMatchingRules() {
		assertThat(termIndex)
			.asMatchingRules()
			.containsOnlyElementsOf(syntacticMatchingRules)
			.doesNotContainAnyElementsOf(syntacticNotMatchingRules);
		
	}

	@Test
	public void weControlPrefixes() {
		assertThat(termIndex)
			.asTermVariations(VariationType.IS_PREFIX_OF)
			.extracting("base.groupingKey", "variant.groupingKey")
			.containsOnly(
					ControlFiles.prefixVariationTuples(lang, "we")
			);
	}

	@Test
	public void weControlDerivates() {
		assertThat(termIndex)
		.asTermVariations(VariationType.DERIVES_INTO)
		.extracting("info", "base.groupingKey", "variant.groupingKey")
		.containsOnly(
				ControlFiles.derivateVariationTuples(lang, "we")
		);
	}

	@Test
	public void weCompounds() throws FileNotFoundException {
		assertThat(termIndex)
			.asCompoundList()
			.extracting(new Extractor<Term, Tuple>() {
				@Override
				public Tuple extract(Term compoundTerm) {
					return tuple(
							compoundTerm.getWords().get(0).getWord().getCompoundType().getShortName(),
							compoundTerm.getGroupingKey(),
							ControlFilesGenerator.toCompoundString(compoundTerm)
							);
				}
			})
			.containsOnly(
					ControlFiles.compoundTuples(lang, "we")
			);

	}
}
