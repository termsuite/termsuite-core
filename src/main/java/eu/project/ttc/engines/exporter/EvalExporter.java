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

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.resource.ResourceInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.project.ttc.engines.AbstractTermIndexExporter;
import eu.project.ttc.models.Term;
import eu.project.ttc.models.TermIndex;
import eu.project.ttc.models.TermVariation;

/**
 * Exports a {@link TermIndex} in the tsv evaluation format.
 * 
 * @author Damien Cram
 *
 */
public class EvalExporter extends AbstractTermIndexExporter {
	private static final Logger LOGGER = LoggerFactory.getLogger(EvalExporter.class);

	public static final String WITH_VARIANTS = "WithVariants";
	@ConfigurationParameter(name=WITH_VARIANTS, mandatory=true)
	private boolean withVariants;
	
	private FileWriter writer;

	@Override
	public void initialize(UimaContext context)
			throws ResourceInitializationException {
		super.initialize(context);
		try {
			this.writer = new FileWriter(this.toFile, false);
		} catch (IOException e) {
			LOGGER.error("Unable to create FileWriter for file {}", this.toFile.getAbsolutePath());
			throw new ResourceInitializationException(e);
		}
	}

	@Override
	protected void processAcceptedTerms(TreeSet<Term> acceptedTerms) throws AnalysisEngineProcessException {
		try {
			for(Term t: acceptedTerms) {
				if(this.withVariants) {
					for (TermVariation v : t.getVariations())
							writer.write(v.getVariant().getGroupingKey() + "#");
				}
				writer.write(t.getGroupingKey());
				writer.write("\t");
				writer.write(Double.toString(termIndexResource.getTermIndex().getWRMeasure().getValue(t)));
				writer.write("\n");
			}
			writer.flush();
			writer.close();
		} catch (IOException e) {
			LOGGER.error("IO error. Unable to write to file {}", this.toFile.getAbsolutePath());
			throw new AnalysisEngineProcessException(e);

		}
	}
}
