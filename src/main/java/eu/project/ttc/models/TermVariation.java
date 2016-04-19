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

import com.google.common.base.Objects;
import com.google.common.collect.ComparisonChain;

import eu.project.ttc.utils.TermUtils;

public class TermVariation implements Comparable<TermVariation> {
	private VariationType variationType;
	private Term base;
	private Term variant;
	private Object info;
	private String _label;
	private double score;
	
	private boolean includedIn;
	private boolean prefixOf;
	private boolean suffixOf;
	
	public TermVariation(VariationType variationType, Term base, Term variant, Object info) {
		super();
		this.variationType = variationType;
		this.base = base;
		this.variant = variant;
		this.info = info;
		this.includedIn = TermUtils.isIncludedIn(base, variant);
		this.prefixOf = TermUtils.isPrefixOf(base, variant);
		this.suffixOf = TermUtils.isSuffixOf(base, variant);
	}
	
	public VariationType getVariationType() {
		return variationType;
	}
	
	public Object getInfo() {
		return info;
	}
	
	public Term getVariant() {
		return variant;
	}
	
	public Term getBase() {
		return base;
	}
	
	@Override
	public String toString() {
		return String.format("%s --- %s --> %s", base.getGroupingKey(), this.info, variant.getGroupingKey());
	}
	
	@Override
	public int hashCode() {
		return Objects.hashCode(this.base, this.variant, this.variationType, this.info);
	}
	
	public String getLabel() {
		if(this._label == null) 
			this._label = this.variationType.getShortName() + ":" + this.info; 
		return this._label;
	}
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof TermVariation) {
			TermVariation v = (TermVariation) obj;
			return Objects.equal(this.score, v.score)
					&& Objects.equal(this.base, v.base)
					&& Objects.equal(this.variant, v.variant)
					&& Objects.equal(this.variationType, v.variationType)
					&& Objects.equal(this.info, v.info);
		} else 
			return false;
	}
	
	public boolean isIncludedIn() {
		return includedIn;
	}
	
	public boolean isSuffixOf() {
		return suffixOf;
	}
	
	public boolean isPrefixOf() {
		return prefixOf;
	}
	
	public void setScore(double score) {
		this.score = score;
	}
	
	public double getScore() {
		return score;
	}
	
	@Override
	public int compareTo(TermVariation tv) {
		return ComparisonChain.start()
				// sort by score desc
				.compare(tv.score, this.score)
				// then by non inclusion first
				.compare(this.includedIn ? 1 : 0, tv.includedIn ? 1 : 0)
				// then by length asc
				.compare(this.variant.getWords().size(), tv.variant.getWords().size())
				// then by term id
				.compare(this.variant.getGroupingKey(), tv.variant.getGroupingKey())
				// makes it consistent with equals
				.compare(this.base.getGroupingKey(), tv.base.getGroupingKey())
				.compare(this.variationType, tv.variationType)
				.result();
				
	}
}
