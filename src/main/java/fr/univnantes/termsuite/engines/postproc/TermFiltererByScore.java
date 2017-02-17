package fr.univnantes.termsuite.engines.postproc;

import java.util.stream.Collectors;

import org.slf4j.Logger;

import fr.univnantes.termsuite.engines.SimpleEngine;
import fr.univnantes.termsuite.framework.InjectLogger;
import fr.univnantes.termsuite.framework.Parameter;
import fr.univnantes.termsuite.framework.service.TermService;
import fr.univnantes.termsuite.resources.PostProcessorOptions;
import fr.univnantes.termsuite.utils.StringUtils;

public class TermFiltererByScore extends SimpleEngine {
	
	@InjectLogger Logger logger;
	
	@Parameter
	private PostProcessorOptions config;

	@Override
	public void execute() {
		terminology.terms()
			.filter(this::filterTermByThresholds)
			.collect(Collectors.toSet())
			.parallelStream()
			.forEach(terminology::removeTerm);
		
		TermPostProcessor.logVariationsAndTerms(logger, terminology);
	}

	private boolean filterTermByThresholds(TermService term) {
		if(StringUtils.getOrthographicScore(term.getLemma()) < this.config.getOrthographicScoreTh()) {
			watchOrthographicFiltering(term);
			return true;
		}
		else if(term.getIndependance() < this.config.getTermIndependanceTh()) {
			watchIndependanceFiltering(term);
			return true;
		}
		return false;
	}

	public void watchIndependanceFiltering(TermService term) {
		if(history.isPresent() && history.get().isTermWatched(term.getTerm()))
			history.get().saveEvent(
					term, 
					this.getClass(), 
					String.format(
							"Removing term because independence score <%.2f> is under threshhold <%.2f>.",
							term.getIndependance(),
							this.config.getTermIndependanceTh()));
	}

	public void watchOrthographicFiltering(TermService term) {
		if(history.isPresent() && history.get().isTermWatched(term.getTerm()))
			history.get().saveEvent(
					term, 
					this.getClass(), 
					String.format(
							"Removing term because orthographic score <%.2f> is under threshhold <%.2f>.",
							StringUtils.getOrthographicScore(term.getLemma()),
							this.config.getOrthographicScoreTh()));
	}
}
