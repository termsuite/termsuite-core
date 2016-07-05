
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

import java.io.StringWriter;
import java.net.URL;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;

import eu.project.ttc.engines.desc.Lang;
import eu.project.ttc.models.Document;
import eu.project.ttc.models.Term;
import eu.project.ttc.models.TermIndex;
import eu.project.ttc.models.TermOccurrence;

/**
 * Computes the form of a term depending on its {@link Lang}.
 * 
 * @author Damien Cram
 *
 */
public class TermFormGetter {
	
	private TermIndex termIndex;
	private boolean downcase = true;
	
	private LoadingCache<Document, String> docContents = CacheBuilder.newBuilder().build(
			new CacheLoader<Document, String>() {
				@Override
				public String load(Document key) throws Exception {
					URL url = new URL(key.getUrl());
					StringWriter writer = new StringWriter();
					IOUtils.copy(url.openStream(), writer);
					return writer.toString();
				}
			}
		);
			
	private LoadingCache<Term, LinkedHashMap<String, Integer>> formCounters = CacheBuilder.newBuilder().build(
			new CacheLoader<Term, LinkedHashMap<String, Integer>>() {
				@Override
				public LinkedHashMap<String, Integer> load(Term key) throws Exception {
					
					Collection<String> occurrences = Lists.newArrayList();
					for(TermOccurrence o:termIndex.getOccurrenceStore().getOccurrences(key)) {
						String coveredText = o.getCoveredText() == null ? readCoveredText(o) : o.getCoveredText();
						occurrences.add(getForm(coveredText));
					}
					return TermSuiteUtils.getCounters(occurrences);
				}

			});
	
	
	TermFormGetter(TermIndex termIndex, boolean downcaseForms) {
		this.termIndex = termIndex;
		this.downcase = downcaseForms;
	}
	
	public Set<String> getForms(Term t) {
		return formCounters.getUnchecked(t).keySet();
	}
	
	public Map<String, Integer> getFormCounts(Term t) {
		return formCounters.getUnchecked(t);
	}
	
	public String getPilot(Term t) {
		return formCounters.getUnchecked(t).keySet().iterator().next();
	}

	
	private String readCoveredText(TermOccurrence o) {
		String documentString = docContents.getUnchecked(o.getSourceDocument());
		return documentString.substring(o.getBegin(), o.getEnd());
	}

	public String getForm(String string) {
		String trim = string.replaceAll("[\r\n\\s]+", " ").trim();
		if(downcase)
			trim = trim.toLowerCase(termIndex.getLang().getLocale());
		return trim;
		
	}

}
