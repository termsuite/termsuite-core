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

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.ExternalResource;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import eu.project.ttc.engines.cleaner.TermProperty;
import eu.project.ttc.models.Term;
import eu.project.ttc.models.TermClass;
import eu.project.ttc.models.TermIndex;
import eu.project.ttc.models.VariationPath;
import eu.project.ttc.resources.TermIndexResource;

/**
 * 
 * Group all terms in a term index into {@link TermClass} objects.
 * 
 * The idea behing term classes is that all possible variations 
 * (syntactic, semantic and graphical) of a term are gathered in one 
 * {@link TermClass} object. The unique representative
 * 
 * @see TermClass
 * @see Term#getVariations(eu.project.ttc.models.VariationType...)
 * @author Damien Cram
 *
 */
public class TermClassifier  extends JCasAnnotator_ImplBase {
	private static final Logger LOGGER = LoggerFactory.getLogger(TermClassifier.class);

	public static final String CLASSIFYING_PROPERTY = "ClassifyingProperty";
	@ConfigurationParameter(name=CLASSIFYING_PROPERTY, mandatory=true)
	private String classifyingPropertyString;
	private TermProperty classifyingProperty;
	
	@ExternalResource(key = TermIndexResource.TERM_INDEX, mandatory = true)
	private TermIndexResource termIndexResource;
	
	
	@Override
	public void initialize(UimaContext context)
			throws ResourceInitializationException {
		super.initialize(context);
		classifyingProperty = TermProperty.forName(classifyingPropertyString);
	}
	
	@Override
	public void process(JCas arg0) throws AnalysisEngineProcessException {
		// do nothing
	}
	
	@Override
	public void collectionProcessComplete()
			throws AnalysisEngineProcessException {
		TermIndex termIndex = this.termIndexResource.getTermIndex();
		LOGGER.info("Classifying term index {} on property {}", 
				termIndex.getName(), 
				classifyingProperty.name());
		
		
		List<Term> terms = Lists.newArrayList(termIndex.getTerms());
		Collections.sort(terms, classifyingProperty.getComparator(termIndex, true));
		Set<Term> added = Sets.newHashSetWithExpectedSize(terms.size());
		
		for(Term t:terms) {
			if(added.contains(t))
				continue;
			else {
				Set<Term> classTerms = Sets.newHashSet();
				classTerms.add(t);
				for(VariationPath path:t.getVariationPaths(10)) {
					if(added.contains(path.getVariant()))
						continue;
					else {
						added.add(path.getVariant());
						classTerms.add(path.getVariant());
					}
				}
				termIndex.classifyTerms(t, classTerms);

			}
		}
		
		LOGGER.info("Number of term classes: {} (nbTerms: {})",
				termIndex.getTermClasses().size(),
				termIndex.getTerms().size());
	}
}
