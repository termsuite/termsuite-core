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
package eu.project.ttc.engines;

import java.util.Collection;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.ExternalResource;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.math.IntMath;

import eu.project.ttc.engines.desc.Lang;
import eu.project.ttc.metrics.DiacriticInsensitiveLevenshtein;
import eu.project.ttc.metrics.EditDistance;
import eu.project.ttc.models.Term;
import eu.project.ttc.models.TermIndex;
import eu.project.ttc.models.TermWord;
import eu.project.ttc.models.VariationType;
import eu.project.ttc.models.index.AbstractTermValueProvider;
import eu.project.ttc.models.index.CustomTermIndex;
import eu.project.ttc.models.index.TermValueProvider;
import eu.project.ttc.resources.ObserverResource;
import eu.project.ttc.resources.ObserverResource.SubTaskObserver;
import eu.project.ttc.resources.TermIndexResource;

/**
 * 
 * Gather terms whe their edit distances (ignoring diacritics) are
 * under certain threshold (not normalized).
 * 
 * @author Damien Cram
 *
 */
public class GraphicalVariantGatherer  extends JCasAnnotator_ImplBase {
	private static final Logger logger = LoggerFactory.getLogger(GraphicalVariantGatherer.class);
	public static final String TASK_NAME = "Computing graphical variants";
	private static final int OBSERVER_STEP = 10000;
	private static final char JOIN_CHAR = ':';

	@ExternalResource(key=ObserverResource.OBSERVER, mandatory=false)
	protected ObserverResource observerResource;
	
	@ExternalResource(key=TermIndexResource.TERM_INDEX, mandatory=true)
	private TermIndexResource termIndexResource;
	
	public static final String LANG = "lang";
	@ConfigurationParameter(name=LANG, mandatory=true)
	private String lang;

	public static final String SIMILARITY_THRESHOLD = "SimilarityThreshold";
	@ConfigurationParameter(name=SIMILARITY_THRESHOLD, mandatory=true)
	private float threshold;
	
	private EditDistance distance;
	private Lang language;

	/*
	 * the n first letter that serves as index
	 */
	private int n = 2;
	
	private int totalComparisons = 0;
	private int nbComparisons = 0;
	

	private TermValueProvider nFirstLettersProvider = new AbstractTermValueProvider("") {
		@Override
		public Collection<String> getClasses(TermIndex termIndex, Term term) {
//			if(term.getWords().size() == 1)
//				// do not gather sw term with that method
//				return ImmutableList.of();
			StringBuilder builder = new StringBuilder();
			String normalizedStem;
			int i = 0;
			for(TermWord tw:term.getWords()) {
				if(i>0) {
					builder.append(JOIN_CHAR);
				}
				normalizedStem = tw.getWord().getNormalizedStem();
				if(normalizedStem.length() > n)
					builder.append(normalizedStem.substring(0, n).toLowerCase(language.getLocale()));
				else
					builder.append(normalizedStem.toLowerCase(language.getLocale()));
				i++;
			}
			if(builder.length() >= n)
				return ImmutableList.of(builder.toString());
			else
				return ImmutableList.of();
		}
	};

	
	@Override
	public void initialize(UimaContext context)
			throws ResourceInitializationException {
		super.initialize(context);
		this.language = Lang.forName(lang);
		distance = new DiacriticInsensitiveLevenshtein(this.language.getLocale());
		if(observerResource != null)
			taskObserver = Optional.of(observerResource.getTaskObserver(TASK_NAME));
	}
	
	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {}
	
	private Optional<SubTaskObserver> taskObserver = Optional.absent();
	
	@Override
	public void collectionProcessComplete()
			throws AnalysisEngineProcessException {
		logger.info("Start graphical term gathering");
		
		
		// create the index
		String indexName = String.format("_%d_first_letters_", n);
		final TermIndex termIndex = this.termIndexResource.getTermIndex();
		CustomTermIndex customIndex = termIndex.createCustomIndex(
				indexName,
				nFirstLettersProvider);
		
		// clean singleton classes
		logger.debug("Cleaning singleton keys");
		customIndex.cleanSingletonKeys();
		
		logger.debug("Graphical gathering over {} classes", customIndex.size());
		
		// get the total number of comparisons
		for(String key:customIndex.keySet()) 
			totalComparisons+= IntMath.binomial(customIndex.getTerms(key).size(), 2);

		if(taskObserver.isPresent())
			taskObserver.get().setTotalTaskWork(totalComparisons);

		logger.debug("Number of distance edition pairs to compute: {}", totalComparisons);
		
		
		// Log the progress every 5 seconds
		Timer progressLoggerTimer = new Timer("Syn. Variant Gathering Timer");
		progressLoggerTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				GraphicalVariantGatherer.logger.info("progress for graphical variant gathering: {}%",
						String.format("%.2f", ((float)nbComparisons*100)/totalComparisons)
						);
			}
		}, 5000l, 5000l);
		
		
		// do the distance computation on each class
		List<Term> terms;
		Term t1, t2;
		int i, j;
		int gatheredCnt = 0;
		double dist = 0d;
		for(String key:customIndex.keySet()) {
			terms = customIndex.getTerms(key);
			logger.trace("Graphical gathering over term class {} of size: {}", key, terms.size());
			for(i=0; i<terms.size();i++) {
				t1 = terms.get(i);
				for(j=i+1; j<terms.size();j++) {
					nbComparisons++;
					if(nbComparisons % OBSERVER_STEP == 0 && taskObserver.isPresent())
						taskObserver.get().work(OBSERVER_STEP);
					t2 = terms.get(j);
					dist = distance.computeNormalized(t1.getLemma(), t2.getLemma());
					if(dist >= this.threshold) {
						gatheredCnt++;
						if(t1.getLemma().compareTo(t2.getLemma()) <= 0)
							t1.addTermVariation(t2, VariationType.GRAPHICAL, dist);
						else
							t2.addTermVariation(t1, VariationType.GRAPHICAL, dist);
					}
				}
			}
		}
		
		// log some stats
		logger.debug("Graphical gathering {} terms gathered / {} pairs compared", gatheredCnt, nbComparisons);
		
		// free memory taken by the index
		termIndex.dropCustomIndex(indexName);
		
		progressLoggerTimer.cancel();
	}
}
