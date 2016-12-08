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
package fr.univnantes.termsuite.uima.engines.termino.cleaning;

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

import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.TermProperty;
import fr.univnantes.termsuite.model.Terminology;
import fr.univnantes.termsuite.model.termino.FrequencyUnderThreshholdSelector;
import fr.univnantes.termsuite.uima.resources.termino.TerminologyResource;


/**
 * Removes terms from the {@link Terminology}.
 */
public class MaxSizeThresholdCleaner extends JCasAnnotator_ImplBase {
	private static final Logger logger = LoggerFactory.getLogger(MaxSizeThresholdCleaner.class);

	@ExternalResource(key=TerminologyResource.TERMINOLOGY, mandatory=true)
	protected TerminologyResource terminoResource;

	public static final String MAX_SIZE="MaxSize";
	@ConfigurationParameter(name=MAX_SIZE, mandatory=true)
	private int maxSize;
	
	public static final String CLEANING_PROPERTY="CleaningProperty";
	@ConfigurationParameter(name=CLEANING_PROPERTY, mandatory=true)
	protected TermProperty property;

	private static final Float REMOVAL_RATIO_THRESHHOLD = 0.6f;
	private int currentThreshhold = 2;
	
	@Override
	public void initialize(UimaContext context) throws ResourceInitializationException {
		super.initialize(context);
		Preconditions.checkArgument(property.isNumeric(),
				"Can only initialize this AE with Integer, Double or Float"
				);
	}
	
	protected boolean acceptTerm(Term term) {
		if(property.getRange().equals(Double.class))
			return term.getPropertyDoubleValue(property) >= currentThreshhold;
		else if(property.getRange().equals(Integer.class))
			return term.getPropertyIntegerValue(property) >= currentThreshhold;
		else if(property.getRange().equals(Float.class))
			return term.getPropertyFloatValue(property) >= currentThreshhold;
		else 
			throw new IllegalStateException("Should never happen since this has been checked at AE init");
	}

	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		
		int sizeBefore = terminoResource.getTerminology().getTerms().size();
		if(terminoResource.getTerminology().getTerms().size() >= maxSize) {
			logger.debug(
					"Current term index size = {} (> {}). Start cleaning with th={}", 
					sizeBefore,
					maxSize, 
					currentThreshhold);
			
			terminoResource.getTerminology().deleteMany(new FrequencyUnderThreshholdSelector(currentThreshhold));

			int sizeAfter = terminoResource.getTerminology().getTerms().size();
			double removalRatio = ((double)(sizeBefore - sizeAfter))/sizeBefore;
			logger.info(
					"Cleaned {} terms [before: {}, after: {}, ratio: {}] from term index (maxSize: {}, currentTh: {})", 
					sizeBefore - sizeAfter,
					sizeBefore,
					sizeAfter,
					String.format("%.3f",removalRatio),
					maxSize, 
					currentThreshhold);
			logger.debug(
					"Removal ratio is: {}. (needs < {} to increase currentTh)", 
					String.format("%.3f",removalRatio),
					String.format("%.3f",REMOVAL_RATIO_THRESHHOLD)
					);
			if(removalRatio < REMOVAL_RATIO_THRESHHOLD) {
				logger.info("Increasing frequency threshhold from {} to {}.",
						this.currentThreshhold,
						this.currentThreshhold+1
						);
				this.currentThreshhold++;
			}
		}
	}
}
