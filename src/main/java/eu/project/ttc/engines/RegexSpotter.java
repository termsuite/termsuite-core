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

import java.util.Collection;
import java.util.Iterator;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.examples.SourceDocumentInformation;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.ExternalResource;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.cas.StringArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uima.sandbox.filter.resources.FilterResource;

import com.google.common.base.Optional;

import eu.project.ttc.resources.OccurrenceFilter;
import eu.project.ttc.resources.TermIndexResource;
import eu.project.ttc.resources.TrueFilter;
import eu.project.ttc.types.TermOccAnnotation;
import eu.project.ttc.types.WordAnnotation;
import eu.project.ttc.utils.JCasUtils;
import eu.project.ttc.utils.OccurrenceBuffer;
import eu.project.ttc.utils.TermUtils;
import fr.univnantes.lina.uima.tkregex.LabelledAnnotation;
import fr.univnantes.lina.uima.tkregex.RegexOccurrence;
import fr.univnantes.lina.uima.tkregex.ae.TokenRegexAE;


/**
 * 
 * Adds all Token Regex Occurrences to the Cas and to the Term Index.
 * 
 * @author Damien Cram
 *
 */
public class RegexSpotter extends TokenRegexAE {
	private static final Logger LOGGER = LoggerFactory.getLogger(RegexSpotter.class);
	
	public static final String POST_PROCESSING_STRATEGY = "PostProcessingStrategy";
	@ConfigurationParameter(name = POST_PROCESSING_STRATEGY, mandatory = false, defaultValue = OccurrenceBuffer.NO_CLEANING)
	private String postProcessingStrategy;

	public static final String LOG_OVERLAPPING_RULES = "LogOverlappingRules";
	@ConfigurationParameter(name = LOG_OVERLAPPING_RULES, mandatory = false, defaultValue = "false")
	private boolean logOverlappingRules;

	public static final String CONTEXTUALIZE = "Contextualize";
	@ConfigurationParameter(name = CONTEXTUALIZE, mandatory = false, defaultValue = "false")
	private boolean contextualize;

	public static final String KEEP_OCCURRENCES_IN_TERM_INDEX = "KeepOccurrencesInTermIndex";
	@ConfigurationParameter(name = KEEP_OCCURRENCES_IN_TERM_INDEX, mandatory = false, defaultValue = "true")
	private boolean keepOccurrencesInTermIndex;


	public static final String CHARACTER_FOOTPRINT_TERM_FILTER = "CharacterFootprintTermFilter";
	@ExternalResource(key =CHARACTER_FOOTPRINT_TERM_FILTER, mandatory = false)
	private OccurrenceFilter termFilter = TrueFilter.INSTANCE;
	
	public static final String STOP_WORD_FILTER = "StopWordFilter";

	@ExternalResource(key =STOP_WORD_FILTER, mandatory = true)
	private FilterResource stopWordFilter;
	
	
	@ExternalResource(key=TermIndexResource.TERM_INDEX, mandatory=true)
	private TermIndexResource termIndexResource;
	
	private String currentFileURI;
	
	@Override
	protected void beforeRuleProcessing(JCas jCas) {
		Optional<SourceDocumentInformation> sdi = JCasUtils.getSourceDocumentAnnotation(jCas);
		this.currentFileURI = sdi.isPresent() ? sdi.get().getUri() : "(no source uri given)";
		this.occurrenceBuffer = new OccurrenceBuffer(this.postProcessingStrategy);
	}
	
	private OccurrenceBuffer occurrenceBuffer;
	
	private int addedOccurrences = 0;
	
	@Override
	public void ruleMatched(JCas jCas, RegexOccurrence occurrence) {
		/*
		 * Do not keep the term if it has too many bad characters
		 */
		if(!termFilter.accept(occurrence))
			return;

		/*
		 * Do not keep the term if it is a stop word
		 */
		WordAnnotation wa = (WordAnnotation)occurrence.getLabelledAnnotations().get(0).getAnnotation();
		if(occurrence.size() == 1 && stopWordFilter.getFilters().contains(wa.getCoveredText().toLowerCase()))
			return;
		if(occurrence.size() == 1 && wa.getLemma() != null && stopWordFilter.getFilters().contains(wa.getLemma().toLowerCase()))
			return;
		
		/*
		 * Add the occurrence the buffer. Will be added to jCas if it is not filtered by any post processing strategy
		 */
		this.occurrenceBuffer.bufferize(occurrence);
	}

	@Override
	protected void allRulesFailed(JCas jCas) {
		flushOccurrenceBuffer(jCas);
	}
	
	/*
	 * 
	 */
	private void flushOccurrenceBuffer(JCas jCas) {
		
		/*
		 * Log a warning if the occurrence was found for another rule
		 */
		if(logOverlappingRules) {
			for(Collection<RegexOccurrence> doublons:this.occurrenceBuffer.findDuplicates()) {
				Iterator<RegexOccurrence> it = doublons.iterator();
				RegexOccurrence base = it.next();
				while(it.hasNext()) {
					RegexOccurrence occ = it.next();
					LOGGER.warn("Rules {} and {} overlap on occurrence [{},{}] \"{}\"", 
							base.getRule().getName(),
							occ.getRule().getName(),
							occ.getBegin(),
							occ.getEnd(),
							TermUtils.collapseText(jCas.getDocumentText().substring(occ.getBegin(), occ.getEnd()))
						);
				}
			}
		}
		
		this.occurrenceBuffer.cleanBuffer();
		for(RegexOccurrence occ:this.occurrenceBuffer)
			addOccurrenceToCas(jCas, occ);
		this.occurrenceBuffer.clear();
	}
	
	private void addOccurrenceToCas(JCas jCas, RegexOccurrence occurrence) {
		TermOccAnnotation annotation = (TermOccAnnotation) jCas
				.getCas().createAnnotation(
						jCas.getCasType(TermOccAnnotation.type),
						occurrence.getBegin(),
						occurrence.getEnd());
		
		annotation.setCategory(occurrence.getRule().getName());
		
		StringArray patternFeature = new StringArray(jCas, occurrence.size());
		FSArray innerWords = new FSArray(jCas, occurrence.size());
		StringBuilder termLemma = new StringBuilder();
		int i = 0;
		for (LabelledAnnotation la:occurrence.getLabelledAnnotations()) {
			patternFeature.set(i, la.getLabel());
			WordAnnotation word = (WordAnnotation) la.getAnnotation();
			termLemma.append(word.getLemma());
			if(i<occurrence.size()-1)
				termLemma.append(' ');
			WordAnnotation wordAnno = (WordAnnotation) la.getAnnotation();
			if(wordAnno.getRegexLabel() != null) {
				if(!wordAnno.getRegexLabel().equals(la.getLabel())) {
					LOGGER.warn("Another label has already been set for WordAnnotation "+wordAnno.getCoveredText()+":"+wordAnno.getRegexLabel()+" ["+wordAnno.getBegin()+","+wordAnno.getEnd()+"]. Ignoring the new label "+la.getLabel()+" (rule: "+occurrence.getRule().getName()+")");
				}
			} else
				wordAnno.setRegexLabel(la.getLabel());
			innerWords.set(i, wordAnno);
			i++;
		}
		annotation.setWords(innerWords);
		annotation.setLemma(termLemma.toString());
		annotation.setPattern(patternFeature);
		annotation.setRuleId(occurrence.getRule().getName());
		//		annotation.addToIndexes();
		this.termIndexResource.getTermIndex().addTermOccurrence(annotation, occurrence, currentFileURI, keepOccurrencesInTermIndex);
		addedOccurrences++;
	}
	
	@Override
	public void collectionProcessComplete()
			throws AnalysisEngineProcessException {
		LOGGER.info("Number of spotted term occurrences added to term index: {}", addedOccurrences);
	}
	@Override
	protected void afterRuleProcessing(JCas jCas) {
		flushOccurrenceBuffer(jCas);
	}

}