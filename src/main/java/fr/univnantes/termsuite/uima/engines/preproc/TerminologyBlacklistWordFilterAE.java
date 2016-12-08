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
package fr.univnantes.termsuite.uima.engines.preproc;

import java.util.Set;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ExternalResource;
import org.apache.uima.jcas.JCas;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.Terminology;
import fr.univnantes.termsuite.uima.resources.termino.TerminologyResource;
import uima.sandbox.filter.resources.FilterResource;

/**
 * An AE that filters {@link Terminology} based on a word black list. Each term having a word
 * in the black list at any of its extremity will be removed from {@link Terminology}.
 * 
 * @author Damien Cram
 *
 */
public class TerminologyBlacklistWordFilterAE extends JCasAnnotator_ImplBase{

	private static final Logger logger = LoggerFactory.getLogger(TerminologyBlacklistWordFilterAE.class);
	
	@ExternalResource(key=TerminologyResource.TERMINOLOGY, mandatory=true)
	private TerminologyResource terminoResource;

	@ExternalResource(key=FilterResource.KEY_FILTERS)
	private FilterResource filter;

//	public static final String REMOVE_AT_TERM_BOUNDARIES = "RemoveAtTermBoundaries";
//	@ConfigurationParameter(name=REMOVE_AT_TERM_BOUNDARIES, mandatory=false, defaultValue="false")
//	private boolean removeAtTermBoundaries;
	
	@Override
	public void process(JCas arg0) throws AnalysisEngineProcessException {
	}
	
	@Override
	public void collectionProcessComplete()
			throws AnalysisEngineProcessException {
		logger.info("Starting word filtering");
		
		Set<String> filters = filter.getFilters();
		
		Set<Term> toRem = Sets.newHashSet();
		for(Term t:terminoResource.getTerminology().getTerms()) {
			if(filters.contains(t.getWords().get(0).getWord().getLemma())) {
				// first word of term is a filter word
				toRem.add(t);
			} else if(t.isMultiWord() && filters.contains(t.getWords().get(t.getWords().size() - 1).getWord().getLemma())) {
				// last word of term is a filter word
				toRem.add(t);
			}
		}
		
		logger.debug("Removing {} terms", toRem.size());
		String remTermMsg = "Removing term {}";
		for(Term t:toRem) {
			logger.trace(remTermMsg, t);
			this.terminoResource.getTerminology().removeTerm(t);
		}
	};
	
}
