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
package eu.project.ttc.engines.variant;

import eu.project.ttc.models.GroovyAdapter;
import eu.project.ttc.models.GroovyTerm;
import eu.project.ttc.models.Term;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

/**
 * 
 * A syntactic variant rule that is expressed and matched using Groovy.
 * 
 * @author Damien Cram
 *
 */
public class VariantRule {
	private static final Logger LOGGER = LoggerFactory.getLogger(VariantRule.class);
	private static final String GROOVY_MATCH_METHOD_NAME = "match";

	private String name;
	private String expression;
	private boolean sourceCompound = false;
	private boolean targetCompound = false;
	private List<String> sourcePatterns = Lists.newArrayList();
	private List<String> targetPatterns = Lists.newArrayList();
	private GroovyObject groovyRule;
	private GroovyAdapter groovyAdapter;
	
	public VariantRule(String name) {
		super();
		this.name = name;
	}
	
	public void setGroovyAdapter(GroovyAdapter groovyAdapter) {
		this.groovyAdapter = groovyAdapter;
	}
	
	void setGroovyRule(String groovyExpression) {
		this.expression = groovyExpression;
		try {
			String script = String.format("def Boolean %s(s, t) { %s }", GROOVY_MATCH_METHOD_NAME, groovyExpression);
			Class<?> groovyClass = getGroovyClassLoader().parseClass(script, name);
			this.groovyRule = (GroovyObject) groovyClass.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new IllegalStateException("Could not load groovy expression as groovy object: " + groovyExpression, e);
		}
	}
	
	private static GroovyClassLoader groovyClassLoader;
	private static GroovyClassLoader getGroovyClassLoader() {
		if(groovyClassLoader == null) {
			ClassLoader classLoader = VariantRule.class.getClassLoader();
			groovyClassLoader = new GroovyClassLoader(classLoader);
		}
		return groovyClassLoader;
	}

	void setSourceCompound(boolean sourceCompound) {
		this.sourceCompound = sourceCompound;
	}
	
	void setTargetCompound(boolean targetCompound) {
		this.targetCompound = targetCompound;
	}

	boolean addSourcePattern(String c) {
		return sourcePatterns.add(c);
	}
	
	boolean addTargetPattern(String c) {
		return targetPatterns.add(c);
	}

	public boolean matchExpression(Term source, Term target) {
		try {
			GroovyTerm s = groovyAdapter.asGroovyTerm(source);
			GroovyTerm t = groovyAdapter.asGroovyTerm(target);
			return (boolean) this.groovyRule.invokeMethod(
				GROOVY_MATCH_METHOD_NAME, 
				new Object[] { s, t });
		} catch(IndexOutOfBoundsException e) {
			return false;
		} catch(Exception e) {
			LOGGER.warn("The variant rule {} throwed an exception: {}", this.name, e.getClass());
			return false;
		}
	}
	
	public List<String> getIndexingKeys(List<String> allPossiblePatterns) {
		List<String> keys = Lists.newArrayList();
		List<String> left = ImmutableList.copyOf(sourcePatterns.isEmpty() ? allPossiblePatterns : sourcePatterns);
		List<String> right = ImmutableList.copyOf(targetPatterns.isEmpty() ? allPossiblePatterns : targetPatterns);
		for(String l:left) {
			for(String r:right) 
				keys.add(l+"x"+r);
		}
		return keys;
	}

	public String getName() {
		return name;
	}

	public boolean isSourceCompound() {
		return sourceCompound;
	}

	public boolean isTargetCompound() {
		return targetCompound;
	}

	public List<String> getSourcePatterns() {
		return sourcePatterns;
	}

	public List<String> getTargetPatterns() {
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
}
