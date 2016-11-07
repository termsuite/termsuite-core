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
package eu.project.ttc.engines.variant;

import java.util.List;
import java.util.regex.Pattern;

import org.codehaus.groovy.runtime.InvokerInvocationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import eu.project.ttc.models.GroovyAdapter;
import eu.project.ttc.models.GroovyTerm;
import eu.project.ttc.models.Term;
import eu.project.ttc.models.TermIndex;
import fr.univnantes.julestar.uima.resources.MultimapFlatResource;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;

/**
 * 
 * A syntactic variant rule that is expressed and matched using Groovy.
 * 
 * @author Damien Cram
 *
 */
public class VariantRule {
	private static final Logger LOGGER = LoggerFactory.getLogger(VariantRule.class);
	private static final Pattern USE_DERIV = Pattern.compile("deriv\\s*\\(");
	private static final Pattern USE_PREFIX = Pattern.compile("prefix\\s*\\(");
	private static final Pattern USE_SYNONYM = Pattern.compile("synonym\\s*\\(");
	private static final String GROOVY_MATCH_METHOD_NAME = "match";
	private static final String GROOVY_SET_HELPER_METHOD_NAME = "setHelper";
	private static int CLASS_NUM = 0;

	private String name;
	private String expression;
	private VariantRuleIndex index = VariantRuleIndex.DEFAULT;

	private boolean sourceCompound = false;
	private boolean targetCompound = false;
	private List<String> sourcePatterns = Lists.newArrayList();
	private List<String> targetPatterns = Lists.newArrayList();
	private GroovyObject groovyRule;
	private GroovyAdapter groovyAdapter;
	private VariantHelper helper;

	public VariantRule(String name) {
		super();
		this.name = name;
	}
	
		
	
	public void setGroovyAdapter(GroovyAdapter groovyAdapter) {
		this.groovyAdapter = groovyAdapter;
	}
	
	public void initialize(TermIndex termIndex, MultimapFlatResource dico) {
		this.helper.setTermIndex(termIndex);
		this.helper.setSynonyms(dico);
	}
	
	void setGroovyRule(String groovyExpression) {
		this.expression = groovyExpression;
		initIndex();
		
		try {
			this.helper = new VariantHelper();
			String script = String.format(""
					+ "class GroovyVariantRule%s {\n"
					+ "def helper;\n"
					+ "def setHelper(h) {this.helper = h;}\n"
					+ "def prefix(s,t){return this.helper.isPrefixOf(s,t);}\n"
					+ "def synonym(s,t){return this.helper.areSynonym(s,t);}\n"
					+ "def deriv(p,s,t){return this.helper.derivesInto(p,s,t);}\n"
					+ "def Boolean match(s, t) { %s }\n"
					+ "}", 
					newRuleClassName(this.name),
					groovyExpression);
			Class<?> groovyClass = getGroovyClassLoader().parseClass(script, name);
			this.groovyRule = (GroovyObject) groovyClass.newInstance();
			this.groovyRule.invokeMethod(
					GROOVY_SET_HELPER_METHOD_NAME, 
					new Object[] { helper });
		} catch (InstantiationException | IllegalAccessException e) {
			throw new IllegalStateException("Could not load groovy expression as groovy object: " + groovyExpression, e);
		}
	}

	public VariantRuleIndex getIndex() {
		return index;
	}
	
	private void initIndex() {
		if(USE_DERIV.matcher(this.expression).find())
			this.index = VariantRuleIndex.DERIVATION;
		if(USE_PREFIX.matcher(this.expression).find())
			this.index = VariantRuleIndex.PREFIX;
	}
	
	public boolean isSynonymicRule() {
		return USE_SYNONYM.matcher(this.expression).find();
	}

	private static String newRuleClassName(String ruleName) {
		return CLASS_NUM++ + ruleName.replaceAll("-", "_").replaceAll("[^a-zA-Z]+", "");
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
		} catch(InvokerInvocationException e) {
			LOGGER.error("An error occurred in groovy variant rule", e);
			throw new RuntimeException(e);
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
