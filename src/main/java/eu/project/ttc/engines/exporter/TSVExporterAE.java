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

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.ExternalResource;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

import eu.project.ttc.api.TSVOptions;
import eu.project.ttc.engines.cleaner.TermProperty;
import eu.project.ttc.models.TermIndex;
import eu.project.ttc.resources.TermIndexResource;
import eu.project.ttc.termino.export.TSVExporter;

/**
 * Exports a {@link TermIndex} in TSV format.
 * 
 * @author Damien Cram
 *
 */
public class TSVExporterAE extends JCasAnnotator_ImplBase {
	
	@ExternalResource(key=TermIndexResource.TERM_INDEX, mandatory=true)
	private TermIndexResource termIndexResource;

	public static final String TERM_PROPERTIES = "TermProperties";
	@ConfigurationParameter(name=TERM_PROPERTIES, mandatory=false, defaultValue="pilot,specificity")
	private String termPropertyList;
	
	public static final String TO_FILE_PATH = "TsvFilePath";
	@ConfigurationParameter(name=TO_FILE_PATH, mandatory=true)
	protected String toFilePath;
	
	public static final String SHOW_HEADERS = "ShowHeaders";
	@ConfigurationParameter(name=SHOW_HEADERS, mandatory=false, defaultValue="true")
	private boolean showHeaders;

	public static final String SHOW_SCORES = "ShowScores";
	@ConfigurationParameter(name=SHOW_SCORES, mandatory=false, defaultValue="false")
	private boolean showScores;

	
	/*
	 * Internal fields
	 */

	/* the tsv index */

	List<TermProperty> properties;
	@Override
	public void initialize(UimaContext context)
			throws ResourceInitializationException {
		super.initialize(context);
		
		properties = Lists.newArrayList();
		for(String propertyName:Splitter.on(",").splitToList(termPropertyList))
			properties.add(TermProperty.forName(propertyName));
	}

	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		
	}
	
	@Override
	public void collectionProcessComplete() throws AnalysisEngineProcessException {
		try (Writer writer = new OutputStreamWriter(
				new FileOutputStream(new File(toFilePath)),
				Charset.forName("UTF-8").newEncoder())) {
			
			TSVExporter.export(termIndexResource.getTermIndex(), writer, new TSVOptions()
					.setShowHeaders(showHeaders)
					.setProperties(properties));
		} catch (Exception e) {
			throw new AnalysisEngineProcessException(e);
		}
	}
	
}
