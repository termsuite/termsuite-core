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

import com.google.common.base.Objects;
import com.google.common.collect.ComparisonChain;

public class TermOccurrence implements Comparable<TermOccurrence> {
	
	private Term term;
	private Form form;
	private Document sourceDocument;
	private int begin;
	private int end;

	public TermOccurrence(Term term, Form form, Document sourceFile,
			int begin, int end) {
		super();
		this.term = term;
		this.form = form;
		this.sourceDocument = sourceFile;
		this.begin = begin;
		this.end = end;
	}
	public Term getTerm() {
		return term;
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
	

	@Override
	public String toString() {
		return getForm().getText();
	}
	
	public Form getForm() {
		return form;
	}
	
	public void setTerm(Term term2) {
		this.term = term2;
	}
	

}
