/*******************************************************************************
 * Copyright 2015 - CNRS (Centre National de Recherche Scientifique)
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

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.ExternalResource;
import org.apache.uima.jcas.JCas;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.project.ttc.models.Document;
import eu.project.ttc.models.TermIndex;
import eu.project.ttc.resources.TermIndexResource;
import eu.project.ttc.utils.TermOccurrenceUtils;
import fr.univnantes.lina.UIMAProfiler;

public class PrimaryOccurrenceDetector extends JCasAnnotator_ImplBase {
	private static final Logger LOGGER = LoggerFactory.getLogger(PrimaryOccurrenceDetector.class);

	public static final String DETECTION_STRATEGY = "DetectionStrategy";
	@ConfigurationParameter(name=DETECTION_STRATEGY, mandatory=true)
	private int detectionStrategy;
	
	@ExternalResource(key = TermIndexResource.TERM_INDEX, mandatory = true)
	private TermIndexResource termIndexResource;

	public void process(JCas arg0) throws AnalysisEngineProcessException {
		// do nothing
	};
	
	public void collectionProcessComplete() throws AnalysisEngineProcessException {
		UIMAProfiler.getProfiler("AnalysisEngine").start(this, "process");
		LOGGER.info("Detecting primary occurrence in TermIndex {} with strategy {}", 
				termIndexResource.getTermIndex().getName(),
				detectionStrategy);
		
		TermIndex termIndex = this.termIndexResource.getTermIndex();
		
		/*
		 * 1- Create the occurrence index
		 */
		LOGGER.debug("1 - creating the occurrence index");
		termIndex.createOccurrenceIndex();

		/*
		 * 2- Detects all primary occurrences
		 */
		LOGGER.debug("2 - detecting primary occurrences for each document");
		for(Document doc:termIndex.getDocuments()) {
			LOGGER.debug("    detecting primary occurrences for in doc {}", doc.getUrl());
			TermOccurrenceUtils.markPrimaryOccurrence(
					doc.getOccurrences(), 
					detectionStrategy);
		}

		/*
		 * 3- Destroy the occurrence index and clear memory
		 */
		LOGGER.debug("3 - clear occurrence index");
		termIndex.clearOccurrenceIndex();

		UIMAProfiler.getProfiler("AnalysisEngine").stop(this, "process");
	};
}
