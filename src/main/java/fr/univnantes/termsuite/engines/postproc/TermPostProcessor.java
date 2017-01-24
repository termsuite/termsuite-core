
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

package fr.univnantes.termsuite.engines.postproc;

import org.slf4j.Logger;

import fr.univnantes.termsuite.engines.prepare.IsExtensionPropertySetter;
import fr.univnantes.termsuite.framework.AggregateTerminologyEngine;
import fr.univnantes.termsuite.framework.Parameter;
import fr.univnantes.termsuite.framework.service.TerminologyService;
import fr.univnantes.termsuite.model.Terminology;
import fr.univnantes.termsuite.resources.PostProcessorOptions;

/**
 * Turn a {@link Terminology} to a {@link ScoredModel}
 * 
 * @author Damien Cram
 *
 */
public class TermPostProcessor extends AggregateTerminologyEngine {

	@Parameter
	private PostProcessorOptions config;

	@Override
	public void configure() {
		pipe("Set IS_EXTENSION before post-processor", IsExtensionPropertySetter.class);
		pipe(IndependanceScorer.class);
		pipe(OrthographicScorer.class);
		pipe(VariationScorer.class);
		pipe(ThresholdExtensionFilterer.class);
		pipe(VariationFiltererByScore.class, config);
		pipe(TwoOrderVariationMerger.class);
		pipe(TermFiltererByScore.class, config);
		pipe(VariationRanker.class);
	}

	static void logVariationsAndTerms(Logger logger, TerminologyService termino) {
		if(logger.isDebugEnabled()) {
			long var = termino.variations().count();
			logger.debug("Number of terms: {}. Number of variations: {}",
					termino.getTerms().size(), var);
		}
	}
}
