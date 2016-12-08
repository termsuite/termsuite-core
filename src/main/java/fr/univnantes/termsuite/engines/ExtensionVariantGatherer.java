package fr.univnantes.termsuite.engines;

import java.util.Objects;

import org.apache.commons.lang.mutable.MutableInt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.univnantes.termsuite.model.RelationProperty;
import fr.univnantes.termsuite.model.RelationType;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.Terminology;
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
public class ExtensionVariantGatherer {

	private static final Logger LOGGER = LoggerFactory.getLogger(ExtensionVariantGatherer.class);
	
	
	private TermHistory history;
	
	public ExtensionVariantGatherer setHistory(TermHistory history) {
		this.history = history;
		return this;
	}
	
	
	public void gather(Terminology termIndex) {
		if(!termIndex.getRelations(RelationType.HAS_EXTENSION).findAny().isPresent())
			LOGGER.warn("Skipping {}. No {} relation found.", this.getClass().getSimpleName(), RelationType.HAS_EXTENSION);
		
		termIndex.getRelations(RelationType.VARIATIONS)
			.filter(r -> !r.isPropertySet(RelationProperty.IS_INFERED))
			.forEach(r-> r.setProperty(RelationProperty.IS_INFERED, false));
		
		final MutableInt cnt = new MutableInt(0);
		
		termIndex.getRelations(RelationType.MORPHOLOGICAL)
			.forEach(relation -> {
				Term m1 = relation.getFrom();
				Term m2 = relation.getTo();
				
				
				termIndex.getOutboundRelations(m1, RelationType.HAS_EXTENSION)
					.stream()
					.forEach(rel1 -> {
						Term affix1 = TermUtils.getExtensionAffix(termIndex, m1, rel1.getTo());
						if(affix1 == null)
							return;
						
						termIndex.getOutboundRelations(m2, RelationType.HAS_EXTENSION)
							.stream()
							.forEach(rel2 -> {
								Term affix2 = TermUtils.getExtensionAffix(termIndex, m2, rel2.getTo());
								if(affix2 == null)
									return;

								if(Objects.equals(affix1, affix2)) {
									cnt.increment();
									
									if(LOGGER.isTraceEnabled()) 
										LOGGER.trace("Found infered variation {} --> {}", rel1.getTo(), rel2.getTo());
									
									TermRelation inferedRel = new TermRelation(RelationType.MORPHOLOGICAL, rel1.getTo(), rel2.getTo());
									inferedRel.setProperty(RelationProperty.IS_INFERED, true);
									inferedRel.setProperty(RelationProperty.IS_EXTENSION, false);
									inferedRel.setProperty(RelationProperty.VARIATION_RULE, relation.getPropertyStringValue(RelationProperty.VARIATION_RULE));
									termIndex.addRelation(inferedRel);
									watch(inferedRel, rel1, rel2);
									
								}
							});
						});
			});
		
		LOGGER.debug("Infered {} variations", cnt.intValue());
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
