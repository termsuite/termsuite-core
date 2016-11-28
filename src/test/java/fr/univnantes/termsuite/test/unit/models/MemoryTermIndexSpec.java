package fr.univnantes.termsuite.test.unit.models;

import org.junit.Before;
import org.junit.Test;

import fr.univnantes.termsuite.model.Lang;
import fr.univnantes.termsuite.model.RelationType;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.TermBuilder;
import fr.univnantes.termsuite.model.TermRelation;
import fr.univnantes.termsuite.model.occurrences.MemoryOccurrenceStore;
import fr.univnantes.termsuite.model.termino.MemoryTermIndex;
import fr.univnantes.termsuite.test.TermSuiteAssertions;

public class MemoryTermIndexSpec {

	MemoryTermIndex termIndex;
	
	private Term term1;
	private Term term2;
	private Term term3;
	
	@Before
	public void setTerms() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		termIndex = new MemoryTermIndex("test1", Lang.FR, new MemoryOccurrenceStore());
		term1 = TermBuilder.start(termIndex).setGroupingKey("t1").createAndAddToIndex();
		term2 = TermBuilder.start(termIndex).setGroupingKey("t2").createAndAddToIndex();
		term3 = TermBuilder.start(termIndex).setGroupingKey("t3").createAndAddToIndex();
	}

	
	@Test
	public void testAddRelation() {
		termIndex.addRelation(new TermRelation(RelationType.SYNTACTICAL, term1, term2));
		TermSuiteAssertions.assertThat(termIndex)
			.hasNTerms(3)
			.containsRelation("t1", RelationType.SYNTACTICAL, "t2")
			.hasNRelations(1)
			.hasNRelationsFrom(1, "t1")
			.hasNRelationsTo(1, "t2")
			;
	}
	
	@Test
	public void testAddRelationTwiceSetTwoRelations() {
		termIndex.addRelation(new TermRelation(RelationType.SYNTACTICAL, term1, term2));
		termIndex.addRelation(new TermRelation(RelationType.SYNTACTICAL, term1, term2));
		TermSuiteAssertions.assertThat(termIndex)
			.hasNTerms(3)
			.containsRelation("t1", RelationType.SYNTACTICAL, "t2")
			.hasNRelationsFrom(2, "t1")
			.hasNRelationsTo(2, "t2")
			.hasNRelations(2)
			;
	}

	
	@Test
	public void testRemoveTermWithRelations() {
		termIndex.addRelation(new TermRelation(RelationType.SYNTACTICAL, term1, term2));
		termIndex.addRelation(new TermRelation(RelationType.SYNTACTICAL, term2, term3));
		termIndex.addRelation(new TermRelation(RelationType.SYNTACTICAL, term3, term1));
		TermSuiteAssertions.assertThat(termIndex)
			.hasNTerms(3)
			.hasNRelations(3);

		termIndex.removeTerm(term2);
		TermSuiteAssertions.assertThat(termIndex)
			.hasNTerms(2)
			.containsTerm("t1")
			.containsTerm("t3")
			.doesNotContainTerm("t2")
			.hasNRelations(1)
			.hasNRelationsFrom(0, "t1")
			.hasNRelationsFrom(1, "t3")
			.hasNRelationsTo(1, "t1")
			.hasNRelationsTo(0, "t3")
		;
	}


}
