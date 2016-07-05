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
package eu.project.ttc.models;

import java.util.Iterator;
import java.util.List;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;

/**
 * A data model class that represents a document in a corpus.
 * 
 * 
 * @author Damien Cram
 *
 */
public class Document {
	
	
	/**
	 * The locator of the document
	 */
	private String url;
	
	private int id;
	
	/**
	 * The ordered LinkedList of occurrences.
	 */
	private List<TermOccurrence> _occurrences = Lists.newArrayList();

	/**
	 * A flag that tells of occurrences must be sorted again
	 */
	private boolean occurrencesDirty = true;

	public Document(int id, String url) {
		super();
		this.id = id;
		this.url = url;
	}
	
	public String getUrl() {
		return url;
	}

	/**
	 * 
	 * @param termOccurrence
	 * @return
	 */
	public int indexTermOccurrence(TermOccurrence termOccurrence) {
		this.occurrencesDirty = true;
		_occurrences.add(termOccurrence);
		return _occurrences.size();
	}
	
	public List<TermOccurrence> getOccurrences() {
		computeOccurrences();
		return this._occurrences;
	}

	private void computeOccurrences() {
		if(this.occurrencesDirty) {
			java.util.Collections.sort(this._occurrences);
			int index = 0;
			for(TermOccurrence o:this._occurrences) {
				o.setOccurrenceIndex(index);
				index++;
			}
			this.occurrencesDirty = false;
		}
	}
	
	/**
	 * 
	 * Produce an iterator over all single-word term occurrences in the scope 
	 * of a given occurrence.
	 * 
	 * @see OccurrenceType
	 * @param occurrence
	 * 			the occurrence whose context iterator will be computed by the method
	 * @param coTermsType
	 * 			the type of occurrence to put in context
	 * @param contextSize
	 * 			the radius (i.e. half the maximum number of wingle-word terms) of the context window size
	 * @return
	 * 			an iterator over the occurrences
	 */
	public Iterator<TermOccurrence> getOccurrenceContext(final TermOccurrence occurrence, final OccurrenceType coTermsType, final int contextSize) {
		computeOccurrences();
		return Iterators.concat(
				new LeftContextIterator(coTermsType, occurrence, contextSize),
				new RightContextIterator(coTermsType, occurrence, contextSize)
			);
	}
	
	private abstract class DirectionalContextIterator extends AbstractIterator<TermOccurrence> {
		protected TermOccurrence occurrence;
		private int radius;
		private OccurrenceType occType;

		protected int index;
		private int returnedOccCnt = 0;
		private TermOccurrence current;
		
		private DirectionalContextIterator(OccurrenceType occType, TermOccurrence occurrence, int radius) {
			super();
			this.occurrence = occurrence;
			this.radius = radius;
			this.index = this.occurrence.getOccurrenceIndex();
			this.occType = occType;
			moveCursor();
		}

		@Override
		protected TermOccurrence computeNext() {
			while (returnedOccCnt < radius && index >=0 && index < Document.this.getOccurrences().size()) {
				this.current = Document.this.getOccurrences().get(this.index);
				
				if(keepOccurrence(this.current)) {
					this.returnedOccCnt++;
					moveCursor();
					return this.current;
				} else 
					moveCursor();

			}
			return endOfData();
		}

		private boolean keepOccurrence(TermOccurrence o) {
			if(overlap(o))
				return false;
			switch (occType) {
			case PRIMARY:
				return o.isPrimaryOccurrence();
			case ALL:
				return true;
			case SINGLE_WORD:
				return o.getTerm().isSingleWord();
			default:
				throw new IllegalStateException();
			}
		}

		protected abstract boolean overlap(TermOccurrence o);
		protected abstract void moveCursor();
	};
	
	/*
	 * Iterates from a given occurrence over its closest left-side 
	 * single-word neighbour term occurrences in the document.
	 */
	private class LeftContextIterator extends DirectionalContextIterator {
		private LeftContextIterator(OccurrenceType occType, TermOccurrence occurrence, int radius) {
			super(occType, occurrence, radius);
		}
		@Override
		protected void moveCursor() {
			this.index--;
		}
		@Override
		protected boolean overlap(TermOccurrence o) {
			return o.getEnd() > this.occurrence.getBegin();
		}
	}

	/*
	 * Iterates from a given occurrence over its closest right-side 
	 * single-word neighbour term occurrences in the document.
	 */
	private class RightContextIterator extends DirectionalContextIterator {
		private RightContextIterator(OccurrenceType occType, TermOccurrence occurrence, int radius) {
			super(occType, occurrence, radius);
		}
		@Override
		protected void moveCursor() {
			this.index++;
		}
		@Override
		protected boolean overlap(TermOccurrence o) {
			return o.getBegin() < this.occurrence.getEnd();
		}
	}
	
	@Override
	public int hashCode() {
		return url.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Document)
			return ((Document) obj).url.equals(this.url);
		else 
			return false;
	}

	/**
	 * Nullifies the inner occurrence list so as to frees memory.
	 */
	public void clearOccurrenceIndex() {
		this._occurrences = Lists.newArrayList();
		this.occurrencesDirty = true;
	}
	
	@Override
	public String toString() {
		return String.format("%s", url);
	}

	public void inspect(String string) {
		System.out.format("%s%s\t_occurrences: %s\n", string, url,  _occurrences.size());
	}

	public Integer getId() {
		return id;
	}
}
