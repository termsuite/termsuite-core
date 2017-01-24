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
import fr.univnantes.termsuite.model.Lang;
import fr.univnantes.termsuite.model.RelationProperty;
import fr.univnantes.termsuite.model.TermRelation;
import fr.univnantes.termsuite.model.Terminology;

public class SemanticGathererSpec {

	private static Terminology termindex;

	public static void extract(Lang lang) {
		ExtractorOptions extractorOptions = TermSuite.getDefaultExtractorConfig(lang);
		extractorOptions.getGathererConfig().setSemanticEnabled(true);
		extractorOptions.getGathererConfig().setMergerEnabled(false);
		extractorOptions.getGathererConfig().setSemanticNbCandidates(5);
		extractorOptions.getGathererConfig().setSemanticSimilarityDistance(Cosine.class);
		extractorOptions.getGathererConfig().setSemanticSimilarityThreshold(0.3);
		extractorOptions.getContextualizerOptions().setEnabled(true);
		extractorOptions.getContextualizerOptions().setMinimumCooccFrequencyThreshold(2);
		
		
		termindex = TermSuite.preprocessor()
				.setTaggerPath(FunctionalTests.getTaggerPath())
				.toTerminology(FunctionalTests.getCorpusWE(lang), true);
			
			TermSuite.terminoExtractor()
						.setOptions(extractorOptions)
						.execute(termindex);
	}
	
	private static final Extractor<TermRelation, Tuple> SYNONYM_EXTRACTOR = new Extractor<TermRelation, Tuple>() {
		@Override
		public Tuple extract(TermRelation input) {
			return new Tuple(
					input.getFrom().getGroupingKey(),
					input.getPropertyBooleanValue(RelationProperty.IS_DISTRIBUTIONAL),
					input.getTo().getGroupingKey()
				);
		}
	};

	@Test
	public void testVariationsFR() {
		extract(Lang.FR);
		List<TermRelation> relations = termindex
				.getRelations()
				.filter(Relations.IS_SEMANTIC)
				.collect(Collectors.toList());
		assertTrue("Expected number of relations between 3800 and 3900. Got: " + relations.size(),
				relations.size() > 3800 && relations.size() < 3900);
		assertThat(relations)
			.extracting(SYNONYM_EXTRACTOR)
			.contains(
					tuple("na: lieu éloigner", false, "na: emplacement éloigner"),
					tuple("na: coût global", false, "na: coût total"),
					tuple("npn: cadre de étude", true, "npn: cadre de projet"),
					tuple("na: batterie rechargeable", true, "na: batterie électrochimique")
			)
			;
	}
	
	@Test
	public void testVariationsEN() {
		extract(Lang.EN);

		List<TermRelation> relations = termindex
				.getRelations()
				.filter(Relations.IS_SEMANTIC)
			.collect(Collectors.toList());
		assertThat(relations)
			.extracting(SYNONYM_EXTRACTOR)
			.contains(
					tuple("an: technical report", false, "an: technical paper"),
					tuple("an: potential hazard", false, "an: potential risk"),
					tuple("nn: max torque", true, "nn: min torque"),
					tuple("an: environmental effect", true, "an: environmental consequence")
			)
			;
		
		assertTrue("Expected number of relations between 2470 and 2500. Got: "  +relations.size() ,
				relations.size() > 2470 && relations.size() < 2500);
		
	}

	
}
