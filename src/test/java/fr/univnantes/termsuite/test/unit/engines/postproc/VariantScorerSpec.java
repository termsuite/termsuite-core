package fr.univnantes.termsuite.test.unit.engines.postproc;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

import fr.univnantes.termsuite.engines.postproc.VariationScorer;
import fr.univnantes.termsuite.framework.TermSuiteFactory;
import fr.univnantes.termsuite.framework.service.TerminologyService;
import fr.univnantes.termsuite.index.Terminology;
import fr.univnantes.termsuite.model.IndexedCorpus;
import fr.univnantes.termsuite.model.Lang;
import fr.univnantes.termsuite.model.RelationProperty;
import fr.univnantes.termsuite.model.RelationType;
import fr.univnantes.termsuite.model.TermBuilder;
import fr.univnantes.termsuite.model.Relation;
import fr.univnantes.termsuite.test.unit.UnitTests;

public class VariantScorerSpec {
	
	
	TerminologyService service;
	Terminology terminology;
	VariationScorer scorer;	
	IndexedCorpus indexedCorpus;
	
	@Before
	public void setup() {
		indexedCorpus = TermSuiteFactory.createIndexedCorpus(Lang.FR, "");
		service = UnitTests.getTerminologyService(indexedCorpus);
		terminology = indexedCorpus.getTerminology();
		populate();
		scorer = UnitTests.createSimpleEngine(indexedCorpus, VariationScorer.class);
	}

	public void populate() {
		addTerm("t1", 3);
		addTerm("t2", 5);
		addTerm("t3", 11);
		addTerm("t4", 17);
		addTerm("t5", 2);
		addTerm("t6", 3);
		addTerm("t7", 0);
		addVariation("t7", "t4", true);
		addVariation("t4", "t5", true);
		addVariation("t4", "t6", false);
		addVariation("t3", "t2", true);
		addVariation("t2", "t1", true);
		addVariation("t1", "t3", true);
	}

	private void addVariation(String from, String to, boolean isExtension) {
		Relation relation = new Relation(RelationType.VARIATION, 
				terminology.getTerms().get(from), 
				terminology.getTerms().get(to));
		relation.setProperty(RelationProperty.IS_EXTENSION, !isExtension);
		service
				.addRelation(relation);
	}

	private void addTerm(String groupingKey, int freq) {
		service.addTerm(new TermBuilder()
						.setGroupingKey(groupingKey)
						.setFrequency(freq)
						.create());
	}
	
	@Test
	public void testVARIANT_FREQUENCY() {
		scorer.doVariationFrenquencies();
		Relation relation1 = service.variations("t7", "t4").findFirst().get();
		assertThat(relation1.getPropertyIntegerValue(RelationProperty.VARIANT_BAG_FREQUENCY))
			.isEqualTo(19);
		
		Relation relation2 = service.variations("t4", "t5").findFirst().get();
		assertThat(relation2.getPropertyIntegerValue(RelationProperty.VARIANT_BAG_FREQUENCY))
			.isEqualTo(2);
		
		Relation relation3 = service.variations("t4", "t6").findFirst().get();
		assertThat(relation3.getPropertyIntegerValue(RelationProperty.VARIANT_BAG_FREQUENCY))
			.isEqualTo(3);
	}
	
	@Test
	public void testVARIANT_FREQUENCY_handleCycles() {
		service = UnitTests.getTerminologyService(indexedCorpus);
		scorer.doVariationFrenquencies();
		Relation relation1 = service.variations("t3", "t2").findFirst().get();
		assertThat(relation1.getPropertyIntegerValue(RelationProperty.VARIANT_BAG_FREQUENCY))
			.isEqualTo(8); // t2.freq + t1.freq

		Relation relation2 = service.variations("t2", "t1").findFirst().get();
		assertThat(relation2.getPropertyIntegerValue(RelationProperty.VARIANT_BAG_FREQUENCY))
			.isEqualTo(14); // t1.freq + t3.freq


		Relation relation3 = service.variations("t1", "t3").findFirst().get();
		assertThat(relation3.getPropertyIntegerValue(RelationProperty.VARIANT_BAG_FREQUENCY))
			.isEqualTo(16); // t3.freq + t2.freq

	}

}
