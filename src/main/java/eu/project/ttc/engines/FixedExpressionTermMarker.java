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

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ExternalResource;
import org.apache.uima.jcas.JCas;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.project.ttc.models.Term;
import eu.project.ttc.resources.FixedExpressionResource;
import eu.project.ttc.resources.TermIndexResource;


/**
 * 
 * 
 * @author Damien Cram
 *
 */
public class FixedExpressionTermMarker extends JCasAnnotator_ImplBase {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(FixedExpressionTermMarker.class);
	
	@ExternalResource(key=FixedExpressionResource.FIXED_EXPRESSION_RESOURCE, mandatory=true)
	protected FixedExpressionResource fixedExpressionResource;

	@ExternalResource(key=TermIndexResource.TERM_INDEX, mandatory=true)
	private TermIndexResource termIndexResource;

	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
	}
	
	
	@Override
	public void collectionProcessComplete() throws AnalysisEngineProcessException {
		LOGGER.info("Start fixed expressions marker");
		if(termIndexResource.getTermIndex().getTerms().isEmpty())
			return;

		int cnt = 0;
		for(Term t:termIndexResource.getTermIndex().getTerms()) {
			boolean fixedExpression = fixedExpressionResource.containsLemma(t.getLemma());
			t.setFixedExpression(fixedExpression);
			if(fixedExpression)
				cnt ++;
		}
		
		LOGGER.debug("Num of fixed expressions: {} (out of {} terms)",
				cnt,
				termIndexResource.getTermIndex().getTerms().size());
		

	}
}