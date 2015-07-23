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


public class SyntacticVariation  {
	
	private Term source;
	private Term target;
	private String variationRule;
	
	SyntacticVariation(Term source, Term target,
			String variationRule) {
		super();
		this.source = source;
		this.target = target;
		this.variationRule = variationRule;
	}

	public Term getSource() {
		return source;
	}

	public Term getTarget() {
		return target;
	}

	public String getVariationRule() {
		return variationRule;
	}
	
	@Override
	public String toString() {
		return String.format("%s --- %s --> %s", source.getGroupingKey(), this.variationRule, target.getGroupingKey());
	}
	
	@Override
	public int hashCode() {
		return Objects.hashCode(this.source, this.target, this.variationRule);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof SyntacticVariation) {
			SyntacticVariation v = (SyntacticVariation) obj;
			return Objects.equal(this.source, v.source)
					&& Objects.equal(this.target, v.target)
					&& Objects.equal(this.variationRule, v.variationRule);
		} else 
			return false;
	}
}
