
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

package eu.project.ttc.engines.morpho;

import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ExternalResource;
import org.apache.uima.jcas.JCas;

import com.google.common.collect.Lists;

import eu.project.ttc.history.TermHistory;
import eu.project.ttc.history.TermHistoryResource;
import eu.project.ttc.models.Term;
import eu.project.ttc.models.TermVariation;
import eu.project.ttc.models.VariationType;
import eu.project.ttc.resources.TermIndexResource;
import fr.univnantes.julestar.uima.resources.MultimapFlatResource;

public class SuffixDerivationExceptionSetter extends JCasAnnotator_ImplBase {
	public static final String SUFFIX_DERIVATION_EXCEPTION = "SuffixDerivationExceptions";
	@ExternalResource(key=SUFFIX_DERIVATION_EXCEPTION, mandatory=true)
	private MultimapFlatResource exceptionsByDerivateExceptionForms;

	@ExternalResource(key=TermIndexResource.TERM_INDEX, mandatory=true)
	private TermIndexResource termIndexResource;

	@ExternalResource(key =TermHistoryResource.TERM_HISTORY, mandatory = true)
	private TermHistoryResource historyResource;

	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		// do nothing
	}
	
	@Override
	public void collectionProcessComplete() throws AnalysisEngineProcessException {
		Term regularForm;
		for(Term derivateForm:termIndexResource.getTermIndex().getTerms()) {
			if(!derivateForm.isSingleWord())
				continue;
			List<TermVariation> toRem = Lists.newArrayList();
			for(String regularFormException:exceptionsByDerivateExceptionForms.getValues(derivateForm.getWords().get(0).getWord().getLemma())) {
				for(TermVariation tv:derivateForm.getBases(VariationType.DERIVES_INTO)) {
					regularForm = tv.getBase();
					if(regularForm.getWords().get(0).getWord().getLemma().equals(regularFormException)) 
						toRem.add(tv);
				}
			}
			for(TermVariation rem:toRem) {
				rem.getBase().removeTermVariation(rem);
				TermHistory history = historyResource.getHistory();
				watch(rem, history);

			}
		}
	}

	private void watch(TermVariation rem, TermHistory history) {
		if(history.isWatched(rem.getBase().getGroupingKey())) {
			history.saveEvent(
				rem.getBase().getGroupingKey(), 
				this.getClass(), 
				"Removing derivation into " + rem.getVariant());
		}
		if(history.isWatched(rem.getVariant().getGroupingKey())) {
			history.saveEvent(
				rem.getVariant().getGroupingKey(), 
				this.getClass(), 
				"Removed as derivate of " + rem.getBase());
		}

	}
}
