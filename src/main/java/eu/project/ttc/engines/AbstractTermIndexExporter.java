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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.ExternalResource;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import eu.project.ttc.api.Traverser;
import eu.project.ttc.models.TermIndex;
import eu.project.ttc.resources.TermIndexResource;

/**
 * Exports a {@link TermIndex} in TSV format
 * 
 * @author Damien Cram
 *
 */
public abstract class AbstractTermIndexExporter extends JCasAnnotator_ImplBase {
	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractTermIndexExporter.class);

	/*
	 *  AE resources
	 */
	@ExternalResource(key=TermIndexResource.TERM_INDEX, mandatory=true)
	protected TermIndexResource termIndexResource;
	
	/*
	 * AE parameters
	 */
	
	public static final String TO_FILE_PATH = "TsvFilePath";
	@ConfigurationParameter(name=TO_FILE_PATH, mandatory=true)
	protected String toFilePath;


	public static final String TRAVSER_STRING = "TraverserString";
	@ConfigurationParameter(name=TRAVSER_STRING, mandatory=false)
	protected String traverserString;
	protected Traverser traverser;
	
	/*
	 * Internal fields
	 */

	/** The destination file **/
	protected File toFile;
	
	protected FileWriter writer;

	@Override
	public void initialize(UimaContext context)
			throws ResourceInitializationException {
		super.initialize(context);
		this.toFile = new File(this.toFilePath);
		Preconditions.checkNotNull(this.toFile.getAbsoluteFile().getParentFile(), String.format("Invalid path %s.", this.toFilePath));
		Preconditions.checkState(this.toFile.getAbsoluteFile().getParentFile().canWrite(), String.format("Cannot write to directory %s.", this.toFile.getAbsoluteFile().getParentFile().getPath()));
		
		if(traverserString != null)
			this.traverser = Traverser.by(traverserString);
		else
			this.traverser = Traverser.create();
		
		try {
			this.writer = new FileWriter(toFile, false);
		} catch (IOException e) {
			LOGGER.error("Could not initialize write to file {}", toFile.getAbsolutePath());
			throw new ResourceInitializationException(e);
		}
	}
	
	
	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		// do nothing
	}
	
	@Override
	public void destroy() {
		super.destroy();
		try {
			writer.flush();
			writer.close();
		} catch (IOException e) {
			LOGGER.error("Could not close writer to file.", e);
		}

	}
}
