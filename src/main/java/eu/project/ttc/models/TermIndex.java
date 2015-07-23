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

import java.util.Collection;
import java.util.Iterator;

import eu.project.ttc.engines.desc.Lang;
import eu.project.ttc.models.index.CustomTermIndex;
import eu.project.ttc.models.index.TermClassProvider;
import eu.project.ttc.types.TermOccAnnotation;
import fr.univnantes.lina.uima.tkregex.RegexOccurrence;

public interface TermIndex {

	
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

	
	/*
	 * Terms
	 */
	public void addTerm(Term term);
	public Collection<Term> getTerms();
	/**
	 * 
	 * @param base
	 * @return
	 */
	public Term getTermByGroupingKey(String groupingKey);
	public Term getTermById(int termId);
	public TermBuilder newTerm(String termId);
	public void removeTerm(Term t);

	/*
	 * Words
	 */
	public void addWord(Word word);
	public Collection<Word> getWords();
	public void cleanOrphanWords();
	/**
	 * Get all single word terms that have the given lemma.
	 * 
	 * There is generally at most one SW term by lemma, except in a few case where
	 * lemma are the same but syntactic labels are diffrente 
	 * (e.g. "v: couvent" & "a: couvent")
	 * 
	 * 
	 * @param lemma
	 * @return
	 */
	public Collection<Term> getSingleWordTermByLemma(String lemma);

	/*
	 * Term classes
	 */
	public Collection<TermClass> getTermClasses();
	public void classifyTerms(Term classHead, Iterable<Term> classTerms);
	
	
	//TODO remove these
	public Term addTermOccurrence(TermOccAnnotation annotation, RegexOccurrence occurrence, String FileUri, boolean keepOccurrenceInTermIndex);
	public Word getWord(String lemma);
	
	//TODO remove these
	public Iterator<Term> singleWordTermIterator();
	public Iterator<Term> multiWordTermIterator();
	public Iterator<Term> compoundWordTermIterator();
	
	/*
	 * Custom indexes
	 */
	public CustomTermIndex getCustomIndex(String indexName);
	public CustomTermIndex createCustomIndex(String indexName, TermClassProvider termClassProvider);
	public void dropCustomIndex(String indexName);

	/*
	 * Occurrence index
	 */
	public void createOccurrenceIndex();
	public void clearOccurrenceIndex();

	
	/*
	 * Id generator
	 */
	public int newId();
	
	
	/*
	 * Specificity
	 */
	public float getMaxSpecificity();
	public void setMaxWR(float maxSpecificity);

	public int singleWordTermCount();
	public int multiWordTermCount();
	public int compoundWordCount();

	/*
	 * Documents
	 */
	public Document getDocument(String url);
	public Collection<Document> getDocuments();


	/*
	 * corpus word and document counts
	 */
	public int getCorpusWordCount();
	public void setCorpusWordCount(int nbWordAnnotations);

	public void setDocumentCount(int nbDocumentCount);
	public int getDocumentCount();

	public void setCorpusId(String corpusID);
	public String getCorpusId();


}
