package fr.univnantes.termsuite.test.unit.engines;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.startsWith;

import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import fr.univnantes.termsuite.engines.gatherer.SynonymicRule;
import fr.univnantes.termsuite.engines.gatherer.YamlRuleSetIO;

public class YamlRuleSetIOSynonymicSpec {

	private SynonymicRule synonymicRule ;

	@Before
	public void setupSynonymicRule() {
		synonymicRule = new SynonymicRule("Test Synonymic rule");
		synonymicRule.setSourcePatterns(Lists.newArrayList("N A"));
		synonymicRule.setTargetPatterns(Lists.newArrayList("N A"));
	}
	
	private void parseSynonymic(String expression) {
		synonymicRule.setExpression(expression);
		YamlRuleSetIO.parseSemanticExpression(synonymicRule);
	}
	
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
	@Test
	public void testTransformExpression1() {
		parseSynonymic("s[2]==t[2] && s[1]==t[1] && synonym(t[0],s[0])");

		assertThat(synonymicRule.getName()).isEqualTo("Test Synonymic rule");
		assertThat(synonymicRule.getExpression().trim()).isEqualTo("s[2]==t[2] && s[1]==t[1]");
		assertThat(synonymicRule.getSynonymSourceWordIndex()).isEqualTo(0);
		assertThat(synonymicRule.getEqIndices()).containsExactly(1,2);
		
	}

	@Test
	public void testTransformExpression2() {
		parseSynonymic("s[2]==t[2] && s[1]==t[1] && synonym(t[12],s[12])");

		assertThat(synonymicRule.getSynonymSourceWordIndex()).isEqualTo(12);
		assertThat(synonymicRule.getEqIndices()).containsExactly(1,2);
	}

	@Test
	public void testTransformExpression3() {
		parseSynonymic("s[2]==t[2] && synonym(t[2],s[2]) && s[1]==t[1] ");

		assertThat(synonymicRule.getExpression().trim()).isEqualTo("s[2]==t[2] && s[1]==t[1]");
		assertThat(synonymicRule.getEqIndices()).containsExactly(1,2);
	}
	
	@Test
	public void testTransformExpression4() {
		parseSynonymic("synonym(t[2],s[2]) && s[2]==t[2] && s[1]==t[1] ");
		assertThat(synonymicRule.getExpression().trim()).isEqualTo("s[2]==t[2] && s[1]==t[1]");
		assertThat(synonymicRule.getEqIndices()).containsExactly(1,2);
	}

	@Test
	public void testTransformExpression5() {
		parseSynonymic("synonym(t[2],s[2]) && s[1]==t[1] && s[2]==t[2] "
				+ "&& s[4]==t[4] && s[5]==t[5] && s[6]==t[6] "
				+ "&& s[8]==t[8] && s[9]==t[9] && s[10]==t[10] ");
		assertThat(synonymicRule.getEqIndices()).containsExactly(4,5,6);
	}

	@Test
	public void failOnNoTargetRef() {
		thrown.expect(IllegalStateException.class);
		thrown.expectMessage(startsWith("Expected extactly one source ref"));
		
		parseSynonymic("s[2]==t[2] && synonym(s[0],s[1])");
	}

	@Test
	public void failOnDifferentIndexesInSynonymExpression() {
		thrown.expect(IllegalStateException.class);
		thrown.expectMessage(startsWith("Synonymic expression parameters must have the same index"));
		
		parseSynonymic("synonym(s[0],t[1])");
	}

	
	@Test
	public void failOnNoSynonymExpression() {
		thrown.expect(IllegalStateException.class);
		thrown.expectMessage(startsWith("No synonym expression found for synonymic rule "));
		
		parseSynonymic("s[2]==t[2]");
	}

	@Test
	public void failOnSeveralSynonymExpressions() {
		thrown.expect(IllegalStateException.class);
		thrown.expectMessage(startsWith("Only one synonym expression allowed"));
		
		parseSynonymic("synonym(s[1],t[1]) && s[2]==t[2] && synonym(s[0],t[0])");
	}

	@Test
	public void failOnDisjonctionInSynonymExpressions() {
		thrown.expect(IllegalStateException.class);
		thrown.expectMessage(startsWith("No disjunction allowed in synonym expression"));
		
		parseSynonymic("synonym(s[1],t[1]) || s[2]==t[2]");
	}
}
