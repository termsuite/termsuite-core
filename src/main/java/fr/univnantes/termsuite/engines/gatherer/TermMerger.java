package fr.univnantes.termsuite.engines.gatherer;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang.mutable.MutableInt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.univnantes.termsuite.framework.TerminologyEngine;
import fr.univnantes.termsuite.framework.TerminologyService;
import fr.univnantes.termsuite.model.OccurrenceStore;
import fr.univnantes.termsuite.model.RelationProperty;
import fr.univnantes.termsuite.model.TermOccurrence;
import fr.univnantes.termsuite.model.TermRelation;
import fr.univnantes.termsuite.utils.TermHistory;
import fr.univnantes.termsuite.utils.TermUtils;

public class TermMerger extends TerminologyEngine {

	private static final Logger LOGGER = LoggerFactory.getLogger(TermMerger.class);
	private static final Double MERGING_THRESHOLD = 2d;

	private Optional<TermHistory> history = Optional.empty();

	
	public TermMerger setHistory(TermHistory history) {
		this.history = Optional.ofNullable(history);
		return this;
	}
	
	public void mergeTerms(TerminologyService termino) {
		mergeGraphicalVariants(termino);
	}

	private void mergeGraphicalVariants(TerminologyService termino) {
		LOGGER.info("Merging graphical variations");
		final MutableInt nbMerged = new MutableInt(0);
		
		List<TermRelation> relationsToMerge = termino.variations()
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
		
		
		LOGGER.debug("Merging {} relations", relationsToMerge.size());

		relationsToMerge.forEach(rel -> {
				LOGGER.trace("Merging variant {} into variant {}", rel.getTo(), rel.getFrom());
				watch(rel);
				
				OccurrenceStore occStore = termino.getTerminology().getOccurrenceStore();
				Collection<TermOccurrence> occurrences = occStore.getOccurrences(rel.getTo());
				for(TermOccurrence o2:occurrences)
					occStore.addOccurrence(rel.getFrom(), o2.getSourceDocument().getUrl(), o2.getBegin(), o2.getEnd(), o2.getCoveredText());
				rel.getFrom().setFrequency(rel.getFrom().getFrequency() + rel.getTo().getFrequency());
				rel.getFrom().setFrequencyNorm(rel.getFrom().getFrequencyNorm() + rel.getTo().getFrequencyNorm());
				rel.getFrom().setGeneralFrequencyNorm(rel.getFrom().getGeneralFrequencyNorm() + rel.getTo().getGeneralFrequencyNorm());
				TermUtils.setSpecificity(rel.getFrom());
				TermUtils.setTfIdf(rel.getFrom());

				termino.removeTerm(rel.getTo());
				
				nbMerged.increment();
			});
		
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug("Nb merges operated: {}", nbMerged);
			LOGGER.debug("Number of terms in termino: {}, Number of variations in termino: {}", 
					termino.termCount(),
					termino.variations().count());
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
