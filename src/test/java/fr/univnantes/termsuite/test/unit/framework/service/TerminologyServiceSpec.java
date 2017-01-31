package fr.univnantes.termsuite.test.unit.framework.service;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;

import org.junit.Before;
import org.junit.Test;

import fr.univnantes.termsuite.engines.gatherer.VariationType;
import fr.univnantes.termsuite.framework.TermSuiteFactory;
import fr.univnantes.termsuite.framework.service.TerminologyService;
import fr.univnantes.termsuite.index.Terminology;
import fr.univnantes.termsuite.model.IndexedCorpus;
import fr.univnantes.termsuite.model.Lang;
import fr.univnantes.termsuite.model.RelationProperty;
import fr.univnantes.termsuite.model.RelationType;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.TermRelation;
import fr.univnantes.termsuite.test.TermSuiteAssertions;
import fr.univnantes.termsuite.test.unit.TermSuiteExtractors;
import fr.univnantes.termsuite.test.unit.UnitTests;

public class TerminologyServiceSpec {

	TerminologyService terminologyService;
	Terminology terminology;
	
	private Term term1;
	private Term term2;
	private Term term3;
	private Term term4;
	private Term term5;
	
	@Before
	public void setup() {
		IndexedCorpus indexedCorpus = TermSuiteFactory.createIndexedCorpus(Lang.FR, "");
		terminology = indexedCorpus.getTerminology();
		term1 = UnitTests.addTerm(terminology, "t1");
		term2 = UnitTests.addTerm(terminology, "t2");
		term3 = UnitTests.addTerm(terminology, "t3");
		term4 = UnitTests.addTerm(terminology, "t4");
		term5 = UnitTests.addTerm(terminology, "t5");
		terminologyService = UnitTests.getTerminologyService(indexedCorpus);
	}
	
	@Test
	public void testInboundRelations() {
		terminologyService.addRelation(new TermRelation(RelationType.VARIATION, term1, term2));
		terminologyService.addRelation(new TermRelation(RelationType.VARIATION, term1, term3));
		terminologyService.addRelation(new TermRelation(RelationType.VARIATION, term4, term3));
		
		assertThat(terminologyService.inboundRelations(term1).collect(toList()))
			.isEmpty();

		
		assertThat(terminologyService.inboundRelations(term3).collect(toList()))
			.hasSize(2)
			.extracting("from")
			.containsOnly(term1, term4)
			;
		assertThat(terminologyService.inboundRelations(term2).collect(toList()))
			.hasSize(1)
			.extracting("from").containsOnly(term1);

		assertThat(terminologyService.inboundRelations(term4).collect(toList()))
			.isEmpty();

		assertThat(terminologyService.inboundRelations(term5).collect(toList()))
			.isEmpty();
	}

	
	@Test
	public void testAddRelation() {
		TermRelation r = new TermRelation(RelationType.VARIATION, term1, term2);
		r.setProperty(RelationProperty.VARIATION_TYPE, VariationType.SYNTAGMATIC);
		terminologyService.addRelation(r);
		
		TermSuiteAssertions.assertThat(terminology)
			.hasNTerms(5)
			.containsVariation("t1", VariationType.SYNTAGMATIC, "t2")
			.hasNRelations(1)
			.hasNRelationsFrom(1, "t1")
			;
	}
	
	@Test
	public void testAddRelationTwiceSetTwoRelations() {
		TermRelation termVariation1 = new TermRelation(RelationType.VARIATION, term1, term2);
		termVariation1.setProperty(RelationProperty.VARIATION_TYPE, VariationType.SYNTAGMATIC);
		TermRelation termVariation2 = new TermRelation(RelationType.VARIATION, term1, term2);
		termVariation2.setProperty(RelationProperty.VARIATION_TYPE, VariationType.SYNTAGMATIC);

		UnitTests.addRelation(terminology, termVariation1);
		UnitTests.addRelation(terminology, termVariation2);
		TermSuiteAssertions.assertThat(terminology)
			.hasNTerms(5)
			.containsVariation("t1", VariationType.SYNTAGMATIC, "t2")
			.hasNRelationsFrom(2, "t1")
			.hasNRelations(2)
			;
	}

	public Collection<TermRelation> outRels(Term from) {
		return terminologyService.outboundRelations(from).collect(toList());
	}

	public Collection<TermRelation> inRels(Term to) {
		return terminologyService.inboundRelations(to).collect(toList());
	}

	@Test
	public void testAddTermVariation() {
		assertThat(outRels(this.term5)).hasSize(0);
		assertThat(inRels(this.term5)).hasSize(0);
		assertThat(outRels(this.term3)).hasSize(0);
		assertThat(inRels(this.term3)).hasSize(0);
		assertThat(outRels(this.term4)).hasSize(0);
		assertThat(inRels(this.term4)).hasSize(0);
		
		TermRelation rel1 = new TermRelation(RelationType.VARIATION, term5, term3);
		rel1.setProperty(RelationProperty.VARIATION_TYPE, VariationType.SYNTAGMATIC);
		rel1.setProperty(RelationProperty.VARIATION_RULE, "Tata");
		terminologyService.addRelation(rel1);
		
		assertThat(outRels(this.term5)).hasSize(1);
		assertThat(inRels(this.term5)).hasSize(0);
		assertThat(outRels(this.term3)).hasSize(0);
		assertThat(inRels(this.term3)).hasSize(1);
		assertThat(inRels(this.term3))
			.extracting(TermSuiteExtractors.RELATION_RULESTR)
			.containsExactly("Tata");
		
		TermRelation rel2 = new TermRelation(RelationType.VARIATION, term5, term4);
		rel2.setProperty(RelationProperty.VARIATION_TYPE, VariationType.SYNTAGMATIC);
		rel2.setProperty(RelationProperty.VARIATION_RULE, "Tata");
		terminologyService.addRelation(rel2);
		assertThat(outRels(this.term5)).hasSize(2);
		assertThat(inRels(this.term5)).hasSize(0);
		assertThat(outRels(this.term3)).hasSize(0);
		assertThat(inRels(this.term3)).hasSize(1);
		assertThat(outRels(this.term4)).hasSize(0);
		assertThat(inRels(this.term4)).hasSize(1);
		assertThat(outRels(this.term5))
			.extracting(TermSuiteExtractors.RELATION_RULESTR)
			.containsExactly("Tata","Tata");
		
		TermRelation rel3 = new TermRelation(RelationType.VARIATION, term5, term3);
		rel3.setProperty(RelationProperty.VARIATION_TYPE, VariationType.SYNTAGMATIC);
		rel3.setProperty(RelationProperty.VARIATION_RULE, "Tata");
		terminologyService.addRelation(rel3);
		assertThat(outRels(this.term5)).hasSize(3);
		assertThat(inRels(this.term5)).hasSize(0);
		assertThat(outRels(this.term3)).hasSize(0);
		assertThat(inRels(this.term3)).hasSize(2);
		assertThat(outRels(this.term4)).hasSize(0);
		assertThat(inRels(this.term4)).hasSize(1);
		assertThat(outRels(this.term5))
			.extracting(TermSuiteExtractors.RELATION_RULESTR)
			.containsExactly("Tata","Tata", "Tata");
	}
		

	
	@Test
	public void testRemoveTermWithRelations() {
		TermRelation termVariation = new TermRelation(RelationType.VARIATION, term1, term2);
		terminologyService.addRelation(termVariation);
		TermRelation termVariation2 = new TermRelation(RelationType.VARIATION, term2, term3);
		terminologyService.addRelation(termVariation2);
		TermRelation termVariation3 = new TermRelation(RelationType.VARIATION, term3, term1);
		terminologyService.addRelation(termVariation3);
		TermSuiteAssertions.assertThat(terminology)
			.hasNTerms(5)
			.hasNRelations(3);

		terminologyService.removeTerm(term2);
		TermSuiteAssertions.assertThat(terminology)
			.hasNTerms(4)
			.containsTerm("t1")
			.containsTerm("t3")
			.containsTerm("t4")
			.containsTerm("t5")
			.doesNotContainTerm("t2")
			.hasNRelations(1)
			.hasNRelationsFrom(0, "t1")
			.hasNRelationsFrom(1, "t3")
		;
	}

}
