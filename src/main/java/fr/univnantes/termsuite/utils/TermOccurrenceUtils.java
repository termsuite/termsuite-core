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
package fr.univnantes.termsuite.utils;

import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Lists;
import com.google.common.io.CharStreams;

import fr.univnantes.termsuite.model.TermOccurrence;

/**
 * A utililty class for {@link TermOccurrence} objects and collections.
 * 
 * @author Damien Cram
 *
 */
public class TermOccurrenceUtils {
	

	public static Comparator<TermOccurrence> uimaNaturalOrder = new Comparator<TermOccurrence>() {
		@Override
		public int compare(TermOccurrence o1, TermOccurrence o2) {
			return ComparisonChain.start()
					.compare(o1.getSourceDocument().getUrl(), o2.getSourceDocument().getUrl())
					.compare(o1.getBegin(), o2.getBegin())
					.compare(o2.getEnd(), o1.getEnd())
					.result();
		}
	};

	/**
	 * Returns a virtual iterator on chunks of an occurrence collection.
	 * 
	 * A occurrence collection's chunk is a list of overlapping {@link TermOccurrence}. Every time
	 * there is a gap between two occurrences (i.e. there do not overlap),
	 * a new chunk is created.
	 * 
	 * @param occurrences
	 * @return
	 */
	public static Iterator<List<TermOccurrence>> occurrenceChunkIterator(Collection<TermOccurrence> occurrences) {
		List<TermOccurrence> asList = Lists.newArrayList(occurrences);
		Collections.sort(asList, TermOccurrenceUtils.uimaNaturalOrder);
		final Iterator<TermOccurrence> it = asList.iterator();
		return new AbstractIterator<List<TermOccurrence>>() {
			private List<TermOccurrence> currentChunk = Lists.newArrayList();
			
			@Override
			protected List<TermOccurrence> computeNext() {
				while(it.hasNext()) {
					TermOccurrence next = it.next();
					if(currentChunk.isEmpty() || hasOverlappingOffsets(next, currentChunk))
						currentChunk.add(next);
					else {
						List<TermOccurrence> ret = copyAndReinit();
						currentChunk.add(next);
						return ret;	
					}
				} 
				if(!currentChunk.isEmpty()) {
					return copyAndReinit();
				} else 
					return endOfData();
			}

			private List<TermOccurrence> copyAndReinit() {
				List<TermOccurrence> copy = Lists.newArrayList(currentChunk);
				currentChunk = Lists.newArrayList();
				return copy;
			}
		};
	}

	
	/**
	 * Removes from an occurrence set all occurrences that overlap
	 * at least one occurrence in a reference occurrence set.
	 * 
	 * @param referenceSet
	 * 			the reference set, not modified by this method
	 * @param occurrenceSet
	 * 			the occurrence set to analyze, will be modified by this method
	 */
	public static void removeOverlaps(Collection<TermOccurrence> referenceSet, Collection<TermOccurrence> occurrenceSet) {
		Iterator<TermOccurrence> it = occurrenceSet.iterator();
		while(it.hasNext()) {
			TermOccurrence occ = it.next();
			for(TermOccurrence refOcc:referenceSet) {
				if(occ.getSourceDocument().equals(refOcc.getSourceDocument())
						&& areOffsetsOverlapping(occ, refOcc)) {
					it.remove();
					break;
				}
			}
		}
	}

		
	/**
	 * True if an occurrence set contains any element overlapping 
	 * with the param occurrence.
	 * 
	 * @param theOcc
	 * @param theOccCollection
	 * @return
	 */
	public static boolean hasOverlappingOffsets(TermOccurrence theOcc, Collection<TermOccurrence> theOccCollection) {
		for(TermOccurrence o:theOccCollection)
			if(areOffsetsOverlapping(theOcc, o))
				return true;
		return false;
	}
	
	/**
	 * True if two {@link TermOccurrence} offsets overlap strictly. Sharing exactly
	 * one offset (e.g. <code>a.end == b.begin</code>) is not considered as overlap.
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public static boolean areOffsetsOverlapping(TermOccurrence a, TermOccurrence b) {
		if(a.getBegin() <= b.getBegin()) 
			return !(a.getBegin() <= b.getEnd() && a.getEnd() <= b.getBegin());
		else
			return !(b.getBegin() <= a.getEnd() && b.getEnd() <= a.getBegin());
			
	}
	
	/**
	 * Returns true if two occurrences are in the same 
	 * document and their offsets overlap.
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public static boolean areOverlapping(TermOccurrence a, TermOccurrence b) {
		return a.getSourceDocument().equals(b.getSourceDocument()) && areOffsetsOverlapping(a, b); 
	}

	/**
	 * True if both source documents are the same and if the 
	 * offsets in the document overlaps.
	 * 
	 * The overlap is interpreted in the sense of opening intervals. I.e
	 * if the begin of the second interval is the end of the first interval, this
	 * is not an overlap.
	 * 
	 * @param o1
	 * @param o2
	 * @return
	 */
	public static boolean overlaps(TermOccurrence o1, TermOccurrence o2) {
		return o1.getSourceDocument().equals(o2.getSourceDocument()) 
				  && o1.getBegin() < o2.getEnd()
				  && o2.getBegin()< o1.getEnd();
	}

	
	
	/**
	 * 
	 * @param o
	 * @param contextSize
	 * 			The number of characters before and after the occurrence.
	 * @return
	 * @throws IOException 
	 */
	public static String getTextualContext(TermOccurrence o, int contextSize) throws IOException {
		FileReader r = new FileReader(o.getSourceDocument().getUrl().replaceFirst("file:", ""));
		String text = CharStreams.toString(r);
		r.close();
		int begin = Math.max(o.getBegin() - contextSize, 0);
		int end = Math.min(o.getEnd() + contextSize, text.length());
		return TermUtils.collapseText(text.substring(begin, end));
	}
	
}
