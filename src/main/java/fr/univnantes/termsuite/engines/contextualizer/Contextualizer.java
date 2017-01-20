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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Iterators;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.AtomicDouble;

import fr.univnantes.termsuite.api.TermSuiteException;
import fr.univnantes.termsuite.framework.Parameter;
import fr.univnantes.termsuite.framework.TerminologyEngine;
import fr.univnantes.termsuite.model.ContextVector;
import fr.univnantes.termsuite.model.ContextVector.Entry;
import fr.univnantes.termsuite.model.Document;
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
	
	@Parameter
	private ContextualizerOptions options;

	public Contextualizer() {
	}
	
	Map<Document, DocumentView> documentViews = new HashMap<>();
	
	@Override
	public void execute() {
		AssociationRate rate = createAssocRate();
				
		if(terminology.getTerms().isEmpty())
			return;
		
		// 0- drop all context vectors
		LOGGER.debug("0 - Drop all context vectors");
		for(Term t:terminology.getTerms())
			t.setContext(null);
		
		
		// 1- index all occurrences in source documents
		LOGGER.debug("1 - Create occurrence index");
		computeDocumentViews();
		
		
		// 2- Generate context vectors
		long total = setContexts();
		
		
		// 3- Normalize context vectors
		LOGGER.debug("3 - Normalizing context vectors");
		LOGGER.debug("3a - Generating the cross table");
		CrossTable crossTable = computeCrossTable();
		LOGGER.debug("3b - Normalizing {} context vectors", total);
		String traceMsg = "[Progress: {} / {}] Normalizing term {}";
		int progress = 0;
		for(Term t:IteratorUtils.toIterable(getTermIterator())) {
			++progress;
			if(progress%500 == 0)
				LOGGER.trace(traceMsg, progress, total, t);
			toAssocRateVector(t, crossTable, rate, true);
		}
		
		// 4- Clean occurrence indexes in source documents
		LOGGER.debug("4 - Clear occurrence index");
		documentViews = null;
	}

	public long setContexts() {
		long total = terminology.terms().filter(t->t.getWords().size()==1).count();
		LOGGER.debug("2 - Create context vectors. (number of contexts to compute: {})", 
				total);
		Iterator<Term> iterator = getTermIterator();
		for(Term t:IteratorUtils.toIterable(iterator)) {
			ContextVector vector =computeContextVector(t, options.getScope(), options.getMinimumCooccFrequencyThreshold());
			t.setContext(vector);
		}
		return total;
	}

	public void computeDocumentViews() {
		documentViews = new HashMap<>();
		for(Document document:terminology.getOccurrenceStore().getDocuments()) 
			documentViews.put(document, new DocumentView());
		for(Term term:terminology.getTerms())
			for(TermOccurrence occ:terminology.getOccurrenceStore().getOccurrences(term))
				documentViews.get(occ.getSourceDocument()).indexTermOccurrence(occ);
	}

	public AssociationRate createAssocRate() {
		AssociationRate rate;
		try {
			rate = options.getAssociationRate().newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new TermSuiteException("Failed to instanciate the association rate measure", e);
		}
		return rate;
	}

	private Iterator<Term> getTermIterator() {
		return terminology
				.terms()
				.filter(t->t.getWords().size()==1)
				.collect(Collectors.toList()).iterator();
	}
	
	/**
	 * Normalize this vector according to a cross table
	 * and an association rate measure.
	 * 
	 * This method recomputes all <code>{@link Entry}.frequency</code> values
	 * with the normalized ones.
	 * @param contextVector 
	 * 
	 * @param table
	 * 			the pre-computed co-occurrences {@link CrossTable}
	 * @param assocRateFunction
	 * 			the {@link AssociationRate} measure implementation
	 * @param normalize 
	 */
	public void toAssocRateVector(Term t, CrossTable table, AssociationRate assocRateFunction, boolean normalize) {
		double assocRate;
		for(Entry coterm:t.getContext().getEntries()) {
			ContextData contextData = computeContextData(table, t, coterm.getCoTerm());
			assocRate = assocRateFunction.getValue(contextData);
			t.getContext().setAssocRate(coterm.getCoTerm(), assocRate);
		}
		if(normalize)
			t.getContext().normalize();

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
	
    /**
     * 
     * Computes coefficients a, b, c and d (available) and computes the association
     * rate based on these coefficients.
     * 
     * These coefficients are made available through the not-thread safe methods <code>#getLastX()</code>.
     * 
     * @param x
     * 			the base term
     * @param y
     * 			the co term
     * @return
     * 			the association rate
     */
    public ContextData computeContextData(CrossTable crossTable, Term x, Term y) {

    	ContextData data = new ContextData();
    	
    	// A = (x & y)
    	data.setA((int)x.getContext().getNbCooccs(y));

    	// B = (x & not(y))
    	AtomicDouble a_plus_b = crossTable.getAPlusB().get(x);
    	data.setB(a_plus_b == null ? 0 : a_plus_b.intValue() - data.getA());
//        int b = x.getFrequency() - a;
    	
         // C = (not(x) & y)
    	AtomicDouble a_plus_c = crossTable.getAPlusC().get(y);
    	data.setC(a_plus_c == null ? 0 : a_plus_c.intValue() - data.getA());
//        int c = y.getFrequency() - a;
         
         // D = (not(x) & not(y))
    	data.setD(crossTable.getTotalCoOccurrences() -  data.getA() - data.getB() - data.getC());
        
		return data;
    }
    
	
	public Iterator<Iterator<TermOccurrence>> contextIterator(final Term t, final int contextSize) {
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
	
	public CrossTable computeCrossTable() {
		
		int totalCoOccurrences = 0;
	    Map<Term, AtomicDouble> aPlusB = Maps.newConcurrentMap() ;
	    Map<Term, AtomicDouble> aPlusC = Maps.newConcurrentMap();

        Term term;
        for (Iterator<Term> it1 = terminology.getTerms().iterator(); 
        		it1.hasNext() ;) {
            term = it1.next();
//            this.totalFrequency++;
            if(term.getContext() == null)
            	continue;
        	ContextVector context = term.getContext();
            for (ContextVector.Entry entry : context.getEntries()) {
            	totalCoOccurrences += entry.getNbCooccs();
                getScoreFromMap(aPlusB, term).addAndGet(entry.getNbCooccs());
                getScoreFromMap(aPlusC, entry.getCoTerm()).addAndGet(entry.getNbCooccs());
            }
        }
        
        return new CrossTable(aPlusB, aPlusC, totalCoOccurrences);
    }


    private AtomicDouble getScoreFromMap(Map<Term, AtomicDouble> aScoreMap, Term key) {
		if(aScoreMap.get(key) == null)
			aScoreMap.put(key, new AtomicDouble(0));
		return aScoreMap.get(key);
    }
}
