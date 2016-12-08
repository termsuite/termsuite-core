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
package fr.univnantes.termsuite.uima.engines.termino;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.ExternalResource;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.univnantes.termsuite.engines.contextualizer.Contextualizer;
import fr.univnantes.termsuite.engines.contextualizer.ContextualizerOptions;
import fr.univnantes.termsuite.metrics.AssociationRate;
import fr.univnantes.termsuite.metrics.LogLikelihood;
import fr.univnantes.termsuite.metrics.MutualInformation;
import fr.univnantes.termsuite.model.OccurrenceType;
import fr.univnantes.termsuite.uima.resources.termino.TermIndexResource;

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
			
			Class<?> assocRateClass;
			try {
				assocRateClass = this.getClass().getClassLoader().loadClass(associationRateName);
			@SuppressWarnings("unchecked")
			ContextualizerOptions options = new ContextualizerOptions()
					.setScope(scope)
					.setCoTermType(coTermType)
					.setAssociationRate((Class<? extends AssociationRate>) assocRateClass)
					.setMinimumCooccFrequencyThreshold(minimumCooccFrequencyThreshold);
			
			new Contextualizer()
					.setOptions(options)
					.contextualize(termIndexResource.getTermIndex());
		} catch (ClassNotFoundException e) {
			throw new AnalysisEngineProcessException(e);
		}
						
	}
}
