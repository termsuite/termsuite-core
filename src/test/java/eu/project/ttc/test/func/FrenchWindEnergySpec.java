package eu.project.ttc.test.func;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static eu.project.ttc.test.func.FunctionalTests.termsByProperty;
import static eu.project.ttc.test.func.FunctionalTests.assertThat;

import org.junit.BeforeClass;
import org.junit.Test;

import eu.project.ttc.engines.cleaner.TermProperty;
import eu.project.ttc.engines.desc.Lang;
import eu.project.ttc.engines.desc.TermSuiteCollection;
import eu.project.ttc.models.TermIndex;
import eu.project.ttc.models.VariationType;
import eu.project.ttc.resources.MemoryTermIndexManager;
import eu.project.ttc.tools.TermSuitePipeline;

public class FrenchWindEnergySpec {
	
	
	static TermIndex termIndex;
	static Lang lang = Lang.FR;
	
	@BeforeClass
	public static void setup() {
		System.out.println(FunctionalTests.getCorpusWEPath(lang));
		System.out.println(FunctionalTests.getResourcePath());
		System.out.println(FunctionalTests.getTaggerPath());
		
		MemoryTermIndexManager.getInstance().clear();
		TermSuitePipeline pipeline = TermSuitePipeline.create(lang.getCode(), "file:")
			.setResourcePath(FunctionalTests.getResourcePath())
			.setCollection(TermSuiteCollection.TXT, FunctionalTests.getCorpusWEPath(lang), "UTF-8")
			.aeWordTokenizer()
			.setTreeTaggerHome(FunctionalTests.getTaggerPath())
			.aeTreeTagger()
			.aeUrlFilter()
			.aeStemmer()
			.aeRegexSpotter()
			.aeStopWordsFilter()
			.aeSpecificityComputer()
			.aeThresholdCleaner(TermProperty.FREQUENCY, 2)
			.aeCompostSplitter()
			.aePrefixSplitter()
			.aeSuffixDerivationDetector()
			.aeSyntacticVariantGatherer()
			.aeGraphicalVariantGatherer()
			.aeExtensionDetector()
			.aeScorer()
			.aeRanker(TermProperty.WR, true)
			.run();
			
		termIndex = pipeline.getTermIndex();
	}
	
	@Test
	public void testTop10ByFreq() {
		assertThat(termsByProperty(termIndex, TermProperty.FREQUENCY, true).subList(0, 10))
			.hasSize(10)
			.extracting("groupingKey")
			.containsExactly(
					"n: puissance", "a: éolien", "n: système", "n: énergie", "n: vitesse", 
					"n: vent", "n: réseau", "n: éolienne", "n: machine", "n: figure");
	}
	
	@Test
	public void testTop10ByWR() {
		assertThat(termsByProperty(termIndex, TermProperty.WR, true).subList(0, 10))
			.hasSize(10)
			.extracting("groupingKey")
			.containsExactly(
					"n: éolienne", "a: électrique", "n: convertisseur", 
					"n: générateur", "n: pale", "n: rotor", "n: optimisation", 
					"npn: vitesse de rotation", "n: réglage", 
					"npn: système de stockage"
					)
			;
	}


	@Test
	public void testTermVitesseDeRotation() {
		assertThat(termIndex.getTermByGroupingKey("npn: vitesse de rotation"))
			.hasFrequency(308)
			.hasNBases(0)
			.hasNVariationsOfType(4, VariationType.SYNTACTICAL)
			.getVariations()
			.extracting("variant.groupingKey", "info", "variant.frequency")
			.containsExactly(
					tuple("napn: vitesse angulaire de rotation", "S-I1-NPN-A", 2),
					tuple("napn: vitesse nominal de rotation", "S-I1-NPN-A", 2),
					tuple("npna: vitesse de rotation correspondant", "S-Ed-NPN-A", 3),
					tuple("npnpna: vitesse de rotation du champ magnétique", "S-Ed-NPN-PNA", 2)
				);
	}

	@Test
	public void testTermEolienne() {
		assertThat(termIndex.getTermByGroupingKey("n: éolienne"))
				.hasFrequency(1102)
				.hasGroupingKey("n: éolienne");
	}

	@Test
	public void testTop10ByRank() {
		assertThat(termsByProperty(termIndex, TermProperty.RANK, false).subList(0, 10))
			.hasSize(10)
			.extracting("groupingKey")
			.containsExactly(
					"n: éolienne", "a: électrique", "n: convertisseur", 
					"n: générateur", "n: pale", "n: rotor", "n: optimisation", 
					"npn: vitesse de rotation", "n: réglage", 
					"npn: système de stockage"
				)
			;
	}

}
