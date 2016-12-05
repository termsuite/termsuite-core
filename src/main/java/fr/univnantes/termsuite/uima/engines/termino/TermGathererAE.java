
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

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright 2, 2015nership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package fr.univnantes.termsuite.uima.engines.termino;

import java.util.Optional;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.ExternalResource;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.univnantes.julestar.uima.resources.MultimapFlatResource;
import fr.univnantes.termsuite.engines.gatherer.GathererOptions;
import fr.univnantes.termsuite.engines.gatherer.YamlTermGatherer;
import fr.univnantes.termsuite.uima.resources.ObserverResource;
import fr.univnantes.termsuite.uima.resources.ObserverResource.SubTaskObserver;
import fr.univnantes.termsuite.uima.resources.TermHistoryResource;
import fr.univnantes.termsuite.uima.resources.termino.TermIndexResource;
import fr.univnantes.termsuite.uima.resources.termino.YamlRuleSetResource;

public class TermGathererAE extends JCasAnnotator_ImplBase {
	private static final Logger LOGGER = LoggerFactory.getLogger(TermGathererAE.class);
	public static final String TASK_NAME = "Syntactic variant gathering";

	@ExternalResource(key=ObserverResource.OBSERVER, mandatory=false)
	protected ObserverResource observerResource;

	@ExternalResource(key=TermIndexResource.TERM_INDEX, mandatory=true)
	private TermIndexResource termIndexResource;
	
	public static final String YAML_VARIANT_RULES = "YamlVariantRules";
	@ExternalResource(key = YAML_VARIANT_RULES, mandatory = true)
	private YamlRuleSetResource yamlVariantRules;

	@ExternalResource(key =TermHistoryResource.TERM_HISTORY, mandatory = true)
	private TermHistoryResource historyResource;
	
	public static final String SYNONYMS = "Synonyms";
	@ExternalResource(key = SYNONYMS, mandatory = false)
	private MultimapFlatResource synonymResource;

	public static final String SEMANTIC_ALIGNER_ENABLED = "SemanticAlignerEnabled";
	@ConfigurationParameter(name=SEMANTIC_ALIGNER_ENABLED, mandatory=false)
	private boolean semanticAlignerEnabled;

	
	private Optional<SubTaskObserver> taskObserver = Optional.empty();
	
	@Override
	public void initialize(UimaContext context) throws ResourceInitializationException {
		super.initialize(context);
		if(observerResource != null)
			taskObserver = Optional.of(observerResource.getTaskObserver(TASK_NAME));
	}
	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
	}

	@Override
	public void collectionProcessComplete()
			throws AnalysisEngineProcessException {
		LOGGER.info("Starting syntactic term gathering for TermIndex {}", this.termIndexResource.getTermIndex().getName());
		
		YamlTermGatherer gatherer = new YamlTermGatherer();
		if(taskObserver.isPresent())
			gatherer
				.setTaskObserver(taskObserver.get());
		gatherer
				.setHistory(historyResource.getHistory())
				.setRules(yamlVariantRules.getRuleSet());
		
		gatherer.setGathererOptions(new GathererOptions()
				.setSemanticGathererEnabled(semanticAlignerEnabled));
		
		if(synonymResource != null)
			gatherer.setDictionary(synonymResource);
				
		gatherer.gather(termIndexResource.getTermIndex());
	}
}
