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
package fr.univnantes.termsuite.resources;

import java.util.Iterator;
import java.util.Map;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

import fr.univnantes.termsuite.engines.splitter.CompostIndexEntry;
import fr.univnantes.termsuite.utils.IndexingKey;

public class CompostIndex {
	
	private IndexingKey<String, String> indexingKey;
	private Map<String, CompostIndexEntry> dico = Maps.newConcurrentMap();
	private Multimap<String, CompostIndexEntry> indexedDico = Multimaps.synchronizedMultimap(HashMultimap.create());
	
	public CompostIndex(IndexingKey<String, String> indexingKey) {
		super();
		this.indexingKey = indexingKey;
	}

	private CompostIndexEntry getCompostDicoEntry(String lemma) {
		CompostIndexEntry entry = dico.get(lemma);
		if(entry == null) {
			entry = new CompostIndexEntry();
			entry.setText(lemma);
			dico.put(lemma, entry);
			indexedDico.put(indexingKey.getIndexKey(lemma), entry);
		}
		return entry;
	}

	public void addDicoWord(String word) {
		getCompostDicoEntry(word).setInDico(true);
	}

	public void addNeoclassicalPrefix(String word) {
		getCompostDicoEntry(word).setInNeoClassicalPrefix(true);
	}

	public void addInCorpus(String lemma) {
		getCompostDicoEntry(lemma).setInCorpus(true);
	}
	
	public CompostIndexEntry getEntry(String word) {
		return dico.get(word);
	}

	public int size() {
		return dico.size();
	}
	
	public Iterator<CompostIndexEntry> closedEntryCandidateIterator(String segment) {
		String indexKey = indexingKey.getIndexKey(segment);
		return indexedDico.get(indexKey).iterator();
	}

	public boolean isNeoclassical(String segment) {
		CompostIndexEntry e = getEntry(segment);
		return e != null && e.isInNeoClassicalPrefix();
	}
}
