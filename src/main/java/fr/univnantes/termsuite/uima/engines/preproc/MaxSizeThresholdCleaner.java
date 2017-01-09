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

import java.util.concurrent.Semaphore;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.ExternalResource;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.univnantes.termsuite.framework.TerminologyService;
import fr.univnantes.termsuite.model.Terminology;
import fr.univnantes.termsuite.uima.resources.termino.TerminologyResource;
import fr.univnantes.termsuite.utils.TermSuiteResourceManager;


/**
 * Removes terms from the {@link Terminology}.
 */
public class MaxSizeThresholdCleaner extends JCasAnnotator_ImplBase {
	private static final Logger logger = LoggerFactory.getLogger(MaxSizeThresholdCleaner.class);

	public static final String MAX_SIZE="MaxSize";
	@ConfigurationParameter(name=MAX_SIZE, mandatory=true)
	private int maxSize;
	
	private static final Float REMOVAL_RATIO_THRESHHOLD = 0.6f;
	private int currentThreshhold = 2;
	
//	public static final String TERMINOLOGY_SERVICE_NAME="TerminologyServiceName";
//	@ConfigurationParameter(name=TERMINOLOGY_SERVICE_NAME, mandatory=true)
//	private String terminologyServiceName;

	public static final String CLEANING_MUTEX_NAME="CleaningMutexName";
	@ConfigurationParameter(name=CLEANING_MUTEX_NAME, mandatory=true)
	private String mutexName;

	@ExternalResource(key=TerminologyResource.TERMINOLOGY, mandatory=true)
	private TerminologyResource terminoResource;

	private Semaphore cleanerMutex;

	private TerminologyService terminologyService;
	@Override
	public void initialize(UimaContext context) throws ResourceInitializationException {
		super.initialize(context);
		this.cleanerMutex = (Semaphore)TermSuiteResourceManager.getInstance().get(mutexName);
		this.terminologyService = new TerminologyService(terminoResource.getTerminology());
	}
	
	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		try {
			cleanerMutex.acquire();
			cleanIfTooBig();
			cleanerMutex.release();
		} catch (InterruptedException e) {
			throw new AnalysisEngineProcessException(e);
		}
	}

	public void cleanIfTooBig() {
		
		long sizeBefore = terminologyService.termCount();
		if(sizeBefore >= maxSize) {
			logger.debug(
					"Current term index size = {} (> {}). Start cleaning with th={}", 
					sizeBefore,
					maxSize, 
					currentThreshhold);
			
			terminologyService.removeAll(t-> t.getFrequency() < currentThreshhold);

			long sizeAfter = terminologyService.termCount();
			double removalRatio = ((double)(sizeBefore - sizeAfter))/sizeBefore;
			logger.info(
					"Cleaned {} terms [before: {}, after: {}, ratio: {}] from term index (maxSize: {}, currentTh: {})", 
					sizeBefore - sizeAfter,
					sizeBefore,
					sizeAfter,
					String.format("%.3f",removalRatio),
					maxSize, 
					currentThreshhold);
			logger.debug(
					"Removal ratio is: {}. (needs < {} to increase currentTh)", 
					String.format("%.3f",removalRatio),
					String.format("%.3f",REMOVAL_RATIO_THRESHHOLD)
				);
			if(removalRatio < REMOVAL_RATIO_THRESHHOLD) {
				logger.info("Increasing frequency threshhold from {} to {}.",
						this.currentThreshhold,
						this.currentThreshhold+1
						);
				this.currentThreshhold++;
			}
		}
	}
}
