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
package fr.univnantes.termsuite.uima.engines.preproc;

import java.util.List;
import java.util.regex.Pattern;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import fr.univnantes.termsuite.types.WordAnnotation;

public class StringRegexFilter extends JCasAnnotator_ImplBase {
	private static final Logger LOGGER = LoggerFactory.getLogger(StringRegexFilter.class);

	private static final Pattern[] PATTERNS = {
		// url
		Pattern.compile("\\b(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]")
	};

	private int totalFiltered;
	
	@Override
	public void initialize(UimaContext context)
			throws ResourceInitializationException {
		super.initialize(context);
		this.totalFiltered = 0;
	}
	@Override
	public void process(JCas cas) throws AnalysisEngineProcessException {
		List<WordAnnotation> rem = Lists.newArrayList();
		FSIterator<Annotation> it = cas.getAnnotationIndex(WordAnnotation.type).iterator();
		WordAnnotation word;
		while(it.hasNext()) {
			word = (WordAnnotation) it.next();
			for(Pattern p:PATTERNS) 
				if(p.matcher(word.getCoveredText()).matches())
					rem.add(word);
		}
		
		this.totalFiltered += rem.size();
		
		for(WordAnnotation wa:rem)
			wa.removeFromIndexes(cas);
	}

	@Override
	public void collectionProcessComplete()
			throws AnalysisEngineProcessException {
		LOGGER.debug("Number of word annotations filtered: {}", this.totalFiltered);
	}
}
