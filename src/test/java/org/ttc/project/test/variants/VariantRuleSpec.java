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

import java.io.IOException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.ttc.project.Fixtures;

import eu.project.ttc.engines.variant.VariantRule;
import eu.project.ttc.engines.variant.VariantRuleBuilder;
import eu.project.ttc.engines.variant.VariantRuleIndex;
import eu.project.ttc.models.GroovyAdapter;
import eu.project.ttc.models.Term;
import groovy.util.ResourceException;
import groovy.util.ScriptException;

public class VariantRuleSpec {
	
	/* énergie éolien */
	private Term term1;

	/* radioélectrique */
	private Term term2;
	
	/* accès radioélectrique de recouvrement */
	private Term term3;
	
	@Before
	public void initRules() {
		term1 = Fixtures.term1();
		term2 = Fixtures.term2();
		term3 = Fixtures.term3();
	}

	private VariantRule getVariantRule(String expression) {
		return VariantRuleBuilder.start("My rule 1")
					.addSourcePattern("blah blah")
					.addTargetPattern("blah veor iv")
					.rule(expression)
					.setGroovyAdapter(new GroovyAdapter())
					.create();
	}
	
	
	@Test
	public void testMatch() throws IOException, ResourceException, ScriptException {
		Assert.assertTrue(getVariantRule("s[0].lemma == t[1].lemma").matchExpression(term2, term3));
		Assert.assertFalse(getVariantRule("s[0].lemma == t[1].lemma").matchExpression(term1, term3));
		Assert.assertFalse(getVariantRule("s[0].lemma == t[1].lemma").matchExpression(term1, term2));
	}
	
	@Test
	public void testInitIndex() {
		VariantRule rulePrefix = VariantRuleBuilder.start("withPrefixPredicate")
			.rule("s[0]==t[0] && prefix(t[1],s[1])")
			.create();
		Assert.assertEquals(VariantRuleIndex.PREFIX, rulePrefix.getIndex());
		
		VariantRule ruleDefault = VariantRuleBuilder.start("withNoSpecialPredicate")
				.rule("s[0]==t[0]")
				.create();
		Assert.assertEquals(VariantRuleIndex.DEFAULT, ruleDefault.getIndex());
			
		VariantRule ruleDeriv = VariantRuleBuilder.start("withDerivatePredicate")
				.rule("s[0]==t[0] && deriv(\"A N\",t[1],s[1])")
				.create();
		Assert.assertEquals(VariantRuleIndex.DERIVATION, ruleDeriv.getIndex());
	}
	
	
	@Test
	public void testEqualsLemmaIfNoPropertyGiven() {
		Assert.assertTrue(getVariantRule("s[0] == t[1]").matchExpression(term2, term3));
		Assert.assertFalse(getVariantRule("s[0] == t[1]").matchExpression(term1, term3));
		Assert.assertFalse(getVariantRule("s[0] == t[1]").matchExpression(term1, term2));
	}

	@Test
	public void testEqualsLemmaToGivenProperty() {
		Assert.assertTrue(getVariantRule("s[0] == t[1].lemma").matchExpression(term2, term3));
		Assert.assertFalse(getVariantRule("s[0] == t[1].lemma").matchExpression(term1, term3));
		Assert.assertFalse(getVariantRule("s[0] == t[1].lemma").matchExpression(term1, term2));
		Assert.assertFalse(getVariantRule("s[0] == t[1].stem").matchExpression(term2, term3));
		Assert.assertFalse(getVariantRule("s[0] == t[1].stem").matchExpression(term1, term3));
		Assert.assertFalse(getVariantRule("s[0] == t[1].stem").matchExpression(term1, term2));
	}

	@Test
	public void testEqualsLemmaToGivenString() {
		Assert.assertTrue(getVariantRule("s[0] == \"radioélectrique\"").matchExpression(term2, term3));
		Assert.assertFalse(getVariantRule("s[0] == \"radioélectrique\"").matchExpression(term1, term3));
		Assert.assertFalse(getVariantRule("s[0] == \"radioélectrique\"").matchExpression(term1, term2));
		Assert.assertTrue(getVariantRule("s[0] == \"énergie\"").matchExpression(term1, term3));
		Assert.assertTrue(getVariantRule("s[0] == \"énergie\"").matchExpression(term1, term2));
		Assert.assertTrue(getVariantRule("s[0] == \"accès\"").matchExpression(term3, term2));
	}
}
