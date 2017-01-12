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
package fr.univnantes.termsuite.engines.prepare;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.univnantes.termsuite.framework.TerminologyEngine;
import fr.univnantes.termsuite.framework.TerminologyService;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.TermProperty;
import fr.univnantes.termsuite.uima.resources.termino.GeneralLanguage;
import fr.univnantes.termsuite.utils.TermHistory;
import fr.univnantes.termsuite.utils.TermUtils;

public class TermSpecificityComputer extends TerminologyEngine {
	private static final Logger LOGGER = LoggerFactory.getLogger(TermSpecificityComputer.class);

	private GeneralLanguage generalLanguage;
	
	private Optional<TermHistory> history = Optional.empty();
	
	public void run(TerminologyService terminologyService) {
		LOGGER.info("Computing specificities");
		
		if(terminologyService.getTerms().isEmpty())
			return;
		
		//		double maxWR = 0.0;
		for(Term term:terminologyService.getTerms()) {
			double generalTermFrequency = (double)generalLanguage.getFrequency(term.getLemma(), term.getPattern());
			double normalizedGeneralTermFrequency = 1000*generalTermFrequency/generalLanguage.getNbCorpusWords();
			double termFrequency = term.getFrequency();
			double normalizedTermFrequency = (1000 * termFrequency)/terminologyService.getWordAnnotationsNum();
			term.setFrequencyNorm(normalizedTermFrequency);
			term.setGeneralFrequencyNorm( normalizedGeneralTermFrequency);
			TermUtils.setSpecificity(term);
			TermUtils.setTfIdf(term);
			watch(term);
		}
	}

	private void watch(Term term) {
		if(history.isPresent())
			if(history.get().isGKeyWatched(term.getGroupingKey()))
				history.get().saveEvent(
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
