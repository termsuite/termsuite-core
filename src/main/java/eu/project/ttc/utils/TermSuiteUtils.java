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
package eu.project.ttc.utils;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.mutable.MutableInt;

import com.google.common.base.Preconditions;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import eu.project.ttc.types.TermOccAnnotation;
import eu.project.ttc.types.WordAnnotation;

public class TermSuiteUtils {
	public static final IndexingKey<String, String> KEY_ONE_FIRST_LETTERS = getNFirstLetterIndexingKey(1);
	public static final IndexingKey<String, String> KEY_TWO_FIRST_LETTERS = getNFirstLetterIndexingKey(2);
	public static final IndexingKey<String, String> KEY_THREE_FIRST_LETTERS = getNFirstLetterIndexingKey(3);
	
	public static IndexingKey<String, String> getNFirstLetterIndexingKey(final int n) {
		Preconditions.checkArgument(n>0, "n must be greater than 0");
		return new IndexingKey<String, String>() {
			@Override
			public String getIndexKey(String fullString) {
				if(fullString.length()<=n)
					return fullString;
				else
					return fullString.substring(0, n);
				
			}
		};
	}

	public static String getSingleWordTermId(WordAnnotation word) {
		/*
		 * Single word terms are pre-gathered by their lemma
		 */
		return word.getLemma();
	}

	public static <T> LinkedHashMap<T, Integer> getCounters(Iterable<T> list) {
		Comparator<Entry<T, MutableInt>> comparator = new Comparator<Entry<T, MutableInt>>() {
			public int compare(Entry<T,MutableInt> o1, Entry<T,MutableInt> o2) {
				return ComparisonChain.start()
						.compare(o2.getValue(), o1.getValue())
						.result();
			};
		};
		
		Map<T, MutableInt> map = Maps.newHashMap();
		for(T e:list) {
			MutableInt counter = map.get(e);
			if(counter == null) {
				counter = new MutableInt(0);
				map.put(e, counter);
			}
			counter.increment();
		}
		List<Entry<T, MutableInt>> entries = Lists.newArrayList(map.entrySet());
		Collections.sort(entries, comparator);
		LinkedHashMap<T, Integer> counters = Maps.newLinkedHashMap();
		for(Entry<T, MutableInt> e:entries)
			counters.put(e.getKey(), e.getValue().intValue());
		return counters;
	}

	public static String trimInside(String coveredText) {
		return coveredText.replaceAll(TermSuiteConstants.WHITESPACE_PATTERN_STRING, TermSuiteConstants.WHITESPACE_STRING).trim();
	}

	public static String getTermGroupingKey(TermOccAnnotation annotation) {
		return annotation.getRuleId() + ": " + annotation.getLemma();
	}
}
