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

import java.util.Optional;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.ExternalResource;
import org.apache.uima.jcas.JCas;

import fr.univnantes.termsuite.engines.cleaner.TerminoFilterOptions;
import fr.univnantes.termsuite.engines.cleaner.TerminologyCleaner;
import fr.univnantes.termsuite.framework.TerminologyService;
import fr.univnantes.termsuite.model.TermProperty;
import fr.univnantes.termsuite.uima.resources.TermHistoryResource;
import fr.univnantes.termsuite.uima.resources.termino.TerminologyResource;


public class TerminologyCleanerAE extends JCasAnnotator_ImplBase {


	@ExternalResource(key=TerminologyResource.TERMINOLOGY, mandatory=true)
	protected TerminologyResource terminoResource;
	
	public static final String CLEANING_PROPERTY="CleaningProperty";
	@ConfigurationParameter(name=CLEANING_PROPERTY, mandatory=true)
	protected TermProperty filteringProperty;

	@ExternalResource(key =TermHistoryResource.TERM_HISTORY, mandatory = true)
	private TermHistoryResource historyResource;
	
	public static final String KEEP_VARIANTS="KeepVariants";
	@ConfigurationParameter(name=KEEP_VARIANTS, mandatory=false,defaultValue="false")
	protected boolean keepVariants;

	public static final String TOP_N="TopN";
	@ConfigurationParameter(name=TOP_N, mandatory=false)
	private Integer topN = null;

	public static final String THRESHOLD="Threshold";
	@ConfigurationParameter(name=THRESHOLD, mandatory=false)
	private Float threshold;
	
	public static final String MAX_NUM_OF_VARIANTS="MaxNumOfVariants";
	@ConfigurationParameter(name=MAX_NUM_OF_VARIANTS, mandatory=false)
	private Integer maxNumOfVariants;

	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
	}
	
	@Override
	public void collectionProcessComplete() throws AnalysisEngineProcessException {
		TerminoFilterOptions options = new TerminoFilterOptions()
				.by(filteringProperty)
				.setKeepVariants(keepVariants);
		
		if(topN != null)
				options.keepTopN(topN);
		if(threshold != null)
			options.keepOverTh(threshold);
		if(maxNumOfVariants != null)
			options.setMaxNumberOfVariants(maxNumOfVariants);

		new TerminologyCleaner()
				.setHistory(Optional.ofNullable(historyResource == null ? null : historyResource.getHistory()))
				.setOptions(options)
				.clean(new TerminologyService(terminoResource.getTerminology()));
	}
}
