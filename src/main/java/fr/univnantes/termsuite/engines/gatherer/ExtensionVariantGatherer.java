package fr.univnantes.termsuite.engines.gatherer;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Stopwatch;

import fr.univnantes.termsuite.framework.TerminologyService;
import fr.univnantes.termsuite.model.RelationProperty;
import fr.univnantes.termsuite.model.RelationType;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.TermRelation;
import fr.univnantes.termsuite.model.Terminology;
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
public class ExtensionVariantGatherer {

	private static final Logger LOGGER = LoggerFactory.getLogger(ExtensionVariantGatherer.class);
	
	
	private TermHistory history;
	
	public ExtensionVariantGatherer setHistory(TermHistory history) {
		this.history = history;
		return this;
	}
	
	
	public void gather(Terminology termino) {
		TerminologyService terminoService = new TerminologyService(termino);
		
		LOGGER.debug("Infering variations of term extensions");
		if(!termino.getRelations(RelationType.HAS_EXTENSION).findAny().isPresent())
			LOGGER.warn("Skipping {}. No {} relation found.", this.getClass().getSimpleName(), RelationType.HAS_EXTENSION);
		
		
		termino.getRelations(RelationType.VARIATION)
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
		inferVariations(terminoService, VariationType.MORPHOLOGICAL);
		inferVariations(terminoService, VariationType.DERIVATION);
		inferVariations(terminoService, VariationType.PREFIXATION);
		inferVariations(terminoService, VariationType.SEMANTIC);
		sw.stop();
		LOGGER.debug("Infered variations of term extensions gathered in {}", sw);
	}


	public void inferVariations(TerminologyService terminoService, VariationType type) {
		AtomicInteger cnt = new AtomicInteger(0);
		Stopwatch sw = Stopwatch.createStarted();
		terminoService.variations()
			.filter(rel -> rel.isPropertySet(RelationProperty.VARIATION_TYPE))
			// Apply to all variations but syntagmatic ones
			.filter(rel -> rel.get(RelationProperty.VARIATION_TYPE) == type)
//			.parallel()
			.forEach(relation -> {
				Term m1 = relation.getFrom();
				Term m2 = relation.getTo();
				terminoService.outboundRelations(m1, RelationType.HAS_EXTENSION)
					.forEach(rel1 -> {
						Term affix1 = TermUtils.getExtensionAffix(terminoService.getTerminology(), m1, rel1.getTo());
						if(affix1 == null)
							return;
						
						terminoService.outboundRelations(m2, RelationType.HAS_EXTENSION)
							.forEach(rel2 -> {
								Term affix2 = TermUtils.getExtensionAffix(terminoService.getTerminology(), m2, rel2.getTo());
								if(affix2 == null)
									return;

								if(Objects.equals(affix1, affix2)) {
									cnt.incrementAndGet();
									
									if(LOGGER.isTraceEnabled()) 
										LOGGER.trace("Found infered variation {} --> {}", rel1.getTo(), rel2.getTo());
									
									TermRelation inferedRel = terminoService.createVariation(VariationType.INFERENCE, rel1.getTo(), rel2.getTo());
									inferedRel.setProperty(RelationProperty.IS_EXTENSION, false);
									inferedRel.setProperty(type.getRelationProperty(), true);
									watch(inferedRel, rel1, rel2);
								}
							});
						});
			});
		
		sw.stop();
		LOGGER.debug("Infered {} variations of type {} in {}", cnt.intValue(), type, sw);
	}

	


	private void watch(TermRelation inferedRel, TermRelation r1, TermRelation r2) {
		if(history != null) {
			if(history.isWatched(inferedRel.getTo()))
				history.saveEvent(inferedRel.getTo().getGroupingKey(), this.getClass(), String.format("New inbound relation {} infered from {} and {}", inferedRel, r1, r2));
			if(history.isWatched(inferedRel.getFrom()))
				history.saveEvent(inferedRel.getFrom().getGroupingKey(), this.getClass(), String.format("New outbound relation {} infered from {} and {}", inferedRel, r1, r2));
		}
		
	}
}
