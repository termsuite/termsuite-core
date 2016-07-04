package eu.project.ttc.test.func;

import static eu.project.ttc.test.func.FunctionalTests.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.util.List;

import org.assertj.core.api.iterable.Extractor;
import org.assertj.core.groups.Tuple;
import org.assertj.core.util.Lists;
import org.junit.Test;

import eu.project.ttc.engines.cleaner.TermProperty;
import eu.project.ttc.engines.desc.Lang;
import eu.project.ttc.engines.desc.TermSuiteCollection;
import eu.project.ttc.models.Term;
import eu.project.ttc.models.TermIndex;
import eu.project.ttc.models.VariationType;
import eu.project.ttc.resources.MemoryTermIndexManager;
import eu.project.ttc.tools.TermSuitePipeline;
import eu.project.ttc.tools.utils.ControlFilesGenerator;

public class WindEnergySpec {

	protected static TermIndex termIndex;
	protected static Lang lang;
	protected static List<String> syntacticRules = Lists.newArrayList();

	protected static void runPipeline() {
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
			.aeCompostSplitter()
			.aePrefixSplitter()
			.aeSuffixDerivationDetector()
			.aeSyntacticVariantGatherer()
			.aeGraphicalVariantGatherer()
			.aeExtensionDetector()
			.aeRanker(TermProperty.WR, true)
			.run();
			
		termIndex = pipeline.getTermIndex();
	}

	@Test
	public void weControlSyntacticRules() {
		for(String ruleName:syntacticRules) {
			assertThat(termIndex)
			.asTermVariations(VariationType.SYNTACTICAL, VariationType.MORPHOLOGICAL)
			.extracting("base.groupingKey", "variant.groupingKey", "variationType")
			.containsOnly(
					ControlFiles.syntacticVariationTuples(lang, "we", ruleName)
			);
		}
	}

	@Test
	public void weControlPrefixes() {
		assertThat(termIndex)
			.asTermVariations(VariationType.IS_PREFIX_OF)
			.extracting("base.groupingKey", "variant.groupingKey")
			.containsOnly(
					ControlFiles.prefixVariationTuples(lang, "we")
			);
	}

	@Test
	public void weControlDerivates() {
		assertThat(termIndex)
		.asTermVariations(VariationType.DERIVES_INTO)
		.extracting("info", "base.groupingKey", "variant.groupingKey")
		.containsOnly(
				ControlFiles.derivateVariationTuples(lang, "we")
		);

	}

	@Test
	public void weCompounds() {
		assertThat(termIndex)
			.asCompoundList()
			.extracting(new Extractor<Term, Tuple>() {
				@Override
				public Tuple extract(Term compoundTerm) {
					return tuple(
							compoundTerm.getWords().get(0).getWord().getCompoundType().getShortName(),
							compoundTerm.getGroupingKey(),
							ControlFilesGenerator.toCompoundString(compoundTerm)
							);
				}
			})
			.containsOnly(
					ControlFiles.compoundTuples(lang, "we")
			);

	}
}
