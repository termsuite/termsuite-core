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
package fr.univnantes.termsuite.model.termino;

import com.google.common.base.Objects;
import com.google.common.collect.ComparisonChain;

import fr.univnantes.termsuite.model.Term;

/**
 * 
 * A term pair (order of term1 and term2 does not matter)
 * 
 * @author Damien Cram
 *
 */
public class TermPair implements Comparable<TermPair> {
	
	private Term term1;
	private Term term2;
	public TermPair(Term term1, Term term2) {
		super();
		if(term1.compareTo(term2) < 0) {
			this.term1 = term1;
			this.term2 = term2;
		} else {
			this.term1 = term2;
			this.term2 = term1;
		}
	}
	
	public Term getTerm1() {
		return term1;
	}
	public Term getTerm2() {
		return term2;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof TermPair) {
			TermPair o = (TermPair) obj;
			return Objects.equal(term1, o.term1)
					&& Objects.equal(term2, o.term2);
		} else
			return false;
	}
	
	
	@Override
	public int hashCode() {
		return Objects.hashCode(term1, term2);
	}
	@Override
	public int compareTo(TermPair o) {
		return ComparisonChain.start()
				.compare(term1, o.term1)
				.compare(term2, o.term2)
				.result();
	}
	
	@Override
	public String toString() {
		return String.format("{%s,%s}", term1, term2);
	}
}
