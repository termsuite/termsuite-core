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
package eu.project.ttc.utils;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.mutable.MutableInt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import eu.project.ttc.models.TermWord;
import eu.project.ttc.types.TermOccAnnotation;
import eu.project.ttc.types.WordAnnotation;

public class TermSuiteUtils {
	private static final Logger LOGGER = LoggerFactory.getLogger(TermSuiteUtils.class);
	private static final String GROUPING_KEY_FORMAT = "%s: %s";
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

	public static String getGroupingKey(TermOccAnnotation annotation) {
		StringBuilder patternSb = new StringBuilder();
		List<String> lemmas = Lists.newArrayListWithExpectedSize(annotation.getWords().size());
		for(int i=0; i< annotation.getWords().size(); i++) {
			patternSb.append(annotation.getPattern(i).toLowerCase());
			lemmas.add(annotation.getWords(i).getLemma());
		}
		return toGroupingKey(patternSb, lemmas);
	}

	private static String toGroupingKey(StringBuilder patternSb, List<String> lemmas) {
		return String.format(GROUPING_KEY_FORMAT, 
				patternSb.toString(), 
				Joiner.on(TermSuiteConstants.WHITESPACE).join(lemmas));
	}
	

	public static String getGroupingKey(TermWord... words) {
		return getGroupingKey(Lists.newArrayList(words));
	}
	public static String getGroupingKey(Collection<TermWord> words) {
		StringBuilder patternSb = new StringBuilder();
		List<String> lemmas = Lists.newArrayListWithExpectedSize(words.size());
		for(TermWord tw:words) {
			patternSb.append(tw.getSyntacticLabel().toLowerCase());
			lemmas.add(tw.getWord().getLemma());
		}
		return toGroupingKey(patternSb, lemmas);
	}

	
	
	/**
	 * 
	 */
	public static void listClasspath() {
	  ClassLoader cl = ClassLoader.getSystemClassLoader();
	  
      URL[] urls = ((URLClassLoader)cl).getURLs();

      for(URL url: urls){
      	System.out.println(url.getFile());
      }
	}
	
//	/**
//	 * Adds a path (jar or directory) to classpath of default Class loader
//	 * @param path
//	 */
//	public static void addToClasspath(String path) {
//		URLClassLoader urlClassLoader = null;
//		try {
//		    File f = new File(path);
//		    Preconditions.checkArgument(f.exists(), "No such file: %s", path);
//		    if(f.isFile()) {
//		    	ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(f));
//		    	boolean isZipped = zipInputStream.getNextEntry() != null;
//		    	Preconditions.checkArgument(isZipped, "No such file: %s", path);
//		    	zipInputStream.close();
//		    } else
//		    	Preconditions.checkArgument(f.isDirectory(), "Should be a directory or a jar : %s", f.getAbsolutePath());
//		    URI u = f.toURI();
//		    urlClassLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
//		    Class<URLClassLoader> urlClass = URLClassLoader.class;
//		    Method method;
//			method = urlClass.getDeclaredMethod("addURL", new Class[]{URL.class});
//			method.setAccessible(true);
//			LOGGER.info("Adding {} to system class loader");
//			method.invoke(urlClassLoader, new Object[]{u.toURL()});
//		} catch (Exception e) {
//			throw new RuntimeException("Could not add "+path+" to classpath", e);
//		}
//	}
}
