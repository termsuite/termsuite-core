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
package eu.project.ttc.engines;

import java.util.Deque;
import java.util.LinkedList;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.ExternalResource;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import eu.project.ttc.resources.FixedExpressionResource;
import eu.project.ttc.types.FixedExpression;
import eu.project.ttc.types.WordAnnotation;
import eu.project.ttc.utils.JCasUtils;


/**
 * 
 * 
 * @author Damien Cram
 *
 */
public class FixedExpressionSpotter extends JCasAnnotator_ImplBase {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(FixedExpressionSpotter.class);
	
	@ExternalResource(key=FixedExpressionResource.FIXED_EXPRESSION_RESOURCE, mandatory=true)
	protected FixedExpressionResource fixedExpressionResource;


	public static final String FIXED_EXPRESSION_MAX_SIZE = "FixedExpressionMaxSize";
	@ConfigurationParameter(name=FIXED_EXPRESSION_MAX_SIZE, mandatory=false, defaultValue="5")
	private int maxFixedExpressionSize;

	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		FSIterator<Annotation> it = aJCas.getAnnotationIndex(WordAnnotation.type).iterator();

		// stats
		int cnt = 0;
		
		// Buffer of last n annotations
		Deque<WordAnnotation> lastNAnnos = new LinkedList<WordAnnotation>();
		
		while (it.hasNext()) {
			WordAnnotation wa = (WordAnnotation) it.next();
			
			/*
			 * Update the buffer with the current anno.
			 */
			lastNAnnos.addLast(wa);
			if(lastNAnnos.size() > maxFixedExpressionSize)
				lastNAnnos.removeFirst();
			
			/*
			 * Iterate over the buffer in the reverse order to build
			 * candidate fixed expressions.
			 */
			LinkedList<WordAnnotation> candidateFE = Lists.newLinkedList(lastNAnnos);
			
			while(candidateFE.size() >= 2) {// needs at least size >= 2 to be a fixed expression
				
				
				/*
				 * Builds the lemma for the current candidate fixed expression
				 */
				StringBuffer candidateFELemmaBuilder = new StringBuffer();
				boolean first = true;
				for(WordAnnotation a:candidateFE) {
					if(!first)
						candidateFELemmaBuilder.append(' ');					
					candidateFELemmaBuilder.append(a.getLemma());
					first = false;
				}
				String candidateFeLemma = candidateFELemmaBuilder.toString();
				
				/*
				 * Tests if the current candidate fixed expression can be found 
				 * in the resource.
				 * 
				 */
				if(fixedExpressionResource.containsLemma(candidateFeLemma)) {
					cnt++;
					if(LOGGER.isTraceEnabled())
						LOGGER.trace("New fixed expression spotted: {} ({}: [{},{}])",
								candidateFeLemma,
								JCasUtils.getSourceDocumentAnnotation(aJCas).get().getUri(),
								candidateFE.getFirst().getBegin(),
								candidateFE.getLast().getEnd());
					
					FixedExpression fe = (FixedExpression)aJCas.getCas().createAnnotation(
							aJCas.getCasType(FixedExpression.type), 
							candidateFE.getFirst().getBegin(),
							candidateFE.getLast().getEnd());
					fe.addToIndexes();
				}

				/*
				 * Loops again without the left-most word
				 */
				candidateFE.removeFirst();
			}
		}
		
		LOGGER.debug("{} fixed expressions found in {}",
				cnt,
				JCasUtils.getSourceDocumentAnnotation(aJCas).get().getUri());
	}
	
	
	@Override
	public void collectionProcessComplete() throws AnalysisEngineProcessException {
	}
}