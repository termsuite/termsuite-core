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

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ExternalResource;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.TermProperty;
import fr.univnantes.termsuite.model.Terminology;
import fr.univnantes.termsuite.uima.resources.TermHistoryResource;
import fr.univnantes.termsuite.uima.resources.termino.GeneralLanguage;
import fr.univnantes.termsuite.uima.resources.termino.TerminologyResource;
import fr.univnantes.termsuite.utils.TermUtils;

public class TermSpecificityComputer extends JCasAnnotator_ImplBase {
	private static final Logger LOGGER = LoggerFactory.getLogger(TermSpecificityComputer.class);

	@ExternalResource(key=TerminologyResource.TERMINOLOGY, mandatory=true)
	private TerminologyResource terminoResource;
	
	public static final String GENERAL_LANGUAGE_FREQUENCIES = "GeneralLanguageFrequencies";
	@ExternalResource(key=GENERAL_LANGUAGE_FREQUENCIES, mandatory=true)
	private GeneralLanguage generalLanguage;
	
	@ExternalResource(key =TermHistoryResource.TERM_HISTORY, mandatory = true)
	private TermHistoryResource historyResource;

	
	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		FSIterator<Annotation> it =  aJCas.getAnnotationIndex().iterator();
		while(it.hasNext()) {
			it.next();
			Terminology termino = this.terminoResource.getTerminology();
			termino.setWordAnnotationsNum(termino.getWordAnnotationsNum() + 1);
		}
	}
	
	@Override
	public void collectionProcessComplete()
			throws AnalysisEngineProcessException {
		LOGGER.info("Computing specificities and measures for Terminology {}", this.terminoResource.getTerminology().getName());
		
		if(terminoResource.getTerminology().getTerms().isEmpty())
			return;
		
		computeSpecifities();
		computeMeasures();
	}

	private void computeMeasures() {
		for(Term term:terminoResource.getTerminology().getTerms())
			term.setSpecificity(Math.log10(1 + term.getFrequencyNorm() / term.getGeneralFrequencyNorm()));
	}

	private void computeSpecifities() {
		Terminology termino = terminoResource.getTerminology();
		
		//		double maxWR = 0.0;
		if(generalLanguage.isCumulatedFrequencyMode()) {
			// process specificity for old versions of GeneralLanguage resources that do not have __NB_CORPUS_WORDS__ key
			
			int totalCount = 0;
			for(Term term:termino.getTerms()) 
				totalCount+=term.getFrequency();
			for(Term term:termino.getTerms()) {
				double frequency = (1000 * (double)term.getFrequency()) / totalCount;
				double generalFrequency = generalLanguage.getNormalizedFrequency(term.getLemma());
				if (generalFrequency != 0.0) {
					term.setFrequencyNorm(frequency);
					term.setGeneralFrequencyNorm(generalFrequency);
					term.setSpecificity(Math.log10(1 + frequency/generalFrequency));
				} else 
					LOGGER.warn("GeneralFrequency resource returned 0.0 for the term " + term.getGroupingKey() + ". Ignoring this term.");
			}
		} else {
			for(Term term:termino.getTerms()) {
				double generalTermFrequency = (double)generalLanguage.getFrequency(term.getLemma(), term.getPattern());
				double normalizedGeneralTermFrequency = 1000*generalTermFrequency/generalLanguage.getNbCorpusWords();
				double termFrequency = term.getFrequency();
				double normalizedTermFrequency = (1000 * termFrequency)/termino.getWordAnnotationsNum();
				term.setFrequencyNorm(normalizedTermFrequency);
				term.setGeneralFrequencyNorm( normalizedGeneralTermFrequency);
				TermUtils.setSpecificity(term);
				TermUtils.setTfIdf(term);
//				term.setSpecificity(Math.log10(1 + normalizedTermFrequency/normalizedGeneralTermFrequency));
//				term.setTfIdf((double)term.getFrequency()/term.getDocumentFrequency());
				watch(term);
			}
		}
	}

	private void watch(Term term) {
		if(historyResource.getHistory().isGKeyWatched(term.getGroupingKey()))
			historyResource.getHistory().saveEvent(
					term.getGroupingKey(), 
					this.getClass(), 
					String.format("Specificity of term set to %.2f [%s=%d, %s=%.2f, %s=%.2f]", 
							term.getSpecificity(),
							TermProperty.FREQUENCY.getShortName(), term.getFrequency(),
							TermProperty.FREQUENCY_NORM.getShortName(), term.getFrequencyNorm()*1000,
							TermProperty.GENERAL_FREQUENCY_NORM.getShortName(), term.getGeneralFrequencyNorm()*1000
						));
	}

}
