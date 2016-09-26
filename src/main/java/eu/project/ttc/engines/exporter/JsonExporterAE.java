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
package eu.project.ttc.engines.exporter;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import eu.project.ttc.api.JSONOptions;
import eu.project.ttc.engines.AbstractTermIndexExporter;
import eu.project.ttc.models.OccurrenceStore;
import eu.project.ttc.models.TermIndex;
import eu.project.ttc.models.index.JSONTermIndexIO;

/**
 * Exports a {@link TermIndex} in the tsv evaluation format.
 * 
 * @author Damien Cram
 *
 */
public class JsonExporterAE extends AbstractTermIndexExporter {

	private static final Logger LOGGER = LoggerFactory.getLogger(JsonExporterAE.class);

	public static final String WITH_OCCURRENCE = "WithOccurrences";
	@ConfigurationParameter(name=WITH_OCCURRENCE, mandatory=false, defaultValue="false")
	private boolean withOccurrences;


	public static final String WITH_CONTEXTS = "WithContexts";
	@ConfigurationParameter(name=WITH_CONTEXTS, mandatory=false, defaultValue="false")
	private boolean withContexts;
	
	public static final String LINKED_MONGO_STORE = "LinkedMongoStore";
	@ConfigurationParameter(name=LINKED_MONGO_STORE, mandatory=false, defaultValue="false")
	private boolean linkedMongoStore;
	
	private OccurrenceStore occurrenceStore;

	@Override
	public void initialize(UimaContext context) throws ResourceInitializationException {
		super.initialize(context);
		occurrenceStore = termIndexResource.getTermIndex().getOccurrenceStore();
		if(linkedMongoStore) {
			Preconditions.checkArgument(
					occurrenceStore.getStoreType() == OccurrenceStore.Type.MONGODB,
					"Bad configuration for JSON Exporter %s is true but occurrence store type is %s.",
					LINKED_MONGO_STORE,
					occurrenceStore.getStoreType()
					);
		}
	}
	
	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
	}
	
	public void collectionProcessComplete() throws AnalysisEngineProcessException {
		try {
			LOGGER.info("Exporting {} terms to JSON file {}", this.termIndexResource.getTermIndex().getTerms().size(), this.toFilePath);
			FileOutputStream fos = new FileOutputStream(toFile);
			Writer writer2 = new OutputStreamWriter(fos, "UTF-8");
			JSONOptions saveOptions = new JSONOptions();
			saveOptions.withOccurrences(withOccurrences);
			saveOptions.withContexts(withContexts);
			if(linkedMongoStore)
				saveOptions.mongoDBOccStoreURI(occurrenceStore.getUrl());
			JSONTermIndexIO.save(writer2, this.termIndexResource.getTermIndex(), saveOptions);
			fos.flush();
			fos.close();
			writer2.close();
		} catch (IOException e) {
			LOGGER.error("Could not export to file due to IOException: {}", e.getMessage());
		}
	}
	
}
