package fr.univnantes.termsuite.test.unit.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.startsWith;

import java.util.Collection;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import com.google.common.collect.Lists;

import fr.univnantes.termsuite.api.Traverser;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.Terminology;
import fr.univnantes.termsuite.test.unit.TermFactory;

public class TraverserSpec {
	Terminology termIndex;
	Collection<Term> terms;
	Term term1, term2, term3;
	
	@Before
	public void setup() {
		termIndex = Mockito.mock(Terminology.class);
		term1 = TermFactory.termMock("t1", 1, 3, 0.8);
		term2 = TermFactory.termMock("t2", 2, 1, 0.8);
		term3 = TermFactory.termMock("t3", 3, 2, 1);
		terms = Lists.newArrayList(
				term1,
				term2,
				term3
			);
		Mockito.when(termIndex.getTerms()).thenReturn(terms);
	}

	@Test
	public void testByFrequency() {
		List<Term> list = Traverser.by("f desc").toList(termIndex);
		assertThat(list)
			.extracting("groupingKey")
			.containsExactly("t3", "t2", "t1");
		
		list = Traverser.by("f").toList(termIndex);
		assertThat(list)
			.extracting("groupingKey")
			.containsExactly("t1", "t2", "t3");

	}
	
	@Test
	public void testDefaultTraverser() {
		List<Term> list = Traverser.create().toList(termIndex);
		assertThat(list)
			.extracting("groupingKey")
			.containsExactly("t2", "t3", "t1");
	}


	@Test
	public void testByRank() {
		List<Term> list = Traverser.by("#").toList(termIndex);
		assertThat(list)
			.extracting("groupingKey")
			.containsExactly("t2", "t3", "t1");
		
		list = Traverser.by("# desc").toList(termIndex);
		assertThat(list)
			.extracting("groupingKey")
			.containsExactly("t1", "t3", "t2");
	}

	@Test
	public void testBySpecificity() {
		List<Term> list = Traverser.by("sp").toList(termIndex);
		assertThat(list)
			.extracting("groupingKey")
			.containsExactly("t2", "t1", "t3");
		
		list = Traverser.by("sp desc").toList(termIndex);
		assertThat(list)
			.extracting("groupingKey")
			.containsExactly("t3", "t2", "t1");
		
	}

	@Test
	public void testBySpecificityAndRank() {
		List<Term> list = Traverser.by("sp desc, #").toList(termIndex);
		assertThat(list)
			.extracting("groupingKey")
			.containsExactly("t3", "t2", "t1");
		
		list = Traverser.by("sp desc, # desc").toList(termIndex);
		assertThat(list)
			.extracting("groupingKey")
			.containsExactly("t3", "t1", "t2");
	}

	@Test
	public void testBySpecificityAndFrequency() {
		List<Term> list = Traverser.by("sp desc, f").toList(termIndex);
		assertThat(list)
			.extracting("groupingKey")
			.containsExactly("t3", "t1", "t2");
		
		list = Traverser.by("sp desc, f desc").toList(termIndex);
		assertThat(list)
			.extracting("groupingKey")
			.containsExactly("t3", "t2", "t1");
	}
	
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
	@Test
	public void testBadOrderingSyntax() {
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage(startsWith("Too many arguments"));
		Traverser.by("sp desc rete");
	}


	@Test
	public void testUnknownOrdering() {
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage(startsWith("Unknown direction"));
		Traverser.by("sp ierygfiuer");
	}

	@Test
	public void testEmptyStringNotAllowed1() {
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("Empty string not allowed");
		Traverser.by("");
	}
	
	@Test
	public void testEmptyStringNotAllowed2() {
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("Empty string not allowed");
		Traverser.by("sp desc,");
	}
	
	
	@Test
	public void testBadPropertyPropertyName() {
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage(startsWith("Bad term property name"));
		Traverser.by("spspspspsps desc");
	}
}
