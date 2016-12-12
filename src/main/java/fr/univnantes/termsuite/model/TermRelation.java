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
package fr.univnantes.termsuite.model;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

public class TermRelation extends PropertyHolder<RelationProperty> {
	private RelationType relationType;
	private Term from;
	private Term to;
	
	public TermRelation(RelationType variationType, Term from, Term to) {
		super(RelationProperty.class);
		Preconditions.checkNotNull(from);
		Preconditions.checkNotNull(to);
		Preconditions.checkNotNull(variationType);
		this.relationType = variationType;
		this.from = from;
		this.to = to;
	}
	
	public RelationType getType() {
		return relationType;
	}
	
	public Term getTo() {
		return to;
	}
	
	public Term getFrom() {
		return from;
	}
	
	@Override
	public String toString() {
		return String.format("%s --- %s --> %s", from.getGroupingKey(), this.relationType.getShortName(), to.getGroupingKey());
	}
	
	@Override
	public int hashCode() {
		/*
		 * FIXME Very bad choice since properties can change over the time.
		 */
		return Objects.hashCode(this.from, this.to, this.relationType, this.properties);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof TermRelation) {
			TermRelation v = (TermRelation) obj;
			return Objects.equal(this.from, v.from)
					&& Objects.equal(this.to, v.to)
					&& Objects.equal(this.relationType, v.relationType)
					&& super.equals(obj);
		} else 
			return false;
	}

}
