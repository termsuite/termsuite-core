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

import eu.project.ttc.utils.TermUtils;

public class TermRelation implements Comparable<TermRelation> {
	private RelationType relationType;
	private Term from;
	private Term to;
	private Object info;
	private String _label;
	private double score;
	
	private boolean includedIn;
	private boolean prefixOf;
	private boolean suffixOf;
	
	public TermRelation(RelationType variationType, Term from, Term to, Object info) {
		super();
		this.relationType = variationType;
		this.from = from;
		this.to = to;
		this.info = info;
		this.includedIn = TermUtils.isIncludedIn(from, to);
		this.prefixOf = TermUtils.isPrefixOf(from, to);
		this.suffixOf = TermUtils.isSuffixOf(from, to);
	}
	
	public RelationType getType() {
		return relationType;
	}
	
	public Object getInfo() {
		return info;
	}
	
	public Term getTo() {
		return to;
	}
	
	public Term getFrom() {
		return from;
	}
	
	@Override
	public String toString() {
		return String.format("%s --- %s --> %s", from.getGroupingKey(), this.info, to.getGroupingKey());
	}
	
	@Override
	public int hashCode() {
		return Objects.hashCode(this.from, this.to, this.relationType, this.info);
	}
	
	public String getLabel() {
		if(this._label == null) 
			this._label = this.relationType.getShortName() + ":" + this.info; 
		return this._label;
	}
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof TermRelation) {
			TermRelation v = (TermRelation) obj;
			return Objects.equal(this.score, v.score)
					&& Objects.equal(this.from, v.from)
					&& Objects.equal(this.to, v.to)
					&& Objects.equal(this.relationType, v.relationType)
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
	public int compareTo(TermRelation tv) {
		return ComparisonChain.start()
				// sort by score desc
				.compare(tv.score, this.score)
				// then by non inclusion first
				.compare(this.includedIn ? 1 : 0, tv.includedIn ? 1 : 0)
				// then by length asc
				.compare(this.to.getWords().size(), tv.to.getWords().size())
				// then by term id
				.compare(this.to.getGroupingKey(), tv.to.getGroupingKey())
				// makes it consistent with equals
				.compare(this.from.getGroupingKey(), tv.from.getGroupingKey())
				.compare(this.relationType, tv.relationType)
				.result();
				
	}
}
