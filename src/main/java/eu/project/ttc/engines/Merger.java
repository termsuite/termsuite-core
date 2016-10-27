
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
import java.util.Locale;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.ExternalResource;
import org.apache.uima.jcas.JCas;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import eu.project.ttc.metrics.DiacriticInsensitiveLevenshtein;
import eu.project.ttc.metrics.EditDistance;
import eu.project.ttc.models.Term;
import eu.project.ttc.models.TermIndex;
import eu.project.ttc.models.TermOccurrence;
import eu.project.ttc.models.TermRelation;
import eu.project.ttc.resources.ObserverResource;
import eu.project.ttc.resources.TermIndexResource;
import eu.project.ttc.utils.TermUtils;

/**
 * 
 * @
 *
 */
public class Merger extends JCasAnnotator_ImplBase {
	private static final Logger logger = LoggerFactory.getLogger(Merger.class);
	public static final String TASK_NAME = "Merging variants";

	@ExternalResource(key=ObserverResource.OBSERVER, mandatory=true)
	protected ObserverResource observerResource;
	
	@ExternalResource(key=TermIndexResource.TERM_INDEX, mandatory=true)
	private TermIndexResource termIndexResource;
	
	public static final String SIMILARITY_THRESHOLD = "SimilarityThreshold";
	@ConfigurationParameter(name=SIMILARITY_THRESHOLD, mandatory=false, defaultValue="0.9")
	private float threshold;
	

	private EditDistance distance = new DiacriticInsensitiveLevenshtein(Locale.getDefault());

	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		/*
		 * Do nothing
		 */
	}
	
	@Override
	public void collectionProcessComplete() throws AnalysisEngineProcessException {
		logger.info("Starting " + TASK_NAME);
		TermIndex termIndex = termIndexResource.getTermIndex();
		int nbMerged = 0;
		
		List<Term> rem = Lists.newArrayList();
		for(Term t:termIndex.getTerms()) {
			List<TermRelation> variations = Lists.newArrayList(termIndex.getOutboundRelations(t));
			TermRelation v1, v2;
			Term t1, t2;
			for(int i=0; i<variations.size(); i++) {
				v1 = variations.get(i);
				t1 = v1.getTo();
				if(!TermUtils.isExtension(termIndex, t,t1))
					// should only merge extensions.
					continue;
				for(int j=i+1; j<variations.size(); j++) {
					v2 = variations.get(j);
					t2 = v2.getTo();
					if(!TermUtils.isExtension(termIndex, t,t2))
						// should only merge extensions.
						continue;
					if(isGraphicalVariant(t1,t2)) {
						nbMerged++;
						logger.debug("Merging variant {} into variant {}", t2, t1);
						t1.addAll(t2.getOccurrences());
						for(TermOccurrence occ:t2.getOccurrences())
							occ.setTerm(t1);
						t1.setFrequency(t1.getFrequency() + t2.getFrequency());
						t1.setFrequencyNorm(t1.getFrequencyNorm() + t2.getFrequencyNorm());
						t1.setGeneralFrequencyNorm(t1.getGeneralFrequencyNorm() + t2.getGeneralFrequencyNorm());
						termIndex.removeRelation(v2);
						
						rem.add(t2);
						
						// TODO Also merge context vectors
						
					}
				}				
			}
		}
		for(Term t:rem)
			termIndex.removeTerm(t);
		logger.debug("Nb merges operated: {}", nbMerged);
	}


	private boolean isGraphicalVariant(Term t1, Term t2) {
		double dist = distance.computeNormalized(t1.getLemma(), t2.getLemma());
		return dist >= this.threshold;
	}
}
