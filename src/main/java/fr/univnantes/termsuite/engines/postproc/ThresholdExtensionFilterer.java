package fr.univnantes.termsuite.engines.postproc;

import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.google.common.collect.Sets;

import fr.univnantes.termsuite.framework.Execute;
import fr.univnantes.termsuite.framework.TerminologyEngine;
import fr.univnantes.termsuite.framework.TerminologyService;
import fr.univnantes.termsuite.model.RelationProperty;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.TermRelation;

public class ThresholdExtensionFilterer extends TerminologyEngine {

	@Execute
	public void filterExtensionsByThresholds(TerminologyService termino) {
		Predicate<? super TermRelation> isExtension = rel -> 
				rel.isPropertySet(RelationProperty.IS_EXTENSION) 
				&& rel.getPropertyBooleanValue(RelationProperty.IS_EXTENSION);
		
		Set<TermRelation> remTargets = Sets.newHashSet();

		
		/*
		 *	Remove target term if it has no affix
		 */
		termino
			.extensions()
			.filter(rel -> 
						rel.isPropertySet(RelationProperty.HAS_EXTENSION_AFFIX)
							&&	!rel.getPropertyBooleanValue(RelationProperty.HAS_EXTENSION_AFFIX))
			.forEach(remTargets::add);

		
		/*
		 *	Remove target term if its frequency is 1 
		 */
		termino
			.extensions()
			.filter(rel -> rel.getTo().getFrequency() == 1 && rel.getFrom().getFrequency() > 1)
			.forEach(remTargets::add);
		
		/*
		 * Actually remove terms
		 */
		remTargets.stream().forEach(rel -> {
			watchTermRemoval(rel.getTo(), String.format("Removing term because it is the poor extension of term %s ", rel.getFrom()));
		});
		
		
		Set<Term> remSet = remTargets.stream().map(TermRelation::getTo).collect(Collectors.toSet());
		getLogger().debug("Removing {} extension targets from term index", remSet.size());
		remSet.stream().forEach(termino::removeTerm);
		
		TermPostProcessor.logVariationsAndTerms(termino);
	}

	private void watchTermRemoval(Term term, String msg) {
		if(history.isPresent() && history.get().isWatched(term))
			history.get().saveEvent(
					term.getGroupingKey(), 
					this.getClass(), 
					msg);
	}

}
