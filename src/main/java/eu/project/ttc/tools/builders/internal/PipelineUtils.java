package eu.project.ttc.tools.builders.internal;

import eu.project.ttc.tools.TermSuitePipeline;
import eu.project.ttc.tools.builders.TerminoFilterConfig;

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
					(int)config.getThreshhold());
			break;
		}

	}
}
