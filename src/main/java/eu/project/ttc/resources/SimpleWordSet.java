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
package eu.project.ttc.resources;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Scanner;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.uima.resource.DataResource;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.SharedResourceObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import eu.project.ttc.utils.TermSuiteConstants;

/**
 * 
 * A simple list of words (one per line), but also provides
 * a dictionary API if ","-separated strings are associated (translations)
 * with the word after a tab.
 * 
 * For example, allowed lines cane be:
 * 
 * word1 # a simple entry, with no word associated
 * word2 # another word with comment after "#" char
 * the expression1 # still considered as one simple entry since not tab-separation
 * word3	word4 # word4 is considered the associated word for entry word3
 * word6	word7, word8 # word7 and word8 are considered as associated words for entry word6
 * 
 * @author Damien Cram
 *
 */
public class SimpleWordSet implements SharedResourceObject {
	private static final Logger LOGGER = LoggerFactory.getLogger(SimpleWordSet.class);

	private Set<String> elements;
	
	private Multimap<String, String> translations = HashMultimap.create();
	
	public void load(DataResource data) throws ResourceInitializationException {
		InputStream inputStream = null;
		this.elements = Sets.newHashSet();
		try {
			inputStream = data.getInputStream();
			Scanner scanner = null;
			try {
				String word, line;
				String[] str, wordTranslations;
				scanner = new Scanner(inputStream);
				scanner.useDelimiter(TermSuiteConstants.LINE_BREAK);
				while (scanner.hasNext()) {
					line = scanner.next().split(TermSuiteConstants.DIESE)[0].trim();
					str = line.split(TermSuiteConstants.TAB);
					word = str[0];
					if(str.length > 0)
						this.elements.add(word);
					if(str.length > 1) {
						// Set the second line as the translation
						wordTranslations = str[1].split(TermSuiteConstants.COMMA);
						for(String translation:wordTranslations) {
							translations.put(word, translation);
						}
					}
						
				}
				this.elements = ImmutableSet.copyOf(this.elements);
			} catch (Exception e) {
				e.printStackTrace();
				throw new ResourceInitializationException(e);
			} finally {
				IOUtils.closeQuietly(scanner);
			}
		} catch (IOException e) {
			LOGGER.error("Could not load file {}",data.getUrl());
			throw new ResourceInitializationException(e);
		} finally {
			IOUtils.closeQuietly(inputStream);
		}
	}

	public boolean contains(Object o) {
		return elements.contains(o);
	}

	public boolean containsAll(Collection<?> c) {
		return elements.containsAll(c);
	}

	public Collection<String> getTranslations(String wordEntry) {
		return translations.get(wordEntry);
	}

	public Set<String> getElements() {
		return this.elements;
	};
	
}
