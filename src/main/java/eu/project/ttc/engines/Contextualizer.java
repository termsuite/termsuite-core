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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Iterators;

import eu.project.ttc.metrics.AssociationRate;
import eu.project.ttc.metrics.LogLikelihood;
import eu.project.ttc.models.ContextVector;
import eu.project.ttc.models.CrossTable;
import eu.project.ttc.models.Document;
import eu.project.ttc.models.DocumentView;
import eu.project.ttc.models.OccurrenceType;
import eu.project.ttc.models.Term;
import eu.project.ttc.models.TermIndex;
import eu.project.ttc.models.TermOccurrence;
import eu.project.ttc.utils.IteratorUtils;

/**
 * An engine that index {@link TermOccurrence}s within {@link Document}s so as
 * to make method {@link TermOccurrence}{@link #getContext()} invokable.
 * 
 * @author Damien Cram
 *
 */
public class Contextualizer  {
	private static final Logger LOGGER = LoggerFactory.getLogger(Contextualizer.class);
	
	private TermIndex termIndex;
	private int scope;
	private int minimumCooccFrequencyThreshold;
	private OccurrenceType coTermType;
	private boolean allTerms;
	private AssociationRate rate;
	
	public Contextualizer(TermIndex termIndex) {
		super();
		this.termIndex = termIndex;
		this.rate = new LogLikelihood();
		this.scope = 3;
		this.allTerms = false;
		this.coTermType = OccurrenceType.SINGLE_WORD;
		this.minimumCooccFrequencyThreshold = 1;
	}

	public Contextualizer setScope(int scope) {
		this.scope = scope;
		return this;
	}


	public Contextualizer setMinimumCooccFrequencyThreshold(int minimumCooccFrequencyThreshold) {
		this.minimumCooccFrequencyThreshold = minimumCooccFrequencyThreshold;
		return this;
	}

	public Contextualizer setCoTermType(OccurrenceType coTermType) {
		this.coTermType = coTermType;
		return this;
	}

	public Contextualizer setAllTerms(boolean allTerms) {
		this.allTerms = allTerms;
		return this;
	}

	public Contextualizer setRate(AssociationRate rate) {
		this.rate = rate;
		return this;
	}


	private Map<Document, DocumentView> documentViews;
	
	public void contextualize() {
		if(termIndex.getTerms().isEmpty())
			return;
		
		

		// 0- drop all context vectors
		LOGGER.debug("0 - Drop all context vectors");
		for(Term t:termIndex.getTerms())
			t.setContext(null);
		
		
		// 1- index all occurrences in source documents
		LOGGER.debug("1 - Create occurrence index");
		documentViews = new HashMap<>();
		for(Document document:termIndex.getOccurrenceStore().getDocuments()) 
			documentViews.put(document, new DocumentView());
		for(Term term:termIndex.getTerms())
			for(TermOccurrence occ:termIndex.getOccurrenceStore().getOccurrences(term))
				documentViews.get(occ.getSourceDocument()).indexTermOccurrence(occ);
		
		
		int total = allTerms ?   termIndex.getTerms().size() : Iterators.size(termIndex.singleWordTermIterator());
		// 2- Generate context vectors
		LOGGER.debug("2 - Create context vectors. allTerms: {} (number of contexts to compute: {})", 
				allTerms,
				total);
		Iterator<Term> iterator = getTermIterator(termIndex);
		for(Term t:IteratorUtils.toIterable(iterator)) {
			ContextVector vector =computeContextVector(termIndex, t, coTermType, scope, this.minimumCooccFrequencyThreshold);
			t.setContext(vector);
		}
		
		
		// 3- Normalize context vectors
		LOGGER.debug("3 - Normalizing context vectors");
		LOGGER.debug("3a - Generating the cross table");
		CrossTable crossTable = new CrossTable(termIndex);
		LOGGER.debug("3b - Normalizing {} context vectors", total);
		String traceMsg = "[Progress: {} / {}] Normalizing term {}";
		int progress = 0;
		for(Term t:IteratorUtils.toIterable(getTermIterator(termIndex))) {
			++progress;
			if(progress%500 == 0)
				LOGGER.trace(traceMsg, progress, total, t);
			t.getContext().toAssocRateVector(crossTable, rate, true);
		}
		
		// 4- Clean occurrence indexes in source documents
		LOGGER.debug("4 - Clear occurrence index");
		documentViews = null;
	}

	private Iterator<Term> getTermIterator(TermIndex termIndex) {
		return allTerms ? termIndex.getTerms().iterator() : termIndex.singleWordTermIterator();
	}
	
	
	/**
	 * 
	 * Regenerate the single-word contextVector of this term and returns it.
	 * 
	 * @param t 
	 * 			the term
	 * @param coTermsType
	 * @param contextSize
	 * @param cooccFrequencyThreshhold
	 * @return
	 * 		The computed {@link ContextVector} object
	 */
	public ContextVector computeContextVector(TermIndex termIndex, Term t, OccurrenceType coTermsType, int contextSize, 
			int cooccFrequencyThreshhold) {
		// 1- compute context vector
		ContextVector vector = new ContextVector(t);
		vector.addAllCooccurrences(Iterators.concat(contextIterator(termIndex, t, coTermsType, contextSize)));
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
	
	private Iterator<Iterator<TermOccurrence>> contextIterator(TermIndex termIndex, final Term t, final OccurrenceType coTermsType, final int contextSize) {
		return new AbstractIterator<Iterator<TermOccurrence>>() {
			private Iterator<TermOccurrence> it = termIndex.getOccurrenceStore().occurrenceIterator(t);
			
			@Override
			protected Iterator<TermOccurrence> computeNext() {
				if(this.it.hasNext()) {
					TermOccurrence occ = it.next();
					Document sourceDocument = occ.getSourceDocument();
					DocumentView documentView = Contextualizer.this.documentViews.get(sourceDocument);
					return documentView.contextIterator(occ, coTermsType, contextSize);
				} else
					return endOfData();
			}
		};
	}
}
