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

import java.util.Collection;
import java.util.Iterator;
import java.util.stream.Stream;

import org.apache.uima.jcas.JCas;

import eu.project.ttc.engines.desc.Lang;
import eu.project.ttc.models.index.CustomTermIndex;
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

	
	
	/**
	 * Retrieves all relations of a given type, or all relations
	 * if no parameter are given.
	 * 
	 * @param types
	 * @return
	 */
	public Stream<TermRelation> getRelations(RelationType... types);
	
	/**
	 * Retrieves all term relation going out of a given term.
	 * 
	 * @param term
	 * @param types
	 * @return
	 */
	public Collection<TermRelation> getOutboundRelations(Term term, RelationType... types);
	
	/**
	 * Retrieves all relations going to given term.
	 * 
	 * @param toTerm
	 * @param types
	 * @return
	 */
	public Collection<TermRelation> getInboundTermRelations(Term toTerm, RelationType... types);
	public void addRelation(TermRelation relation);
	public void removeRelation(TermRelation relation);

	
	public Iterator<Term> singleWordTermIterator();
	public Iterator<Term> multiWordTermIterator();
	public Iterator<Term> compoundWordTermIterator();
	
	/*
	 * Custom indexes
	 */
	public CustomTermIndex getCustomIndex(String indexName);
	public CustomTermIndex createCustomIndex(String indexName, TermValueProvider termClassProvider);
	public void dropCustomIndex(String indexName);

	public void deleteMany(TermSelector selector);

	
	/*
	 * Occurrences
	 */
	//TODO remove these
	@Deprecated // Should use import JCas (important for the inner nbWordAnnotation)
	public Term addTermOccurrence(TermOccAnnotation annotation, String FileUri, boolean keepOccurrenceInTermIndex);

	public void importCas(JCas cas, boolean keepOccurrenceInTermIndex);

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
