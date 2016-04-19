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
package eu.project.ttc.models;

import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;

import com.google.common.base.Objects;
import com.google.common.collect.ComparisonChain;
import com.google.common.io.CharStreams;

import eu.project.ttc.utils.TermUtils;

public class TermOccurrence implements Comparable<TermOccurrence> {
	
	private Term term;
	private String coveredText;
	private Document sourceDocument;
	private int begin;
	private int end;

	/**
	 * the status of the occurrence
	 */
	private boolean primaryOccurrence;

	/**
	 * The index in the list of occurrences in sourceDocument.
	 */
	private int occurrenceIndex;
	
	public TermOccurrence(Term term, String coveredText, Document sourceFile,
			int begin, int end) {
		super();
		this.term = term;
		this.coveredText = coveredText;
		this.sourceDocument = sourceFile;
		this.begin = begin;
		this.end = end;
	}
	public Term getTerm() {
		return term;
	}
	
	public String getCoveredText() {
		return coveredText;
	}
	public Document getSourceDocument() {
		return sourceDocument;
	}
	public int getBegin() {
		return begin;
	}
	public int getEnd() {
		return end;
	}
	
	@Override
	public int hashCode() {
		return Objects.hashCode(sourceDocument, begin, end);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof TermOccurrence) {
			TermOccurrence o = (TermOccurrence) obj;
			return Objects.equal(sourceDocument, o.sourceDocument) 
					&& Objects.equal(begin, o.begin)
					&& Objects.equal(end, o.end);
		} else
			return false;
	}
	
	@Override
	public int compareTo(TermOccurrence o) {
		return ComparisonChain.start()
				.compare(sourceDocument.getUrl(), o.sourceDocument.getUrl())
				.compare(begin, o.begin)
				.compare(o.end, end)
				.result();
	}
	
	public String getForm() {
		return TermUtils.collapseText(coveredText.toLowerCase());
	}
	
	/**
	 * 
	 * @param contextSize
	 * 			The number of characters before and after the occurrence.
	 * @return
	 * @throws IOException 
	 */
	public String getTextualContext(int contextSize) throws IOException {
		FileReader r = new FileReader(sourceDocument.getUrl().replaceFirst("file:", ""));
		String text = CharStreams.toString(r);
		r.close();
		int begin = Math.max(this.begin - contextSize, 0);
		int end = Math.min(this.end + contextSize, text.length());
		return TermUtils.collapseText(text.substring(begin, end));
	}
	
	public Iterator<TermOccurrence> contextIterator(OccurrenceType coTermsType, int contextSize) {
		return this.sourceDocument.getOccurrenceContext(this, coTermsType, contextSize);
	}

	@Override
	public String toString() {
		return getForm();
	}
	
	public void setTerm(Term term2) {
		this.term = term2;
	}
	
	int getOccurrenceIndex() {
		return occurrenceIndex;
	}
	
	void setOccurrenceIndex(int occurrenceIndex) {
		this.occurrenceIndex = occurrenceIndex;
	}
	
	
	/**
	 * True if this occurrence is marked as primary, i.e. if
	 * it can be considered as a true occurrence of term in a 
	 * non-overlapping sequence of term occurrences
	 * 
	 * 
	 * @return
	 */
	public boolean isPrimaryOccurrence() {
		return primaryOccurrence;
	}

	public void setPrimaryOccurrence(boolean primaryOccurrence) {
		this.primaryOccurrence = primaryOccurrence;
	}
	
	/**
	 * True if both source documents are the same and if the 
	 * offsets in the document overlaps.
	 * 
	 * The overlap is interpreted in the sense of opening intervals. I.e
	 * if the begin of the second interval is the end of the first interval, this
	 * is not an overlap.
	 * 
	 * @param theOcc
	 * @return
	 */
	public boolean overlaps(TermOccurrence theOcc) {
		return this.sourceDocument.equals(theOcc.sourceDocument) 
				  && this.begin < theOcc.end 
				  && theOcc.begin < this.end;
	}
}
