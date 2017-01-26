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
package fr.univnantes.termsuite.model;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Stream;

import com.google.common.collect.Multimap;

public interface Terminology {
	
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
	public void removeTerm(Term t);
	public void addTerm(Term term);
	public Map<String, Term> getTerms();

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
	public Collection<TermRelation> getInboundRelations(Term toTerm, RelationType... types);
	public void addRelation(TermRelation relation);
	public void removeRelation(TermRelation relation);

	
	public long getWordAnnotationsNum();
	public void setWordAnnotationsNum(long nbWordAnnos);
	public void incWordAnnotationsNum(int nbWordNums);
	public long getSpottedTermsNum();
	public void setSpottedTermsNum(long spottedTermsNum);
	public void incSpottedTermsNum(int nbSpottedTerms);


	public Stream<TermRelation> getRelations(Term from, Term to, RelationType... types);

	public Multimap<Term, TermRelation> getOutboundRelations();

	public Multimap<Term, TermRelation> getInboundRelations();




}
