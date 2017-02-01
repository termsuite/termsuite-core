package fr.univnantes.termsuite.test.func;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.stream.Collectors;

import org.assertj.core.api.iterable.Extractor;
import org.assertj.core.groups.Tuple;
import org.junit.Test;

import fr.univnantes.termsuite.api.ExtractorOptions;
import fr.univnantes.termsuite.api.TermSuite;
import fr.univnantes.termsuite.framework.Relations;
import fr.univnantes.termsuite.metrics.Cosine;
import fr.univnantes.termsuite.model.IndexedCorpus;
import fr.univnantes.termsuite.model.Lang;
import fr.univnantes.termsuite.model.RelationProperty;
import fr.univnantes.termsuite.model.TermRelation;
import fr.univnantes.termsuite.test.TermSuiteAssertions;
import fr.univnantes.termsuite.utils.TermHistory;

public class SemanticGathererSpec {

	private static IndexedCorpus corpus;

	public static void extract(Lang lang) {
		ExtractorOptions extractorOptions = TermSuite.getDefaultExtractorConfig(lang);
		extractorOptions.getGathererConfig().setSemanticEnabled(true);
		extractorOptions.getGathererConfig().setMergerEnabled(false);
		extractorOptions.getGathererConfig().setSemanticNbCandidates(5);
		extractorOptions.getGathererConfig().setSemanticSimilarityDistance(Cosine.class);
		extractorOptions.getGathererConfig().setSemanticSimilarityThreshold(0.3);
		extractorOptions.getContextualizerOptions().setEnabled(true);
		extractorOptions.getContextualizerOptions().setMinimumCooccFrequencyThreshold(2);
		extractorOptions.getPostProcessorConfig().setEnabled(false);
		
		corpus = TermSuite.preprocessor()
				.setTaggerPath(FunctionalTests.getTaggerPath())
				.toIndexedCorpus(FunctionalTests.getCorpusWE(lang), 5000000);
			
			TermHistory history = TermHistory.create("na: coût global");
			TermSuite.terminoExtractor()
						.setOptions(extractorOptions)
						.setHistory(history)
						.execute(corpus);
	}
	
	private static final Extractor<TermRelation, Tuple> SYNONYM_EXTRACTOR_WITH_TYPE = new Extractor<TermRelation, Tuple>() {
		@Override
		public Tuple extract(TermRelation input) {
			return new Tuple(
					input.getFrom().getGroupingKey(),
					input.getPropertyBooleanValue(RelationProperty.IS_DISTRIBUTIONAL) ? "distrib" : "-",
					input.getPropertyBooleanValue(RelationProperty.IS_DICO) ? "dico" : "-",
					input.getTo().getGroupingKey()
				);
		}
	};
	
	private static final Extractor<TermRelation, Tuple> SYNONYM_EXTRACTOR = new Extractor<TermRelation, Tuple>() {
		@Override
		public Tuple extract(TermRelation input) {
			return new Tuple(
					input.getFrom().getGroupingKey(),
					input.getTo().getGroupingKey()
				);
		}
	};


	@Test
	public void testVariationsFR() {
		extract(Lang.FR);
		List<TermRelation> relations = corpus.getTerminology()
				.getOutboundRelations().values().stream()
				.filter(Relations.IS_SEMANTIC)
				.filter(Relations.NOT_INFERED)
				.collect(Collectors.toList());
		
		
		TermSuiteAssertions.assertThat(corpus.getTerminology())
			.containsTerm("na: batterie rechargeable")
			.containsTerm("na: batterie électrochimique")
			.containsTerm("na: coût global")
			.containsTerm("npn: cadre de étude")
			.containsTerm("na: lieu éloigner")
			.containsTerm("na: emplacement éloigner")
			.containsTerm("na: coût total")
			.containsTerm("npn: cadre de projet")
			;
			
		assertThat(relations)
			.extracting(SYNONYM_EXTRACTOR)
			.contains(
					tuple("na: lieu éloigner", "na: emplacement éloigner"),
					tuple("na: coût global", "na: coût total"),
					tuple("npn: cadre de étude", "npn: cadre de projet"),
					tuple("na: batterie rechargeable", "na: batterie électrochimique")
			)
			;

		assertThat(relations)
			.extracting(SYNONYM_EXTRACTOR_WITH_TYPE)
			.contains(
					tuple("na: lieu éloigner", "distrib", "dico", "na: emplacement éloigner"),
					tuple("na: coût global", "distrib", "dico", "na: coût total"),
					tuple("npn: cadre de étude", "distrib", "-", "npn: cadre de projet"),
					tuple("na: batterie rechargeable", "distrib", "-", "na: batterie électrochimique")
			)
			;

		assertTrue("Expected number of relations between 10900 and 11000. Got: " + relations.size(),
				relations.size() > 10900 && relations.size() < 11000);
	}
	
	@Test
	public void testVariationsEN() {
		extract(Lang.EN);

		List<TermRelation> relations = corpus.getTerminology()
				.getOutboundRelations().values().stream()
				.filter(Relations.IS_SEMANTIC)
				.filter(Relations.NOT_INFERED)
			.collect(Collectors.toList());
		
		assertTrue("Expected number of relations between 13600 and 13700. Got: "  +relations.size() ,
				relations.size() > 13600 && relations.size() < 13800);

		assertThat(relations)
			.extracting(SYNONYM_EXTRACTOR)
			.contains(
					tuple("an: technical report", "an: technical paper"),
					tuple("an: potential hazard", "an: potential risk"),
					tuple("nn: max torque", "nn: min torque"),
					tuple("an: environmental effect", "an: environmental consequence")
			)
			;

		assertThat(relations)
			.extracting(SYNONYM_EXTRACTOR_WITH_TYPE)
			.contains(
					tuple("an: technical report", "distrib", "dico", "an: technical paper"),
					tuple("an: potential hazard", "distrib", "dico", "an: potential risk"),
					tuple("nn: max torque", "distrib", "-", "nn: min torque"),
					tuple("an: environmental effect", "distrib", "dico", "an: environmental consequence")
			)
			;

	}
}
