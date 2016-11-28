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
package fr.univnantes.termsuite.uima.engines.termino.gathering;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.yaml.snakeyaml.Yaml;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import fr.univnantes.termsuite.utils.TermSuiteUtils;

public class VariantRuleYamlIO {
	public static final String P_SOURCE = "source";
	public static final String P_TARGET = "target";
	public static final String P_RULE = "rule";
	public static final String OPT_COMPOUND = "compound";
	public static final String COMPOUND_REGEX = "\\[\\s*(\\w+)\\s*\\]";
	
	private static final List<String> ALLOWED_PROPS = ImmutableList.of(
			P_SOURCE,
			P_TARGET,
			P_RULE
			);
	
	private List<VariantRule> variantRules;
	
	private String yaml;
	
	private VariantRuleYamlIO(String yamlString){
		this.yaml = yamlString;
	}
	
	public static VariantRuleYamlIO fromYaml(String yamlString) {
		return new VariantRuleYamlIO(yamlString);
	}

	private void fromYaml() {
		this.variantRules = Lists.newArrayList();
		Object yaml = new Yaml().load(this.yaml);
		Preconditions.checkArgument(
				yaml instanceof LinkedHashMap, 
				"Bad format for yaml rules file. Expected key-values, got: " + yaml.getClass());
		
		Map<?, ?> map = (Map<?, ?>) yaml;
		for(Map.Entry<?, ?> entry:map.entrySet()) {
			String ruleName = (String)entry.getKey();
			VariantRuleBuilder builder = VariantRuleBuilder.start(ruleName);
			Preconditions.checkArgument(
					entry.getValue() instanceof LinkedHashMap, 
					String.format("Bad format for rule %s. Expected key-values, got: %s", entry.getKey(), entry.getClass()));
			Map<?,?> props = (Map<?,?>)entry.getValue();
			Preconditions.checkArgument(ALLOWED_PROPS.containsAll(props.keySet()),
					String.format("Unexcpected property in rule %s. Allowed rule properties: %s", 
							ruleName, ALLOWED_PROPS));
			if(props.get(P_SOURCE) != null )
				parseSourceTarget(builder, ruleName, P_SOURCE, props.get(P_SOURCE));
			if(props.get(P_TARGET) != null )
				parseSourceTarget(builder, ruleName, P_TARGET, props.get(P_TARGET));
			if(props.get(P_RULE) != null ) {
				parseRule(builder, ruleName, props.get(P_RULE));
			} 
			this.variantRules.add(builder.create());
		}
	}
	
	private void parseRule(VariantRuleBuilder builder, String ruleName, Object valueStr) {
		Preconditions.checkArgument(
				valueStr instanceof CharSequence,
				String.format("Bad format for property rule (in rule %s). String expected. Got %s", 
						ruleName,
						valueStr.getClass().getName()));
		builder.rule(StringUtils.chomp((String) valueStr));
	}

	private void parseSourceTarget(VariantRuleBuilder builder, String ruleName, String prop, Object valueStr) {
		Preconditions.checkArgument(
				valueStr instanceof CharSequence,
				String.format("Bad format for property %s (rule %s). String expected. Got %s", 
						prop,
						ruleName,
						valueStr.getClass().getName()));
		
		String value = (String) valueStr;
		Matcher compoundMatcher = Pattern.compile(COMPOUND_REGEX).matcher(value);
		boolean isCompound = false;
		int cnt = 0;
		while(compoundMatcher.find()) {
			if(++cnt > 1)
				// do not allow to have multiple option
				throw new IllegalArgumentException(
						String.format("Only one option bracket allowed (rule %s). Got: %s", 
								ruleName, 
								value));
			if(compoundMatcher.group(1).equals(OPT_COMPOUND))
				isCompound = true;
			else
				throw new IllegalArgumentException(
						String.format("Illegal options for rule %s: %s", 
								ruleName, 
								compoundMatcher.group(1)));
		}
		String withoutOptions = value.replaceAll(COMPOUND_REGEX, "").trim();
		Iterable<String> occPatterns = Splitter.on(',').split(withoutOptions);
		if(P_SOURCE.equals(prop)) {
			if(isCompound)
				builder.sourceCompound();
			for(String p:occPatterns) 
				if(!p.trim().isEmpty())
					builder.addSourcePattern(TermSuiteUtils.trimInside(p));
		} else if(P_TARGET.equals(prop)) {
			if(isCompound)
				builder.targetCompound();
			for(String p:occPatterns) 
				if(!p.trim().isEmpty())
					builder.addTargetPattern(TermSuiteUtils.trimInside(p));
		}
	}
	
	public List<VariantRule> getVariantRules() {
		if(this.variantRules == null)
			fromYaml();
		return variantRules;
	}

}
