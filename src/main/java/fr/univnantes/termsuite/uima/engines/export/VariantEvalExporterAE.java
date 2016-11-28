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
package fr.univnantes.termsuite.uima.engines.export;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;

import fr.univnantes.termsuite.export.VariantEvalExporter;
import fr.univnantes.termsuite.model.TermIndex;
import fr.univnantes.termsuite.uima.engines.termino.AbstractTermIndexExporter;

/**
 * Exports a {@link TermIndex} in the tsv evaluation format.
 * 
 * @author Damien Cram
 *
 */
public class VariantEvalExporterAE extends AbstractTermIndexExporter {

	public static final String TOP_N = "TopN";
	@ConfigurationParameter(name = TOP_N, mandatory = false, defaultValue = "50")
	private int topN;

	public static final String NB_EXAMPLE_OCCURRENCES = "NbExampleOccurrences";
	@ConfigurationParameter(name = NB_EXAMPLE_OCCURRENCES, mandatory = false, defaultValue = "5")
	private int nbExampleOccurrences;

	public static final String OCCURRENCE_CONTEXT_SIZE = "OccurrenceContextSize";

	@ConfigurationParameter(name = OCCURRENCE_CONTEXT_SIZE, mandatory = false, defaultValue = "50")
	private int contextSize;
	
	public static final String NB_VARIANTS_PER_TERM = "NbVariantsPerTerm";
	@ConfigurationParameter(name = NB_VARIANTS_PER_TERM, mandatory = false, defaultValue = "100000")
	private int nbVariantsPerTerm;

	@Override
	public void collectionProcessComplete() throws AnalysisEngineProcessException {
		VariantEvalExporter.export(termIndexResource.getTermIndex(), writer, nbVariantsPerTerm, contextSize, nbExampleOccurrences, topN);
	}

}
