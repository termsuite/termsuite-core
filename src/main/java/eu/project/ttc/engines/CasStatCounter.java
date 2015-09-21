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

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.mutable.MutableInt;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.ExternalResource;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Functions;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;

import eu.project.ttc.models.Term;
import eu.project.ttc.models.TermIndex;
import eu.project.ttc.models.TermOccurrence;
import eu.project.ttc.resources.TermIndexResource;
import fr.univnantes.lina.UIMAProfiler;

/**
 * Compiles and logs CAS stats.
 * 
 * @author Damien Cram
 *
 */
public class CasStatCounter extends JCasAnnotator_ImplBase {
	private static final Logger LOGGER = LoggerFactory.getLogger(CasStatCounter.class);
	
	private Map<String, MutableInt> counters = Maps.newHashMap();
	
	public static final String STAT_NAME = "StatName";
	@ConfigurationParameter(name=STAT_NAME, mandatory=true)
	private String statName;

	@ExternalResource(key=TermIndexResource.TERM_INDEX, mandatory=true)
	private TermIndexResource termIndexResource;

	
	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		UIMAProfiler.getProfiler("AnalysisEngine").start(this, "process");
		FSIterator<Annotation> it =  aJCas.getAnnotationIndex().iterator();
		Annotation a;
		MutableInt i;
		while(it.hasNext()) {
			a = it.next();
			i = counters.get(a.getType().getShortName());
			if(i == null) 
				counters.put(a.getType().getShortName(), new MutableInt(1));
			else
				i.increment();
		}
		UIMAProfiler.getProfiler("AnalysisEngine").stop(this, "process");
	}
	
	@Override
	public void collectionProcessComplete()
			throws AnalysisEngineProcessException {
		Ordering<String> a = Ordering.natural().reverse().onResultOf(Functions.forMap(counters)).compound(Ordering.natural());
		Map<String, MutableInt> map = ImmutableSortedMap.copyOf(counters, a);
		
		Iterator<Entry<String, MutableInt>> it = map.entrySet().iterator();
		if(it.hasNext()) {// it will be empty if pipeline is run on empty collection
			Entry<String, MutableInt> mostFrequentAnno = it.next();
			LOGGER.info("[{}] {}: {} ", statName, mostFrequentAnno.getKey(), mostFrequentAnno.getValue().intValue());
		}
		int nbSyntacticVariants = 0;
		int nbGraphicalVariants = 0;
		int nbOccurrences = 0;
		int nbPrimaryOccOccurrences = 0;
		TermIndex tIndex = termIndexResource.getTermIndex();
		for(Term t:tIndex.getTerms()) {
			nbSyntacticVariants+=t.getSyntacticVariants().size();
			nbGraphicalVariants+=t.getGraphicalVariants().size();
			nbOccurrences+=t.getOccurrences().size();
			for(TermOccurrence o:t.getOccurrences()) {
				if(o.isPrimaryOccurrence())
					nbPrimaryOccOccurrences++;
			}
		}
		// graphical variants are bidirectional
		nbGraphicalVariants/=2;
		
		LOGGER.info("[{}] Nb terms:    {} [sw: {}, mw: {}]", statName, tIndex.getTerms().size(), tIndex.singleWordTermCount(),tIndex.multiWordTermCount());
		LOGGER.info("[{}] Nb words:    {} [compounds: {}]", statName, tIndex.getWords().size(), tIndex.compoundWordCount());
		LOGGER.info("[{}] Nb occurrences: {} [primary: {}]", statName, nbOccurrences, nbPrimaryOccOccurrences);
		LOGGER.info("[{}] Nb variants: {} [syn: {}, graph: {}]", statName, nbSyntacticVariants + nbGraphicalVariants, nbSyntacticVariants, nbGraphicalVariants);
	}
}
