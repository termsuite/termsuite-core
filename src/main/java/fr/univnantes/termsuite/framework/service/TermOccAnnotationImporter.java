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
package fr.univnantes.termsuite.framework.service;

import java.util.Optional;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;

import org.apache.uima.cas.FSIterator;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterators;
import com.google.inject.name.Named;

import fr.univnantes.termsuite.index.Terminology;
import fr.univnantes.termsuite.model.OccurrenceStore;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.TermBuilder;
import fr.univnantes.termsuite.model.Word;
import fr.univnantes.termsuite.types.SourceDocumentInformation;
import fr.univnantes.termsuite.types.TermOccAnnotation;
import fr.univnantes.termsuite.types.WordAnnotation;
import fr.univnantes.termsuite.utils.JCasUtils;
import fr.univnantes.termsuite.utils.TermSuiteUtils;

/**
 * Imports all {@link TermOccAnnotation} to a {@link Terminology}.
 */
public class TermOccAnnotationImporter {
	private static final Logger LOGGER = LoggerFactory.getLogger(TermOccAnnotationImporter.class);
	private static final String MSG_PATTERN_EMPTY = "Pattern must not be empty";
	private static final String MSG_LEMMAS_EMPTY = "Words array must not be empty";
	private static final String MSG_NOT_SAME_LENGTH = "Pattern and words must have same length";

	private static final Float REMOVAL_RATIO_THRESHHOLD = 0.6f;
	
	private int currentThreshold = 2;
	
	@Inject
	private OccurrenceStore occurrenceStore;

	@Inject
	private TerminologyService  terminoService;
	
	@Inject
	@Named("maxSize")
	private int maxSize;

	private AtomicInteger casCount = new AtomicInteger(0);

	private Semaphore mutex = new Semaphore(1);
	
	public void importCas(JCas jCas)  {
		mutex.acquireUninterruptibly();
		importToTerminology(jCas);
		cleanIfTooBig();
		int incrementAndGet = casCount.incrementAndGet();
		mutex.release();
		if(incrementAndGet % 10 == 0)
			this.occurrenceStore.log();
	}

	private Term createOrGetTerm(String[] pattern, Word[] words) {
		Preconditions.checkArgument(pattern.length > 0, MSG_PATTERN_EMPTY);
		Preconditions.checkArgument(words.length > 0, MSG_LEMMAS_EMPTY);
		Preconditions.checkArgument(words.length == pattern.length, MSG_NOT_SAME_LENGTH);

		String termGroupingKey = TermSuiteUtils.getGroupingKey(pattern, words);
	
		Term term;
		if(this.terminoService.containsTerm(termGroupingKey)) {
			term = this.terminoService.getTerm(termGroupingKey);
		} else {
			TermBuilder builder = new TermBuilder();
			for (int i = 0; i < pattern.length; i++)
				builder.addWord(words[i], pattern[i]);
			builder.setFrequency(0);
			term = builder.create();
			this.terminoService.addTerm(term);
		}
		return term;
	}

	private synchronized Word createOrGetWord(String lemma, String stem) {
		if(!this.terminoService.containsWord(lemma))
			this.terminoService.addWord(new Word(lemma, stem));
		return this.terminoService.getWord(lemma);
	}

	public void importToTerminology(JCas jCas) {
		Optional<SourceDocumentInformation> sdi = JCasUtils.getSourceDocumentAnnotation(jCas);
		String currentFileURI = sdi.isPresent() ? sdi.get().getUri() : "(no source uri given)";
		FSIterator<Annotation> it = jCas.getAnnotationIndex(TermOccAnnotation.type).iterator();
		TermOccAnnotation toa;
		while(it.hasNext()) {
			toa = (TermOccAnnotation) it.next();
			String gKey = TermSuiteUtils.getGroupingKey(toa);
			
			Term term;
			if(terminoService.containsTerm(gKey))
				term = terminoService.getTerm(gKey);
			else {
				Word[] words = new Word[toa.getWords().size()];
				for (int i = 0; i < toa.getWords().size(); i++) {
					WordAnnotation wa = toa.getWords(i);
					if(this.terminoService.containsWord(wa.getLemma()))
						words[i] = this.terminoService.getWord(wa.getLemma());
					else
						words[i]= createOrGetWord(wa.getLemma(), wa.getStem());
				}

				term = createOrGetTerm(
						toa.getPattern().toStringArray(), words);
				term.setSpottingRule(toa.getSpottingRuleName());
			}

			term.setFrequency(term.getFrequency() + 1);
			occurrenceStore.addOccurrence(
					term,
					currentFileURI, 
					toa.getBegin(),
					toa.getEnd(),
					toa.getCoveredText());
		}
		
		FSIterator<Annotation> termIt = jCas.getAnnotationIndex(TermOccAnnotation.type).iterator();
		terminoService.incrementSpottedTermsNum(Iterators.size(termIt));
		FSIterator<Annotation> wordIt = jCas.getAnnotationIndex(WordAnnotation.type).iterator();
		terminoService.incrementWordAnnotationNum(Iterators.size(wordIt));
		occurrenceStore.flush();
	}
	
	public void cleanIfTooBig() {
		if(maxSize != -1) {
			// max size filtering is activated
		
			long sizeBefore = terminoService.termCount();
			if(sizeBefore >= maxSize) {
				LOGGER.debug(
						"Current term index size = {} (> {}). Start cleaning with th={}", 
						sizeBefore,
						maxSize, 
						currentThreshold);
				
				terminoService.removeAll(t-> t.getFrequency() < currentThreshold);
	
				long sizeAfter = terminoService.termCount();
				double removalRatio = ((double)(sizeBefore - sizeAfter))/sizeBefore;
				LOGGER.info(
						"Cleaned {} terms [before: {}, after: {}, ratio: {}] from term index (maxSize: {}, currentTh: {})", 
						sizeBefore - sizeAfter,
						sizeBefore,
						sizeAfter,
						String.format("%.3f",removalRatio),
						maxSize, 
						currentThreshold);
				LOGGER.debug(
						"Removal ratio is: {}. (needs < {} to increase currentTh)", 
						String.format("%.3f",removalRatio),
						String.format("%.3f",REMOVAL_RATIO_THRESHHOLD)
					);
				if(removalRatio < REMOVAL_RATIO_THRESHHOLD) {
					LOGGER.info("Increasing frequency threshhold from {} to {}.",
							this.currentThreshold,
							this.currentThreshold+1
							);
					this.currentThreshold++;
				}
			}
		}
	}
}
