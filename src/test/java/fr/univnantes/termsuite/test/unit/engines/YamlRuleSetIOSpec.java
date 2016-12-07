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

package fr.univnantes.termsuite.test.unit.engines;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collection;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.google.common.collect.ImmutableSet;

import fr.univnantes.termsuite.engines.gatherer.RuleType;
import fr.univnantes.termsuite.engines.gatherer.VariantRule;
import fr.univnantes.termsuite.engines.gatherer.VariantRuleFormatException;
import fr.univnantes.termsuite.engines.gatherer.YamlRuleSet;
import fr.univnantes.termsuite.engines.gatherer.YamlRuleSetIO;
import fr.univnantes.termsuite.test.unit.TestUtil;


public class YamlRuleSetIOSpec {

	private static final String RULES_1_PATH = "fr/univnantes/termsuite/test/variants/rules1.yml";
	private static final String RULES_2_PATH = "fr/univnantes/termsuite/test/variants/rules2.yml";
	private static final String BAD_RULES_1_PATH = "fr/univnantes/termsuite/test/variants/bad_rules1.yml";
	private static final String BAD_RULES_2_PATH = "fr/univnantes/termsuite/test/variants/bad_rules2.yml";
	private static final String BAD_RULES_3_PATH = "fr/univnantes/termsuite/test/variants/bad_rules3.yml";
	private static final String BAD_RULES_4_PATH = "fr/univnantes/termsuite/test/variants/bad_rules4.yml";
	private static final String BAD_RULES_5_PATH = "fr/univnantes/termsuite/test/variants/bad_rules5.yml";
	private static final String BAD_RULES_6_PATH = "fr/univnantes/termsuite/test/variants/bad_rules6.yml";

	
	@Test
	public void should_parse_valid_rule()  {
		Collection<VariantRule> rules = YamlRuleSetIO.fromYaml(TestUtil.readFile(RULES_1_PATH)).getVariantRules();
		assertEquals(1, rules.size());
		VariantRule rule1 = rules.iterator().next();
		assertEquals("My rule 1", rule1.getName());
		assertEquals(ImmutableSet.of("N", "A"), rule1.getSourcePatterns());
		assertEquals(ImmutableSet.of("A A N", "A N N", "A R N"), rule1.getTargetPatterns());
		assertFalse(rule1.isSourceCompound());
		assertFalse(rule1.isTargetCompound());
		assertEquals("The rule expr", rule1.getExpression());
	}

	@Test
	public void should_parse_valid_rule_with_multi_line_expression()  {
		String file = TestUtil.readFile(RULES_2_PATH);
		YamlRuleSet ruleSet = YamlRuleSetIO.fromYaml(file);
		Collection<VariantRule> rules = ruleSet.getVariantRules();
		assertEquals(2, rules.size());
		VariantRule rule1 = rules.stream()
				.filter(r->r.getName().equals("My rule 1"))
				.findFirst().get();
		assertEquals("My rule 1", rule1.getName());
		assertEquals(ImmutableSet.of("N"), rule1.getSourcePatterns());
		assertEquals(ImmutableSet.of("N A"), rule1.getTargetPatterns());
		assertTrue(rule1.isSourceCompound());
		assertFalse(rule1.isTargetCompound());
		assertEquals("s[0] == t[1].stem && s[1][0] != t[0]", rule1.getExpression());
		
		VariantRule rule2 = rules.stream()
				.filter(r->r.getName().equals("My rule 2"))
				.findFirst().get();
		assertEquals("My rule 2", rule2.getName());
		assertEquals(ImmutableSet.of("N"), rule2.getSourcePatterns());
		assertEquals(ImmutableSet.of("N", "A N"), rule2.getTargetPatterns());
		assertTrue(rule2.isSourceCompound());
		assertFalse(rule2.isTargetCompound());
		assertEquals("s[0] == t[1].stem  && s[1][0] != t[0]", rule2.getExpression());
	}

	@Test
	public void should_raise_exception_if_input_file_is_no_map() {
		checkVariantRuleException(
				BAD_RULES_1_PATH, 
				IllegalArgumentException.class,
				"Bad format for yaml rules file. Expected key-values");
	}
	

	@Test
	public void should_raise_exception_if_multiple_option_bracket() {
		checkVariantRuleException(
				BAD_RULES_2_PATH, 
				IllegalArgumentException.class,
				"Only one option bracket allowed");
	}

	@Test
	public void should_raise_exception_if_bad_source_format() {
		checkVariantRuleException(
				BAD_RULES_3_PATH, 
				IllegalArgumentException.class,
				"Bad format for property");
	}

	@Test
	public void should_raise_exception_if_bad_source_option() {
		checkVariantRuleException(
				BAD_RULES_4_PATH, 
				IllegalArgumentException.class,
				"Illegal options for rule");
	}
	
	@Test
	public void should_raise_exception_if_rule_is_no_key_value_hash() {
		checkVariantRuleException(
				BAD_RULES_5_PATH, 
				IllegalArgumentException.class,
				"Bad format for rule");
		checkVariantRuleException(
				BAD_RULES_6_PATH, 
				IllegalArgumentException.class,
				"Bad format for rule");
	}

	
	private void checkVariantRuleException(String badRules1Path,
			Class<? extends Throwable> t, String description) {
		thrown.expect(t);
		thrown.expectMessage(description);
		
		YamlRuleSetIO.fromYaml(TestUtil.readFile(badRules1Path));
	}
	
	
	
	
	@Test
	public void testInferRuleType() {
		assertEquals(
				RuleType.PREFIXATION, 
				YamlRuleSetIO.inferRuleType("s[0]==t[0] && prefix(t[1],s[1])", "Toto"));
		

		assertEquals(
				RuleType.SYNTAGMATIC, 
				YamlRuleSetIO.inferRuleType("s[0]==t[0]", "Toto"));


		assertEquals(
				RuleType.DERIVATION, 
				YamlRuleSetIO.inferRuleType("s[0]==t[0] && deriv(\"A N\",t[1],s[1])", "Toto"));

		assertEquals(
				RuleType.SEMANTIC, 
				YamlRuleSetIO.inferRuleType("s[0]==t[0] && synonym(t[1],s[1])", "Toto"));

		assertEquals(
				RuleType.MORPHOLOGICAL, 
				YamlRuleSetIO.inferRuleType("s[0][2]==t[0]", "Toto"));

	}

	
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void testInferRuleTypeFailIfMultiple1() {
		thrown.expect(VariantRuleFormatException.class);
		thrown.expectMessage(containsString("types: [" + RuleType.DERIVATION + ", " + RuleType.MORPHOLOGICAL));
		YamlRuleSetIO.inferRuleType("s[0][2]==t[0] && deriv(\"A N\",t[1],s[1])", "Toto");
	}

	@Test
	public void testInferRuleTypeFailIfMultiple2() {
		thrown.expect(VariantRuleFormatException.class);
		thrown.expectMessage(containsString("types: [" + RuleType.PREFIXATION + ", " + RuleType.SEMANTIC));
		YamlRuleSetIO.inferRuleType("synonym(s[0],t[0]) && prefix(t[1],s[1])", "Toto");
	}

}
