package fr.univnantes.termsuite.engines.gatherer;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;

import com.google.common.base.Stopwatch;

import fr.univnantes.termsuite.framework.InjectLogger;
import fr.univnantes.termsuite.framework.TerminologyEngine;
import fr.univnantes.termsuite.model.RelationProperty;
import fr.univnantes.termsuite.model.RelationType;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.TermRelation;
import fr.univnantes.termsuite.utils.TermHistory;
import fr.univnantes.termsuite.utils.TermUtils;

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
public class ExtensionVariantGatherer extends TerminologyEngine {
	@InjectLogger Logger logger;

	
	private Optional<TermHistory> history = Optional.empty();
	
	public ExtensionVariantGatherer setHistory(TermHistory history) {
		this.history = Optional.ofNullable(history);
		return this;
	}
	
	@Override
	public void execute() {
		
		logger.debug("Infering variations of term extensions");
		if(!terminology.extensions().findAny().isPresent())
			logger.warn("Skipping {}. No {} relation found.", this.getClass().getSimpleName(), RelationType.HAS_EXTENSION);
		
		
		terminology
			.variations()
			.filter(r -> !r.isPropertySet(RelationProperty.IS_INFERED))
			.forEach(r-> r.setProperty(RelationProperty.IS_INFERED, false));
		
		Stopwatch sw = Stopwatch.createStarted();
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
		sw.stop();
		logger.debug("Infered variations of term extensions gathered in {}", sw);
	}


	public void inferVariations(VariationType type) {
		AtomicInteger cnt = new AtomicInteger(0);
		Stopwatch sw = Stopwatch.createStarted();
		terminology.variations()
			.filter(rel -> rel.isPropertySet(RelationProperty.VARIATION_TYPE))
			// Apply to all variations but syntagmatic ones
			.filter(rel -> rel.get(RelationProperty.VARIATION_TYPE) == type)
//			.parallel()
			.forEach(relation -> {
				Term m1 = relation.getFrom();
				Term m2 = relation.getTo();
				terminology.outboundRelations(m1, RelationType.HAS_EXTENSION)
					.forEach(rel1 -> {
						Term affix1 = TermUtils.getExtensionAffix(terminology, m1, rel1.getTo());
						if(affix1 == null)
							return;
						
						terminology.outboundRelations(m2, RelationType.HAS_EXTENSION)
							.forEach(rel2 -> {
								Term affix2 = TermUtils.getExtensionAffix(terminology, m2, rel2.getTo());
								if(affix2 == null)
									return;

								if(Objects.equals(affix1, affix2)) {
									cnt.incrementAndGet();
									
									if(logger.isTraceEnabled()) 
										logger.trace("Found infered variation {} --> {}", rel1.getTo(), rel2.getTo());
									
									TermRelation inferedRel = terminology.createVariation(VariationType.INFERENCE, rel1.getTo(), rel2.getTo());
									inferedRel.setProperty(RelationProperty.IS_EXTENSION, false);
									inferedRel.setProperty(type.getRelationProperty(), true);
									
									if(type == VariationType.SEMANTIC) {
										inferedRel.setProperty(
												RelationProperty.IS_DISTRIBUTIONAL, 
												relation.get(RelationProperty.IS_DISTRIBUTIONAL));
										
										if(relation.isPropertySet(RelationProperty.SEMANTIC_SIMILARITY))
											inferedRel.setProperty(
													RelationProperty.SEMANTIC_SIMILARITY, 
													relation.get(RelationProperty.SEMANTIC_SIMILARITY));
									}
									
									watch(inferedRel, rel1, rel2);
								}
							});
						});
			});
		
		sw.stop();
		logger.debug("Infered {} variations of type {} in {}", cnt.intValue(), type, sw);
	}

	private void watch(TermRelation inferedRel, TermRelation r1, TermRelation r2) {
		if(history.isPresent()) {
			if(history.get().isWatched(inferedRel.getTo()))
				history.get().saveEvent(inferedRel.getTo().getGroupingKey(), this.getClass(), String.format("New inbound relation {} infered from {} and {}", inferedRel, r1, r2));
			if(history.get().isWatched(inferedRel.getFrom()))
				history.get().saveEvent(inferedRel.getFrom().getGroupingKey(), this.getClass(), String.format("New outbound relation {} infered from {} and {}", inferedRel, r1, r2));
		}
	}
}
