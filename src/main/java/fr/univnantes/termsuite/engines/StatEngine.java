package fr.univnantes.termsuite.engines;

import org.slf4j.Logger;

import fr.univnantes.termsuite.framework.InjectLogger;
import fr.univnantes.termsuite.framework.Parameter;

public class StatEngine extends SimpleEngine {
	@InjectLogger Logger logger;

	@Parameter
	private String statName;

	@Override
	public void execute() {
		logger.info("[Stat {}] Num terms in terminology: {}", statName, terminology.termCount());
		logger.info("[Stat {}] Num words in terminology: {}", statName, terminology.wordCount());
	}
}
