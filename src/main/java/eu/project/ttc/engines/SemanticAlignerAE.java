
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

package eu.project.ttc.engines;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ExternalResource;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.project.ttc.engines.variant.SynonymVariantHelper;
import eu.project.ttc.engines.variant.VariantRule;
import eu.project.ttc.engines.variant.VariantRuleIndex;
import eu.project.ttc.history.TermHistoryResource;
import eu.project.ttc.resources.TermIndexResource;
import eu.project.ttc.resources.YamlVariantRules;
import fr.univnantes.julestar.uima.resources.MultimapFlatResource;

public class SemanticAlignerAE extends JCasAnnotator_ImplBase {
	private static final Logger LOGGER = LoggerFactory.getLogger(SemanticAlignerAE.class);

	@ExternalResource(key=TermIndexResource.TERM_INDEX, mandatory=true)
	private TermIndexResource termIndexResource;

	@ExternalResource(key =TermHistoryResource.TERM_HISTORY, mandatory = true)
	private TermHistoryResource historyResource;

	
	public static final String YAML_VARIANT_RULES = "YamlVariantRules";
	@ExternalResource(key = YAML_VARIANT_RULES, mandatory = true)
	private YamlVariantRules yamlVariantRules;

	public static final String SYNONYMS = "Synonyms";
	@ExternalResource(key = SYNONYMS, mandatory = false)
	private MultimapFlatResource synonymResource;

	
	@Override
	public void initialize(UimaContext context) throws ResourceInitializationException {
		super.initialize(context);
	}
	
	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		
	}
	
	@Override
	public void collectionProcessComplete() throws AnalysisEngineProcessException {
		LOGGER.info("Detecting term extensions for TermIndex {}", this.termIndexResource.getTermIndex().getName());
		for(VariantRule rule:yamlVariantRules.getVariantRules(VariantRuleIndex.SYNONYM)) {
			rule.setHelper(new SynonymVariantHelper());
			new SemanticAligner()
				.setHistory(historyResource.getHistory())
				.setRule(rule)
				.setDictionary(this.synonymResource)
				.align(termIndexResource.getTermIndex());
		}
	}
}
