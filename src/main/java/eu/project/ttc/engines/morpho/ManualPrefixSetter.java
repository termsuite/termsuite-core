
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

package eu.project.ttc.engines.morpho;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ExternalResource;
import org.apache.uima.jcas.JCas;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import eu.project.ttc.history.TermHistory;
import eu.project.ttc.history.TermHistoryResource;
import eu.project.ttc.models.Term;
import eu.project.ttc.models.TermVariation;
import eu.project.ttc.models.VariationType;
import eu.project.ttc.models.Word;
import eu.project.ttc.resources.ManualSegmentationResource;
import eu.project.ttc.resources.TermIndexResource;

public class ManualPrefixSetter extends JCasAnnotator_ImplBase {
	private static final Logger LOGGER = LoggerFactory.getLogger(ManualPrefixSetter.class);
	
	public static final String PREFIX_EXCEPTIONS = "PrefixExceptions";
	@ExternalResource(key=PREFIX_EXCEPTIONS, mandatory=true)
	private ManualSegmentationResource prefixExceptions;

	@ExternalResource(key=TermIndexResource.TERM_INDEX, mandatory=true)
	private TermIndexResource termIndexResource;

	@ExternalResource(key =TermHistoryResource.TERM_HISTORY, mandatory = true)
	private TermHistoryResource historyResource;

	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		// do nothing
	}
	
	@Override
	public void collectionProcessComplete() throws AnalysisEngineProcessException {
		Segmentation segmentation;
		for(Term swt:termIndexResource.getTermIndex().getTerms()) {
			if(!swt.isSingleWord())
				continue;
			Word word = swt.getWords().get(0).getWord();
			segmentation = prefixExceptions.getSegmentation(word.getLemma());
			if(segmentation != null) 
				if(segmentation.size() <= 1) {
					for(TermVariation tv:Lists.newArrayList(termIndexResource.getTermIndex().getOutboundTermVariations(swt, VariationType.IS_PREFIX_OF))) {
						termIndexResource.getTermIndex().removeTermVariation(tv);
						watch(swt, tv);
					}
				} else {
					LOGGER.warn("Ignoring prefix exception {}->{} since non-expty prefix exceptions are not allowed.",
							word.getLemma(),
							segmentation);
				}
		}
	}

	private void watch(Term swt, TermVariation tv) {
		TermHistory history = historyResource.getHistory();
		if(history.isWatched(swt.getGroupingKey()))
			history.saveEvent(
				swt.getGroupingKey(), 
				this.getClass(), 
				"Prefix variation of term " + tv.getVariant().getGroupingKey() + " removed");
		if(history.isWatched(tv.getVariant().getGroupingKey()))
			history.saveEvent(
				tv.getVariant().getGroupingKey(), 
				this.getClass(), 
				"Prefix variation of term " + swt + " removed");

	}

}
