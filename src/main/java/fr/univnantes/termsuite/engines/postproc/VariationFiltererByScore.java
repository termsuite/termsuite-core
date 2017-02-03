package fr.univnantes.termsuite.engines.postproc;

import java.util.stream.Collectors;

import org.slf4j.Logger;

import fr.univnantes.termsuite.engines.SimpleEngine;
import fr.univnantes.termsuite.framework.InjectLogger;
import fr.univnantes.termsuite.framework.Parameter;
import fr.univnantes.termsuite.framework.service.RelationService;
import fr.univnantes.termsuite.framework.service.TermService;
import fr.univnantes.termsuite.resources.PostProcessorOptions;

public class VariationFiltererByScore extends SimpleEngine {

	@InjectLogger Logger logger;

	@Parameter
	private PostProcessorOptions config;

	@Override
	public void execute() {
		terminology.variations()
				.filter(this::filterVariation)
				.collect(Collectors.toSet())
				.stream()
				.forEach(terminology::removeRelation);
		
		TermPostProcessor.logVariationsAndTerms(logger, terminology);
	}

	private boolean filterVariation(RelationService relation) {
		TermService variant = relation.getTo();
		TermService base = relation.getFrom();
		if(relation.getVariantScore() < config.getVariationScoreTh()) {
			watchVariationRemoval(variant, base, 
					"Removing variant <%s> because the variation score <%.2f> is under threshhold <%.2f>.",
					relation.getVariantScore(), this.config.getVariationScoreTh());
			watchVariationRemoval(base, variant, 
					"Removed as variant of term <%s> because the variation score <%.2f> is under threshhold <%.2f>.",
					relation.getVariantScore(), this.config.getVariationScoreTh());
			return true;
		} else if(relation.isExtension()) {
			if(relation.hasExtensionAffix()) {
				if(relation.getNormalizedAffixScore() < config.getAffixScoreTh()) {
					watchVariationRemoval(variant, base, 
							"Removing variant <%s> because the affix score <%.2f> is under threshhold <%.2f>.",
							relation.getNormalizedAffixScore(), this.config.getAffixScoreTh());
					watchVariationRemoval(base, variant, 
							"Removing as variant of term <%s> because the affix score  <%.2f> is under threshhold <%.2f>.",
							relation.getNormalizedAffixScore(), this.config.getAffixScoreTh());
					return true;
				}
			}
		}
		return  false;
	}

	private void watchVariationRemoval(TermService variant, TermService base, String msg, double score, double th) {
		if(history.isPresent() && history.get().isWatched(base.getTerm()))
			history.get().saveEvent(
					base.getGroupingKey(), 
					this.getClass(), 
					String.format(
							msg,
							variant,
							score,
							th)
					);
	}
}
