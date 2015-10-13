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
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.io.IOUtils;
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
import eu.project.ttc.utils.TermUtils;

/**
 * Exports a {@link TermIndex} in TSV format.
 * 
 * @author Damien Cram
 *
 */
public class TSVExporter extends AbstractTermIndexExporter {
	private static final Logger LOGGER = LoggerFactory.getLogger(TSVExporter.class);
	
	private static final double THRESHOLD_EXTENSION_GAIN = 0.333333d;

	
	public static final String TERM_PROPERTIES = "TermProperties";
	@ConfigurationParameter(name=TERM_PROPERTIES, mandatory=false, defaultValue="pilot,specificity")
	private String termPropertyList;
	
	/*
	 * Internal fields
	 */

	/* the tsv index */
	private IndexerTSVBuilder tsv;
	private OutputStreamWriter streamWriter = null;
	
	@Override
	public void initialize(UimaContext context)
			throws ResourceInitializationException {
		super.initialize(context);
		try {
			List<TermProperty> properties = Lists.newArrayList();
			for(String propertyName:Splitter.on(",").splitToList(termPropertyList))
				properties.add(TermProperty.forName(propertyName));
			streamWriter = new OutputStreamWriter(
				new FileOutputStream(new File(toFilePath)),
				Charset.forName("UTF-8").newEncoder());
			tsv = new IndexerTSVBuilder(
					streamWriter,
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
			double maxWRlog = 0d;

			for(Term t:termIndexResource.getTermIndex().getTerms()) {
				if(t.getWRLog() > maxWRlog)
					maxWRlog = t.getWRLog();
			}
					
					
			Set<Term> ignore = Sets.newHashSet();
			for(final Term t:acceptedTerms) {
				if(ignore.contains(t))
					continue;
				tsv.startTerm(t);
				List<ScoredVariation> variations = Lists.newArrayListWithExpectedSize(t.getVariations().size());
				
				// get max variation frequency
				
				int maxFrequency = 0;
				for(TermVariation tv:t.getVariations())
					if(tv.getVariant().getFrequency() > maxFrequency)
						maxFrequency = tv.getVariant().getFrequency();
				
				for(TermVariation tv:t.getVariations()) {
					double strictness = 100*TermUtils.getStrictness(tv.getVariant(), t);
					double extensionGain = 100*THRESHOLD_EXTENSION_GAIN;
					double extensionSpec = 0d;
					double frequencyScore = 100*((double)tv.getVariant().getFrequency())/maxFrequency;

					if(strictness < 100d) {
						// probably an extension
						Term extensionAffix = null;
						try {
							extensionAffix = TermUtils.getExtensionAffix(
									termIndexResource.getTermIndex(),
									tv.getBase(),
									tv.getVariant()
								);
							if(extensionAffix == null) {
//								LOGGER.warn("Found no affix in TermIndex for extension term {} and base term {}", tv.getVariant(), tv.getBase());
							} else {
								extensionGain = 100*TermUtils.getExtensionGain(
										tv.getVariant(), 
										extensionAffix);
								extensionSpec = 100*(extensionAffix.getWRLog() / maxWRlog);
							}
						} catch(IllegalStateException e) {
							// do nothing
							LOGGER.warn(e.getMessage());
						}
					}
					variations.add(new ScoredVariation(
							tv, 
							strictness, 
							extensionGain,
							extensionSpec,
							frequencyScore));
				}
				Collections.sort(variations);
				for(ScoredVariation v:variations) {
					tsv.addVariant(v.getVariant(), v.getLabel());
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
	
	
	@Override
	public void destroy() {
		super.destroy();
		IOUtils.closeQuietly(streamWriter);
	}
}
