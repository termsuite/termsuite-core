package fr.univnantes.termsuite.engines;

import java.util.stream.Collectors;

import org.apache.commons.lang.mutable.MutableInt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.univnantes.termsuite.model.OccurrenceStore;
import fr.univnantes.termsuite.model.RelationType;
import fr.univnantes.termsuite.model.Terminology;
import fr.univnantes.termsuite.model.TermOccurrence;

public class TermMerger {

	private static final Logger LOGGER = LoggerFactory.getLogger(TermMerger.class);
	
	private static final Double MERGING_THRESHOLD = 2d;
	public void mergeTerms(Terminology termIndex) {
		mergeGraphicalVariants(termIndex);
	}

	private void mergeGraphicalVariants(Terminology termIndex) {
		final MutableInt nbMerged = new MutableInt(0);
		
		termIndex.getRelations(RelationType.GRAPHICAL)
			.filter(rel -> rel.getFrom().getFrequency() > rel.getTo().getFrequency())
			.filter(rel -> rel.getTo().getFrequency() > 0)
			.filter(rel -> (double)rel.getFrom().getFrequency() / rel.getTo().getFrequency() > MERGING_THRESHOLD)
			.collect(Collectors.toList())
			.forEach(rel -> {
				LOGGER.trace("Merging variant {} into variant {}", rel.getTo(), rel.getFrom());
				OccurrenceStore occStore = termIndex.getOccurrenceStore();
				for(TermOccurrence occ:occStore.getOccurrences(rel.getTo()))
					occ.setTerm(rel.getFrom());
				for(TermOccurrence o2:occStore.getOccurrences(rel.getTo()))
					occStore.addOccurrence(rel.getFrom(), o2.getSourceDocument().getUrl(), o2.getBegin(), o2.getEnd(), o2.getForm().getText());
				rel.getFrom().setFrequency(rel.getFrom().getFrequency() + rel.getTo().getFrequency());
				rel.getFrom().setFrequencyNorm(rel.getFrom().getFrequencyNorm() + rel.getTo().getFrequencyNorm());
				rel.getFrom().setGeneralFrequencyNorm(rel.getFrom().getGeneralFrequencyNorm() + rel.getTo().getGeneralFrequencyNorm());
				termIndex.removeTerm(rel.getTo());
				nbMerged.increment();
			});
		
		LOGGER.debug("Nb merges operated: {}", nbMerged);
	}
}
