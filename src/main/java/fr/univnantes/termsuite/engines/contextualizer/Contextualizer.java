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
package fr.univnantes.termsuite.engines.contextualizer;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.mutable.MutableDouble;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Iterators;
import com.google.common.collect.Maps;

import fr.univnantes.termsuite.api.TermSuiteException;
import fr.univnantes.termsuite.framework.Parameter;
import fr.univnantes.termsuite.framework.TerminologyEngine;
import fr.univnantes.termsuite.framework.service.TerminologyService;
import fr.univnantes.termsuite.metrics.AssociationRate;
import fr.univnantes.termsuite.model.ContextVector;
import fr.univnantes.termsuite.model.CrossTable;
import fr.univnantes.termsuite.model.Document;
import fr.univnantes.termsuite.model.DocumentView;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.TermOccurrence;
import fr.univnantes.termsuite.utils.IteratorUtils;

/**
 * An engine that index {@link TermOccurrence}s within {@link Document}s so as
 * to make method {@link TermOccurrence}{@link #getContext()} invokable.
 * 
 * @author Damien Cram
 *
 */
public class Contextualizer extends TerminologyEngine {
	private static final Logger LOGGER = LoggerFactory.getLogger(Contextualizer.class);
	
	
	private Map<Document, DocumentView> documentViews;
	
	@Parameter
	private ContextualizerOptions options;

	public Contextualizer() {
	}
	
	@Override
	public void execute() {
		AssociationRate rate;
		try {
			rate = options.getAssociationRate().newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new TermSuiteException("Failed to instanciate the association rate measure", e);
		}
				
		if(terminology.getTerms().isEmpty())
			return;
		
		// 0- drop all context vectors
		LOGGER.debug("0 - Drop all context vectors");
		for(Term t:terminology.getTerms())
			t.setContext(null);
		
		
		// 1- index all occurrences in source documents
		LOGGER.debug("1 - Create occurrence index");
		documentViews = new HashMap<>();
		for(Document document:terminology.getOccurrenceStore().getDocuments()) 
			documentViews.put(document, new DocumentView());
		for(Term term:terminology.getTerms())
			for(TermOccurrence occ:terminology.getOccurrenceStore().getOccurrences(term))
				documentViews.get(occ.getSourceDocument()).indexTermOccurrence(occ);
		
		
		long total = terminology.terms().filter(t->t.getWords().size()==1).count();
		// 2- Generate context vectors
		LOGGER.debug("2 - Create context vectors. (number of contexts to compute: {})", 
				total);
		Iterator<Term> iterator = getTermIterator();
		for(Term t:IteratorUtils.toIterable(iterator)) {
			ContextVector vector =computeContextVector(t, options.getScope(), options.getMinimumCooccFrequencyThreshold());
			t.setContext(vector);
		}
		
		
		// 3- Normalize context vectors
		LOGGER.debug("3 - Normalizing context vectors");
		LOGGER.debug("3a - Generating the cross table");
		CrossTable crossTable = computeCrossTable(terminology);
		LOGGER.debug("3b - Normalizing {} context vectors", total);
		String traceMsg = "[Progress: {} / {}] Normalizing term {}";
		int progress = 0;
		for(Term t:IteratorUtils.toIterable(getTermIterator())) {
			++progress;
			if(progress%500 == 0)
				LOGGER.trace(traceMsg, progress, total, t);
			t.getContext().toAssocRateVector(crossTable, rate, true);
		}
		
		// 4- Clean occurrence indexes in source documents
		LOGGER.debug("4 - Clear occurrence index");
		documentViews = null;
	}

	private Iterator<Term> getTermIterator() {
		return terminology
				.terms()
				.filter(t->t.getWords().size()==1)
				.collect(Collectors.toList()).iterator();
	}
	
	private ContextVector computeContextVector(Term t,int contextSize, 
			int cooccFrequencyThreshhold) {
		// 1- compute context vector
		ContextVector vector = new ContextVector(t);
		vector.addAllCooccurrences(Iterators.concat(contextIterator(t, contextSize)));
		vector.removeCoTerm(t);
		
		// 2- filter entries that under the co-occurrence threshold
		if(cooccFrequencyThreshhold > 1) {
			for(ContextVector.Entry e:vector.getEntries()) {
				if(e.getNbCooccs()<cooccFrequencyThreshhold)
					vector.removeCoTerm(e.getCoTerm());
			}
		}
		
		return vector;
	}
	
	private Iterator<Iterator<TermOccurrence>> contextIterator(final Term t, final int contextSize) {
		return new AbstractIterator<Iterator<TermOccurrence>>() {
			private Iterator<TermOccurrence> it = terminology.getOccurrenceStore().getOccurrences(t).iterator();
			
			@Override
			protected Iterator<TermOccurrence> computeNext() {
				if(this.it.hasNext()) {
					TermOccurrence occ = it.next();
					Document sourceDocument = occ.getSourceDocument();
					DocumentView documentView = Contextualizer.this.documentViews.get(sourceDocument);
					return documentView.contextIterator(occ, contextSize);
				} else
					return endOfData();
			}
		};
	}
	
	private CrossTable computeCrossTable(TerminologyService terminologyService) {
		
		int totalCoOccurrences = 0;
	    Map<Term, MutableDouble> aPlusB = Maps.newConcurrentMap() ;
	    Map<Term, MutableDouble> aPlusC = Maps.newConcurrentMap();

        Term term;
        for (Iterator<Term> it1 = terminologyService.getTerms().iterator(); 
        		it1.hasNext() ;) {
            term = it1.next();
//            this.totalFrequency++;
            if(term.getContext() == null)
            	continue;
        	ContextVector context = term.getContext();
            for (ContextVector.Entry entry : context.getEntries()) {
            	totalCoOccurrences += entry.getNbCooccs();
                getScoreFromMap(aPlusB, term).add(entry.getNbCooccs());
                getScoreFromMap(aPlusC, entry.getCoTerm()).add(term.getFrequency());
            }
        }
        
        return new CrossTable(aPlusB, aPlusC, totalCoOccurrences);
    }


    private MutableDouble getScoreFromMap(Map<Term, MutableDouble> aScoreMap, Term key) {
		if(aScoreMap.get(key) == null)
			aScoreMap.put(key, new MutableDouble(0));
		return aScoreMap.get(key);
    }
}
