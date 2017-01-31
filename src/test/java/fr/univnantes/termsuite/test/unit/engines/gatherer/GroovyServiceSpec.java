package fr.univnantes.termsuite.test.unit.engines.gatherer;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import fr.univnantes.termsuite.engines.gatherer.GroovyHelper;
import fr.univnantes.termsuite.engines.gatherer.GroovyService;
import fr.univnantes.termsuite.engines.gatherer.VariantRule;
import fr.univnantes.termsuite.framework.TermSuiteFactory;
import fr.univnantes.termsuite.framework.service.TerminologyService;
import fr.univnantes.termsuite.index.Terminology;
import fr.univnantes.termsuite.model.IndexedCorpus;
import fr.univnantes.termsuite.model.Lang;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.test.unit.Fixtures;
import fr.univnantes.termsuite.test.unit.UnitTests;
import groovy.util.ResourceException;
import groovy.util.ScriptException;

public class GroovyServiceSpec {

	/* énergie éolien */
	private Term term1;

	/* radioélectrique */
	private Term term2;
	
	/* accès radioélectrique de recouvrement */
	private Term term3;
	
	private Terminology tIndex;
	private IndexedCorpus indexedCorpus;
	
	@Before
	public void initRules() {
		indexedCorpus = TermSuiteFactory.createIndexedCorpus(Lang.FR, "tata");
		tIndex = indexedCorpus.getTerminology();
		
		term1 = Fixtures.term1(tIndex);
		term2 = Fixtures.term2(tIndex);
		term3 = Fixtures.term3(tIndex);
	}
	
	@Test
	public void testEqualsLemmaIfNoPropertyGiven() {
		Assert.assertTrue(expr("s[0] == t[1]", term2, term3));
		Assert.assertFalse(expr("s[0] == t[1]", term1, term3));
		Assert.assertFalse(expr("s[0] == t[1]", term1, term2));
	}

	
	private boolean expr(String expr, Term t1, Term t2) {
		VariantRule rule = new VariantRule("Titi");
		rule.setExpression(expr);
		GroovyService groovyService = new GroovyService();
		TerminologyService terminoService = UnitTests.getTerminologyService(indexedCorpus);
		GroovyHelper helper = new GroovyHelper();
		try {
			UnitTests.setField(helper, "termino", terminoService);
			UnitTests.setField(groovyService, "groovyHelper", helper);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return groovyService.matchesRule(rule, t1, t2);
	}

	@Test
	public void testEqualsLemmaToGivenProperty() {
		Assert.assertTrue(expr("s[0] == t[1].lemma", term2, term3));
		Assert.assertFalse(expr("s[0] == t[1].lemma", term1, term3));
		Assert.assertFalse(expr("s[0] == t[1].lemma", term1, term2));
		Assert.assertFalse(expr("s[0] == t[1].stem", term2, term3));
		Assert.assertFalse(expr("s[0] == t[1].stem", term1, term3));
		Assert.assertFalse(expr("s[0] == t[1].stem", term1, term2));
	}

	@Test
	public void testEqualsLemmaToGivenString() {
		Assert.assertTrue(expr("s[0] == \"radioélectrique\"", term2, term3));
		Assert.assertFalse(expr("s[0] == \"radioélectrique\"", term1, term3));
		Assert.assertFalse(expr("s[0] == \"radioélectrique\"", term1, term2));
		Assert.assertTrue(expr("s[0] == \"énergie\"", term1, term3));
		Assert.assertTrue(expr("s[0] == \"énergie\"", term1, term2));
		Assert.assertTrue(expr("s[0] == \"accès\"", term3, term2));
	}

	@Test
	public void testMatch() throws IOException, ResourceException, ScriptException {
		Assert.assertTrue(expr("s[0].lemma == t[1].lemma", term2, term3));
		Assert.assertFalse(expr("s[0].lemma == t[1].lemma", term1, term3));
		Assert.assertFalse(expr("s[0].lemma == t[1].lemma", term1, term2));
	}

}
