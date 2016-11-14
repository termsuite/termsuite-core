package eu.project.ttc.test.unit.engines;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.startsWith;

import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import eu.project.ttc.engines.variant.SynonymicRule;
import eu.project.ttc.engines.variant.VariantRule;

public class SynonymicRuleSpec {

	private VariantRule rawRule ;
	private SynonymicRule rule;

	@Before
	public void setup() {
		rawRule = Mockito.mock(VariantRule.class);
	}
	
	private void initExpression(String expression) {
		Mockito.when(rawRule.getName()).thenReturn("RuleName");
		Mockito.when(rawRule.getExpression()).thenReturn(expression);
		Mockito.when(rawRule.getSourcePatterns()).thenReturn(Lists.newArrayList("N A"));
		Mockito.when(rawRule.getTargetPatterns()).thenReturn(Lists.newArrayList("N A"));
		rule = SynonymicRule.parseSynonymicRule(rawRule);
	}
	
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
	@Test
	public void testTransformExpression1() {
		initExpression("s[2]==t[2] && s[1]==t[1] && synonym(t[0],s[0])");

		assertThat(rule.getName()).isEqualTo("RuleName");
		assertThat(rule.getExpression().trim()).isEqualTo("s[2]==t[2] && s[1]==t[1]");
		assertThat(rule.getSynonymSourceWordIndex()).isEqualTo(0);
		assertThat(rule.getEqIndices()).containsExactly(1,2);
		
	}

	@Test
	public void testTransformExpression2() {
		initExpression("s[2]==t[2] && s[1]==t[1] && synonym(t[12],s[12])");

		assertThat(rule.getSynonymSourceWordIndex()).isEqualTo(12);
		assertThat(rule.getEqIndices()).containsExactly(1,2);
	}

	@Test
	public void testTransformExpression3() {
		initExpression("s[2]==t[2] && synonym(t[2],s[2]) && s[1]==t[1] ");

		assertThat(rule.getExpression().trim()).isEqualTo("s[2]==t[2] && s[1]==t[1]");
		assertThat(rule.getEqIndices()).containsExactly(1,2);
	}
	
	@Test
	public void testTransformExpression4() {
		initExpression("synonym(t[2],s[2]) && s[2]==t[2] && s[1]==t[1] ");
		assertThat(rule.getExpression().trim()).isEqualTo("s[2]==t[2] && s[1]==t[1]");
		assertThat(rule.getEqIndices()).containsExactly(1,2);
	}

	@Test
	public void testTransformExpression5() {
		initExpression("synonym(t[2],s[2]) && s[1]==t[1] && s[2]==t[2] "
				+ "&& s[4]==t[4] && s[5]==t[5] && s[6]==t[6] "
				+ "&& s[8]==t[8] && s[9]==t[9] && s[10]==t[10] ");
		assertThat(rule.getEqIndices()).containsExactly(4,5,6);
	}

	@Test
	public void failOnNoTargetRef() {
		thrown.expect(IllegalStateException.class);
		thrown.expectMessage(startsWith("Expected extactly one source ref"));
		
		initExpression("s[2]==t[2] && synonym(s[0],s[1])");
	}

	@Test
	public void failOnDifferentIndexesInSynonymExpression() {
		thrown.expect(IllegalStateException.class);
		thrown.expectMessage(startsWith("Synonymic expression parameters must have the same index"));
		
		initExpression("synonym(s[0],t[1])");
	}

	
	@Test
	public void failOnNoSynonymExpression() {
		thrown.expect(IllegalStateException.class);
		thrown.expectMessage(startsWith("No synonym expression found for synonymic rule "));
		
		initExpression("s[2]==t[2]");
	}

	@Test
	public void failOnSeveralSynonymExpressions() {
		thrown.expect(IllegalStateException.class);
		thrown.expectMessage(startsWith("Only one synonym expression allowed"));
		
		initExpression("synonym(s[1],t[1]) && s[2]==t[2] && synonym(s[0],t[0])");
	}

	@Test
	public void failOnDisjonctionInSynonymExpressions() {
		thrown.expect(IllegalStateException.class);
		thrown.expectMessage(startsWith("No disjunction allowed in synonym expression"));
		
		initExpression("synonym(s[1],t[1]) || s[2]==t[2]");
	}

}
