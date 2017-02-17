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
package fr.univnantes.termsuite.index;

import java.util.Iterator;

import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;

public class TermIndexStats {
	private Multimap<Integer,String> sizeCounters;
	private TermIndex customIndex;
	private int biggestSize = 0;

	public TermIndexStats(TermIndex termIndex) {
		super();
		this.customIndex = termIndex;
		compute();
	}
	
	private void compute() {
		this.sizeCounters = TreeMultimap.create();
		int size;
		for(String cls:customIndex.keySet()) {
			size= customIndex.getTerms(cls).size();
			sizeCounters.put(size, cls);
			if(size>biggestSize)
				biggestSize = size;
		}
	}
	
	public Multimap<Integer, String> getSizeCounters() {
		return sizeCounters;
	}
	
	public int getBiggestSize() {
		return biggestSize;
	}
	
	public String getBiggestClass() {
		Iterator<String> iterator = sizeCounters.get(getBiggestSize()).iterator();
		if(iterator.hasNext())
			return iterator.next();
		else
			return null;
	}
}
