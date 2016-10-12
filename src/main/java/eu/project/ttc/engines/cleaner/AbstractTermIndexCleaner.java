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
package eu.project.ttc.engines.cleaner;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.ExternalResource;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

import eu.project.ttc.history.TermHistoryResource;
import eu.project.ttc.models.Term;
import eu.project.ttc.models.TermIndex;
import eu.project.ttc.models.TermVariation;
import eu.project.ttc.resources.TermIndexResource;

/**
 * 
 * An abstract AE for {@link TermIndex} post-processing cleaning 
 * based on a property.
 * 
 * @author Damien Cram
 *
 */
public abstract class AbstractTermIndexCleaner extends JCasAnnotator_ImplBase {
	private static final Logger logger = LoggerFactory.getLogger(AbstractTermIndexCleaner.class);
	
	@ExternalResource(key=TermIndexResource.TERM_INDEX, mandatory=true)
	protected TermIndexResource termIndexResource;
	
	public static final String CLEANING_PROPERTY="CleaningProperty";
	@ConfigurationParameter(name=CLEANING_PROPERTY, mandatory=true)
	protected TermProperty property;

	@ExternalResource(key =TermHistoryResource.TERM_HISTORY, mandatory = true)
	private TermHistoryResource historyResource;

	
	public static final String KEEP_VARIANTS="KeepVariants";
	@ConfigurationParameter(name=KEEP_VARIANTS, mandatory=false,defaultValue="false")
	protected boolean keepVariants;

	public static final String PERIODIC_CAS_CLEAN_ON="PeriodicCASCleanOn";
	@ConfigurationParameter(name=PERIODIC_CAS_CLEAN_ON, mandatory=false,defaultValue="false")
	protected boolean periodicCasClean;

	public static final String CLEANING_PERIOD = "CleaningPeriod";
	@ConfigurationParameter(name=CLEANING_PERIOD, mandatory=false,defaultValue="1000")
	protected int cleaningPeriod;

	public static final String NUM_TERMS_CLEANING_TRIGGER = "NumTermsCleaningTrigger";
	@ConfigurationParameter(name=NUM_TERMS_CLEANING_TRIGGER, mandatory=false,defaultValue="0")
	protected int numTermsCleaningTrigger;

	
	private int casNum = 0;
	
	@Override
	public void initialize(UimaContext context)
			throws ResourceInitializationException {
		super.initialize(context);
		this.casNum = 0;
	}
	
	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		casNum++;
		if(
				(periodicCasClean && this.casNum % this.cleaningPeriod == 0) // Term Index must be cleaned every cleaningPeriod
				|| (numTermsCleaningTrigger > 0 && this.termIndexResource.getTermIndex().getTerms().size() > numTermsCleaningTrigger) // Term Index must be cleaning because it goes above the max size
				) {
			int beforeSize = this.termIndexResource.getTermIndex().getTerms().size();
			clean();
			int afterSize = this.termIndexResource.getTermIndex().getTerms().size();
			logger.info("[{} on property {}] TermIndex cleaned at iteration {}. Before removal: {} terms, after removal: {} terms.",
					this.getClass().getSimpleName(),
					this.property,
					casNum,
					beforeSize,
					afterSize
				);
		}
	}
	
	@Override
	public void collectionProcessComplete()
			throws AnalysisEngineProcessException {
		logger.info("Cleaning TermIndex {} on property {} - {}", 
				termIndexResource.getTermIndex().getName(), 
				this.property,
				this.toString());
		clean();
	}

	protected void clean() {
		HashSet<Term> kept = Sets.newHashSet();
		HashSet<Term> rem = Sets.newHashSet();
		doCleaningPartition(kept, rem);
		
		if(keepVariants) {
			Term current;
			Set<Term> currentBases;
			for(Iterator<Term> it = rem.iterator() ; it.hasNext() ; ) {
				current = it.next();
				// checks that this is no variant of a kept term
				currentBases = Sets.newHashSet();
				for(TermVariation v:current.getBases()) 
					currentBases.add(v.getBase());

				for(Term v:currentBases) {	
					if(kept.contains(v)) {
						kept.add(current);
						it.remove();
						break;
					}
				}
			}
		}
		
		// effectively remove terms
		for(Term r:rem) {
			this.termIndexResource.getTermIndex().removeTerm(r);
			
			if(historyResource.getHistory().isWatched(r))
				historyResource.getHistory().saveEvent(
						r.getGroupingKey(), 
						this.getClass(), 
						"Term is removed from term index by cleaner AE <" + toString() + ">");
		}
		
		termIndexResource.getTermIndex().cleanOrphanWords();
	}
	
	/**
	 * Does the specialized cleaning job (on which the logic 
	 * for variant keeping will apply)
	 * 
	 * @param keptTerms
	 * 			The set of terms to keep in index (to be fill by implementor)
	 * @param removedTerms
	 * 			The set of terms to remove index (to be fill by implementor)
	 */
	protected abstract void doCleaningPartition(Set<Term> keptTerms, Set<Term> removedTerms);
}
