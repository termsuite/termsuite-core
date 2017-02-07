package fr.univnantes.termsuite.engines.gatherer;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;

import com.google.common.base.Stopwatch;

import fr.univnantes.termsuite.engines.SimpleEngine;
import fr.univnantes.termsuite.framework.InjectLogger;
import fr.univnantes.termsuite.framework.service.RelationService;
import fr.univnantes.termsuite.framework.service.TermService;
import fr.univnantes.termsuite.model.RelationProperty;
import fr.univnantes.termsuite.model.RelationType;
import fr.univnantes.termsuite.utils.TermHistory;
import fr.univnantes.termsuite.utils.VariationUtils;

/**
 * 
 * Infers small-size terms variations on bigger-size extension terms.
 * 
 * When we have:
 * 	 1. horizontal-axis --M--> horizontal axis
 * 	 2. horizontal-axis wind turbine --IS_EXTENSION--> horizontal-axis
 * 	 3. horizontal axis wind turbine --IS_EXTENSION--> horizontal axis
 * 
 * We infer:
 *   horizontal-axis wind turbine --M--> horizontal axis wind turbine
 * 
 * @author Damien Cram
 *
 */
public class ExtensionVariantGatherer extends SimpleEngine {
	@InjectLogger Logger logger;

	
	private Optional<TermHistory> history = Optional.empty();
	
	public ExtensionVariantGatherer setHistory(TermHistory history) {
		this.history = Optional.ofNullable(history);
		return this;
	}
	
	@Override
	public void execute() {
		if(!terminology.extensions().findAny().isPresent())
			logger.warn("Skipping {}. No {} relation found.", this.getClass().getSimpleName(), RelationType.HAS_EXTENSION);
		
		terminology
			.variations()
			.filter(r -> !r.isPropertySet(RelationProperty.IS_INFERED))
			.forEach(r-> r.setProperty(RelationProperty.IS_INFERED, false));
		
		/*
		 * Infer variations for all types but VariationType.SYNTAGMATIC
		 * and VariationType.GRAPHICAL as:
		 *  1- syntagmatic extensions variants are supposed to be exhaustively
		 * listed in lang-multi-word-rule-system.regex resource.
		 *  2- graphical variants are merged later on
		 */
		inferVariations(VariationType.MORPHOLOGICAL);
		inferVariations(VariationType.DERIVATION);
		inferVariations(VariationType.PREFIXATION);
		inferVariations(VariationType.SEMANTIC);
	}
	
	private static final RelationProperty[] INFERED_PROPERTIES = new RelationProperty[] {
			RelationProperty.IS_DERIVATION,
			RelationProperty.IS_DICO,
			RelationProperty.IS_DISTRIBUTIONAL,
			RelationProperty.IS_GRAPHICAL,
			RelationProperty.IS_MORPHOLOGICAL,
			RelationProperty.IS_PREFIXATION,
			RelationProperty.IS_SEMANTIC,
			RelationProperty.IS_SYNTAGMATIC,
			RelationProperty.SEMANTIC_SCORE,
			RelationProperty.SEMANTIC_SIMILARITY,
			RelationProperty.GRAPHICAL_SIMILARITY,
			RelationProperty.VARIATION_RULES,
	};


	public void inferVariations(VariationType type) {
		AtomicInteger cnt = new AtomicInteger(0);
		Stopwatch sw = Stopwatch.createStarted();
		terminology.variations()
			.filter(rel -> rel.isVariationOfType(type))
			.collect(toList())
			.forEach(relation -> {
				TermService m1 = relation.getFrom();
				TermService m2 = relation.getTo();
				
				List<RelationService> m1Extensions = m1.extensions().collect(toList());
				for(RelationService rel1:m1Extensions) {
					Optional<TermService> affix1 = rel1.getExtensionAffix(m1.getTerm());
					if(!affix1.isPresent())
						continue;
					
					List<RelationService> m2extensions = m2.extensions().collect(toList());
					for(RelationService rel2:m2extensions) {
						Optional<TermService> affix2 = rel2.getExtensionAffix(m2.getTerm());
							if(!affix2.isPresent())
								continue;

							if(Objects.equals(affix1, affix2)) {
								cnt.incrementAndGet();
								
								TermService inferedFrom = rel1.getTo();
								TermService inferedTo = rel2.getTo();
								if(inferedFrom.equals(inferedTo))
									continue;
								
								if(logger.isTraceEnabled()) 
									logger.trace("Found infered variation {} --> {}", inferedFrom, inferedTo);
								
								RelationService inferedRel = terminology.createVariation(VariationType.INFERENCE, inferedFrom.getTerm(), inferedTo.getTerm());
								for(RelationProperty p:INFERED_PROPERTIES) 
									VariationUtils.copyRelationPropertyIfSet(
											relation.getRelation(), 
											inferedRel.getRelation(), 
											p);
								inferedRel.setProperty(RelationProperty.IS_EXTENSION, false);
								inferedRel.setProperty(RelationProperty.IS_INFERED, true);
								watch(inferedRel, rel1, rel2);
							}
						}
					}
			});
		sw.stop();
		logger.debug("Infered {} variations of type {} in {}", cnt.intValue(), type, sw);
	}

	private void watch(RelationService inferedRel, RelationService r1, RelationService r2) {
		if(history.isPresent()) {
			if(history.get().isWatched(inferedRel.getTo().getTerm()))
				history.get().saveEvent(inferedRel.getTo().getGroupingKey(), this.getClass(), String.format("New inbound relation {} infered from {} and {}", inferedRel, r1, r2));
			if(history.get().isWatched(inferedRel.getFrom().getTerm()))
				history.get().saveEvent(inferedRel.getFrom().getGroupingKey(), this.getClass(), String.format("New outbound relation {} infered from {} and {}", inferedRel, r1, r2));
		}
	}
}
