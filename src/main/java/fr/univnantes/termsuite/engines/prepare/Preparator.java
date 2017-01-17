package fr.univnantes.termsuite.engines.prepare;

import fr.univnantes.termsuite.framework.AggregateTerminologyEngine;

public class Preparator extends AggregateTerminologyEngine {

	@Override
	public void configure() {
		pipe(StopWordCleaner.class);
		pipe(SWTFlagSetter.class);
		pipe(SWTSizeSetter.class);
		pipe(CorpusWidePropertiesSetter.class);
		pipe(TermSpecificityComputer.class);
		pipe(ExtensionDetecter.class);
	}
}
