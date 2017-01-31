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

import javax.inject.Inject;

import org.slf4j.Logger;

import fr.univnantes.termsuite.engines.SimpleEngine;
import fr.univnantes.termsuite.framework.InjectLogger;
import fr.univnantes.termsuite.framework.Resource;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.TermProperty;
import fr.univnantes.termsuite.uima.ResourceType;
import fr.univnantes.termsuite.uima.resources.termino.GeneralLanguage;
import fr.univnantes.termsuite.utils.TermHistory;
import fr.univnantes.termsuite.utils.TermUtils;

public class TermSpecificityComputer extends SimpleEngine {
	@InjectLogger Logger logger;

	@Resource(type=ResourceType.GENERAL_LANGUAGE)
	private GeneralLanguage generalLanguage;
	
	@Inject
	private Optional<TermHistory> history = Optional.empty();
	
	@Override
	public void execute() {
		if(terminology.getTerms().isEmpty())
			return;
		
		//		double maxWR = 0.0;
		for(Term term:terminology.getTerms()) {
			double generalTermFrequency = (double)generalLanguage.getFrequency(term.getLemma(), term.getPattern());
			double normalizedGeneralTermFrequency = 1000*generalTermFrequency/generalLanguage.getNbCorpusWords();
			double termFrequency = term.getFrequency();
			double normalizedTermFrequency = (1000 * termFrequency)/terminology.getWordAnnotationsNum();
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
