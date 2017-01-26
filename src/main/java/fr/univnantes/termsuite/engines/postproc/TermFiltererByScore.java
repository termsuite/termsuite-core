package fr.univnantes.termsuite.engines.postproc;

import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;

import fr.univnantes.termsuite.SimpleEngine;
import fr.univnantes.termsuite.framework.InjectLogger;
import fr.univnantes.termsuite.framework.Parameter;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.TermProperty;
import fr.univnantes.termsuite.resources.PostProcessorOptions;
import fr.univnantes.termsuite.utils.StringUtils;

public class TermFiltererByScore extends SimpleEngine {
	
	@InjectLogger Logger logger;
	
	@Parameter
	private PostProcessorOptions config;

	@Override
	public void execute() {
		Set<Term> remTerms = terminology.getTerms().stream()
			.filter(this::filterTermByThresholds)
			.collect(Collectors.toSet());
		remTerms
			.parallelStream()
			.forEach(terminology::removeTerm);
		
		TermPostProcessor.logVariationsAndTerms(logger, terminology);
	}

	private boolean filterTermByThresholds(Term term) {
		if(StringUtils.getOrthographicScore(term.getLemma()) < this.config.getOrthographicScoreTh()) {
			watchOrthographicFiltering(term);
			return true;
		}
		else if(term.getPropertyDoubleValue(TermProperty.INDEPENDANCE) < this.config.getTermIndependanceTh()) {
			watchIndependanceFiltering(term);
			return true;
		}
		return false;
	}

	public void watchIndependanceFiltering(Term term) {
		if(history.isPresent() && history.get().isWatched(term))
			history.get().saveEvent(
					term.getGroupingKey(), 
					this.getClass(), 
					String.format(
							"Removing term because independence score <%.2f> is under threshhold <%.2f>.",
							term.getPropertyDoubleValue(TermProperty.INDEPENDANCE),
							this.config.getTermIndependanceTh()));
	}

	public void watchOrthographicFiltering(Term term) {
		if(history.isPresent() && history.get().isWatched(term))
			history.get().saveEvent(
					term.getGroupingKey(), 
					this.getClass(), 
					String.format(
							"Removing term because orthographic score <%.2f> is under threshhold <%.2f>.",
							StringUtils.getOrthographicScore(term.getLemma()),
							this.config.getOrthographicScoreTh()));
	}
}
