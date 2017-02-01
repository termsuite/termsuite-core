package fr.univnantes.termsuite.engines.postproc;

import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;

import fr.univnantes.termsuite.engines.SimpleEngine;
import fr.univnantes.termsuite.framework.InjectLogger;
import fr.univnantes.termsuite.framework.Parameter;
import fr.univnantes.termsuite.model.RelationProperty;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.Relation;
import fr.univnantes.termsuite.resources.PostProcessorOptions;

public class VariationFiltererByScore extends SimpleEngine {

	@InjectLogger Logger logger;

	@Parameter
	private PostProcessorOptions config;

	@Override
	public void execute() {
		Set<Relation> remRelations = terminology.variations()
				.filter(this::filterVariation)
				.collect(Collectors.toSet());
		remRelations
			.stream()
			.forEach(terminology::removeRelation);
		
		TermPostProcessor.logVariationsAndTerms(logger, terminology);
	}

	private boolean filterVariation(Relation relation) {
		Term variant = relation.getTo();
		Term base = relation.getFrom();
		if(relation.getPropertyDoubleValue(RelationProperty.VARIANT_SCORE) < config.getVariationScoreTh()) {
			watchVariationRemoval(variant, base, 
					"Removing variant <%s> because the variation score <%.2f> is under threshhold <%.2f>.",
					relation.getPropertyDoubleValue(RelationProperty.VARIANT_SCORE), this.config.getVariationScoreTh());
			watchVariationRemoval(base, variant, 
					"Removed as variant of term <%s> because the variation score <%.2f> is under threshhold <%.2f>.",
					relation.getPropertyDoubleValue(RelationProperty.VARIANT_SCORE), this.config.getVariationScoreTh());
			return true;
		} else if(relation.getPropertyBooleanValue(RelationProperty.IS_EXTENSION)) {
			if(relation.getPropertyBooleanValue(RelationProperty.HAS_EXTENSION_AFFIX)) {
				if(relation.getPropertyDoubleValue(RelationProperty.NORMALIZED_AFFIX_SCORE) < config.getAffixScoreTh()) {
					watchVariationRemoval(variant, base, 
							"Removing variant <%s> because the affix score <%.2f> is under threshhold <%.2f>.",
							relation.getPropertyDoubleValue(RelationProperty.NORMALIZED_AFFIX_SCORE), this.config.getAffixScoreTh());
					watchVariationRemoval(base, variant, 
							"Removing as variant of term <%s> because the affix score  <%.2f> is under threshhold <%.2f>.",
							relation.getPropertyDoubleValue(RelationProperty.NORMALIZED_AFFIX_SCORE), this.config.getAffixScoreTh());
					return true;
				}
			}
		}
		return  false;
	}

	private void watchVariationRemoval(Term variant, Term base, String msg, double score, double th) {
		if(history.isPresent() && history.get().isWatched(base))
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
