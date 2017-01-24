package fr.univnantes.termsuite.engines.gatherer;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.slf4j.Logger;

import com.google.common.base.Stopwatch;
import com.google.inject.Inject;

import fr.univnantes.termsuite.framework.InjectLogger;
import fr.univnantes.termsuite.framework.Parameter;
import fr.univnantes.termsuite.framework.Resource;
import fr.univnantes.termsuite.framework.TerminologyEngine;
import fr.univnantes.termsuite.framework.service.TerminologyService;
import fr.univnantes.termsuite.model.RelationProperty;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.TermRelation;
import fr.univnantes.termsuite.model.termino.CustomTermIndex;
import fr.univnantes.termsuite.uima.ResourceType;

public class VariationTypeGatherer extends TerminologyEngine {
	
	private static final int DEFAULT_MAX_CLASS_COMPLEXITY = 1000000;

	@Inject GroovyService groovyService;
	@InjectLogger Logger logger;

	@Resource(type=ResourceType.VARIANTS)
	protected YamlRuleSet variantRules;

	@Parameter
	private VariationType variationType;
	
	
	@Parameter(optional = true) // optional for SemanticGatherer
	protected String indexName ;
	
	@Parameter(optional= true)
	protected boolean dropIndexAtEnd = true;

	@Override
	public void execute() {
		logger.info("Gathering {} variants", variationType.name().toLowerCase());
		if(variantRules.getVariantRules().isEmpty())
			return;

		AtomicLong cnt = new AtomicLong(0);
		Stopwatch indexSw = Stopwatch.createStarted();
		CustomTermIndex index = terminology.getTerminology().getCustomIndex(indexName);
		index.cleanSingletonKeys();
		logger.debug("Term grouped in classes in {}", indexSw);

		indexSw.stop();
		Stopwatch gatheringSw = Stopwatch.createStarted();
		index.keySet().stream()
			.parallel()
			.forEach(key -> {
				Collection<Term> terms = index.getTerms(key);
				gather(terminology, groovyService, terms, key, cnt);
			});
		gatheringSw.stop();
		
		if(dropIndexAtEnd)
			terminology.getTerminology().dropCustomIndex(indexName);
		
		logger.debug("Term gathered in {} - Num of comparisons: {}", gatheringSw, cnt);
	}
	

	private void watch(Term source, Term target, TermRelation tv) {
		if(history.isPresent()) {
			if(history.get().isGKeyWatched(source.getGroupingKey()))
				history.get().saveEvent(
						source.getGroupingKey(),
						this.getClass(), 
						"Term has a new variation: " + tv + " ("+tv.get(RelationProperty.VARIATION_TYPE)+", rule: "+tv.get(RelationProperty.VARIATION_RULE)+")");
			if(history.get().isGKeyWatched(target.getGroupingKey()))
				history.get().saveEvent(
						target.getGroupingKey(),
						this.getClass(), 
						"Term has a new variation base: " + tv + " ("+tv.get(RelationProperty.VARIATION_TYPE)+", rule: "+tv.get(RelationProperty.VARIATION_RULE)+")");
		}
	}

	protected void gather(TerminologyService termino, GroovyService groovyService,
			Collection<Term> termClass, String clsName,
			AtomicLong cnt) {
		for(VariantRule rule:variantRules.getVariantRules(variationType)) {
			Set<Term> sources = termClass.stream()
				.filter(rule::isSourceAcceptable)
				.collect(Collectors.toSet());
			if(sources.isEmpty())
				continue;

			Set<Term> targets = termClass.stream()
					.filter(rule::isTargetAcceptable)
					.collect(Collectors.toSet());
			if(targets.isEmpty())
				continue;
			
			long complexity = sources.size() * targets.size();
			if(complexity > DEFAULT_MAX_CLASS_COMPLEXITY)
				logger.debug("Skipping term class {} because complexity is too high. Complexity: {}. Max: {}", clsName, complexity, DEFAULT_MAX_CLASS_COMPLEXITY);
			
			for(Term source:sources) {
				for(Term target:targets) {
					if(source.equals(target))
						continue;
					
					cnt.incrementAndGet();
					if(groovyService.matchesRule(rule, source, target)) 
						createVariationRuleRelation(termino, source, target, rule);
				}
			}
		}
	}

	private synchronized void createVariationRuleRelation(TerminologyService terminoService, Term source, Term target, VariantRule rule) {
		Optional<TermRelation> variation = terminoService.getVariation(source, target);
				
		if(!variation.isPresent()){
			TermRelation relation = terminoService.createVariation(rule.getVariationType(), source, target);
			relation.setProperty(RelationProperty.IS_GRAPHICAL, false);
			variation = Optional.of(relation);
			watch(source, target, relation);
		}
		variation.get().setProperty(RelationProperty.VARIATION_RULE, rule.getName());
	}

}
