package fr.univnantes.termsuite.test.func;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.stream.Collectors;

import org.assertj.core.api.iterable.Extractor;
import org.assertj.core.groups.Tuple;
import org.junit.Before;
import org.junit.Test;

import fr.univnantes.termsuite.api.TerminoExtractor;
import fr.univnantes.termsuite.engines.contextualizer.ContextualizerOptions;
import fr.univnantes.termsuite.framework.Relations;
import fr.univnantes.termsuite.model.Lang;
import fr.univnantes.termsuite.model.RelationProperty;
import fr.univnantes.termsuite.model.TermRelation;
import fr.univnantes.termsuite.model.Terminology;
import fr.univnantes.termsuite.utils.TermSuiteResourceManager;

public class SemanticGathererSpec {

	private static Terminology termindex;
	
	@Before
	public void setup() {
		TermSuiteResourceManager manager = TermSuiteResourceManager.getInstance();
		manager.clear();
	}

	public static void extract(Lang lang) {
		ContextualizerOptions opt = new ContextualizerOptions()
				.setMinimumCooccFrequencyThreshold(2);
		termindex = TerminoExtractor
			.fromTxtCorpus(lang, FunctionalTests.getCorpusWEPath(lang), "**/*.txt")
			.setTreeTaggerHome(FunctionalTests.getTaggerPath())
			.useContextualizer(opt)
			.enableSemanticAlignment()
			.execute();
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
		assertThat(relations)
			.extracting(SYNONYM_EXTRACTOR)
			.contains(
					tuple("na: courant continu", false, "na: courant constant"),
					tuple("na: coût global", false, "na: coût total"),
					tuple("npn: cadre de étude", true, "npn: cadre de projet"),
					tuple("na: batterie rechargeable", true, "na: batterie électrochimique")
			)
			;
		
		assertTrue("Expected number of relations between 3980 and 3990. Got: " + relations.size(),
				relations.size() > 3980 && relations.size() < 3990);

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
		
		assertTrue("Expected number of relations between 2470 and 2490. Got: "  +relations.size() ,
				relations.size() > 2470 && relations.size() < 2490);
		
	}

	
}
