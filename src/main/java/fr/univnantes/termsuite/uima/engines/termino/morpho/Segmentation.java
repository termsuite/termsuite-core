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
package fr.univnantes.termsuite.uima.engines.termino.morpho;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import fr.univnantes.termsuite.utils.TermSuiteConstants;

public class Segmentation {
	
	private String string;
	private List<CuttingPoint> cuttingPoints;
	private List<Segment> segments;
	
	public Segmentation(String word, Segment... segments) {
		this.string = word;
		this.segments = Lists.newArrayList(segments);
	}

	private Segmentation(String word, List<CuttingPoint> cuttingPoints) {
		super();
		this.string = word;
		this.cuttingPoints = cuttingPoints;
		this.segments = Lists.newArrayListWithCapacity(cuttingPoints.size()+1);
		int lastBegin = 0;
		for(CuttingPoint cp:this.cuttingPoints) {
			this.segments.add(Segment.createFromParentString(lastBegin, cp.getIndex(), this.string));
			lastBegin = cp.getIndex() + cp.getOffset();
		}
		this.segments.add(Segment.createFromParentString(lastBegin, this.string.length(), this.string));
	}

	public List<Segment> getSegments() {
		return segments;
	}
	
	@Override
	public String toString() {
		List<String> substrings = Lists.newArrayList();
		for(Segment s:getSegments()) 
			substrings.add(s.getSubstring());
		return Joiner.on('+').join(substrings);
	}
	
	public int size() {
		return this.segments.size();
	}

	public static List<Segmentation> getSegmentations(String str, int nbMaxComponents, int minComponentSize) {
		return getSegmentations(str, 0, str.length(), nbMaxComponents, minComponentSize, new ArrayList<CuttingPoint>(minComponentSize));
	}

	
	/*
	 * Recursively find the segmentation
	 */
	private static List<Segmentation> getSegmentations(String str, int begin, int end, int nbMaxComponents, int minComponentSize, List<CuttingPoint> cuttingPoints) {
		List<Segmentation> s = new ArrayList<Segmentation>(nbMaxComponents);
		for(CuttingPoint cp:getPossibleCuttingPoints(str, begin, end, nbMaxComponents, minComponentSize)) {
			s.add(new Segmentation(str, addToDuplicate(cuttingPoints, cp, nbMaxComponents - 1)));
			if(nbMaxComponents > 2) {
				if(cp.isHypen())
					// recursivity on the left part only if the cp was an hypen
					if((cp.getIndex() - begin) >= 2*minComponentSize)
						s.addAll(getSegmentations(str, begin, cp.getIndex(), nbMaxComponents -1, minComponentSize, addToDuplicate(cuttingPoints, cp, nbMaxComponents - 1)));
				
				if((end - (cp.getIndex() + cp.getOffset())) >= 2*minComponentSize)
					s.addAll(getSegmentations(str, cp.getIndex() + cp.getOffset(), end, nbMaxComponents -1, minComponentSize, addToDuplicate(cuttingPoints, cp, nbMaxComponents - 1)));
			}
		}
		return s;
	}
	
	private static List<CuttingPoint> addToDuplicate(List<CuttingPoint> baseList, CuttingPoint toAdd, int capacity) {
		List<CuttingPoint> dup = new ArrayList<CuttingPoint>(capacity);
		dup.addAll(baseList);
		for(int i=0; i< dup.size(); i++) {
			if(toAdd.compareTo(dup.get(i)) <= 0) {
				dup.add(i, toAdd);
				return dup;
			}
		}
		dup.add(toAdd);
		return dup;
	}
	
	private static List<CuttingPoint> getPossibleCuttingPoints(String str, int begin, int end, int nbMaxComponents, int minComponentSize) {
		List<CuttingPoint> l = Lists.newArrayList();
		if(nbMaxComponents <= 1)
			return l;
		else {
			String substring = str.substring(begin, end);
			int hyphenIndex = substring.indexOf(TermSuiteConstants.HYPHEN);
			if(hyphenIndex != -1) {
				Preconditions.checkPositionIndex(hyphenIndex+begin, str.length());
				Preconditions.checkPositionIndex(hyphenIndex+begin+1, str.length());
				l.add(new CuttingPoint(hyphenIndex+begin, 1, true));
			} else {
				for(int i=minComponentSize; i<=substring.length()-minComponentSize; i++) {
					Preconditions.checkPositionIndex(begin+i, str.length());
					l.add(new CuttingPoint(begin+i, 0, false));
				}
			}
			return l;
		}
	}
	
	public String getString() {
		return string;
	}
}
