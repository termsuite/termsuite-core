package fr.univnantes.termsuite.test.unit;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

import fr.univnantes.termsuite.engines.postproc.VariationScorer;
import fr.univnantes.termsuite.framework.service.TerminologyService;
import fr.univnantes.termsuite.model.Lang;
import fr.univnantes.termsuite.model.RelationProperty;
import fr.univnantes.termsuite.model.RelationType;
import fr.univnantes.termsuite.model.TermBuilder;
import fr.univnantes.termsuite.model.TermRelation;
import fr.univnantes.termsuite.model.Terminology;
import fr.univnantes.termsuite.model.occurrences.MemoryOccurrenceStore;
import fr.univnantes.termsuite.model.termino.MemoryTerminology;

public class VariantScorerSpec {
	
	
	Terminology terminology;
	VariationScorer scorer;	
	@Before
	public void setup() {
		terminology = new MemoryTerminology("", Lang.FR, new MemoryOccurrenceStore(Lang.FR));
		populate();
		scorer = new VariationScorer();
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

	private void addVariation(String from, String to, boolean strict) {
		TermRelation relation = new TermRelation(RelationType.VARIATION, 
				terminology.getTermByGroupingKey(from), 
				terminology.getTermByGroupingKey(to));
		new TerminologyService(terminology)
				.addRelation(relation);
	}

	private void addTerm(String groupingKey, int freq) {
		new TerminologyService(terminology)
				.addTerm(TermBuilder.start()
						.setGroupingKey(groupingKey)
						.setFrequency(freq)
						.create());
	}
	
	@Test
	public void testVARIANT_FREQUENCY() {
		TerminologyService service = new TerminologyService(terminology);
		scorer.doVariationFrenquencies(service);
		TermRelation relation1 = service.variations("t7", "t4").findFirst().get();
		assertThat(relation1.getPropertyIntegerValue(RelationProperty.VARIANT_BAG_FREQUENCY))
			.isEqualTo(19);
		
		TermRelation relation2 = service.variations("t4", "t5").findFirst().get();
		assertThat(relation2.getPropertyIntegerValue(RelationProperty.VARIANT_BAG_FREQUENCY))
			.isEqualTo(2);
		
		TermRelation relation3 = service.variations("t4", "t6").findFirst().get();
		assertThat(relation3.getPropertyIntegerValue(RelationProperty.VARIANT_BAG_FREQUENCY))
			.isEqualTo(3);
	}
	
	@Test
	public void testVARIANT_FREQUENCY_handleCycles() {
		TerminologyService service = new TerminologyService(terminology);
		scorer.doVariationFrenquencies(service);
		TermRelation relation1 = service.variations("t3", "t2").findFirst().get();
		assertThat(relation1.getPropertyIntegerValue(RelationProperty.VARIANT_BAG_FREQUENCY))
			.isEqualTo(8); // t2.freq + t1.freq

		TermRelation relation2 = service.variations("t2", "t1").findFirst().get();
		assertThat(relation2.getPropertyIntegerValue(RelationProperty.VARIANT_BAG_FREQUENCY))
			.isEqualTo(14); // t1.freq + t3.freq


		TermRelation relation3 = service.variations("t1", "t3").findFirst().get();
		assertThat(relation3.getPropertyIntegerValue(RelationProperty.VARIANT_BAG_FREQUENCY))
			.isEqualTo(16); // t3.freq + t2.freq

	}

}
