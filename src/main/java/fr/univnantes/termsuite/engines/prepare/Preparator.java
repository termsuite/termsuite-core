package fr.univnantes.termsuite.engines.prepare;

import fr.univnantes.termsuite.framework.AggregateTerminologyEngine;
import fr.univnantes.termsuite.framework.TerminologyPipeline;

public class Preparator extends AggregateTerminologyEngine {

	@Override
	public void configurePipeline(TerminologyPipeline pipeline) {
		pipeline.pipeEngine(StopWordCleaner.class);
		pipeline.pipeEngine(SWTFlagSetter.class);
		pipeline.pipeEngine(SWTSizeSetter.class);
		pipeline.pipeEngine(TermSpecificityComputer.class);
		pipeline.pipeEngine(CorpusWidePropertiesSetter.class);
		pipeline.pipeEngine(ExtensionDetecter.class);
	}
}
