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
package eu.project.ttc.engines.cleaner;

import java.util.Set;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.project.ttc.models.Term;
import eu.project.ttc.models.TermIndex;


/**
 * Removes terms from the {@link TermIndex}.
 */
public class MaxSizeThresholdCleaner extends AbstractTermIndexCleaner {
	private static final Logger logger = LoggerFactory.getLogger(MaxSizeThresholdCleaner.class);

	public static final String MAX_SIZE="MaxSize";
	@ConfigurationParameter(name=MAX_SIZE, mandatory=true)
	private int maxSize;
	
	private static final Float REMOVAL_RATIO_THRESHHOLD = 0.6f;
	private int currentFrequencyThreshhold = 2;
	
	protected boolean acceptTerm(Term term) {
		return property.getDoubleValue(termIndexResource.getTermIndex(), term) >= this.currentFrequencyThreshhold;
	}

	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		super.process(aJCas);
		
		int sizeBefore = termIndexResource.getTermIndex().getTerms().size();
		if(termIndexResource.getTermIndex().getTerms().size() >= maxSize) {
			logger.debug(
					"Current term index size = {} (> {}). Start cleaning with th={}", 
					sizeBefore,
					maxSize, 
					currentFrequencyThreshhold);
			clean();
			int sizeAfter = termIndexResource.getTermIndex().getTerms().size();
			float removalRatio = ((float)(sizeBefore - sizeAfter))/sizeBefore;
			logger.info(
					"Cleaned {} terms [before: {}, after: {}, ratio: {}] from term index (maxSize: {}, currentTh: {})", 
					sizeBefore - sizeAfter,
					sizeBefore,
					sizeAfter,
					String.format("%.3f",removalRatio),
					maxSize, 
					currentFrequencyThreshhold);
			logger.debug(
					"Removal ratio is: {}. (needs < {} to increase currentTh)", 
					String.format("%.3f",removalRatio),
					String.format("%.3f",REMOVAL_RATIO_THRESHHOLD)
					);
			if(removalRatio < REMOVAL_RATIO_THRESHHOLD) {
				logger.info("Increasing frequency threshhold from {} to {}.",
						this.currentFrequencyThreshhold,
						this.currentFrequencyThreshhold+1
						);
				this.currentFrequencyThreshhold++;
			}
		}
	}
	
	@Override
	protected void doCleaningPartition(Set<Term> keptTerms,
			Set<Term> removedTerms) {
		for(Term t:termIndexResource.getTermIndex().getTerms()) {
			if(acceptTerm(t))
				keptTerms.add(t);
			else
				removedTerms.add(t);
		}
	}
}
