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

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.resource.ResourceInitializationException;

import com.google.common.base.Splitter;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import eu.project.ttc.engines.AbstractTermIndexExporter;
import eu.project.ttc.engines.cleaner.TermProperty;
import eu.project.ttc.models.Term;
import eu.project.ttc.models.TermVariation;
import eu.project.ttc.models.VariationType;
import eu.project.ttc.utils.TermSuiteConstants;

public class VariationExporter extends AbstractTermIndexExporter {

	private static final String SOURCE_LINE_FORMAT = "%-30s f=%-3d";
	private static final String EMPTY_LINE_FORMAT = "%-36s";

	private static final String TARGET_LINE_FORMAT = " %-25s %-30s f=%d %n";

	public static final String VARIATION_TYPES = "VariationTypes";
	@ConfigurationParameter(name = VARIATION_TYPES, mandatory=true)
	private String variationTypeStrings;
	protected List<VariationType> variationTypes;

	@Override
	public void initialize(UimaContext context) throws ResourceInitializationException {
		super.initialize(context);
		List<String> strings = Splitter.on(TermSuiteConstants.COMMA).splitToList(variationTypeStrings);
		variationTypes = Lists.newArrayList();
		for(String vType:strings) 
			variationTypes.add(VariationType.valueOf(vType));
	}
	
	@Override
	protected void processAcceptedTerms(TreeSet<Term> acceptedTerms) throws AnalysisEngineProcessException {
		
		try {
			Multimap<Term,TermVariation> acceptedVariations = HashMultimap.create();
			for(Term t:acceptedTerms) {
				for(TermVariation v:t.getVariations()) {
					if(this.variationTypes.contains(v.getVariationType())) {
						acceptedVariations.put(t, v);
					}
				}
			}
			
			Set<Term> sortedTerms = new TreeSet<Term>(TermProperty.SPECIFICITY.getComparator(
					this.termIndexResource.getTermIndex(), 
					true));
			sortedTerms.addAll(acceptedVariations.keySet());
			
			for(Term t:sortedTerms) {
				Set<TermVariation> variations = Sets.newHashSet(acceptedVariations.get(t));
				boolean first = true;
				for(TermVariation tv:variations) {
					if(first)
						writer.write(String.format(SOURCE_LINE_FORMAT, 
							t.getGroupingKey(),
							t.getFrequency()));
					else
						writer.write(String.format(EMPTY_LINE_FORMAT, ""));
					writer.write(String.format(TARGET_LINE_FORMAT,
							tv.getVariationType() + " ["+tv.getInfo()+"]",
							tv.getVariant().getGroupingKey(),
							tv.getVariant().getFrequency()
							));
					first = false;
				}
			}
		} catch (IOException e) {
			throw new AnalysisEngineProcessException(e);
		}		
	}
}
