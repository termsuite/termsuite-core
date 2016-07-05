
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

import eu.project.ttc.models.index.selectors.TermSelector;

public interface OccurrenceStore {
	public static enum Type {MEMORY, MONGODB}
	public static enum State{COLLECTING,INDEXING,INDEXED}

	public Iterator<TermOccurrence> occurrenceIterator(Term term);
	public Collection<TermOccurrence> getOccurrences(Term term);
	public void addOccurrence(Term term, TermOccurrence e);
	public void addAllOccurrences(Term term, Collection<TermOccurrence> c);
	public Type getStoreType();
	public void flush();
	public State getCurrentState();
	public void makeIndex();


	
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
	
	public void deleteMany(TermSelector selector);
	void close();
}
