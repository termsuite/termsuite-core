package fr.univnantes.termsuite.engines.gatherer;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.commons.lang.mutable.MutableInt;
import org.slf4j.Logger;

import fr.univnantes.termsuite.engines.SimpleEngine;
import fr.univnantes.termsuite.framework.InjectLogger;
import fr.univnantes.termsuite.framework.service.RelationService;
import fr.univnantes.termsuite.model.OccurrenceStore;
import fr.univnantes.termsuite.model.Relation;
import fr.univnantes.termsuite.model.RelationProperty;
import fr.univnantes.termsuite.model.TermOccurrence;
import fr.univnantes.termsuite.utils.TermHistory;

public class TermMerger extends SimpleEngine {
	@InjectLogger Logger logger;

	@Inject
	OccurrenceStore occStore;
	
	private static final Double MERGING_THRESHOLD = 2d;

	private Optional<TermHistory> history = Optional.empty();

	public TermMerger setHistory(TermHistory history) {
		this.history = Optional.ofNullable(history);
		return this;
	}

	@Override
	public void execute() {
		final MutableInt nbMerged = new MutableInt(0);
		
		List<RelationService> relationsToMerge = terminology.variations()
			.filter(RelationService::isGraphical)
			.filter(RelationService::notPrefixation)
			.filter(RelationService::notDerivation)
			.filter(rel -> rel.getFrom().getFrequency() >= rel.getTo().getFrequency())
			.filter(rel -> rel.getTo().getFrequency() > 0)

			/*
			 * Merges graphical variations 
			 * 	- when frequencies are two or below
			 *  - or when frequencies are higher than 2 but the frequency 
			 *    ratio is bigger than MERGING_THRESHOLD
			 *  - or when graphical similarity is one (when patterns are different)
			 */
			.filter(rel -> 
					rel.getFrom().getFrequency() <= 2
					|| (double)rel.getFrom().getFrequency() / rel.getTo().getFrequency() > MERGING_THRESHOLD
					|| (rel.isPropertySet(RelationProperty.GRAPHICAL_SIMILARITY)
							&& rel.getGraphicalSimilarity() == 1d)
					)
			.collect(Collectors.toList());
		
		
		logger.debug("Merging {} relations", relationsToMerge.size());

		relationsToMerge.forEach(rel -> {
				if(logger.isTraceEnabled())
					logger.trace("Merging variant {} into variant {}", rel.getTo(), rel.getFrom());
				watch(rel.getRelation());
				
				Collection<TermOccurrence> occurrences = occStore.getOccurrences(rel.getTo().getTerm());
				for(TermOccurrence o2:occurrences)
					occStore.addOccurrence(rel.getFrom().getTerm(), o2.getSourceDocument().getUrl(), o2.getBegin(), o2.getEnd(), o2.getCoveredText());
				rel.getFrom().setFrequency(rel.getFrom().getFrequency() + rel.getTo().getFrequency());
				rel.getFrom().setFrequencyNorm(rel.getFrom().getFrequencyNorm() + rel.getTo().getFrequencyNorm());
				rel.getFrom().setGeneralFrequencyNorm(rel.getFrom().getGeneralFrequencyNorm() + rel.getTo().getGeneralFrequencyNorm());
				rel.getFrom().updateSpecificity();
				rel.getFrom().updateTfIdf();

				terminology.removeTerm(rel.getTo());
				
				nbMerged.increment();
			});
		
		if(logger.isDebugEnabled()) {
			logger.debug("Nb merges operated: {}", nbMerged);
			logger.debug("Number of terms in termino: {}, Number of variations in termino: {}", 
					terminology.termCount(),
					terminology.variations().count());
		}
	}

	private void watch(Relation rel) {
		if(history.isPresent()) {
			if(history.get().isTermWatched(rel.getFrom()) )
				history.get().saveEvent(rel.getFrom(), this.getClass(), String.format(
						"Merging %s into %s",
						rel.getTo(),
						rel.getFrom()
						));
			if(history.get().isTermWatched(rel.getTo()) )
				history.get().saveEvent(rel.getTo(), this.getClass(), String.format(
						"Merging %s into %s",
						rel.getTo(),
						rel.getFrom()
						));
		}
		
	}
}
