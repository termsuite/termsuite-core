package fr.univnantes.termsuite.engines.gatherer;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.slf4j.Logger;

import com.google.inject.Inject;

import fr.univnantes.termsuite.engines.SimpleEngine;
import fr.univnantes.termsuite.framework.InjectLogger;
import fr.univnantes.termsuite.framework.Parameter;
import fr.univnantes.termsuite.framework.Resource;
import fr.univnantes.termsuite.framework.service.RelationService;
import fr.univnantes.termsuite.framework.service.TerminologyService;
import fr.univnantes.termsuite.index.TermIndex;
import fr.univnantes.termsuite.model.Relation;
import fr.univnantes.termsuite.model.RelationProperty;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.uima.ResourceType;

public abstract class VariationTypeGatherer extends SimpleEngine {
	
	private static final int DEFAULT_MAX_CLASS_COMPLEXITY = 1000000;

	@Inject GroovyService groovyService;
	@InjectLogger Logger logger;

	@Resource(type=ResourceType.VARIANTS)
	protected YamlRuleSet variantRules;

	@Parameter
	protected VariationType variationType;
	
	@Override
	public void execute() {
		if(variantRules.getVariantRules(variationType).isEmpty())
			return;

		AtomicLong cnt = new AtomicLong(0);
		TermIndex termIndex = getTermIndex();
		
		termIndex.keySet().stream()
			.parallel()
			.forEach(key -> {
				Collection<Term> terms = termIndex.getTerms(key);
				if(terms.size() > 1)
					gather(terminology, groovyService, terms, key, cnt);
			});
		
		logger.debug("Num of comparisons: {}", cnt);
	}
	

	private void watch(Term source, Term target, Relation tv) {
		if(history.isPresent()) {
			if(history.get().isGKeyWatched(source.getGroupingKey()))
				history.get().saveEvent(
						source.getGroupingKey(),
						this.getClass(), 
						"Term has a new variation type: "+variationType+", relation: "+tv+", rule: "+tv.get(RelationProperty.VARIATION_RULE)+"");
			if(history.get().isGKeyWatched(target.getGroupingKey()))
				history.get().saveEvent(
						target.getGroupingKey(),
						this.getClass(), 
						"Term has a new base-variation type: "+variationType+", relation: "+tv+", rule: "+tv.get(RelationProperty.VARIATION_RULE)+"");
		}
	}

	protected void gather(TerminologyService termino, GroovyService groovyService,
			Collection<Term> termClass, String clsName,
			AtomicLong cnt) {
		for(VariantRule rule:variantRules.getVariantRules(variationType)) {
			Set<Term> sources = termClass.stream()
				.map(t -> termino.asTermService(t))
				.filter(rule::isSourceAcceptable)
				.map(t -> t.getTerm())
				.collect(Collectors.toSet());
			if(sources.isEmpty())
				continue;

			Set<Term> targets = termClass.stream()
					.map(t -> termino.asTermService(t))
					.filter(rule::isTargetAcceptable)
					.map(t -> t.getTerm())
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
		Optional<RelationService> variation = terminoService.getVariation(source, target);
				
		if(!variation.isPresent()){
			
			RelationService relation = terminoService.createVariation(rule.getVariationType(), source, target);
			variation = Optional.of(relation);
			watch(source, target, relation.getRelation());
		}
		variation.get().setProperty(RelationProperty.VARIATION_RULE, rule.getName());
	}

	protected abstract TermIndex getTermIndex();
}
