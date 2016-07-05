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

import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ExternalResource;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

import com.google.common.collect.Lists;

import eu.project.ttc.resources.MateLemmatizerModel;
import eu.project.ttc.resources.MateTaggerModel;
import eu.project.ttc.types.WordAnnotation;
import is2.data.SentenceData09;
import is2.lemmatizer.Lemmatizer;
import is2.tag.Tagger;

/**
 * Post-process the lemma found by TreeTagger
 * 
 * @author Damien Cram
 *
 */
public class MateLemmatizerTagger extends JCasAnnotator_ImplBase {
	
	public static final String LEMMATIZER = "Lemmatizer";
	@ExternalResource(key = LEMMATIZER, mandatory = true)
	private MateLemmatizerModel mateLemmatizerModel;
	
	public static final String TAGGER = "Tagger";
	@ExternalResource(key = TAGGER, mandatory = true)
	private MateTaggerModel mateTaggerModel;
	
	@Override
	public void process(JCas jcas) throws AnalysisEngineProcessException {
		Lemmatizer mateLemmatizer = mateLemmatizerModel.getEngine();
		Tagger mateTagger = mateTaggerModel.getEngine();

		/*
		 * keeps an array of annotations in memory so as to be able 
		 * to access them by index.
		 */
		List<WordAnnotation> annotations = Lists.newArrayList();
		FSIterator<Annotation> it = jcas.getAnnotationIndex(WordAnnotation.type).iterator();
		while(it.hasNext()) {
			WordAnnotation a = (WordAnnotation) it.next();
			annotations.add(a);
		}
		
		
		String[] tokens = new String[annotations.size()+2];
		
		// preprends to fake words to prevent Mate from bugging on the two first words
		tokens[0] = "<root>";
		tokens[1] = "<root2>";
		for(int i = 0; i< annotations.size() ; i++)
			tokens[i+2] = annotations.get(i).getCoveredText();
		
		SentenceData09 mateSentence = new SentenceData09();
		mateSentence.init(tokens);

		// Run POS tagging
		mateSentence = mateTagger.apply(mateSentence);
		
		// Run lemmatization
		mateSentence = mateLemmatizer.apply(mateSentence);
		
		
		WordAnnotation wordAnnotation;
		for(int j=1; j<mateSentence.length(); j++) {
			wordAnnotation = annotations.get(j-1);
			wordAnnotation.setTag(mateSentence.ppos[j]);
			wordAnnotation.setLemma(mateSentence.plemmas[j]);
		}
	}
}
