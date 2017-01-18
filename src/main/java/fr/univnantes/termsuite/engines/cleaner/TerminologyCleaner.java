package fr.univnantes.termsuite.engines.cleaner;

import static java.util.stream.Collectors.toSet;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.univnantes.termsuite.engines.cleaner.TerminoFilterOptions.FilterType;
import fr.univnantes.termsuite.framework.TerminologyEngine;
import fr.univnantes.termsuite.framework.service.TerminologyService;
import fr.univnantes.termsuite.model.RelationProperty;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.TermProperty;
import fr.univnantes.termsuite.model.TermRelation;
import fr.univnantes.termsuite.utils.TermHistory;

public class TerminologyCleaner extends TerminologyEngine {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(TerminologyCleaner.class);

	private TerminoFilterOptions options = new TerminoFilterOptions();
	
	private Optional<TermHistory> history = Optional.empty();
	
	@Inject TerminologyService termino;
	
	public TerminologyCleaner setHistory(Optional<TermHistory> history) {
		this.history = history;
		return this;
	}
	
	public TerminologyCleaner setOptions(TerminoFilterOptions options) {
		this.options = options;
		return this;
	}
	
	public void execute() {
		LOGGER.info("Cleaning terminology");
		
		long termcount = 0;
		long relcount = 0;
		if(LOGGER.isDebugEnabled()) {
			termcount = termino.termCount();
			relcount = termino.variations().count();
		}
		
		setTermFiltered(termino);
		cleanVariations(termino);
		cleanFilteredTerms(termino);
		
		if(LOGGER.isDebugEnabled()) {
			long termcountAfter = termino.termCount();
			long relcountAfter = termino.variations().count();
			LOGGER.debug("Finished terminology cleaning.");
			LOGGER.debug("At end of filtering - Number of terms: {} (num of filtered terms: {})", termcountAfter, termcount-termcountAfter);
			LOGGER.debug("At end of filtering - Number of variations: {} (num of filtered variations: {})", relcountAfter, relcount-relcountAfter);
		}
	}

	private void cleanFilteredTerms(TerminologyService termino) {
		Stream<Term> terms = termino.terms();
		if(!options.isKeepVariants()) 
			terms = terms
				.filter(term -> term.getPropertyBooleanValue(TermProperty.FILTERED));
		else {
			Set<Term> keptTerms = termino.variations()
					.map(TermRelation::getTo)
					.collect(toSet());

			terms = terms
				.filter(term -> term.getPropertyBooleanValue(TermProperty.FILTERED)
						&& !keptTerms.contains(term));
		}
		terms
			.collect(toSet())
			.forEach(termino::removeTerm);
	}
	private void cleanVariations(TerminologyService termino) {
		termino.variations()
			.filter(variation -> variation.getPropertyIntegerValue(RelationProperty.VARIATION_RANK) > options.getMaxVariantNum())
			.collect(toSet())
			.forEach(termino::removeRelation);

		termino.variations()
			.filter(variation -> variation.getFrom().getPropertyBooleanValue(TermProperty.FILTERED))
			.collect(toSet())
			.forEach(termino::removeRelation);
	}

	public void setTermFiltered(TerminologyService termino) {
		termino.terms().forEach(term -> {
			term.setProperty(TermProperty.FILTERED, true);
		});
		
		Stream<Term> terms = termino.terms();
		if(options.getFilterType() == FilterType.TOP_N) 
			terms = terms
				.sorted(options.getFilterProperty().getComparator(true))
				.limit(options.getTopN());
		
		if(options.getFilterType() == FilterType.THRESHOLD) 
			terms = terms
				.filter(term -> term.getPropertyNumberValue(options.getFilterProperty()).doubleValue() >= options.getThreshold().doubleValue());

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


