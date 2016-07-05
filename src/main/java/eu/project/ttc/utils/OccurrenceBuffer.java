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

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Lists;

import fr.univnantes.lina.uima.tkregex.RegexOccurrence;

/**
 * A utility class that helps storing tk occurrence in a buffer
 * and clean them according to a given strategy.
 * 
 * @author Damien Cram
 *
 */
public class OccurrenceBuffer implements Iterable<RegexOccurrence> {
	public static final String NO_CLEANING = "nocleaning";
	public static final String KEEP_SUFFIXES = "rem-pref";
	public static final String KEEP_PREFIXES = "rem-suff";
	private static final String REMOVE_INCLUDED = "remove-included";
	
	private LinkedList<RegexOccurrence> occurrences = Lists.newLinkedList();
	private String cleaningStrategy;
	
	public OccurrenceBuffer(String cleaningStrategy) {
		super();
		this.cleaningStrategy = cleaningStrategy;
	}

	public void bufferize(RegexOccurrence occ) {
		this.occurrences.addLast(occ);
	}

	public ListIterator<RegexOccurrence> listIterator() {
		return occurrences.listIterator();
	}

	public boolean isEmpty() {
		return occurrences.isEmpty();
	}
	
	public void clear() {
		this.occurrences.clear();
	}

	public void cleanBuffer() {
		if(this.occurrences.isEmpty())
			return;
		RegexOccurrence current;
		for(ListIterator<RegexOccurrence> it = this.occurrences.listIterator(); it.hasNext();) {
			current = it.next();
			for(RegexOccurrence other:occurrences) {
				if(current != other) {
					if(shouldRemove(current, other)) {
						it.remove();
						break;
					}
				}
			}
		}
		
	}
	
	private boolean shouldRemove(RegexOccurrence current, RegexOccurrence other) {
		switch (this.cleaningStrategy) {
		case KEEP_SUFFIXES:
			return current.getBegin() >= other.getBegin() && current.getEnd() < other.getEnd();
		case KEEP_PREFIXES:
			return current.getBegin() > other.getBegin() && current.getEnd() <= other.getEnd();
		case REMOVE_INCLUDED:
			return current.getBegin() >= other.getBegin() && current.getEnd() <= other.getEnd();
		case NO_CLEANING:
			return false;
		default:
			throw new IllegalStateException("Unkown strategy: " + this.cleaningStrategy);
		}
	}

	@Override
	public Iterator<RegexOccurrence> iterator() {
		return this.occurrences.iterator();
	}

	
	/**
	 * Finds duplicated occurrences in this buffer and returns them
	 * as a collection of dups lists.
	 * @return
	 */
	public Collection<Collection<RegexOccurrence>> findDuplicates() {
		Collection<Collection<RegexOccurrence>> setToReturn = Lists.newArrayList();
		if(! this.occurrences.isEmpty()) {
			List<RegexOccurrence> sortedOcc = Lists.newArrayList(this.occurrences);
			Collections.sort(sortedOcc, new Comparator<RegexOccurrence>() {
				@Override
				public int compare(RegexOccurrence o1, RegexOccurrence o2) {
					return ComparisonChain.start()
							.compare(o1.getBegin(), o2.getBegin())
							.compare(o1.getEnd(), o2.getEnd())
							.result();
				}
			});
			ListIterator<RegexOccurrence> it = sortedOcc.listIterator();
			
			RegexOccurrence current;
			List<RegexOccurrence> doublons = Lists.newArrayListWithCapacity(3);
			doublons.add(it.next());
			while(it.hasNext()) {
				current = it.next();
				if(current.getBegin() == doublons.get(0).getBegin() && current.getEnd() == doublons.get(0).getEnd()) {
					doublons.add(current);
				} else {
					if(doublons.size()>1)
						setToReturn.add(doublons);
					doublons = Lists.newArrayListWithCapacity(3);
					doublons.add(current);
				}
			}
			if(doublons.size()>1)
				setToReturn.add(doublons);
		}
		return setToReturn;
	}
}
