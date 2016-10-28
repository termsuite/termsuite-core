
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

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ExternalResource;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import eu.project.ttc.history.TermHistoryResource;
import eu.project.ttc.models.Term;
import eu.project.ttc.models.TermIndex;
import eu.project.ttc.models.TermRelation;
import eu.project.ttc.models.scored.ScoredModel;
import eu.project.ttc.models.scored.ScoredTerm;
import eu.project.ttc.models.scored.ScoredVariation;
import eu.project.ttc.resources.ObserverResource;
import eu.project.ttc.resources.ObserverResource.SubTaskObserver;
import eu.project.ttc.resources.TermIndexResource;
import eu.project.ttc.termino.engines.TermVariationScorer;
import eu.project.ttc.termino.engines.VariantScorerConfig;

public class ScorerAE extends JCasAnnotator_ImplBase {
	private static final Logger logger = LoggerFactory.getLogger(ScorerAE.class);
	public static final String TASK_NAME = "Scorying terms";

	@ExternalResource(key=ObserverResource.OBSERVER, mandatory=true)
	protected ObserverResource observerResource;
	
	@ExternalResource(key=TermIndexResource.TERM_INDEX, mandatory=true)
	private TermIndexResource termIndexResource;

	@ExternalResource(key =TermHistoryResource.TERM_HISTORY, mandatory = true)
	private TermHistoryResource historyResource;

	
	private Optional<SubTaskObserver> taskObserver = Optional.empty();

	@Override
	public void initialize(UimaContext context) throws ResourceInitializationException {
		super.initialize(context);
		if(observerResource != null)
			taskObserver = Optional.of(observerResource.getTaskObserver(TASK_NAME));
	}
	
	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		/*
		 * Do nothing
		 */
	}
	
	@Override
	public void collectionProcessComplete() throws AnalysisEngineProcessException {
		logger.info("Scorying terms for TermIndex {}", this.termIndexResource.getTermIndex().getName());
		TermIndex termIndex = termIndexResource.getTermIndex();
		VariantScorerConfig defaultScorerConfig = termIndex.getLang().getScorerConfig();
		TermVariationScorer variantScorer = new TermVariationScorer(defaultScorerConfig);
		
		/*
		 * 1- compute scores
		 */
		logger.debug("Scorying");
		variantScorer.setHistory(historyResource.getHistory());
		ScoredModel scoredModel = variantScorer.score(termIndex);
		
		/*
		 * 2- remove filtered terms
		 */
		logger.debug("Selecting terms for removal");
		Set<Term> keptTerms = Sets.newHashSet();
		for(ScoredTerm st:scoredModel.getTerms())
			keptTerms.add(st.getTerm());
		Set<Term> remTerms = Sets.newHashSet();
		int size = termIndex.getTerms().size();
		for(Term t:termIndex.getTerms())
			if(!keptTerms.contains(t))
				remTerms.add(t);
		logger.debug("Removing filtered terms");
		if(taskObserver.isPresent())
			taskObserver.get().setTotalTaskWork(remTerms.size());
		for(Term t:remTerms) {
			if(taskObserver.isPresent())
				taskObserver.get().work(1);
			termIndex.removeTerm(t);
		}
		logger.debug("{} terms filtered (and removed from term index) out of {}", 
				remTerms.size(),
				size
				);
		
		/*
		 * 3- reset the sorted variations and unique bases
		 */
		logger.debug("Resetting scored variations and unique bases");
		for(ScoredTerm st:scoredModel.getTerms()) {
			for(ScoredVariation sv:st.getVariations()) {
				TermRelation termVariation = sv.getTermVariation();
				
				/*
				 * Add this term variation to the variants
				 */
				termVariation.setScore(sv.getVariationScore());

				List<TermRelation> toRem = Lists.newArrayList(termIndex.getInboundTerRelat(termVariation.getTo()));
				for(TermRelation tv:toRem)
					termIndex.removeRelation(tv);

				termIndex.addRelation(termVariation);
			}
		}
	}
}
