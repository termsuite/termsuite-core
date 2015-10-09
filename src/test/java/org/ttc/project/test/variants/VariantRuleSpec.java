package org.ttc.project.test.variants;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.ttc.project.Fixtures;

import eu.project.ttc.engines.variant.VariantRule;
import eu.project.ttc.engines.variant.VariantRuleBuilder;
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
