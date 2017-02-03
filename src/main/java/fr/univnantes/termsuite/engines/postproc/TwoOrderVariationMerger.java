package fr.univnantes.termsuite.engines.postproc;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import org.slf4j.Logger;

import fr.univnantes.termsuite.engines.SimpleEngine;
import fr.univnantes.termsuite.framework.InjectLogger;
import fr.univnantes.termsuite.framework.service.RelationService;
import fr.univnantes.termsuite.model.Relation;
import fr.univnantes.termsuite.model.RelationType;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.TermProperty;

/*
 *  Detect 2-order extension variations.
 *  
 *  
 *  wind turbine --> axis wind turbine
 *  axis wind turbine --> horizontal axis wind turbine
 *  wind turbine --> horizontal axis wind turbine
 *  
 *  would result in removal of wind turbine --> horizontal axis wind turbine
 *  
 *  IMPORTANT: must occur after term variation filtering. Otherwise:
 *  				wind turbine --> axis wind turbine --> horizontal axis wind turbine
 *  
 *  		   will remove "wind turbine --> horizontal axis wind turbine"
 */
public class TwoOrderVariationMerger extends SimpleEngine {

	@InjectLogger Logger logger;

	@Override
	public void execute() {

		/*
		 * Merge pattern n°1: t --extension--> v1 --extension--> v2
		 * 
		 *  When 
		 *  
		 *  blade passage --> rotor blade passage
		 *  rotor blade passage --> typical rotor blade passage
		 *  blade passage --> typical rotor blade passage
		 *  
		 *  Then remove
		 *  
		 *  blade passage --> typical rotor blade passage
		 *  
		 */
		mergeTwoOrderVariations(
				RelationService::isExtension,
				RelationService::isExtension
			);

		/*
		 * Merge pattern n°2: t --extension--> v1 --morph|deriv|prefix|sem--> v2
		 * 
		 * When 
		 * 
		 * wind turbine --> small-scale wind turbine
		 * small-scale wind turbine --> small scale wind turbine
		 * wind turbine --> small scale wind turbine
		 * 
		 * Then remove
		 * 
		 * wind turbine --> small scale wind turbine
		 * 
		 */
		mergeTwoOrderVariations(
				RelationService::isExtension,
				r2 -> r2.isMorphological()
							|| r2.isDerivation()
							|| r2.isPrefixation()
							|| r2.isSemantic()
			);
		
		
		TermPostProcessor.logVariationsAndTerms(logger, terminology);
	}

	
	/*
	 * When
	 * 
	 *   baseTerm -r1-> v1
	 *   baseTerm --rtrans--> v2
	 *   and v1 -r2-> v2
	 *   
	 * Then remove 
	 *   
	 *   baseTerm --rtrans--> v2 
	 * 
	 */
	private void mergeTwoOrderVariations(Predicate<RelationService> p1, Predicate<RelationService> p2) {
		/*
		 *  t1 --r1--> t2 --r2--> t3
		 *  t1 --rtrans--> t3
		 */

		terminology.terms(TermProperty.FREQUENCY.getComparator(true))
		.forEach(t1 -> {
			final Map<Term, Relation> r1Set = new HashMap<>();

			t1.variations().forEach(r1 -> {
				if(p1.test(r1))
					r1Set.put(r1.getTo().getTerm(), r1.getRelation());
			});
			
			final Set<Relation> rem = new HashSet<>();
			
			r1Set.keySet().forEach(t2-> {
				terminology
					.outboundRelations(t2, RelationType.VARIATION)
					.filter(p2)
					.filter(r2 -> r1Set.containsKey(r2.getTo()))
					.forEach(r2 -> {
						Term t3 = r2.getTo().getTerm();
						
						Relation rtrans = r1Set.get(t3);
						if(logger.isTraceEnabled()) {
							logger.trace("Found order-2 relation in variation set {}-->{}-->{}", t1, t2, t3);
							logger.trace("Removing {}", rtrans);
						}
						watchRemoval(t1.getTerm(), t2, t3, rtrans);
						rem.add(rtrans);
					})
				;
			});
			
			rem.forEach(rel -> {
				terminology.removeRelation(rel);
			});
		});

	}

	private void watchRemoval(Term t1, Term t2, Term t3, Relation rtrans) {
		if(history.isPresent()) {
			if(history.get().isWatched(t1)) 
				history.get().saveEvent(t1.getGroupingKey(), this.getClass(), String.format("Removing two-order relation %s because it has a length-2 path %s -> %s -> %s", rtrans, t1, t2, t3));
			if(history.get().isWatched(t3)) 
				history.get().saveEvent(t3.getGroupingKey(), this.getClass(), String.format("Removing two-order relation %s because it has a length-2 path %s -> %s -> %s", rtrans, t1, t2, t3));
		}
	}
}
