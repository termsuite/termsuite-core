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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.yaml.snakeyaml.Yaml;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import fr.univnantes.termsuite.utils.TermSuiteUtils;

public class YamlRuleSetIO {
	public static final String P_SOURCE = "source";
	public static final String P_TARGET = "target";
	public static final String P_RULE = "rule";
	public static final String OPT_COMPOUND = "compound";
	public static final String COMPOUND_REGEX = "\\[\\s*(\\w+)\\s*\\]";
	
	
	private static final Pattern MORPHOLOGICAL_RULE_PATTERN = Pattern.compile("\\[\\s*\\d+\\s*\\]\\s*\\[\\s*\\d+\\s*\\]");
	private static final Pattern SYNONYMIC_RULE_PATTERN = Pattern.compile("synonym\\s*\\(\\s*([st]\\[\\s*\\d+\\s*\\])\\s*,\\s*([st]\\[\\s*\\d+\\s*\\])\\s*\\)");
	private static final Pattern PREFIX_RULE_PATTERN = Pattern.compile("prefix\\s*\\(\\s*([st]\\[\\s*\\d+\\s*\\])\\s*,\\s*([st]\\[\\s*\\d+\\s*\\])\\s*\\)");
	private static final Pattern DERIVATION_RULE_PATTERN = Pattern.compile("deriv\\s*\\([^\\)]*\\)");

	/*
	 * For synonymic rules only
	 */
	private static final Pattern SYNONYM_EXPRESSION_PATTERN = Pattern.compile("synonym\\s*\\(\\s*([st]\\[\\s*\\d+\\s*\\])\\s*,\\s*([st]\\[\\s*\\d+\\s*\\])\\s*\\)");
	private static final Pattern EQUALITY_EXPRESSION_PATTERN = Pattern.compile("[st]\\s*\\[\\s*\\d+\\s*\\]\\s*==\\s*[st]\\s*\\[\\s*\\d+\\s*\\]");

	private static final List<String> ALLOWED_PROPS = ImmutableList.of(
			P_SOURCE,
			P_TARGET,
			P_RULE
			);
	
	private Set<VariantRule> variantRules;
	
	private String yaml;
	
	private YamlRuleSetIO(String yamlString){
		this.yaml = yamlString;
	}
	
	public static YamlRuleSet fromYaml(String yamlString) {
		YamlRuleSetIO io = new YamlRuleSetIO(yamlString);
		io.fromYaml();
		return new YamlRuleSet(io.variantRules);
	}

	private void fromYaml() {
		this.variantRules = new HashSet<>();
		Object yaml = new Yaml().load(this.yaml);
		Preconditions.checkArgument(
				yaml instanceof LinkedHashMap, 
				"Bad format for yaml rules file. Expected key-values, got: " + yaml.getClass());
		
		Map<?, ?> map = (Map<?, ?>) yaml;
		for(Map.Entry<?, ?> entry:map.entrySet()) {
			String ruleName = (String)entry.getKey();	
			Preconditions.checkArgument(
					entry.getValue() instanceof LinkedHashMap, 
					String.format("Bad format for rule %s. Expected key-values, got: %s", entry.getKey(), entry.getClass()));
			Map<?,?> props = (Map<?,?>)entry.getValue();

			Preconditions.checkArgument(ALLOWED_PROPS.containsAll(props.keySet()),
					String.format("Unexcpected property in rule %s. Allowed rule properties: %s", 
							ruleName, ALLOWED_PROPS));
			
			for(String key:Lists.newArrayList(P_RULE, P_SOURCE, P_TARGET))
				Preconditions.checkArgument(
						props.containsKey(key),
						"Missing key %s for rule named %s", key, ruleName);
			
			String expression = (String)props.get(P_RULE);
			VariationType type = inferRuleType(expression, ruleName);
			VariantRule rule = type == VariationType.SEMANTIC ? new SynonymicRule(ruleName) : new VariantRule(ruleName);
			rule.setVariationType(type);
			parseSourceTarget(rule, P_SOURCE, props.get(P_SOURCE));
			parseSourceTarget(rule, P_TARGET, props.get(P_TARGET));
			parseExpression(rule, props.get(P_RULE));
			if(type == VariationType.SEMANTIC) {
				parseSemanticExpression((SynonymicRule)rule);
			}
			this.variantRules.add(rule);
		}
	}
	
	public static List<Integer> parseEqualityIndices(String expression) {
		Matcher matcher = EQUALITY_EXPRESSION_PATTERN.matcher(expression);
		List<Integer> equalityIndices = Lists.newArrayList();
		
		while(matcher.find()) {
			Matcher sourceMatcher = Pattern.compile("s\\s*\\[\\s*(\\d+)\\s*\\]").matcher(matcher.group());
			if(!sourceMatcher.find())
				continue;
			Matcher targetMatcher = Pattern.compile("t\\s*\\[\\s*(\\d+)\\s*\\]").matcher(matcher.group());
			if(!targetMatcher.find())
				continue;
			
			int sourceIndex = Integer.parseInt(sourceMatcher.group(1));
			int targetIndex = Integer.parseInt(targetMatcher.group(1));
			
			if(sourceIndex != targetIndex) 
				continue;
			equalityIndices.add(sourceIndex);
		} 

		if(equalityIndices.isEmpty())
			return Lists.newArrayList();
		else {
			Collections.sort(equalityIndices);
			LinkedList<Integer> currentSequence = new LinkedList<>();
			List<List<Integer>> sequences = new LinkedList<>();
			for(Integer index:equalityIndices) {
				if(currentSequence.isEmpty())
					currentSequence.add(index);
				else {
					if(index == currentSequence.getLast() + 1)
						currentSequence.add(index);
					else {
						sequences.add(currentSequence);
						currentSequence = new LinkedList<>();
						currentSequence.add(index);
					}
				}
			}
			
			if(!currentSequence.isEmpty())
				sequences.add(currentSequence);
			
			Optional<List<Integer>> max = sequences.stream().max(new Comparator<List<Integer>>() {
				@Override
				public int compare(List<Integer> o1, List<Integer> o2) {
					return ComparisonChain.start()
							.compare(o1.size(), o2.size())
							.compare(o2.get(0), o1.get(0))
							.result()
							;
				}
			});
			if(max.isPresent()) {
				return max.get();
			} 
				else
					return Lists.newArrayList();
		}

	}

	public static void parseSemanticExpression(SynonymicRule rule) {
		Preconditions.checkArgument(rule.getSourcePatterns().size() == 1, "Only one source pattern allowed in a synonymic rule. Got: %s", rule.getSourcePatterns().size());
		Preconditions.checkArgument(rule.getTargetPatterns().size() == 1, "Only one target pattern allowed in a synonymic rule. Got: %s", rule.getTargetPatterns().size());
		Preconditions.checkArgument(rule.getSourcePatterns().equals(rule.getTargetPatterns()), "Source pattern must be equal to target pattern");
		
		if(rule.getExpression().contains("||"))
			throw new IllegalStateException("No disjunction allowed in synonym expression");

		Matcher matcher = SYNONYM_EXPRESSION_PATTERN.matcher(rule.getExpression());
		
		if(matcher.find()) {
			String sourceExpr = matcher.group(1);
			String targetExpr = matcher.group(2);
			
			// ensure there is on s and one t as parameters
			if((sourceExpr.startsWith("s") && targetExpr.startsWith("t"))
					|| (sourceExpr.startsWith("t") && targetExpr.startsWith("s"))) {
				
				// switch s and t if required
				if(sourceExpr.startsWith("t")) {
					String aux = sourceExpr;
					sourceExpr = targetExpr;
					targetExpr = aux;
				}
				
				rule.setSynonymSourceWordIndex(Integer.parseInt(sourceExpr.replaceAll("\\D+", "")));
				int synonymTargetWordIndex = Integer.parseInt(targetExpr.replaceAll("\\D+", ""));
				Preconditions.checkState(rule.getSynonymSourceWordIndex() == synonymTargetWordIndex,
						"Synonymic expression parameters must have the same index. Got " 
								+ rule.getSynonymSourceWordIndex() + " and " + synonymTargetWordIndex);
				
				String expression = removeSubExpression(
						SYNONYM_EXPRESSION_PATTERN.toString(), 
						rule.getExpression());
				
				rule.setEqIndices(new LinkedList<>(parseEqualityIndices(expression)));
				
				rule.setExpression(expression);
			} else
				throw new IllegalStateException("Expected extactly one source ref (s[]) and one target ref (t[]) in synonym expression. Got:  " + matcher.group());
		} else
			throw new IllegalStateException("No synonym expression found for synonymic rule " + rule.getName());
		
		if(matcher.find()) 
			throw new IllegalStateException("Only one synonym expression allowed. Rule <"+rule.getName()+"> has several ones.");

	}
	
	
	public static String removeSubExpression(String pattern, String sourceString) {
		String expression = sourceString.replaceAll(pattern, "");
		expression = expression
				.replaceAll("&&\\s*&&", "&&")
				.replaceAll("&&\\s*$", "")
				.replaceAll("^\\s*&&", "")
				;
		return expression;
	}


	public static void parseExpression(VariantRule rule, Object valueStr) {
		Preconditions.checkArgument(
				valueStr instanceof CharSequence,
				String.format("Bad format for property rule (in rule %s). String expected. Got %s", 
						rule,
						valueStr.getClass().getName()));
		rule.setExpression(StringUtils.chomp((String) valueStr));
	}

	private void parseSourceTarget(VariantRule rule, String prop, Object valueStr) {
		Preconditions.checkArgument(
				valueStr instanceof CharSequence,
				String.format("Bad format for property %s (rule %s). String expected. Got %s", 
						prop,
						rule,
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
								rule, 
								value));
			if(compoundMatcher.group(1).equals(OPT_COMPOUND))
				isCompound = true;
			else
				throw new IllegalArgumentException(
						String.format("Illegal options for rule %s: %s", 
								rule, 
								compoundMatcher.group(1)));
		}
		String withoutOptions = value.replaceAll(COMPOUND_REGEX, "").trim();
		Set<String> occPatterns = Splitter.on(',').splitToList(withoutOptions).stream()
					.filter(p->!p.trim().isEmpty())
					.map(p->TermSuiteUtils.trimInside(p))
					.collect(Collectors.toSet());
		if(P_SOURCE.equals(prop)) {
			rule.setSourceCompound(isCompound);
			rule.setSourcePatterns(occPatterns);
		} else if(P_TARGET.equals(prop)) {
			rule.setTargetCompound(isCompound);
			rule.setTargetPatterns(occPatterns);
		}
	}
	
	

	public static VariationType inferRuleType(String expression, String ruleName) {
		List<VariationType> ruleTypes = new ArrayList<>();
		if(DERIVATION_RULE_PATTERN.matcher(expression).find())
			ruleTypes.add(VariationType.DERIVATION);
		if(PREFIX_RULE_PATTERN.matcher(expression).find())
			ruleTypes.add(VariationType.PREFIXATION);
		if(SYNONYMIC_RULE_PATTERN.matcher(expression).find())
			ruleTypes.add(VariationType.SEMANTIC);
		if(MORPHOLOGICAL_RULE_PATTERN.matcher(expression).find())
			ruleTypes.add(VariationType.MORPHOLOGICAL);
		if(ruleTypes.isEmpty())
			ruleTypes.add(VariationType.SYNTAGMATIC);
	
		if(ruleTypes.size()>1)
			throw new VariantRuleFormatException(
					String.format("A variation rule cannot have more than one matching rule types [Rule: %s, types: %s]", ruleName, ruleTypes),
					ruleName);
		else
			return ruleTypes.iterator().next();
	}

}
