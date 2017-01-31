
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

import org.slf4j.Logger;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import fr.univnantes.termsuite.engines.SimpleEngine;
import fr.univnantes.termsuite.framework.InjectLogger;
import fr.univnantes.termsuite.framework.Resource;
import fr.univnantes.termsuite.model.RelationType;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.TermRelation;
import fr.univnantes.termsuite.model.Word;
import fr.univnantes.termsuite.uima.ResourceType;
import fr.univnantes.termsuite.uima.resources.preproc.PrefixTree;

public class PrefixSplitter extends SimpleEngine {
	@InjectLogger Logger logger;

	@Resource(type=ResourceType.PREFIX_BANK)
	private PrefixTree prefixTree;
	
	private boolean checkIfMorphoExtensionInCorpus = true;

	@Override
	public void execute() {
		Multimap<String, Term> lemmaIndex = HashMultimap.create();
		int nb = 0;
		String prefixExtension, lemma, pref;
		for(Term swt:terminology.getTerms()) {
			if(!swt.isSingleWord())
				continue;
			else {
				lemmaIndex.put(swt.getLemma(), swt);
			}
		}
		for(Term swt:terminology.getTerms()) {
			if(!swt.isSingleWord())
				continue;

			Word word = swt.getWords().get(0).getWord();
			lemma = word.getLemma();
			pref = prefixTree.getPrefix(lemma);
			if(pref != null && pref.length() < lemma.length()) {
				prefixExtension = lemma.substring(pref.length(),lemma.length());
				if(logger.isTraceEnabled())
					logger.trace("Found prefix: {} for word {}", pref, lemma);
				if(checkIfMorphoExtensionInCorpus) {
					if(!lemmaIndex.containsKey(prefixExtension)) {
						if(logger.isTraceEnabled())
							logger.trace("Prefix extension: {} for word {} is not found in corpus. Aborting composition for this word.", prefixExtension, lemma);
						continue;
					} else {
						for(Term target:lemmaIndex.get(prefixExtension)) {
							watch(swt, target);
							terminology.addRelation(new TermRelation(
									RelationType.IS_PREFIX_OF,
									swt, 
									target
									));
						}
					}
				}
				nb++;
			}
		}
		logger.debug("Number of words with prefix composition: {} out of {}", 
				nb, 
				terminology.wordCount());
	}

	private void watch(Term swt, Term target) {
		if(history.isPresent()) {
			if(history.get().isGKeyWatched(swt.getGroupingKey()))
				history.get().saveEvent(
						swt.getGroupingKey(), 
						this.getClass(), 
						"Term is prefix of term " + target);
			if(history.get().isGKeyWatched(target.getGroupingKey()))
				history.get().saveEvent(
						target.getGroupingKey(), 
						this.getClass(), 
						"Term has a new found prefix: " + swt);
		}

	}
}
