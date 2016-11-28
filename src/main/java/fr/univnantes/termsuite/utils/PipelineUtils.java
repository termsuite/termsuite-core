package fr.univnantes.termsuite.utils;

import fr.univnantes.termsuite.api.TerminoFilterConfig;
import fr.univnantes.termsuite.uima.TermSuitePipeline;

public class PipelineUtils {

	public static void filter(TermSuitePipeline pipeline, TerminoFilterConfig config) {
		pipeline.setKeepVariantsWhileCleaning(config.isKeepVariants());
		switch(config.getFilterType()) {
		case THRESHHOLD:
			pipeline.aeThresholdCleaner(
					config.getFilterProperty(), 
					(float)config.getThreshhold());
			break;
		case TOP_N:
			pipeline.aeTopNCleaner(
					config.getFilterProperty(), 
					(int)config.getTopN());
			break;
		}

	}
}
