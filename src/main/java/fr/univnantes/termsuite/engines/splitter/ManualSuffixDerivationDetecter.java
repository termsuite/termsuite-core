
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

import com.google.common.collect.Lists;

import fr.univnantes.julestar.uima.resources.MultimapFlatResource;
import fr.univnantes.termsuite.model.RelationType;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.TermRelation;
import fr.univnantes.termsuite.model.Terminology;
import fr.univnantes.termsuite.utils.TermHistory;

public class ManualSuffixDerivationDetecter {
	
	private MultimapFlatResource manualSuffixDerivations;
	private TermHistory history;
	
	public ManualSuffixDerivationDetecter setManualSuffixDerivations(MultimapFlatResource manualSuffixDerivations) {
		this.manualSuffixDerivations = manualSuffixDerivations;
		return this;
	}
	
	public ManualSuffixDerivationDetecter setHistory(TermHistory history) {
		this.history = history;
		return this;
	}

	public void detectDerivations(Terminology termino) {
		Term regularForm;
		for(Term derivateForm:termino.getTerms()) {
			if(!derivateForm.isSingleWord())
				continue;
			List<TermRelation> toRem = Lists.newArrayList();
			for(String regularFormException:manualSuffixDerivations.getValues(derivateForm.getWords().get(0).getWord().getLemma())) {
				for(TermRelation tv:termino.getInboundRelations(derivateForm, RelationType.DERIVES_INTO)) {
					regularForm = tv.getFrom();
					if(regularForm.getWords().get(0).getWord().getLemma().equals(regularFormException)) 
						toRem.add(tv);
				}
			}
			for(TermRelation rem:toRem) {
				termino.removeRelation(rem);
				watch(rem);
			}
		}
	}

	private void watch(TermRelation rem) {
		if(history != null) {
			if(history.isWatched(rem.getFrom().getGroupingKey())) {
				history.saveEvent(
					rem.getFrom().getGroupingKey(), 
					this.getClass(), 
					"Removing derivation into " + rem.getTo());
			}
			if(history.isWatched(rem.getTo().getGroupingKey())) {
				history.saveEvent(
					rem.getTo().getGroupingKey(), 
					this.getClass(), 
					"Removed as derivate of " + rem.getFrom());
			}
		}
	}
}
