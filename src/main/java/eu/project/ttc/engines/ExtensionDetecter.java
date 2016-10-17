
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

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ExternalResource;
import org.apache.uima.jcas.JCas;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.project.ttc.history.TermHistoryResource;
import eu.project.ttc.models.Term;
import eu.project.ttc.models.index.CustomTermIndex;
import eu.project.ttc.models.index.TermIndexes;
import eu.project.ttc.models.index.TermValueProviders;
import eu.project.ttc.resources.TermIndexResource;
import eu.project.ttc.utils.TermUtils;

public class ExtensionDetecter extends JCasAnnotator_ImplBase {
	private static final Logger LOGGER = LoggerFactory.getLogger(ExtensionDetecter.class);
	private static final int WARNING_CRITICAL_SIZE = 10000;

	@ExternalResource(key=TermIndexResource.TERM_INDEX, mandatory=true)
	private TermIndexResource termIndexResource;

	@ExternalResource(key =TermHistoryResource.TERM_HISTORY, mandatory = true)
	private TermHistoryResource historyResource;

	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		
	}
	
	@Override
	public void collectionProcessComplete() throws AnalysisEngineProcessException {
		LOGGER.info("Detecting term extensions for TermIndex {}", this.termIndexResource.getTermIndex().getName());
		if(termIndexResource.getTermIndex().getTerms().isEmpty())
			return;

		String gatheringKey = TermIndexes.WORD_COUPLE_LEMMA_LEMMA;
		CustomTermIndex customIndex = this.termIndexResource.getTermIndex().createCustomIndex(
				gatheringKey,
				TermValueProviders.get(gatheringKey));
		LOGGER.debug("Rule-based gathering over {} classes", customIndex.size());

		// clean singleton classes
		LOGGER.debug("Cleaning singleton keys");
		customIndex.cleanSingletonKeys();

		// clean biggest classes
		customIndex.dropBiggerEntries(WARNING_CRITICAL_SIZE, true);
		
		Term t1;
		Term t2;
		for (String cls : customIndex.keySet()) {
			List<Term> list = customIndex.getTerms(cls);
			for(int i = 0; i< list.size(); i++) {
				t1 = list.get(i);
				for(int j = i+1; j< list.size(); j++) {
					t2 = list.get(j);
					if(TermUtils.isIncludedIn(t1, t2)) {
						t1.addExtension(t2);
						watch(t1, t2);

					} else if(TermUtils.isIncludedIn(t2, t1)) {
						t2.addExtension(t1);
						watch(t2, t1);
					}
				}
			}
		}
		
		//finalize
		this.termIndexResource.getTermIndex().dropCustomIndex(gatheringKey);
	}

	private void watch(Term t1, Term t2) {
		if(historyResource.getHistory().isWatched(t1.getGroupingKey()))
			historyResource.getHistory().saveEvent(
					t1.getGroupingKey(),
					this.getClass(), 
					"Term has a new extension: " + t2);

		if(historyResource.getHistory().isWatched(t2.getGroupingKey()))
			historyResource.getHistory().saveEvent(
					t2.getGroupingKey(),
					this.getClass(), 
					"Term is the extension of " + t1);
	}

}
