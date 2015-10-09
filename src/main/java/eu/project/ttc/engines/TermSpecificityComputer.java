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

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ExternalResource;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.project.ttc.models.Term;
import eu.project.ttc.models.TermIndex;
import eu.project.ttc.resources.GeneralLanguage;
import eu.project.ttc.resources.TermIndexResource;

public class TermSpecificityComputer extends JCasAnnotator_ImplBase {
	private static final Logger LOGGER = LoggerFactory.getLogger(TermSpecificityComputer.class);

	@ExternalResource(key=TermIndexResource.TERM_INDEX, mandatory=true)
	private TermIndexResource termIndexResource;
	
	public static final String GENERAL_LANGUAGE_FREQUENCIES = "GeneralLanguageFrequencies";
	@ExternalResource(key=GENERAL_LANGUAGE_FREQUENCIES, mandatory=true)
	private GeneralLanguage generalLanguage;
	
	private int nbWordAnnotations = 0;
	
	private double wrLogStandardDeviation = 0d;
	private double wrLogAverage = 0d;
	
	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		FSIterator<Annotation> it =  aJCas.getAnnotationIndex().iterator();
		while(it.hasNext()) {
			it.next();
			this.nbWordAnnotations++;
		}
	}
	
	@Override
	public void collectionProcessComplete()
			throws AnalysisEngineProcessException {
		LOGGER.info("Computing specificities");
		final TermIndex termIndex = this.termIndexResource.getTermIndex();
		
		/*
		 * Ugly trick to access nbWordAnnotations from outside (only 
		 * for the GeneralLanguage resource generation) without
		 * modifying TermIndex data model.
		 */
		System.getProperties().setProperty("termsuite.nbWordAnnotations", Integer.toString(nbWordAnnotations));

		computeWR(termIndex);
		computeStandardDeviation(termIndex);
		computeWRLogZScore(termIndex);
	}

	private void computeWRLogZScore(TermIndex termIndex) {
		for(Term term:termIndex.getTerms())
			term.setWRLogZScore((term.getWRLog() - wrLogAverage)/wrLogStandardDeviation);
	}

	private void computeStandardDeviation(TermIndex termIndex) {
		double sum = 0d;
		for(Term term:termIndex.getTerms()) 
			sum += term.getWRLog();
		
		int size = termIndex.getTerms().size();
		wrLogAverage = sum / size;
		LOGGER.debug("wrLog average is {}", wrLogAverage);
		
		double sigmaSquare = 0;
		for(Term term:termIndex.getTerms()) 
			sigmaSquare+=Math.pow(term.getWRLog() - wrLogAverage, 2);
		
		wrLogStandardDeviation = Math.sqrt(1.0/size * sigmaSquare);
		LOGGER.debug("wrLog stadard deviation is {}", wrLogStandardDeviation);
	}

	private void computeWR(final TermIndex termIndex) {
		double maxWR = 0.0;
		if(generalLanguage.isCumulatedFrequencyMode()) {
			// process specificity for old versions of GeneralLanguage resources that do not have __NB_CORPUS_WORDS__ key
			
			int totalCount = 0;
			for(Term term:termIndexResource.getTermIndex().getTerms()) 
				totalCount+=term.getFrequency();
			for(Term term:termIndexResource.getTermIndex().getTerms()) {
				double frequency = ((float)term.getFrequency()) / totalCount;
//				double frequency = ((float)term.getAllOccurrences(2).size()) / totalCount;
				double generalFrequency = generalLanguage.getNormalizedFrequency(term.getLemma());
				if (generalFrequency != 0.0) {
					double wr = frequency / generalFrequency;
					term.setWR((float)wr);
					term.setWRLog(Math.log10(1 + wr));
					if(wr>maxWR)
						maxWR = wr;
				} else 
					LOGGER.warn("GeneralFrequency resource returned 0.0 for the term " + term.getGroupingKey() + ". Ignoring this term.");
			}
		} else {
			for(Term term:termIndexResource.getTermIndex().getTerms()) {
				float generalTermFrequency = (float)generalLanguage.getFrequency(term.getLemma(), term.getPattern());
				float normalizedGeneralTermFrequency = generalTermFrequency/generalLanguage.getNbCorpusWords();
				float termFrequency = term.getFrequency();
//				float termFrequency = term.getAllOccurrences(2).size();
				float normalizedTermFrequency = termFrequency/this.nbWordAnnotations;
				
				term.setWR(normalizedTermFrequency/normalizedGeneralTermFrequency);
				term.setWRLog(Math.log10(1 + normalizedTermFrequency/normalizedGeneralTermFrequency));
				if(term.getWR()>maxWR)
					maxWR = term.getWR();
				
			}
		}
	}

}
