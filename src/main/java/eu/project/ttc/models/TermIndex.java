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
package eu.project.ttc.models;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;

import eu.project.ttc.engines.desc.Lang;
import eu.project.ttc.models.index.CustomTermIndex;
import eu.project.ttc.models.index.TermMeasure;
import eu.project.ttc.models.index.TermValueProvider;
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
	
	
	/*
	 * Terms
	 */
	public Term getTermByGroupingKey(String groupingKey);
	public Term getTermById(int termId);
//	public TermBuilder newTerm(String termId);
	public void removeTerm(Term t);
	public void addTerm(Term term);
	public Collection<Term> getTerms();
	public Collection<TermClass> getTermClasses();
	public void classifyTerms(Term classHead, Iterable<Term> classTerms);

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
	
	
//	/**
//	 * Get all single word terms that have the given lemma.
//	 * 
//	 * There is generally at most one SW term by lemma, except in a few case where
//	 * lemma are the same but syntactic labels are diffrente 
//	 * (e.g. "v: couvent" & "a: couvent")
//	 * 
//	 * 
//	 * @param lemma
//	 * @return
//	 */
//	public Collection<Term> getSingleWordTermByLemma(String lemma);

	/*
	 * Term classes
	 */
	
	

	
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

	
	/*
	 * Occurrences
	 */
	//TODO remove these
	public Term addTermOccurrence(TermOccAnnotation annotation, String FileUri, boolean keepOccurrenceInTermIndex);
	public void createOccurrenceIndex();
	public void clearOccurrenceIndex();

	
	/*
	 * Id generator
	 */
	public int newId();

	public void setWordAnnotationsNum(int nbWordAnnotations);
	public int getWordAnnotationsNum();

	
}
