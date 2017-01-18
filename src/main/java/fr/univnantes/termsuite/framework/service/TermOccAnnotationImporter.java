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

import org.apache.uima.cas.FSIterator;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterators;

import fr.univnantes.termsuite.api.TermSuiteException;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.Terminology;
import fr.univnantes.termsuite.model.Word;
import fr.univnantes.termsuite.types.SourceDocumentInformation;
import fr.univnantes.termsuite.types.TermOccAnnotation;
import fr.univnantes.termsuite.types.WordAnnotation;
import fr.univnantes.termsuite.utils.JCasUtils;
import fr.univnantes.termsuite.utils.TermHistory;
import fr.univnantes.termsuite.utils.TermSuiteUtils;

/**
 * Imports all {@link TermOccAnnotation} to a {@link Terminology}.
 */
public class TermOccAnnotationImporter {
	private static final Logger logger = LoggerFactory.getLogger(TermOccAnnotationImporter.class);
	private static final Float REMOVAL_RATIO_THRESHHOLD = 0.6f;
	
	private int currentThreshold = 2;
	private Optional<TermHistory> history = Optional.empty();
	private Optional<Integer> maxSize = Optional.empty();
	private TerminologyService terminoService;
	private Semaphore cleanerMutex = new Semaphore(1);

	public TermOccAnnotationImporter(Terminology termino, int maxSize) {
		this(termino);
		this.maxSize = Optional.of(maxSize);
	}

	public TermOccAnnotationImporter(Terminology termino) {
		super();
		this.terminoService = new TerminologyService(termino);
	}

	public void importCas(JCas jCas)  {
		importToTerminology(jCas);
		try {
			cleanerMutex.acquire();
			cleanIfTooBig();
			cleanerMutex.release();
		} catch (InterruptedException e) {
			throw new TermSuiteException("", e);
		}
		if(casCount.incrementAndGet() % 10 == 0)
			this.terminoService.getOccurrenceStore().log();
	}

	public void importToTerminology(JCas jCas) {
		Optional<SourceDocumentInformation> sdi = JCasUtils.getSourceDocumentAnnotation(jCas);
		String currentFileURI = sdi.isPresent() ? sdi.get().getUri() : "(no source uri given)";
		FSIterator<Annotation> it = jCas.getAnnotationIndex(TermOccAnnotation.type).iterator();
		TermOccAnnotation toa;
		while(it.hasNext()) {
			toa = (TermOccAnnotation) it.next();
			String gKey = TermSuiteUtils.getGroupingKey(toa);
			watchTermImportation(gKey);
			
			Word[] words = new Word[toa.getWords().size()];
			for (int i = 0; i < toa.getWords().size(); i++) {
				WordAnnotation wa = toa.getWords(i);
				words[i]= terminoService.createOrGetWord(wa.getLemma(), wa.getStem());
			}

			Term term = terminoService.createOrGetTerm(
					toa.getPattern().toStringArray(), words);
			term.setSpottingRule(toa.getSpottingRuleName());
			terminoService.incrementFrequency(term);
			terminoService.getOccurrenceStore().addOccurrence(
					term,
					currentFileURI, 
					toa.getBegin(),
					toa.getEnd(),
					toa.getCoveredText());
		}
		
		FSIterator<Annotation> wordIt = jCas.getAnnotationIndex(WordAnnotation.type).iterator();
		terminoService.incrementWordAnnotationNum(Iterators.size(wordIt));
		terminoService.getOccurrenceStore().flush();
	}
	
	public void cleanIfTooBig() {
		if(maxSize.isPresent()) {
			// max size filtering is activated
		
			long sizeBefore = terminoService.termCount();
			if(sizeBefore >= maxSize.get()) {
				logger.debug(
						"Current term index size = {} (> {}). Start cleaning with th={}", 
						sizeBefore,
						maxSize.get(), 
						currentThreshold);
				
				terminoService.removeAll(t-> t.getFrequency() < currentThreshold);
	
				long sizeAfter = terminoService.termCount();
				double removalRatio = ((double)(sizeBefore - sizeAfter))/sizeBefore;
				logger.info(
						"Cleaned {} terms [before: {}, after: {}, ratio: {}] from term index (maxSize: {}, currentTh: {})", 
						sizeBefore - sizeAfter,
						sizeBefore,
						sizeAfter,
						String.format("%.3f",removalRatio),
						maxSize.get(), 
						currentThreshold);
				logger.debug(
						"Removal ratio is: {}. (needs < {} to increase currentTh)", 
						String.format("%.3f",removalRatio),
						String.format("%.3f",REMOVAL_RATIO_THRESHHOLD)
					);
				if(removalRatio < REMOVAL_RATIO_THRESHHOLD) {
					logger.info("Increasing frequency threshhold from {} to {}.",
							this.currentThreshold,
							this.currentThreshold+1
							);
					this.currentThreshold++;
				}
			}
		}
	}


	private void watchTermImportation(String gKey) {
		if(history.isPresent()) {
			if(history.get().isGKeyWatched(gKey)) {
				if(this.terminoService.getTerm(gKey) == null)
					history.get().saveEvent(
							gKey, 
							this.getClass(), 
							"Term added to Terminology");
			}
		}
	}
	

	private AtomicInteger casCount = new AtomicInteger(0);

}
