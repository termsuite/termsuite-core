
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

package eu.project.ttc.io;

import java.util.List;
import java.util.regex.Pattern;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

import eu.project.ttc.engines.morpho.Segment;
import eu.project.ttc.engines.morpho.Segmentation;
import fr.univnantes.julestar.uima.resources.ResourceFormatException;

public class SegmentationParser {
	
	private static final Pattern EMPTY_SEGMENTATION = Pattern.compile("\\s*\\[\\s*\\]\\s*");
	
	public Segmentation parse(String string) {
		if(EMPTY_SEGMENTATION.matcher(string).matches())
			return new Segmentation(string);
		else {
			
			boolean inSegment = false;
			int patternIndex = 0;
			StringBuffer targetString = new StringBuffer();
			StringBuffer currentSequence = new StringBuffer();
			List<Segment> segments = Lists.newArrayList();
			for(char c:string.toCharArray()) {
				if(c=='[') {
					if(inSegment)
						fail("Illegal character \"[\" at index %d", patternIndex);
					else {
						currentSequence = new StringBuffer();						
						inSegment = true;
					}
				} else if(c==']') {
					if(inSegment) {
						String substring = currentSequence.toString().trim();
						if(substring.isEmpty())
							fail("Empty segment not allowed");
						Segment segment = toSegment(targetString.length(), substring);
						segments.add(segment);
						inSegment = false;
						targetString.append(segment.getSubstring());
						currentSequence = new StringBuffer();												
					} else 
						fail("Illegal character \"]\" at index %d", patternIndex);
				} else {
					if(!inSegment) {
						targetString.append(c);
					}
					currentSequence.append(c);
				}
				patternIndex++;
			}
			if(inSegment)
				fail("Expected \"]\" at end of string");
			
			Segmentation segmentation = new Segmentation(
					targetString.toString().trim(), 
					segments.toArray(new Segment[segments.size()]));
			return segmentation;
		}
	}

	private static final String SEMI_COL = ":";
	private Segment toSegment(int begin, String string) {
		String trimmed = string.trim();
		if(trimmed.startsWith(SEMI_COL))
			fail("Cannot start segment with \":\"");
		else if(trimmed.endsWith(SEMI_COL))
			fail("Cannot end segment with \":\"");
		List<String> ar = Splitter.on(':').splitToList(trimmed);
		if(ar.size() == 0)
			fail("Empty segments not allowed");
		else if(ar.size() > 3)
			fail("Only one \":\" allowed in segment");
		else {
			String substring = ar.get(0).trim();
			Segment s = new Segment(begin, begin + substring.length());
			s.setSubstring(substring);
			if(ar.size() == 2)
				s.setLemma(ar.get(1).trim());
			else
				s.setLemma(substring);
			return s;
		}
		throw new IllegalStateException();
	}

	private void fail(String string, Object... args) {
		throw new ResourceFormatException(String.format(string, args));
	}

}
