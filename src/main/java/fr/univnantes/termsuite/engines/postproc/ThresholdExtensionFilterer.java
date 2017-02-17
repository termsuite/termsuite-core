package fr.univnantes.termsuite.engines.postproc;

import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;

import com.google.common.collect.Sets;

import fr.univnantes.termsuite.engines.SimpleEngine;
import fr.univnantes.termsuite.framework.InjectLogger;
import fr.univnantes.termsuite.framework.service.RelationService;
import fr.univnantes.termsuite.framework.service.TermService;

public class ThresholdExtensionFilterer extends SimpleEngine {

	@InjectLogger Logger logger;

	@Override
	public void execute() {
		Set<RelationService> remTargets = Sets.newHashSet();

		/*
		 *	Remove target term if it has no affix
		 */
		terminology
			.extensions()
			.filter(RelationService::hasExtensionAffix)
			.forEach(remTargets::add);

		
		/*
		 *	Remove target term if its frequency is 1 
		 */
		terminology
			.extensions()
			.filter(rel -> rel.getTo().getFrequency() == 1 && rel.getFrom().getFrequency() > 1)
			.forEach(remTargets::add);
		
		/*
		 * Actually remove terms
		 */
		remTargets.stream().forEach(rel -> {
			watchTermRemoval(rel.getTo(), String.format("Removing term because it is the poor extension of term %s ", rel.getFrom()));
		});
		
		
		Set<TermService> remSet = remTargets.stream().map(RelationService::getTo).collect(Collectors.toSet());
		logger.debug("Removing {} extension targets from term index", remSet.size());
		remSet.stream().forEach(terminology::removeTerm);
		
		TermPostProcessor.logVariationsAndTerms(logger, terminology);
	}

	private void watchTermRemoval(TermService term, String msg) {
		if(history.isPresent() && history.get().isTermWatched(term.getTerm()))
			history.get().saveEvent(
					term, 
					this.getClass(), 
					msg);
	}

}
