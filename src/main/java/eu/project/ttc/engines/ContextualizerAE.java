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

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.ExternalResource;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.project.ttc.metrics.AssociationRate;
import eu.project.ttc.metrics.LogLikelihood;
import eu.project.ttc.metrics.MutualInformation;
import eu.project.ttc.models.OccurrenceType;
import eu.project.ttc.resources.TermIndexResource;

/**
 * A UIMA AE wrapper for {@link Contextualizer}.
 * 
 * @author Damien Cram
 *
 */
public class ContextualizerAE extends JCasAnnotator_ImplBase {
	private static final Logger LOGGER = LoggerFactory.getLogger(ContextualizerAE.class);
	
	@ExternalResource(key=TermIndexResource.TERM_INDEX, mandatory=true)
	private TermIndexResource termIndexResource;
	
	public static final String SCOPE = "Scope";
	@ConfigurationParameter(name=SCOPE, mandatory=false, defaultValue="3")
	private int scope;

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
		if(associationRateName != null) {
			if(associationRateName.equalsIgnoreCase(MutualInformation.class.getName()))
				this.rate = new MutualInformation();
			else
				this.rate = new LogLikelihood();
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
		Contextualizer contextualizer = new Contextualizer(termIndexResource.getTermIndex());
		contextualizer
					.setAllTerms(allTerms)
					.setCoTermType(coTermType)
					.setMinimumCooccFrequencyThreshold(minimumCooccFrequencyThreshold)
					.setRate(rate)
					.setScope(scope)
					.contextualize();
						
	}
}
