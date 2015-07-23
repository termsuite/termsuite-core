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
package eu.project.ttc.engines.exporter;

import java.io.FileWriter;
import java.io.IOException;
import java.util.TreeSet;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.project.ttc.engines.AbstractTermIndexExporter;
import eu.project.ttc.models.Term;
import eu.project.ttc.models.TermIndex;
import eu.project.ttc.models.index.JSONTermIndexIO;
import fr.univnantes.lina.UIMAProfiler;

/**
 * Exports a {@link TermIndex} in the tsv evaluation format.
 * 
 * @author Damien Cram
 *
 */
public class JsonExporter extends AbstractTermIndexExporter {

	private static final Logger LOGGER = LoggerFactory.getLogger(JsonExporter.class);

	public static final String WITH_OCCURRENCE = "WithOccurrences";
	@ConfigurationParameter(name=WITH_OCCURRENCE, mandatory=false, defaultValue="false")
	private boolean withOccurrences;


	public static final String WITH_CONTEXTS = "WithContexts";
	@ConfigurationParameter(name=WITH_CONTEXTS, mandatory=false, defaultValue="false")
	private boolean withContexts;
	
	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
	}
	
	public void collectionProcessComplete() throws AnalysisEngineProcessException {
		UIMAProfiler.getProfiler("AnalysisEngine").start(this, "process");
		try {
			LOGGER.info("Exporting {} terms to JSON file {}", this.termIndexResource.getTermIndex().getTerms().size(), this.toFilePath);
			JSONTermIndexIO.save(new FileWriter(toFile), this.termIndexResource.getTermIndex(), this.withOccurrences, this.withContexts);
		} catch (IOException e) {
			LOGGER.error("Could not export to file due to IOException: {}", e.getMessage());
		}
		UIMAProfiler.getProfiler("AnalysisEngine").stop(this, "process");
	}
	
	@Override
	protected void processAcceptedTerms(TreeSet<Term> acceptedTerms)
			throws AnalysisEngineProcessException {
	};

}
