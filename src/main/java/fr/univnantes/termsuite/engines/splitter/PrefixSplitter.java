
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
import org.slf4j.LoggerFactory;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import fr.univnantes.termsuite.model.RelationType;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.TermRelation;
import fr.univnantes.termsuite.model.Terminology;
import fr.univnantes.termsuite.model.Word;
import fr.univnantes.termsuite.uima.resources.preproc.PrefixTree;
import fr.univnantes.termsuite.utils.TermHistory;

public class PrefixSplitter {
	private static final Logger LOGGER = LoggerFactory.getLogger(PrefixSplitter.class);

	private PrefixTree prefixTree;
	private TermHistory history;
	private boolean checkIfMorphoExtensionInCorpus = true;
	
	public PrefixSplitter setCheckIfMorphoExtensionInCorpus(boolean checkIfMorphoExtensionInCorpus) {
		this.checkIfMorphoExtensionInCorpus = checkIfMorphoExtensionInCorpus;
		return this;
	}
	
	public PrefixSplitter setPrefixTree(PrefixTree prefixTree) {
		this.prefixTree = prefixTree;
		return this;
	}
	
	
	public PrefixSplitter setHistory(TermHistory history) {
		this.history = history;
		return this;
	}
	
	public void splitPrefixes(Terminology termino) {
		LOGGER.info("Starting prefix splitting for termino {}", termino.getName());
		Multimap<String, Term> lemmaIndex = HashMultimap.create();
		int nb = 0;
		String prefixExtension, lemma, pref;
		for(Term swt:termino.getTerms()) {
			if(!swt.isSingleWord())
				continue;
			else {
				lemmaIndex.put(swt.getLemma(), swt);
			}
		}
		for(Term swt:termino.getTerms()) {
			if(!swt.isSingleWord())
				continue;

			Word word = swt.getWords().get(0).getWord();
			lemma = word.getLemma();
			pref = prefixTree.getPrefix(lemma);
			if(pref != null && pref.length() < lemma.length()) {
				prefixExtension = lemma.substring(pref.length(),lemma.length());
				if(LOGGER.isTraceEnabled())
					LOGGER.trace("Found prefix: {} for word {}", pref, lemma);
				if(checkIfMorphoExtensionInCorpus) {
					if(!lemmaIndex.containsKey(prefixExtension)) {
						if(LOGGER.isTraceEnabled())
							LOGGER.trace("Prefix extension: {} for word {} is not found in corpus. Aborting composition for this word.", prefixExtension, lemma);
						continue;
					} else {
						for(Term target:lemmaIndex.get(prefixExtension)) {
							watch(swt, target);
							termino.addRelation(new TermRelation(
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
		LOGGER.debug("Number of words with prefix composition: {} out of {}", 
				nb, 
				termino.getWords().size());
	}

	private void watch(Term swt, Term target) {
		if(history != null) {
			if(history.isWatched(swt.getGroupingKey()))
				history.saveEvent(
						swt.getGroupingKey(), 
						this.getClass(), 
						"Term is prefix of term " + target);
			if(history.isWatched(target.getGroupingKey()))
				history.saveEvent(
						target.getGroupingKey(), 
						this.getClass(), 
						"Term has a new found prefix: " + swt);
		}

	}
}
