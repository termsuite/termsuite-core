
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

package fr.univnantes.termsuite.engines.splitter;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.univnantes.termsuite.model.RelationProperty;
import fr.univnantes.termsuite.model.RelationType;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.TermRelation;
import fr.univnantes.termsuite.model.TermWord;
import fr.univnantes.termsuite.model.Terminology;
import fr.univnantes.termsuite.model.termino.CustomTermIndex;
import fr.univnantes.termsuite.model.termino.TermValueProviders;
import fr.univnantes.termsuite.resources.SuffixDerivation;
import fr.univnantes.termsuite.uima.resources.termino.SuffixDerivationList;
import fr.univnantes.termsuite.utils.TermHistory;

public class SuffixDerivationDetecter {
	private static final Logger LOGGER = LoggerFactory.getLogger(SuffixDerivationDetecter.class);

	private static final String LEMMA_INDEX = "LemmaIndex";

	private SuffixDerivationList suffixDerivationList;

	private TermHistory history;
	
	public SuffixDerivationDetecter setHistory(TermHistory history) {
		this.history = history;
		return this;
	}

	public SuffixDerivationDetecter setSuffixDerivationList(SuffixDerivationList suffixDerivationList) {
		this.suffixDerivationList = suffixDerivationList;
		return this;
	}
	
	public void detectDerivations(Terminology termino) {
		LOGGER.info("Detecting suffix derivations for termino {}", termino.getName());
		CustomTermIndex lemmaIndex = termino.createCustomIndex(
				LEMMA_INDEX, 
				TermValueProviders.TERM_LEMMA_LOWER_CASE_PROVIDER);
		
		int nbDerivations = 0, nbSwt = 0;
		TermWord candidateDerivateTermWord, baseTermWord;
		for(Term swt:termino.getTerms()) {
			if(!swt.isSingleWord())
				continue;
			nbSwt++;
			candidateDerivateTermWord = swt.getWords().get(0);
			List<SuffixDerivation> derivations = suffixDerivationList.getDerivationsFromDerivateForm(candidateDerivateTermWord);
			for(SuffixDerivation suffixDerivation:derivations) {
				if(suffixDerivation.isKnownDerivate(candidateDerivateTermWord)) {
					baseTermWord = suffixDerivation.getBaseForm(candidateDerivateTermWord);
					List<Term> baseTerms = lemmaIndex.getTerms(baseTermWord.getWord().getLemma());
					for(Term baseTerm:baseTerms) {
						if(baseTerm.getWords().get(0).equals(baseTermWord)) {
							nbDerivations++;
							if(LOGGER.isTraceEnabled())
								LOGGER.trace("Found derivation base: {} for derivate word {}", baseTerm, swt);
							TermRelation relation = new TermRelation(RelationType.DERIVES_INTO, baseTerm, swt);
							relation.setProperty(RelationProperty.DERIVATION_TYPE, suffixDerivation.getType());
							termino.addRelation(relation);
							watch(swt, baseTerm);
						}
					}
				}
			}
		}
		
		termino.dropCustomIndex(LEMMA_INDEX);
		
		LOGGER.debug("Number of derivations found: {} out of {} SWTs", 
				nbDerivations, 
				nbSwt);
	}

	private void watch(Term swt, Term baseTerm) {
		if(history != null) {
			if(history.isGKeyWatched(swt.getGroupingKey())) 
				history.saveEvent(
						swt.getGroupingKey(), 
						this.getClass(), 
						"Term is a derivate of term " + baseTerm);
			
			if(history.isGKeyWatched(baseTerm.getGroupingKey())) 
				history.saveEvent(
						baseTerm.getGroupingKey(), 
						this.getClass(), 
						"Term has a new found derivate: " + swt);
		}
	}
}
