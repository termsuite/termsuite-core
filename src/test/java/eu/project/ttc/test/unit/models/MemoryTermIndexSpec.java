package eu.project.ttc.test.unit.models;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

import eu.project.ttc.engines.desc.Lang;
import eu.project.ttc.models.RelationType;
import eu.project.ttc.models.Term;
import eu.project.ttc.models.TermBuilder;
import eu.project.ttc.models.TermRelation;
import eu.project.ttc.models.index.MemoryTermIndex;
import eu.project.ttc.models.occstore.MemoryOccurrenceStore;
import eu.project.ttc.test.TermSuiteAssertions;

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
