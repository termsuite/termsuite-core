/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright 2, 2015nership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package eu.project.ttc.resources;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Scanner;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.uima.resource.DataResource;
import org.apache.uima.resource.ResourceInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Objects;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

public class GeneralLanguageResource implements GeneralLanguage {
	private static final Logger LOGGER = LoggerFactory.getLogger(GeneralLanguageResource.class);

	/**
	 * The number of WordAnnotation in the corpus that has been used 
	 * to produced this general language resource.
	 * 
	 * This field is used as the reference for frequency normalization.
	 */
	private int nbCorpusWords = -1;
	
	private boolean cumulatedFrequencyMode = false;
	private int cumulatedFrequency;
	
	private Multimap<String, Entry> frequencies;
	private Set<String> words;

	public class Entry {
		private String lemma;
		private String pattern;
		private int frequency;
		public Entry(String lemma, String pattern, int frequency) {
			super();
			this.lemma = lemma;
			this.pattern = pattern;
			this.frequency = frequency;
		}
		public String getLemma() {
			return lemma;
		}
		public String getPattern() {
			return pattern;
		}
		public int getFrequency() {
			return frequency;
		}
		
		@Override
		public int hashCode() {
			return Objects.hashCode(this.lemma, this.pattern);
		}
		
		@Override
		public boolean equals(Object obj) {
			if (obj instanceof Entry) 
				return Objects.equal(((Entry)obj).lemma, ((Entry)obj).pattern);
			else
				return false;
		}
	}
	
	public GeneralLanguageResource() {
		this.cumulatedFrequency = 0;
		this.frequencies = HashMultimap.create();
	}
	
    @Override
	public void load(DataResource data) throws ResourceInitializationException {
		try {
			load(data.getInputStream());
		} catch (IOException e) {
			throw new ResourceInitializationException(e);
		}
	}

	public void load(InputStream inputStream)
			throws ResourceInitializationException {
		words = Sets.newHashSet();
		Scanner scanner = null;
		try {
			scanner = new Scanner(inputStream, "UTF-8");
			scanner.useDelimiter("\n");
			int index = 0;
			while (scanner.hasNext()) {
				index++;
				String line = scanner.next();
				String[] items = line.split("::");
				if (items.length == 3) {
					String key = items[0].trim();
					if(!key.contains(" "))
						this.words.add(key);
					Integer value = Integer.valueOf(items[2].trim());
					this.cumulatedFrequency += value.intValue();
					String lemma = key;
					this.frequencies.put(lemma, new Entry(lemma, items[1], new Integer(value.intValue())));
				} else {
					throw new IOException("Wrong general language format at line " + index + ": " + line);
				}
			}
			this.words = ImmutableSet.copyOf(this.words);
			
			if(this.frequencies.containsKey(PARAM_NB_CORPUS_WORDS))
				this.nbCorpusWords = this.frequencies.get(PARAM_NB_CORPUS_WORDS).iterator().next().getFrequency();
			else {
				LOGGER.warn("No such key for in GeneralLanguage resource {}",
						PARAM_NB_CORPUS_WORDS);
				LOGGER.warn("Switch to cumulatedFrequency mode");
				this.cumulatedFrequencyMode = true;
			}
		} catch (Exception e) {
			throw new ResourceInitializationException(e);
		} finally {
			IOUtils.closeQuietly(scanner);
		}
	}

	@Override
	public int getFrequency(String lemma, String pattern) {
		Entry e = getEntry(lemma, pattern);
		return e == null ? 1 : e.getFrequency();
	}
	
	public Entry getEntry(String lemma, String pattern) {
		for(Entry e:this.frequencies.get(lemma.toLowerCase()))
			if(e.getPattern().equals(pattern))
				return e;
		return null;
	}
	
	@Override
	public int getNbCorpusWords() {
		return nbCorpusWords;
	}

	@Override
	@Deprecated
	public double getNormalizedFrequency(String entry) {
		if(cumulatedFrequencyMode) {
			Collection<Entry> l = this.frequencies.get(entry.toLowerCase());
			if (l.isEmpty()) {
				return 1.0 / this.cumulatedFrequency;
			} else {
				double freq = new Double(l.iterator().next().getFrequency()).doubleValue();
				return freq / this.cumulatedFrequency;
			}
		} else 
			throw new IllegalStateException("Not in cumulatedFrequencyMode. Should call #getFrequency() instead");
	}

	
	@Override
	public boolean isCumulatedFrequencyMode() {
		return cumulatedFrequencyMode;
	}

	@Override
	public boolean findSingleWord(String word) {
		return this.words.contains(word);
	}
	
	@Override
	public Set<String> getWords() {
		return words;
	}
}
