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
package eu.project.ttc.models;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;

import org.apache.uima.jcas.JCas;

import eu.project.ttc.engines.desc.Lang;
import eu.project.ttc.models.index.CustomTermIndex;
import eu.project.ttc.models.index.TermMeasure;
import eu.project.ttc.models.index.TermValueProvider;
import eu.project.ttc.models.index.selectors.TermSelector;
import eu.project.ttc.types.TermOccAnnotation;

public interface TermIndex {

	
	
	/*
	 * Attributes
	 */
	
	/**
	 * The term index id name
	 * @return
	 */
	public String getName();
	
	/**
	 * The language of the pipeline
	 * @return
	 */
	public Lang getLang();
	public void setCorpusId(String corpusID);
	public String getCorpusId();
	
	/**
	 * The occurrence store 
	 */
	public OccurrenceStore getOccurrenceStore();

	
	/*
	 * Terms
	 */
	public Term getTermByGroupingKey(String groupingKey);
	public Term getTermById(int termId);
//	public TermBuilder newTerm(String termId);
	public void removeTerm(Term t);
	public void addTerm(Term term);
	public Collection<Term> getTerms();

	/*
	 * Words
	 */
	public void addWord(Word word);
	public Collection<Word> getWords();
	public void cleanOrphanWords();
	public Word getWord(String lemma);

	/*
	 * Documents
	 */
	/**
	 * Returns the document identified by this url or 
	 * creates a new one.
	 * 
	 * @param url
	 * 			A url accessible by {@link File}'s constructor.
	 * @return
	 * 			The created document
	 */
	public Document getDocument(String url);
	public Collection<Document> getDocuments();
	
	
	/**
	 * Retrieves all variations of a given type, or all variations
	 * if no parameter are given.
	 * 
	 * @param types
	 * @return
	 */
	public Collection<TermVariation> getTermVariations(VariationType... types);
	
	/**
	 * Retrieves all term variation variations of a given term base.
	 * 
	 * @param base
	 * @param types
	 * @return
	 */
	public Collection<TermVariation> getOutboundTermVariations(Term base, VariationType... types);
	
	/**
	 * Retrieves all term base variation of a given term variation.
	 * 
	 * @param variant
	 * @param types
	 * @return
	 */
	public Collection<TermVariation> getInboundTermVariations(Term variant, VariationType... types);
	public TermVariation addTermVariation(Term base, Term variant, VariationType type, Object info);
	public void addTermVariation(TermVariation termVariation);
	public void removeTermVariation(TermVariation variation);

	
	public Iterator<Term> singleWordTermIterator();
	public Iterator<Term> multiWordTermIterator();
	public Iterator<Term> compoundWordTermIterator();
	
	/*
	 * Custom indexes
	 */
	public CustomTermIndex getCustomIndex(String indexName);
	public CustomTermIndex createCustomIndex(String indexName, TermValueProvider termClassProvider);
	public void dropCustomIndex(String indexName);

	/*
	 * Term measures
	 */
	public TermMeasure getWRMeasure();
	public TermMeasure getWRLogMeasure();
	public TermMeasure getFrequencyMeasure();
	public Iterable<TermMeasure> getMeasures();

	
	public void deleteMany(TermSelector selector);

	
	/*
	 * Occurrences
	 */
	//TODO remove these
	@Deprecated // Should use import JCas (important for the inner nbWordAnnotation)
	public Term addTermOccurrence(TermOccAnnotation annotation, String FileUri, boolean keepOccurrenceInTermIndex);
	public void createOccurrenceIndex();
	public void clearOccurrenceIndex();

	public void importCas(JCas cas, boolean keepOccurrenceInTermIndex);

	
	/*
	 * Id generator
	 */
	public int newId();

	public void setWordAnnotationsNum(int nbWordAnnotations);
	public int getWordAnnotationsNum();

	/**
	 * 
	 * The number of terms added to this TermIndex by invoking
	 * {@link TermIndex#addTermOccurrence(TermOccAnnotation, String, boolean)}
	 * 
	 * @return
	 * 		The number of spotted terms, 0 if none has been added through
	 * 		the method {@link #addTermOccurrence(TermOccAnnotation, String, boolean)}
	 */
	public int getSpottedTermsNum();
	public void setSpottedTermsNum(int nbSpottedTerms);

}
