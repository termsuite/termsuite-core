package fr.univnantes.termsuite.test.func.api;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.univnantes.termsuite.api.ExtractorOptions;
import fr.univnantes.termsuite.api.TermSuite;
import fr.univnantes.termsuite.engines.gatherer.VariationType;
import fr.univnantes.termsuite.framework.PipelineStats;
import fr.univnantes.termsuite.framework.service.TermService;
import fr.univnantes.termsuite.framework.service.TerminologyService;
import fr.univnantes.termsuite.model.IndexedCorpus;
import fr.univnantes.termsuite.model.Lang;
import fr.univnantes.termsuite.model.RelationType;
import fr.univnantes.termsuite.model.TermProperty;
import fr.univnantes.termsuite.test.func.FunctionalTests;

public class ItalianLauncherSpec {
	private static final Logger LOGGER = LoggerFactory.getLogger(ItalianLauncherSpec.class);
	
	private IndexedCorpus corpus;
	
	@Before
	public void setup() {
		corpus = TermSuite.preprocessor()
				.setTaggerPath(FunctionalTests.getTaggerPath())
				.toIndexedCorpus(FunctionalTests.getCorpusComputersci(Lang.IT), 5000000);
		
		ExtractorOptions extractorOptions = TermSuite.getDefaultExtractorConfig(Lang.IT);
		PipelineStats stats = TermSuite.terminoExtractor()
				.setOptions(extractorOptions)
				.execute(corpus);
		LOGGER.info("Pipeline finished in {} ms", stats.getTotalTime());
		LOGGER.info("Total indexing time: {} ms", stats.getIndexingTime());
		LOGGER.info("Engine Stats: \n{}", stats.getEngineStats());
	}

	@Test
	public void testTermino() {
		TerminologyService termino = TermSuite.getTerminologyService(corpus);
		List<TermService> top10 = termino
			.terms(TermProperty.SPECIFICITY.getComparator(true))
			.limit(10)
			.collect(toList());
		
		assertThat(top10)
			.extracting("term.groupingKey")
			.contains("na: software libero")
			.contains("npna: movimento del software libero")
			.contains("n: source")
			.contains("n: ebook")
			.contains("n: wikisource")
			;
		

		assertThat(termino.relations(RelationType.DERIVES_INTO).collect(toList()))
			.extracting("from.groupingKey", "to.groupingKey")
			.isNotEmpty()
			.contains(tuple("n: possibilità", "a: possibile"))
			;

		assertThat(termino.relations(RelationType.IS_PREFIX_OF).collect(toList()))
			.extracting("from.groupingKey", "to.groupingKey")
			.isNotEmpty()
			.contains(tuple("a: immorale", "a: morale"))
			;
		
		assertThat(termino.variations(VariationType.DERIVATION).collect(toList()))
			.isEmpty();

		assertThat(termino.variations(VariationType.MORPHOLOGICAL).collect(toList()))
			.isEmpty();

		assertThat(termino.variations(VariationType.PREFIXATION).collect(toList()))
			.isEmpty();

		assertThat(termino.variations(VariationType.GRAPHICAL).collect(toList()))
			.isEmpty();

		assertThat(termino.variations(VariationType.SYNTAGMATIC).collect(toList()))
			.extracting("from.groupingKey", "to.groupingKey")
			.isNotEmpty()
			.contains(tuple(
					"na: software libero", 
					"nra: software non libero"))
			;
	}
}
