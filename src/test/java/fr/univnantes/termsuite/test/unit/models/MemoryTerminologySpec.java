package fr.univnantes.termsuite.test.unit.models;

import org.junit.Before;
import org.junit.Test;

import fr.univnantes.termsuite.engines.gatherer.VariationType;
import fr.univnantes.termsuite.model.Lang;
import fr.univnantes.termsuite.model.RelationProperty;
import fr.univnantes.termsuite.model.RelationType;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.TermBuilder;
import fr.univnantes.termsuite.model.TermRelation;
import fr.univnantes.termsuite.model.occurrences.MemoryOccurrenceStore;
import fr.univnantes.termsuite.model.termino.MemoryTerminology;
import fr.univnantes.termsuite.test.TermSuiteAssertions;

public class MemoryTerminologySpec {

	MemoryTerminology terminology;
	
	private Term term1;
	private Term term2;
	private Term term3;
	
	@Before
	public void setTerms() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		terminology = new MemoryTerminology("test1", Lang.FR, new MemoryOccurrenceStore());
		term1 = TermBuilder.start(terminology).setGroupingKey("t1").createAndAddToIndex();
		term2 = TermBuilder.start(terminology).setGroupingKey("t2").createAndAddToIndex();
		term3 = TermBuilder.start(terminology).setGroupingKey("t3").createAndAddToIndex();
	}

	
	@Test
	public void testAddRelation() {
		TermRelation r = new TermRelation(RelationType.VARIATION, term1, term2);
		r.setProperty(RelationProperty.VARIATION_TYPE, VariationType.SYNTAGMATIC);
		terminology.addRelation(r);
		
		TermSuiteAssertions.assertThat(terminology)
			.hasNTerms(3)
			.containsVariation("t1", VariationType.SYNTAGMATIC, "t2")
			.hasNRelations(1)
			.hasNRelationsFrom(1, "t1")
			.hasNRelationsTo(1, "t2")
			;
	}
	
	@Test
	public void testAddRelationTwiceSetTwoRelations() {
		TermRelation termVariation1 = new TermRelation(RelationType.VARIATION, term1, term2);
		termVariation1.setProperty(RelationProperty.VARIATION_TYPE, VariationType.SYNTAGMATIC);
		TermRelation termVariation2 = new TermRelation(RelationType.VARIATION, term1, term2);
		termVariation2.setProperty(RelationProperty.VARIATION_TYPE, VariationType.SYNTAGMATIC);

		terminology.addRelation(termVariation1);
		terminology.addRelation(termVariation2);
		TermSuiteAssertions.assertThat(terminology)
			.hasNTerms(3)
			.containsVariation("t1", VariationType.SYNTAGMATIC, "t2")
			.hasNRelationsFrom(2, "t1")
			.hasNRelationsTo(2, "t2")
			.hasNRelations(2)
			;
	}

	
	@Test
	public void testRemoveTermWithRelations() {
		TermRelation termVariation = new TermRelation(RelationType.VARIATION, term1, term2);
		terminology.addRelation(termVariation);
		TermRelation termVariation2 = new TermRelation(RelationType.VARIATION, term2, term3);
		terminology.addRelation(termVariation2);
		TermRelation termVariation3 = new TermRelation(RelationType.VARIATION, term3, term1);
		terminology.addRelation(termVariation3);
		TermSuiteAssertions.assertThat(terminology)
			.hasNTerms(3)
			.hasNRelations(3);

		terminology.removeTerm(term2);
		TermSuiteAssertions.assertThat(terminology)
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
