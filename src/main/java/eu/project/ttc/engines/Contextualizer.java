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
package eu.project.ttc.engines;

import java.util.Iterator;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.ExternalResource;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterators;

import eu.project.ttc.metrics.AssociationRate;
import eu.project.ttc.metrics.LogLikelihood;
import eu.project.ttc.metrics.MutualInformation;
import eu.project.ttc.models.CrossTable;
import eu.project.ttc.models.Document;
import eu.project.ttc.models.OccurrenceType;
import eu.project.ttc.models.Term;
import eu.project.ttc.models.TermIndex;
import eu.project.ttc.models.TermOccurrence;
import eu.project.ttc.resources.TermIndexResource;
import eu.project.ttc.utils.IteratorUtils;

/**
 * An AE that index {@link TermOccurrence}s within {@link Document}s so as
 * to make method {@link TermOccurrence}{@link #getContext()} invokable.
 * 
 * @author Damien Cram
 *
 */
public class Contextualizer extends JCasAnnotator_ImplBase {
	private static final Logger LOGGER = LoggerFactory.getLogger(Contextualizer.class);
	
	@ExternalResource(key=TermIndexResource.TERM_INDEX, mandatory=true)
	private TermIndexResource termIndexResource;
	
	public static final String SCOPE = "Scope";
	@ConfigurationParameter(name=SCOPE, mandatory=false, defaultValue="3")
	private int scope;

	public static final String NORMALIZE_ASSOC_RATE = "NormalizeAssocRate";
	@ConfigurationParameter(name=NORMALIZE_ASSOC_RATE, mandatory=false, defaultValue="true")
	private boolean normalizeAssocRate;

	public static final String ASSOCIATION_RATE = "AssociationRate";
	@ConfigurationParameter(name=ASSOCIATION_RATE, mandatory=false)
	private String associationRateName;

	public static final String MINIMUM_COOCC_FREQUENCY_THRESHOLD = "MinCooccFrequencyThreshold";
	@ConfigurationParameter(name=MINIMUM_COOCC_FREQUENCY_THRESHOLD, mandatory=false, defaultValue="2")
	private int minimumCooccFrequencyThreshold;

	public static final String CO_TERMS_TYPE = "CoTermsType";
	@ConfigurationParameter(name=CO_TERMS_TYPE, mandatory=false, defaultValue="SINGLE_WORD")
	private String coTermsTypeString;
	private OccurrenceType coTermType;

	public static final String USE_TERM_CLASSES = "UseTermClasses";
	@ConfigurationParameter(name=USE_TERM_CLASSES, mandatory=false, defaultValue="false")
	private boolean useTermClasses;

	public static final String COMPUTE_CONTEXTS_FOR_ALL_TERMS = "ComputeContextForAllTerms";
	@ConfigurationParameter(name=COMPUTE_CONTEXTS_FOR_ALL_TERMS, mandatory=false, defaultValue="true")
	private boolean allTerms;

	/**
	 * The association rate measure for context vector normalization.
	 */
	private AssociationRate rate;

	
	@Override
	public void initialize(UimaContext context)
			throws ResourceInitializationException {
		super.initialize(context);
		this.coTermType = OccurrenceType.valueOf(coTermsTypeString);
		if(normalizeAssocRate) {
			if(associationRateName != null) {
				if(associationRateName.equalsIgnoreCase(MutualInformation.class.getName()))
					this.rate = new MutualInformation();
				else
					this.rate = new LogLikelihood();
			}
			LOGGER.debug("Context vector normalization is activated. Assoc. rate measure is {}", this.rate.getClass().getSimpleName());
		}
	}
	
	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
	}
	
	@Override
	public void collectionProcessComplete()
			throws AnalysisEngineProcessException {
		LOGGER.info("Contextualizing");

		// 0- drop all context vectors
		LOGGER.debug("0 - Drop all context vectors");
		TermIndex termIndex = termIndexResource.getTermIndex();
		for(Term t:termIndex.getTerms())
			if(t.isContextVectorComputed())
				t.clearContext();
		
		// 1- index all occurrences in source documents
		LOGGER.debug("1 - Create occurrence index");
		termIndex.createOccurrenceIndex();
		
		int total = allTerms ?   termIndex.getTerms().size() : Iterators.size(termIndex.singleWordTermIterator());
		// 2- Generate context vectors
		LOGGER.debug("2 - Create context vectors. allTerms: {} (number of contexts to compute: {})", 
				allTerms,
				total);
		Iterator<Term> iterator = getTermIterator();
		for(Term t:IteratorUtils.toIterable(iterator)) 
			t.computeContextVector(coTermType, scope, this.minimumCooccFrequencyThreshold, useTermClasses);
		
		
		// 3- Normalize context vectors
		if(normalizeAssocRate) {
			LOGGER.debug("3 - Normalizing context vectors");
			LOGGER.debug("3a - Generating the cross table");
			CrossTable crossTable = new CrossTable(termIndex);
			LOGGER.debug("3b - Normalizing {} context vectors", total);
			String traceMsg = "[Progress: {} / {}] Normalizing term {}";
			int progress = 0;
			for(Term t:IteratorUtils.toIterable(getTermIterator())) {
				++progress;
				if(progress%500 == 0)
					LOGGER.trace(traceMsg, progress, total, t);
				t.getContextVector().toAssocRateVector(crossTable, rate, true);
			}
		}
		
		// 4- Clean occurrence indexes in source documents
		LOGGER.debug("4 - Clear occurrence index");
		termIndex.clearOccurrenceIndex();
	}

	private Iterator<Term> getTermIterator() {
		return allTerms ? termIndexResource.getTermIndex().getTerms().iterator() : termIndexResource.getTermIndex().singleWordTermIterator();
	}
}
