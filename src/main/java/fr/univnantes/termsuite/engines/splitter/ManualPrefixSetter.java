
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

import com.google.common.collect.Lists;

import fr.univnantes.termsuite.model.RelationType;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.TermRelation;
import fr.univnantes.termsuite.model.Terminology;
import fr.univnantes.termsuite.model.Word;
import fr.univnantes.termsuite.uima.resources.preproc.ManualSegmentationResource;
import fr.univnantes.termsuite.utils.TermHistory;

public class ManualPrefixSetter {
	private static final Logger LOGGER = LoggerFactory.getLogger(ManualPrefixSetter.class);
	
	private ManualSegmentationResource prefixExceptions;
	private TermHistory history;
	
	public ManualPrefixSetter setHistory(TermHistory history) {
		this.history = history;
		return this;
	}
	
	public ManualPrefixSetter setPrefixExceptions(ManualSegmentationResource prefixExceptions) {
		this.prefixExceptions = prefixExceptions;
		return this;
	}

	public void setPrefixes(Terminology termino)  {
		Segmentation segmentation;
		for(Term swt:termino.getTerms()) {
			if(!swt.isSingleWord())
				continue;
			Word word = swt.getWords().get(0).getWord();
			segmentation = prefixExceptions.getSegmentation(word.getLemma());
			if(segmentation != null) 
				if(segmentation.size() <= 1) {
					for(TermRelation tv:Lists.newArrayList(termino.getOutboundRelations(swt, RelationType.IS_PREFIX_OF))) {
						termino.removeRelation(tv);
						watch(swt, tv);
					}
				} else {
					LOGGER.warn("Ignoring prefix exception {}->{} since non-expty prefix exceptions are not allowed.",
							word.getLemma(),
							segmentation);
				}
		}
	}

	private void watch(Term swt, TermRelation tv) {
		if(history != null) {
			if(history.isGKeyWatched(swt.getGroupingKey()))
				history.saveEvent(
						swt.getGroupingKey(), 
						this.getClass(), 
						"Prefix variation of term " + tv.getTo().getGroupingKey() + " removed");
			if(history.isGKeyWatched(tv.getTo().getGroupingKey()))
				history.saveEvent(
						tv.getTo().getGroupingKey(), 
						this.getClass(), 
						"Prefix variation of term " + swt + " removed");
		}
	}
}
