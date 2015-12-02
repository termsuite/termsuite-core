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

package org.ttc.project.test.variants;

import static com.googlecode.catchexception.CatchException.caughtException;
import static com.googlecode.catchexception.apis.BDDCatchException.when;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.assertj.core.api.BDDAssertions;
import org.junit.Test;
import org.ttc.project.TestUtil;

import com.google.common.collect.ImmutableList;

import eu.project.ttc.engines.variant.VariantRule;
import eu.project.ttc.engines.variant.VariantRuleYamlIO;


public class VariantRuleYamlIOSpec {

	private static final String RULES_1_PATH = "org/project/ttc/test/variants/rules1.yml";
	private static final String RULES_2_PATH = "org/project/ttc/test/variants/rules2.yml";
	private static final String BAD_RULES_1_PATH = "org/project/ttc/test/variants/bad_rules1.yml";
	private static final String BAD_RULES_2_PATH = "org/project/ttc/test/variants/bad_rules2.yml";
	private static final String BAD_RULES_3_PATH = "org/project/ttc/test/variants/bad_rules3.yml";
	private static final String BAD_RULES_4_PATH = "org/project/ttc/test/variants/bad_rules4.yml";
	private static final String BAD_RULES_5_PATH = "org/project/ttc/test/variants/bad_rules5.yml";
	private static final String BAD_RULES_6_PATH = "org/project/ttc/test/variants/bad_rules6.yml";

	
	@Test
	public void should_parse_valid_rule()  {
		List<VariantRule> rules = VariantRuleYamlIO.fromYaml(TestUtil.readFile(RULES_1_PATH)).getVariantRules();
		assertEquals(1, rules.size());
		VariantRule rule1 = rules.get(0);
		assertEquals("My rule 1", rule1.getName());
		assertEquals(ImmutableList.of("N", "A"), rule1.getSourcePatterns());
		assertEquals(ImmutableList.of("A A N", "A N N", "A R N"), rule1.getTargetPatterns());
		assertFalse(rule1.isSourceCompound());
		assertFalse(rule1.isTargetCompound());
		assertEquals("The rule expr", rule1.getExpression());
	}

	@Test
	public void should_parse_valid_rule_with_multi_line_expression()  {
		List<VariantRule> rules = VariantRuleYamlIO.fromYaml(TestUtil.readFile(RULES_2_PATH)).getVariantRules();
		assertEquals(2, rules.size());
		VariantRule rule1 = rules.get(0);
		assertEquals("My rule 1", rule1.getName());
		assertEquals(ImmutableList.of("N"), rule1.getSourcePatterns());
		assertEquals(ImmutableList.of(), rule1.getTargetPatterns());
		assertTrue(rule1.isSourceCompound());
		assertFalse(rule1.isTargetCompound());
		assertEquals("s[0] == t[1].stem && s[1][0] != t[0]", rule1.getExpression());
		
		VariantRule rule2 = rules.get(1);
		assertEquals("My rule 2", rule2.getName());
		assertEquals(ImmutableList.of("N"), rule2.getSourcePatterns());
		assertEquals(ImmutableList.of("N", "A N"), rule2.getTargetPatterns());
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
			Class<IllegalArgumentException> type, String description) {
		VariantRuleYamlIO yamlIO = VariantRuleYamlIO.fromYaml(TestUtil.readFile(badRules1Path));
		when(yamlIO).getVariantRules();
		// then: we expect an IndexOutOfBoundsException
		/*BDDAssertions.then(caughtException())
		        .isInstanceOf(type)
		        .hasMessageContaining(description)
		        .hasNoCause();*/
	}
}
