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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.resource.ResourceInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import eu.project.ttc.engines.AbstractTermIndexExporter;
import eu.project.ttc.engines.cleaner.TermProperty;
import eu.project.ttc.models.Term;
import eu.project.ttc.models.TermIndex;
import eu.project.ttc.models.TermVariation;
import eu.project.ttc.tools.utils.IndexerTSVBuilder;

/**
 * Exports a {@link TermIndex} in TSV format
 * 
 * @author Damien Cram
 *
 */
public class TSVExporter extends AbstractTermIndexExporter {
	private static final Logger LOGGER = LoggerFactory.getLogger(TSVExporter.class);
	
	public static final String TERM_PROPERTIES = "TermProperties";
	@ConfigurationParameter(name=TERM_PROPERTIES, mandatory=false, defaultValue="pilot,specificity")
	private String termPropertyList;
	
	/*
	 * Internal fields
	 */

	/* the tsv index */
	private IndexerTSVBuilder tsv;
	
	@Override
	public void initialize(UimaContext context)
			throws ResourceInitializationException {
		super.initialize(context);
		
		try {
			List<TermProperty> properties = Lists.newArrayList();
			for(String propertyName:Splitter.on(",").splitToList(termPropertyList))
				properties.add(TermProperty.forName(propertyName));
			tsv = new IndexerTSVBuilder(
					new OutputStreamWriter(
						new FileOutputStream(new File(toFilePath)),
						Charset.forName("UTF-8").newEncoder()),
					properties
					);
		} catch (FileNotFoundException e) {
			LOGGER.error("Could not open a writer to file {}", this.toFilePath);
			throw new ResourceInitializationException(e);
		}
	}

	@Override
	protected void processAcceptedTerms(TreeSet<Term> acceptedTerms) throws AnalysisEngineProcessException {
		LOGGER.info("Exporting {} terms to TSV file {}", acceptedTerms.size(), this.toFilePath);
		try {
			Set<Term> ignore = Sets.newHashSet();
			for(Term t:acceptedTerms) {
				if(ignore.contains(t))
					continue;
				tsv.startTerm(t);
				for(TermVariation v:t.getVariations()) {
					tsv.addVariant(v.getVariant());
					ignore.add(v.getVariant());
				}
				tsv.endTerm();
			}
			tsv.close();
		} catch(IOException e) {
			LOGGER.error("Could not write terms to TSV file {}", this.toFilePath);
			throw new AnalysisEngineProcessException(e);
		}
	}
}
