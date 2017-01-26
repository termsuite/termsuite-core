package fr.univnantes.termsuite.engines.gatherer;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.commons.lang.mutable.MutableInt;
import org.slf4j.Logger;

import fr.univnantes.termsuite.SimpleEngine;
import fr.univnantes.termsuite.framework.InjectLogger;
import fr.univnantes.termsuite.model.OccurrenceStore;
import fr.univnantes.termsuite.model.RelationProperty;
import fr.univnantes.termsuite.model.TermOccurrence;
import fr.univnantes.termsuite.model.TermRelation;
import fr.univnantes.termsuite.utils.TermHistory;
import fr.univnantes.termsuite.utils.TermUtils;

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
		
		List<TermRelation> relationsToMerge = terminology.variations()
			.filter(rel -> rel.isPropertySet(RelationProperty.IS_GRAPHICAL)
							&& rel.isPropertySet(RelationProperty.IS_PREXATION)
							&& rel.isPropertySet(RelationProperty.IS_DERIVATION)
					)
			.filter(rel -> rel.getPropertyBooleanValue(RelationProperty.IS_GRAPHICAL)
								&& !rel.getPropertyBooleanValue(RelationProperty.IS_DERIVATION)
								&& !rel.getPropertyBooleanValue(RelationProperty.IS_PREXATION)
								)
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
							&& rel.getPropertyDoubleValue(RelationProperty.GRAPHICAL_SIMILARITY) == 1d)
					)
			.collect(Collectors.toList());
		
		
		logger.debug("Merging {} relations", relationsToMerge.size());

		relationsToMerge.forEach(rel -> {
				logger.trace("Merging variant {} into variant {}", rel.getTo(), rel.getFrom());
				watch(rel);
				
				Collection<TermOccurrence> occurrences = occStore.getOccurrences(rel.getTo());
				for(TermOccurrence o2:occurrences)
					occStore.addOccurrence(rel.getFrom(), o2.getSourceDocument().getUrl(), o2.getBegin(), o2.getEnd(), o2.getCoveredText());
				rel.getFrom().setFrequency(rel.getFrom().getFrequency() + rel.getTo().getFrequency());
				rel.getFrom().setFrequencyNorm(rel.getFrom().getFrequencyNorm() + rel.getTo().getFrequencyNorm());
				rel.getFrom().setGeneralFrequencyNorm(rel.getFrom().getGeneralFrequencyNorm() + rel.getTo().getGeneralFrequencyNorm());
				TermUtils.setSpecificity(rel.getFrom());
				TermUtils.setTfIdf(rel.getFrom());

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

	private void watch(TermRelation rel) {
		if(history.isPresent()) {
			if(history.get().isWatched(rel.getFrom()) )
				history.get().saveEvent(rel.getFrom().getGroupingKey(), this.getClass(), String.format(
						"Merging %s into %s",
						rel.getTo(),
						rel.getFrom()
						));
			if(history.get().isWatched(rel.getTo()) )
				history.get().saveEvent(rel.getTo().getGroupingKey(), this.getClass(), String.format(
						"Merging %s into %s",
						rel.getTo(),
						rel.getFrom()
						));
		}
		
	}
}
