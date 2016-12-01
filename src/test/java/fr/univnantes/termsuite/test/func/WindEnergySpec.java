
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

package fr.univnantes.termsuite.test.func;

import static fr.univnantes.termsuite.test.TermSuiteAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.yaml.snakeyaml.Yaml;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import fr.univnantes.termsuite.model.Lang;
import fr.univnantes.termsuite.model.RelationType;
import fr.univnantes.termsuite.model.TermIndex;
import fr.univnantes.termsuite.model.TermProperty;
import fr.univnantes.termsuite.model.TermSuiteCollection;
import fr.univnantes.termsuite.test.unit.TermSuiteExtractors;
import fr.univnantes.termsuite.tools.ControlFilesGenerator;
import fr.univnantes.termsuite.uima.TermSuitePipeline;
import fr.univnantes.termsuite.uima.TermSuiteResource;
import fr.univnantes.termsuite.utils.TermSuiteResourceManager;

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
						TermIndex termIndex = runPipeline(lang);
						File controlDir = FunctionalTests.getFunctionalTestsControlDir().resolve("we-" + lang.getCode()).toFile();
						controlDir.mkdirs();
						new ControlFilesGenerator(termIndex).generate(controlDir);
						return termIndex;
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
			.aeTermVariantGatherer()
			.aeGraphicalVariantGatherer()
			.aeExtensionDetector()
			.aeExtensionVariantGatherer()
			.aeRanker(TermProperty.SPECIFICITY, true)
			.run();
			
		return pipeline.getTermIndex();
	}

	@Test
	public void weControlSyntacticMatchingRules() {
		try(InputStream openStream = TermSuiteResource.VARIANTS.fromClasspath(lang).openStream()) {
			@SuppressWarnings("unchecked")
			Set<String> ruleNames = (Set<String>) ((Map<?, ?>)new Yaml().load(openStream)).keySet();
			
			assertThat(ruleNames)
				.containsAll(syntacticMatchingRules);
			
			syntacticMatchingRules.stream().forEach(rule-> {
				assertTrue(String.format("Bad rule name. Rule <%s> does not exist", rule),
						ruleNames.contains(rule));
			});
	
			syntacticNotMatchingRules.stream().forEach(rule-> {
				assertTrue(String.format("Bad rule name. Rule <%s> does not exist", rule),
						ruleNames.contains(rule));
			});
	
			ruleNames.removeAll(syntacticMatchingRules);
			ruleNames.removeAll(syntacticNotMatchingRules);
			
			assertTrue(String.format("Bad rule list. Some rule are not under test: <%s>", ruleNames),
					ruleNames.isEmpty());
		
			assertThat(termIndex)
				.asMatchingRules()
				.containsAll(syntacticMatchingRules)
				.doesNotContainAnyElementsOf(syntacticNotMatchingRules);
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
		
		
	}

	@Test
	public void weControlPrefixes() {
		assertThat(termIndex)
			.asTermVariations(RelationType.IS_PREFIX_OF)
			.extracting("from.groupingKey", "to.groupingKey")
			.containsOnly(
					ControlFiles.prefixVariationTuples(lang, "we")
			);
	}

	@Test
	public void weControlDerivates() {
		assertThat(termIndex)
		.asTermVariations(RelationType.DERIVES_INTO)
		.extracting(TermSuiteExtractors.RELATION_DERIVTYPE_FROMGKEY_TOGKEY)
		.containsOnly(
				ControlFiles.derivateVariationTuples(lang, "we")
		);
	}

	@Test
	public void weCompounds() throws FileNotFoundException {
		assertThat(termIndex)
			.hasExpectedCompounds(
					FunctionalTests.getTestsOutputFile(String.format("compounds-we-%s.txt", lang.getCode())),
					ControlFiles.compoundTuples(lang, "we"));
	}
}
