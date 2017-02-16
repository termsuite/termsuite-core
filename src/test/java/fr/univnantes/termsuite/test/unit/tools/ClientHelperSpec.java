package fr.univnantes.termsuite.test.unit.tools;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import fr.univnantes.termsuite.api.ExtractorOptions;
import fr.univnantes.termsuite.api.TermSuite;
import fr.univnantes.termsuite.engines.contextualizer.MutualInformation;
import fr.univnantes.termsuite.metrics.Jaccard;
import fr.univnantes.termsuite.model.Lang;
import fr.univnantes.termsuite.model.TermProperty;
import fr.univnantes.termsuite.tools.CommandLineClient;
import fr.univnantes.termsuite.tools.TermSuiteCliException;
import fr.univnantes.termsuite.tools.opt.ClientHelper;

public class ClientHelperSpec {
	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	private ClientHelper helper;
	private CommandLineClient client;
	private static final ExtractorOptions DEFAULTS = TermSuite.getDefaultExtractorConfig(Lang.EN);
	private Lang lang = Lang.EN;
	
	@Before
	public void setup() {
		client = new CommandLineClient("") {
			protected void run() throws Exception {}
			public void configureOpts() {
				clientHelper.declareExtractorOptions();
			}
		};
		helper = new ClientHelper(client);
	}

	@Test
	public void testParentOptionNotSetShouldFailContextualize() {
		try {
			client.launch(args(
					" --context-assoc-rate MutualInformation"
					));
			fail("Should have raised exception");
		} catch(Exception e) {
			assertThat(e)
				.isInstanceOf(TermSuiteCliException.class)
				.hasMessageContaining("Option --context-assoc-rate can be set only when option --contextualize is present");
		}
	}

	@Test
	public void testParentOptionNotSetShouldFailPreFilter() {
		try {
			client.launch(args(
					" --pre-filter-keep-variants"
					));
			fail("Should have raised exception");
		} catch(Exception e) {
			assertThat(e)
				.isInstanceOf(TermSuiteCliException.class)
				.hasMessageContaining("Option --pre-filter-keep-variants can be set only when option --pre-filter-property is present");
		}
	}


	@Test
	public void testCannotBeThAndTopNPostFilter() {
		try {
			client.launch(args(
					" --post-filter-property fnorm" 
					+ " --post-filter-top-n 10"
					+ " --post-filter-th 10"
					));
			fail("Should have raised exception");
		} catch(Exception e) {
			assertThat(e)
				.isInstanceOf(TermSuiteCliException.class)
				.hasMessageContaining("At most one option in --post-filter-th, --post-filter-top-n must be set");
		}
	}


	@Test
	public void testCannotBeThAndTopNPreFilter() {
		try {
			client.launch(args(
					" --pre-filter-property fnorm" 
					+ " --pre-filter-top-n 10"
					+ " --pre-filter-th 10"
					));
			fail("Should have raised exception");
		} catch(Exception e) {
			assertThat(e)
				.isInstanceOf(TermSuiteCliException.class)
				.hasMessageContaining("At most one option in --pre-filter-th, --pre-filter-top-n must be set");
		}
	}

	@Test
	public void testParentOptionNotSetShouldFailPostFilter() {
		try {
			client.launch(args(
					" --post-filter-keep-variants"
					));
			fail("Should have raised exception");
		} catch(Exception e) {
			assertThat(e)
				.isInstanceOf(TermSuiteCliException.class)
				.hasMessageContaining("Option --post-filter-keep-variants can be set only when option --post-filter-property is present");
		}
	}
	
	
	@Test
	public void testParentOptionNotSetShouldFailSemantic() {
		try {
			client.launch(args(
					" --semantic-similarity-th 15"
					));
			fail("Should have raised exception");
		} catch(Exception e) {
			assertThat(e)
				.isInstanceOf(TermSuiteCliException.class)
				.hasMessageContaining("Option --semantic-similarity-th can be set only when option --enable-semantic-gathering is present");
		}
	}


	@Test
	public void testExtractorOptionsWithPostProc() {
		client.launch(args("--postproc-independance-th 0.55"
				+ " --postproc-variation-score-th 0.77"
				));
		ExtractorOptions expected = defaults();
		expected.getPostProcessorConfig().setEnabled(true);
		expected.getPostProcessorConfig().setTermIndependanceTh(0.55);
		expected.getPostProcessorConfig().setVariationScoreTh(0.77);
		assertOptionsEquals(expected, helper.getExtractorOptions(lang));
	}

	@Test
	public void testExtractorOptionsWithPostProcDisable() {
		client.launch(args("--disable-post-processing"
				));
		ExtractorOptions expected = defaults();
		expected.getPostProcessorConfig().setEnabled(false);
		assertOptionsEquals(expected, helper.getExtractorOptions(lang));
	}

	@Test
	public void testCannotSetPostProcOptionWhenDisabled() {
		try {
			client.launch(args(
					" --disable-post-processing --postproc-variation-score-th 0.77"
					));
			fail("Should have raised exception");
		} catch(Exception e) {
			assertThat(e)
				.isInstanceOf(TermSuiteCliException.class)
				.hasMessageContaining("Option --postproc-variation-score-th cannot be set when option --disable-post-processing is present");
		}
	}
	
	
	@Test
	public void testExtractorOptionsWithPreFilterTh() {
		client.launch(args("--pre-filter-property fnorm"
				+ " --pre-filter-keep-variants"
				+ " --pre-filter-max-variants 5"
				+ " --pre-filter-th 12.34"
				));
		ExtractorOptions expected = defaults();
		expected.getPreFilterConfig().setProperty(TermProperty.FREQUENCY_NORM);
		expected.getPreFilterConfig().setEnabled(true);
		expected.getPreFilterConfig().keepOverTh(12.34);
		expected.getPreFilterConfig().keepVariants();
		expected.getPreFilterConfig().setMaxVariantNum(5);
		assertOptionsEquals(expected, helper.getExtractorOptions(lang));
	}

	@Test
	public void testExtractorOptionsWithPreFilterTopN() {
		client.launch(args("--pre-filter-property fnorm"
				+ " --pre-filter-max-variants 6"
				+ " --pre-filter-top-n 321"
				));
		ExtractorOptions expected = defaults();
		expected.getPreFilterConfig().setProperty(TermProperty.FREQUENCY_NORM);
		expected.getPreFilterConfig().setEnabled(true);
		expected.getPreFilterConfig().keepTopN(321);
		expected.getPreFilterConfig().setMaxVariantNum(6);
		assertOptionsEquals(expected, helper.getExtractorOptions(lang));
	}


	@Test
	public void testExtractorOptionsWithPostFilterTh() {
		client.launch(args("--post-filter-property fnorm"
				+ " --post-filter-keep-variants"
				+ " --post-filter-max-variants 5"
				+ " --post-filter-th 12.34"
				));
		ExtractorOptions expected = defaults();
		expected.getPostFilterConfig().setProperty(TermProperty.FREQUENCY_NORM);
		expected.getPostFilterConfig().setEnabled(true);
		expected.getPostFilterConfig().keepOverTh(12.34);
		expected.getPostFilterConfig().keepVariants();
		expected.getPostFilterConfig().setMaxVariantNum(5);
		assertOptionsEquals(expected, helper.getExtractorOptions(lang));
	}
	
	
	@Test
	public void testExtractorOptionsWithPostFilterTopN() {
		client.launch(args("--post-filter-property fnorm"
				+ " --post-filter-max-variants 6"
				+ " --post-filter-top-n 321"
				));
		ExtractorOptions expected = defaults();
		expected.getPostFilterConfig().setProperty(TermProperty.FREQUENCY_NORM);
		expected.getPostFilterConfig().setEnabled(true);
		expected.getPostFilterConfig().setKeepVariants(false);
		expected.getPostFilterConfig().keepTopN(321);
		expected.getPostFilterConfig().setMaxVariantNum(6);
		assertOptionsEquals(expected, helper.getExtractorOptions(lang));
	}

	@Test
	public void testExtractorOptionsWithContextualizer() {
		client.launch(args("--contextualize"
				+ " --context-coocc-th 123"
				+ " --context-scope 5"
				+ " --context-assoc-rate MutualInformation"
				));
		ExtractorOptions expected = defaults();
		expected.getContextualizerOptions().setEnabled(true);
		expected.getContextualizerOptions().setScope(5);
		expected.getContextualizerOptions().setMinimumCooccFrequencyThreshold(123);
		expected.getContextualizerOptions().setAssociationRate(MutualInformation.class);
		assertOptionsEquals(expected, helper.getExtractorOptions(lang));
	}

	@Test
	public void testExtractorOptionsWithGatherer() {
		client.launch(args(
				"--enable-semantic-gathering"
				+ " --disable-merging"
				+ " --semantic-similarity-th 0.33"
				+ " --graphical-similarity-th 0.44"
				+ " --nb-semantic-candidates 55"
				+ " --semantic-distance Jaccard"
				));
		ExtractorOptions expected = defaults();
		expected.getGathererConfig().setSemanticEnabled(true);
		expected.getGathererConfig().setMergerEnabled(false);
		expected.getGathererConfig().setSemanticSimilarityThreshold(0.33);
		expected.getGathererConfig().setSemanticSimilarityDistance(Jaccard.class);
		expected.getGathererConfig().setSemanticNbCandidates(55);
		expected.getGathererConfig().setGraphicalEnabled(true);
		expected.getGathererConfig().setGraphicalSimilarityThreshold(0.44);
		assertOptionsEquals(expected, helper.getExtractorOptions(lang));
	}


	@Test
	public void testExtractorOptionsWithMorphology() {
		client.launch(args(
				"--disable-prefix-splitting"
				+ " --disable-derivative-splitting"
				+ " --disable-native-splitting"
				));
		ExtractorOptions expected = defaults();
		expected.getMorphologicalConfig().setEnabled(true);
		expected.getMorphologicalConfig().setPrefixSplitterEnabled(false);
		expected.getMorphologicalConfig().setDerivativesDetecterEnabled(false);
		expected.getMorphologicalConfig().setNativeSplittingEnabled(false);
		assertOptionsEquals(expected, helper.getExtractorOptions(lang));
	}

	@Test
	public void testExtractorOptionsWithMorphologyDisabled() {
		client.launch(args(
				"--disable-morphology"
				));
		ExtractorOptions expected = defaults();
		expected.getMorphologicalConfig().setEnabled(false);
		assertOptionsEquals(expected, helper.getExtractorOptions(lang));
	}

	
	@Test
	public void testRankingShouldBeAscOrDesc() {
		try {
			client.launch(args(
					"--ranking-desc rule --ranking-asc titi "
					));
			fail("Should have raised exception");
		} catch(Exception e) {
			assertThat(e)
				.isInstanceOf(TermSuiteCliException.class)
				.hasMessageContaining("At most one option in --ranking-asc, --ranking-desc must be set");
		}
	}

	@Test
	public void testRankingPropertyShouldBeNumberProperty() {
		try {
			client.launch(args(
					"--ranking-asc rule"
					));
			helper.getExtractorOptions(lang);
			fail("Should have raised exception");
		} catch(Exception e) {
			assertThat(e)
				.isInstanceOf(TermSuiteCliException.class)
				.hasMessageContaining("\"rule\" is an invalid property for option --ranking-asc. Expected a number property.");
		}

	}

	@Test
	public void testExtractorOptionsWithRankingAsc() {
		client.launch(args(
				"--ranking-asc fnorm"
				));
		ExtractorOptions expected = defaults();
		expected.getRankingConfig().setDesc(false);		
		expected.getRankingConfig().setRankingProperty(TermProperty.FREQUENCY_NORM);
		assertOptionsEquals(expected, helper.getExtractorOptions(lang));
	}

	@Test
	public void testExtractorOptionsWithRankingDesc() {
		client.launch(args(
				"--ranking-desc sp"
				));
		ExtractorOptions expected = defaults();
		expected.getRankingConfig().setDesc(true);		
		expected.getRankingConfig().setRankingProperty(TermProperty.SPECIFICITY);
		assertOptionsEquals(expected, helper.getExtractorOptions(lang));
	}


	
	@Test
	public void testDefaultExtractorOptions() {
		client.launch(args(""));
		ExtractorOptions options  = helper.getExtractorOptions(lang);
		assertOptionsEquals(DEFAULTS, options);
	}

	private ExtractorOptions defaults() {
		return TermSuite.getDefaultExtractorConfig(Lang.EN);
	}
	
	private void assertOptionsEquals(ExtractorOptions expected, ExtractorOptions options) {
		assertEquals(expected.getContextualizerOptions(), options.getContextualizerOptions());
		assertEquals(expected.getPreFilterConfig(), options.getPreFilterConfig());
		assertEquals(expected.getMorphologicalConfig(), options.getMorphologicalConfig());
		assertEquals(expected.getPostProcessorConfig(), options.getPostProcessorConfig());
		assertEquals(expected.getPostFilterConfig(), options.getPostFilterConfig());
		assertEquals(expected.getGathererConfig(), options.getGathererConfig());
		assertEquals(expected.getRankingConfig(), options.getRankingConfig());
	}

	private String[] args(String additional) {
		return additional.split(" ");
	}
}
