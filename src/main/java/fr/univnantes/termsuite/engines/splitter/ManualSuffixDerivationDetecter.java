
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

import static java.util.stream.Collectors.toList;

import java.util.List;

import com.google.common.collect.Lists;

import fr.univnantes.julestar.uima.resources.MultimapFlatResource;
import fr.univnantes.termsuite.engines.SimpleEngine;
import fr.univnantes.termsuite.framework.Resource;
import fr.univnantes.termsuite.framework.service.RelationService;
import fr.univnantes.termsuite.framework.service.TermService;
import fr.univnantes.termsuite.model.Relation;
import fr.univnantes.termsuite.model.RelationType;
import fr.univnantes.termsuite.uima.ResourceType;

public class ManualSuffixDerivationDetecter extends SimpleEngine {

	@Resource(type=ResourceType.SUFFIX_DERIVATION_EXCEPTIONS)
	private MultimapFlatResource manualSuffixDerivations;
	
	@Override
	public void execute() {
		TermService regularForm;
		String lemma;
		List<RelationService> toRem;
		for(TermService derivateForm:terminology.getTerms()) {
			if(!derivateForm.isSingleWord())
				continue;
			toRem = Lists.newArrayList();
			lemma = derivateForm.getWords().get(0).getWord().getLemma();
			for(String regularFormException:manualSuffixDerivations.getValues(lemma)) {
				for(RelationService tv:derivateForm.inboundRelations(RelationType.DERIVES_INTO).collect(toList())) {
					regularForm = tv.getFrom();
					if(regularForm.getWords().get(0).getWord().getLemma().equals(regularFormException)) 
						toRem.add(tv);
				}
			}
			for(RelationService rem:toRem) {
				terminology.removeRelation(rem);
				watch(rem.getRelation());
			}
		}
	}

	private void watch(Relation rem) {
		if(history.isPresent()) {
			if(history.get().isTermWatched(rem.getFrom())) {
				history.get().saveEvent(
					rem.getFrom(), 
					this.getClass(), 
					"Removing derivation into " + rem.getTo());
			}
			if(history.get().isTermWatched(rem.getTo())) {
				history.get().saveEvent(
					rem.getTo(), 
					this.getClass(), 
					"Removed as derivate of " + rem.getFrom());
			}
		}
	}
}
