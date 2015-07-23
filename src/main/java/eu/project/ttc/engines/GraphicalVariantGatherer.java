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

import com.google.common.math.IntMath;

import eu.project.ttc.engines.desc.Lang;
import eu.project.ttc.metrics.DiacriticInsensitiveLevenshtein;
import eu.project.ttc.metrics.EditDistance;
import eu.project.ttc.models.Term;
import eu.project.ttc.models.TermIndex;
import eu.project.ttc.models.index.CustomTermIndex;
import eu.project.ttc.models.index.TermClassProviders;
import eu.project.ttc.resources.TermIndexResource;
import fr.univnantes.lina.UIMAProfiler;

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
	
	
	
	@ExternalResource(key=TermIndexResource.TERM_INDEX, mandatory=true)
	private TermIndexResource termIndexResource;
	
	public static final String LANG = "lang";
	@ConfigurationParameter(name=LANG, mandatory=true)
	private String lang;

	public static final String SIMILARITY_THRESHOLD = "SimilarityThreshold";
	@ConfigurationParameter(name=SIMILARITY_THRESHOLD, mandatory=true)
	private float threshold;
	

	private EditDistance distance = new DiacriticInsensitiveLevenshtein();
	private Lang language;

	/*
	 * the n first letter that serves as index
	 */
	private int n = 2;
	
	private int totalComparisons = 0;
	private int nbComparisons = 0;

	
	@Override
	public void initialize(UimaContext context)
			throws ResourceInitializationException {
		super.initialize(context);
		this.language = Lang.forName(lang);
	}
	
	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {}
	
	@Override
	public void collectionProcessComplete()
			throws AnalysisEngineProcessException {
		UIMAProfiler.getProfiler("AnalysisEngine").start(this, "process");
		logger.info("Start graphical term gathering");
		
		// create the index
		String indexName = String.format("_%d_first_letters_", n);
		final TermIndex termIndex = this.termIndexResource.getTermIndex();
		CustomTermIndex customIndex = termIndex.createCustomIndex(
				indexName,
				TermClassProviders.getNFirstLettersNormalizedClassProvider(n, language.getLocale()));
		
		// clean singleton classes
		logger.debug("Cleaning singleton keys");
		customIndex.cleanSingletonKeys();
		
		logger.debug("Graphical gathering over {} classes", customIndex.size());
		
		// get the total number of comparisons
		for(String key:customIndex.keySet()) 
			totalComparisons+= IntMath.binomial(customIndex.getTerms(key).size(), 2);

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
		for(String key:customIndex.keySet()) {
			terms = customIndex.getTerms(key);
			logger.trace("Graphical gathering over term class {} of size: {}", key, terms.size());
			for(i=0; i<terms.size();i++) {
				t1 = terms.get(i);
				for(j=i+1; j<terms.size();j++) {
					nbComparisons++;
					t2 = terms.get(j);
					if(distance.computeNormalized(t1.getLemma(), t2.getLemma()) >= this.threshold) {
						gatheredCnt++;
						t1.addGraphicalVariant(t2);
						if(UIMAProfiler.isActivated())
							UIMAProfiler.getProfiler("GraphicalGatherer").hit(
									"graphical variants", 
									String.format("%s || %s", t1.getGroupingKey(), t2.getGroupingKey()));
					}
				}
			}
		}
		
		// log some stats
		logger.debug("Graphical gathering {} terms gathered / {} pairs compared", gatheredCnt, nbComparisons);
		
		// free memory taken by the index
		termIndex.dropCustomIndex(TermClassProviders.KEY_3RD_FIRST_LETTERS);
		
		progressLoggerTimer.cancel();
		
		UIMAProfiler.getProfiler("AnalysisEngine").stop(this, "process");
	}
}
