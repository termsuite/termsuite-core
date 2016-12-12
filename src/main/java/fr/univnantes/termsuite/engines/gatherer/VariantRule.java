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
package fr.univnantes.termsuite.engines.gatherer;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.google.common.base.Objects;

import fr.univnantes.termsuite.model.Term;

/**
 * 
 * A syntactic variant rule that is expressed and matched using Groovy.
 * 
 * @author Damien Cram
 *
 */
public class VariantRule {
	private static final Pattern USE_SYNONYM = Pattern.compile("synonym\\s*\\(");

	protected String name;
	protected String expression;

	protected boolean sourceCompound = false;
	protected boolean targetCompound = false;
	protected Set<Integer> sourceSizes;
	protected Set<Integer> targetSizes;

	protected Set<String> sourcePatterns = new HashSet<>();
	protected Set<String> targetPatterns = new HashSet<>();

	private VariationType variationType;
	
	public VariationType getVariationType() {
		return variationType;
	}
	
	public void setVariationType(VariationType ruleType) {
		this.variationType = ruleType;
	}
	
	public VariantRule(String name) {
		super();
		this.name = name;
	}
	
	public boolean isSynonymicRule() {
		return USE_SYNONYM.matcher(this.expression).find();
	}

	public void setSourceCompound(boolean sourceCompound) {
		this.sourceCompound = sourceCompound;
	}
	
	public void setTargetCompound(boolean targetCompound) {
		this.targetCompound = targetCompound;
	}

	public void setSourcePatterns(Collection<String> patterns) {
		sourcePatterns = new HashSet<>(patterns.size());
		sourcePatterns.addAll(patterns);
		sourceSizes = sourcePatterns.stream().map(p -> p.split(" ").length).collect(Collectors.toSet());
	}
	
	public void setTargetPatterns(Collection<String> patterns) {
		targetPatterns = new HashSet<>(patterns.size());
		targetPatterns.addAll(patterns);
		targetSizes = targetPatterns.stream().map(p -> p.split(" ").length).collect(Collectors.toSet());
	}

//	public List<String> getIndexingKeys(List<String> allPossiblePatterns) {
//		List<String> keys = Lists.newArrayList();
//		List<String> left = ImmutableList.copyOf(sourcePatterns.isEmpty() ? allPossiblePatterns : sourcePatterns);
//		List<String> right = ImmutableList.copyOf(targetPatterns.isEmpty() ? allPossiblePatterns : targetPatterns);
//		for(String l:left) {
//			for(String r:right) 
//				keys.add(l+"x"+r);
//		}
//		return keys;
//	}

	public String getName() {
		return name;
	}

	public boolean isSourceCompound() {
		return sourceCompound;
	}

	public boolean isTargetCompound() {
		return targetCompound;
	}

	public Set<Integer> getTargetSizes() {
		return targetSizes;
	}
	
	public Set<Integer> getSourceSizes() {
		return sourceSizes;
	}

	public Set<String> getSourcePatterns() {
		return sourcePatterns;
	}

	public Set<String> getTargetPatterns() {
		return targetPatterns;
	}
	
	public String getExpression() {
		return expression;
	}
	
	@Override
	public int hashCode() {
		return this.name.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof VariantRule) {
			VariantRule o = (VariantRule) obj;
			return Objects.equal(name,  o.name);
		} else
			return false;
	}
	
	@Override
	public String toString() {
		return getName();
	}

	public void setExpression(String expression) {
		this.expression = expression;
	}
	
	public boolean isSourceAcceptable(Term source) {
		return sourceSizes.contains(source.getWords().size())
				&& sourcePatterns.contains(source.getPattern())
				&& (sourceCompound ? source.isCompound() : true);
	}

	public boolean isTargetAcceptable(Term target) {
		return targetSizes.contains(target.getWords().size())
				&& targetPatterns.contains(target.getPattern())
				&& (targetCompound ? target.isCompound() : true);
	}
}
