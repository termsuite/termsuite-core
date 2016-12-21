package fr.univnantes.termsuite.utils;

import fr.univnantes.termsuite.engines.cleaner.TerminoFilterOptions;
import fr.univnantes.termsuite.uima.TermSuitePipeline;

public class PipelineUtils {

	public static void filter(TermSuitePipeline pipeline, TerminoFilterOptions config) {
		pipeline.setKeepVariantsWhileCleaning(config.isKeepVariants());
		switch(config.getFilterType()) {
		case THRESHOLD:
			pipeline.aeThresholdCleaner(
					config.getFilterProperty(), 
					config.getThreshold().floatValue());
			break;
		case TOP_N:
			pipeline.aeTopNCleaner(
					config.getFilterProperty(), 
					config.getTopN());
			break;
		}

	}
}
