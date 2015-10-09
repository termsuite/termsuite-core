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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.uima.resource.DataResource;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.SharedResourceObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.project.ttc.types.WordAnnotation;
import fr.univnantes.lina.uima.tkregex.LabelledAnnotation;
import fr.univnantes.lina.uima.tkregex.RegexOccurrence;


public class CharacterFootprintTermFilter  implements SharedResourceObject, OccurrenceFilter {
	private static final Logger LOGGER = LoggerFactory.getLogger(CharacterFootprintTermFilter.class);

	private static final int BAD_CHAR_RATE_THRESHOLD = 41;
	private char[] allowedChars;
	
	@Override
	public boolean accept(RegexOccurrence occurrence) {
		if(allowedChars == null)
			return true;
		int totalChars = 0;
		int totalWords = 0;
		int nbBadWords = 0;
		MutableInt badChars = new MutableInt(0);
		for(LabelledAnnotation a:occurrence.getLabelledAnnotations()) {
			WordAnnotation w = (WordAnnotation) a.getAnnotation();
			totalChars += w.getCoveredText().length();
			totalWords += 1;
			if(isBadWord(w, badChars))
				nbBadWords +=1;
		}
		if(nbBadWords > 1)
			return false;
		if(totalChars <= totalWords*3 && totalWords > 1)
			return false;
		int badCharRate = 100*badChars.intValue()/totalChars;
		if(badCharRate >= BAD_CHAR_RATE_THRESHOLD)
			return false;
		return true;
	}
	
	/**
	 * 
	 * @param anno the word anno
	 * @param badChars the bad char counter. Being incremented
	 * @return true if the word has one bad char, false otherwise
	 */
	private boolean isBadWord(WordAnnotation anno, MutableInt badChars) {
		final String coveredText = anno.getCoveredText();
		boolean foundOneBadChar = false;
		for(int i=0; i< coveredText.length(); i++) {
			boolean found = false;
			char c = coveredText.charAt(i);
			for(char a:this.allowedChars) {
				if(a==c) 
					found = true;
			}
			if(!found) {
				badChars.increment();
				foundOneBadChar = true;
			}
		}
		return foundOneBadChar;
	}

	@Override
	public void load(DataResource aData) throws ResourceInitializationException {
		LOGGER.debug("Loading resource character footprint resource at: " + aData.getUri());
		InputStream inputStream = null;
		try {
			inputStream = aData.getInputStream();
			List<Character> chars = new LinkedList<Character>();
			BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, Charset.forName("UTF-8")));
			int c;
			while((c = br.read()) != -1) {
				if(!Character.isWhitespace((char)c))
						chars.add((char)c);
			}
			this.allowedChars = new char[chars.size()];
			for(int i=0; i< chars.size(); i++) 
				this.allowedChars[i] = chars.get(i);
			br.close();
		} catch (IOException e) {
			LOGGER.error("Could not load resource character footprint resource due to an exception.");
			LOGGER.warn("Continuing with the TrueFilter (always accept terms)");
			this.allowedChars = null;
		} catch(Exception e) {
			LOGGER.warn("PB loading "+aData.getUri()+". Continuing with the TrueFilter (always accept terms)");
			this.allowedChars = null;
			return;
		} finally {
			if(inputStream != null)
				try {
					inputStream.close();
				} catch (IOException e) {
					LOGGER.error("Could not close input stream.");
				}
		}
	}
}
