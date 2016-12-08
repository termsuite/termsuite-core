
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

package fr.univnantes.termsuite.uima.engines.termino;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.ExternalResource;
import org.apache.uima.jcas.JCas;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.TermProperty;
import fr.univnantes.termsuite.uima.resources.ObserverResource;
import fr.univnantes.termsuite.uima.resources.TermHistoryResource;
import fr.univnantes.termsuite.uima.resources.termino.TerminologyResource;

public class Ranker extends JCasAnnotator_ImplBase {
	private static final Logger logger = LoggerFactory.getLogger(Ranker.class);
	public static final String TASK_NAME = "Ranking terms";

	@ExternalResource(key=ObserverResource.OBSERVER, mandatory=true)
	protected ObserverResource observerResource;
	
	@ExternalResource(key=TerminologyResource.TERMINOLOGY, mandatory=true)
	private TerminologyResource terminoResource;

	@ExternalResource(key =TermHistoryResource.TERM_HISTORY, mandatory = true)
	private TermHistoryResource historyResource;

	
	public static final String RANKING_PROPERTY="RankingProperty";
	@ConfigurationParameter(name=RANKING_PROPERTY, mandatory=true)
	protected TermProperty rankingProperty;

	public static final String DESC="Desc";
	@ConfigurationParameter(name=DESC, mandatory=false, defaultValue="false")
	protected boolean reverse;

	
	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		/*
		 * Do nothing
		 */
	}
	
	@Override
	public void collectionProcessComplete() throws AnalysisEngineProcessException {
		logger.info("Ranking terms for TermIndex {}", terminoResource.getTerminology().getName());
		List<Term> ranked = Lists.newArrayList(terminoResource.getTerminology().getTerms());
		Comparator<Term> comparator = rankingProperty.getComparator(reverse);
		Collections.sort(ranked, comparator);
		for(int index = 0; index < ranked.size(); index++) {
			ranked.get(index).setRank(index + 1);
			if(historyResource.getHistory().isWatched(ranked.get(index)))
				historyResource.getHistory().saveEvent(
						ranked.get(index).getGroupingKey(), 
						this.getClass(), 
						"Set term rank: " + (index+1));
		}
	}
}
