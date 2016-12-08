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
package fr.univnantes.termsuite.model.termino;

import java.util.Collection;
import java.util.List;

import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.Terminology;

public interface CustomTermIndex {
	public Collection<String> keySet();
	public List<Term> getTerms(String key);
	
	/**
	 * Adds the param term to internal index. Ignores it
	 * if it is null.
	 * 
	 * @param term
	 * 			The term to add to index.
	 */
	public void indexTerm(Terminology termino, Term term);
	public void cleanSingletonKeys();
	public int size();
	public void removeTerm(Terminology termino, Term t);
	
	/**
	 * For each entry of this index, increase the frequency threshhold and 
	 * remove all terms under the frequency threshold until there are
	 * less than <code>maxSize</code> terms left in the enrty.
	 * 
	 * @param maxSize
	 */
	public void cleanEntriesByMaxSize(int maxSize);
	
	public void dropBiggerEntries(int threshholdSize, boolean logWarning);
	public boolean containsKey(String key);
	public void dropEntry(String key);
}
