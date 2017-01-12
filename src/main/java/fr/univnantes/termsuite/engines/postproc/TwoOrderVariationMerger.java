package fr.univnantes.termsuite.engines.postproc;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import fr.univnantes.termsuite.framework.Execute;
import fr.univnantes.termsuite.framework.TerminologyEngine;
import fr.univnantes.termsuite.framework.TerminologyService;
import fr.univnantes.termsuite.model.RelationProperty;
import fr.univnantes.termsuite.model.RelationType;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.TermProperty;
import fr.univnantes.termsuite.model.TermRelation;

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
public class TwoOrderVariationMerger extends TerminologyEngine {

	@Execute
	public void mergeTwoOrderVariationPatterns(TerminologyService terminoService) {

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
		mergeTwoOrderVariations(terminoService,
				r1 -> r1.getPropertyBooleanValue(RelationProperty.IS_EXTENSION),
				r2 -> r2.getPropertyBooleanValue(RelationProperty.IS_EXTENSION)
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
		mergeTwoOrderVariations(terminoService,
				r1 -> r1.getPropertyBooleanValue(RelationProperty.IS_EXTENSION),
				r2 -> r2.getPropertyBooleanValue(RelationProperty.IS_MORPHOLOGICAL)
						|| r2.getPropertyBooleanValue(RelationProperty.IS_DERIVATION)
						|| r2.getPropertyBooleanValue(RelationProperty.IS_PREXATION)
						|| r2.getPropertyBooleanValue(RelationProperty.IS_SEMANTIC)
			);
		
		
		TermPostProcessor.logVariationsAndTerms(terminoService);
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
	private void mergeTwoOrderVariations(TerminologyService terminoService, Predicate<TermRelation> p1, Predicate<TermRelation> p2) {
		/*
		 *  t1 --r1--> t2 --r2--> t3
		 *  t1 --rtrans--> t3
		 */

		terminoService.terms()
		.sorted(TermProperty.FREQUENCY.getComparator(true))
		.forEach(t1 -> {
			final Map<Term, TermRelation> r1Set = new HashMap<>();

			terminoService.outboundRelations(t1, RelationType.VARIATION).forEach(r1 -> {
				if(p1.test(r1))
					r1Set.put(r1.getTo(), r1);
			});
			
			final Set<TermRelation> rem = new HashSet<>();
			
			r1Set.keySet().forEach(t2-> {
				terminoService
					.outboundRelations(t2, RelationType.VARIATION)
					.filter(p2)
					.filter(r2 -> r1Set.containsKey(r2.getTo()))
					.forEach(r2 -> {
						Term t3 = r2.getTo();
						
						TermRelation rtrans = r1Set.get(t3);
						if(getLogger().isTraceEnabled()) {
							getLogger().trace("Found order-2 relation in variation set {}-->{}-->{}", t1, t2, t3);
							getLogger().trace("Removing {}", rtrans);
						}
						watchRemoval(t1, t2, t3, rtrans);
						rem.add(rtrans);
					})
				;
			});
			
			rem.forEach(rel -> {
				terminoService.removeRelation(rel);
			});
		});

	}

	private void watchRemoval(Term t1, Term t2, Term t3, TermRelation rtrans) {
		if(history.isPresent()) {
			if(history.get().isWatched(t1)) 
				history.get().saveEvent(t1.getGroupingKey(), this.getClass(), String.format("Removing two-order relation %s because it has a length-2 path %s -> %s -> %s", rtrans, t1, t2, t3));
			if(history.get().isWatched(t3)) 
				history.get().saveEvent(t3.getGroupingKey(), this.getClass(), String.format("Removing two-order relation %s because it has a length-2 path %s -> %s -> %s", rtrans, t1, t2, t3));
		}
	}
}
