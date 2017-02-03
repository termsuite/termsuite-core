package fr.univnantes.termsuite.engines.cleaner;

import static java.util.stream.Collectors.toSet;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.slf4j.Logger;

import fr.univnantes.termsuite.engines.SimpleEngine;
import fr.univnantes.termsuite.engines.cleaner.TerminoFilterOptions.FilterType;
import fr.univnantes.termsuite.framework.InjectLogger;
import fr.univnantes.termsuite.framework.Parameter;
import fr.univnantes.termsuite.framework.service.RelationService;
import fr.univnantes.termsuite.framework.service.TermService;
import fr.univnantes.termsuite.framework.service.TerminologyService;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.TermProperty;
import fr.univnantes.termsuite.utils.TermHistory;

public class TerminologyCleaner extends SimpleEngine {
	@InjectLogger Logger logger;

	@Parameter
	private TerminoFilterOptions options;
	
	@Inject TerminologyService termino;
	
	public TerminologyCleaner setHistory(Optional<TermHistory> history) {
		this.history = history;
		return this;
	}
	
	public TerminologyCleaner setOptions(TerminoFilterOptions options) {
		this.options = options;
		return this;
	}
	
	@Override
	public void execute() {
		long termcount = 0;
		long relcount = 0;
		if(logger.isDebugEnabled()) {
			termcount = termino.termCount();
			relcount = termino.variations().count();
		}
		
		setTermFiltered(termino);
		cleanVariations(termino);
		cleanFilteredTerms(termino);
		
		if(logger.isDebugEnabled()) {
			long termcountAfter = termino.termCount();
			long relcountAfter = termino.variations().count();
			logger.debug("At end of filtering - Number of terms: {} (num of filtered terms: {})", termcountAfter, termcount-termcountAfter);
			logger.debug("At end of filtering - Number of variations: {} (num of filtered variations: {})", relcountAfter, relcount-relcountAfter);
		}
	}

	private void cleanFilteredTerms(TerminologyService termino) {
		Stream<TermService> terms = termino.terms();
		if(!options.isKeepVariants()) 
			terms = terms
				.filter(TermService::isFiltered);
		else {
			Set<TermService> keptTerms = termino.variations()
					.map(RelationService::getTo)
					.collect(toSet());

			terms = terms
				.filter(term -> term.isFiltered() && !keptTerms.contains(term));
		}
		terms
			.collect(toSet())
			.stream()
			.map(TermService::getTerm)
			.forEach(termino::removeTerm);
	}
	private void cleanVariations(TerminologyService termino) {
		termino.variations()
			.filter(variation -> variation.getRank() > options.getMaxVariantNum())
			.collect(toSet())
			.forEach(termino::removeRelation);

		termino.variations()
			.filter(variation -> variation.getFrom().isFiltered())
			.collect(toSet())
			.forEach(termino::removeRelation);
	}

	public void setTermFiltered(TerminologyService termino) {
		termino.terms().forEach(term -> {
			term.setProperty(TermProperty.FILTERED, true);
		});
		
		Stream<Term> terms = termino.terms().map(TermService::getTerm);
		if(options.getFilterType() == FilterType.TOP_N) 
			terms = terms
				.sorted(options.getFilterProperty().getComparator(true))
				.limit(options.getTopN());
		
		if(options.getFilterType() == FilterType.THRESHOLD) 
			terms = terms
				.filter(term -> term.getNumber(options.getFilterProperty()).doubleValue() >= options.getThreshold().doubleValue());

		terms.forEach(term -> {
			term.setProperty(TermProperty.FILTERED, false);
			watchMarkedAsFiltered(term);
		});
	}

	private void watchMarkedAsFiltered(Term term) {
		if(history.isPresent()) {
			if(history.get().isWatched(term)) {
				history.get().saveEvent(term.getGroupingKey(), this.getClass(), String.format(
						"Term %s is marked as filtered because %s=%s",
						term,
						options.getFilterProperty(),
						term.get(options.getFilterProperty())
					));
			}
		}
	}
}


