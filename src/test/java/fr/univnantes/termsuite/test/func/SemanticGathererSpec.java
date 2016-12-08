package fr.univnantes.termsuite.test.func;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.util.List;
import java.util.stream.Collectors;

import org.assertj.core.api.iterable.Extractor;
import org.assertj.core.groups.Tuple;
import org.junit.BeforeClass;
import org.junit.Test;

import fr.univnantes.termsuite.api.TerminoExtractor;
import fr.univnantes.termsuite.model.Lang;
import fr.univnantes.termsuite.model.RelationProperty;
import fr.univnantes.termsuite.model.RelationType;
import fr.univnantes.termsuite.model.TermIndex;
import fr.univnantes.termsuite.model.TermRelation;
import fr.univnantes.termsuite.utils.TermSuiteResourceManager;

public class SemanticGathererSpec {

	private static TermIndex termindex;
	
	@BeforeClass
	public static void setup() {
		TermSuiteResourceManager manager = TermSuiteResourceManager.getInstance();
		manager.clear();
		termindex = TerminoExtractor
			.fromTxtCorpus(Lang.FR, FunctionalTests.getCorpusWEPath(Lang.FR), "**/*.txt")
			.setTreeTaggerHome(FunctionalTests.getTaggerPath())
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
	public void testVariations() {
		List<TermRelation> relations = termindex
				.getRelations(RelationType.SYNONYMIC)
				.collect(Collectors.toList());
		assertThat(relations)
			.extracting(SYNONYM_EXTRACTOR)
			.hasSize(2518)
			.contains(
					tuple("na: courant continu", false, "na: courant constant"),
					tuple("npn: cadre de étude", true, "npn: cadre de projet"),
					tuple("na: parc éolien", true, "na: ferme éolien"),
					tuple("na: puissance maximal", false, "na: puissance maximum")
			)
			;
	}
	
}
