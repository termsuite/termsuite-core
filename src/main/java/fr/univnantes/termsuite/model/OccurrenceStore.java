
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

import java.io.Closeable;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface OccurrenceStore extends Closeable {
	public static enum Type {MEMORY, DISK, EMPTY}
	public static enum State{COLLECTING,INDEXING,INDEXED}

	public Collection<TermOccurrence> getOccurrences(Term term);
	public Type getStoreType();
	public void flush();
	
	/**
	 * Returns the path to access the occurrence store if
	 * this occurrence store is of type {@link Type#FILE}, 
	 * <code>null</code> otherwise.
	 * 
	 * @return
	 * 		the URL, <code>null</code> if this store is of type {@link Type#MEMORY}
	 */
	public String getUrl();
	
	/**
	 * Removes all occurrences of the term
	 * @param t
	 */
	public void removeTerm(Term t);
	
	void close();
	public List<Form> getForms(Term term);
	public void addOccurrence(Term term, String documentUrl, int begin, int end, String coveredText);
	
	public Document getDocument(String url);
	public Collection<Document> getDocuments();
	
	public String getMostFrequentForm(Term t);
	public Set<Document> getDocuments(Term t);
	
	/**
	 * The number of occurrences in occurrence store.
	 * 
	 * @return
	 */
	public long size();
	
}
