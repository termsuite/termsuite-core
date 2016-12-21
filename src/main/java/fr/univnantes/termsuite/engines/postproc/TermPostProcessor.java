
/*******************************************************************************
 * Copyright 2015-2016 - CNRS (Centre National de Recherche Scientifique)
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *******************************************************************************/

package fr.univnantes.termsuite.engines.postproc;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.lang.mutable.MutableInt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Sets;

import fr.univnantes.termsuite.engines.ExtensionDetecter;
import fr.univnantes.termsuite.framework.TerminologyService;
import fr.univnantes.termsuite.model.RelationProperty;
import fr.univnantes.termsuite.model.RelationType;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.TermProperty;
import fr.univnantes.termsuite.model.TermRelation;
import fr.univnantes.termsuite.model.Terminology;
import fr.univnantes.termsuite.resources.PostProcConfig;
import fr.univnantes.termsuite.utils.StringUtils;
import fr.univnantes.termsuite.utils.TermHistory;

/**
 * Turn a {@link Terminology} to a {@link ScoredModel}
 * 
 * @author Damien Cram
 *
 */
public class TermPostProcessor {
	private static final Logger LOGGER = LoggerFactory.getLogger(TermPostProcessor.class);

	private PostProcConfig config;
	
	private TermHistory history;
	
	public TermPostProcessor(PostProcConfig config) {
		super();
		this.config = config;
	}

	public TermPostProcessor setHistory(TermHistory history) {
		this.history = history;
		return this;
	}
	
	public void postprocess(Terminology termino) {
		LOGGER.info("Post-processing terms and variants");
		logVariationsAndTerms(termino);

		Stopwatch sw = Stopwatch.createStarted();

		// resets all IS_EXTENSION properies
		new ExtensionDetecter().setIsExtensionProperty(termino);
		

		// Score terms 
		LOGGER.debug("[Post-processing] Scoring terms");
		new TermScorer().score(termino);
		logVariationsAndTerms(termino);

		
		// Score variations
		LOGGER.debug("[Post-processing] Scoring variations");
		new VariationScorer().score(termino);
		logVariationsAndTerms(termino);

		
		// Filter extensions
		LOGGER.debug("[Post-processing] Filtering extensions by threshold");
		filterExtensionsByThresholds(termino);
		logVariationsAndTerms(termino);
		
		
		
		// Filter variations
		LOGGER.debug("[Post-processing] Filtering variations by scores");
		filterVariationsByScores(termino);
		logVariationsAndTerms(termino);

		
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
		LOGGER.debug("[Post-processing] Merging two-order variations");
		mergeTwoOrderVariationPatterns(new TerminologyService(termino));
		logVariationsAndTerms(termino);

		
		/*
		 *  Filter terms with bad orthograph and no independance
		 *  
		 *  IMPORTANT: must occur AFTER detection of 2-order extension variations
		 */
		LOGGER.debug("[Post-processing] Filtering terms by scores");
		filterTermsByScores(termino);
		logVariationsAndTerms(termino);

		
		// Rank variations extensions
		LOGGER.debug("Ranking term variations");
		rankVariations(termino);
		sw.stop();
		
		LOGGER.debug("[Post-processing] Post-processing finished in {}", sw);
	}

	private void logVariationsAndTerms(Terminology termino) {
		if(LOGGER.isDebugEnabled()) {
			long var = termino.getRelations(RelationType.VARIATION).count();
			LOGGER.debug("Number of terms: {}. Number of variations: {}",
					termino.getTerms().size(), var);
		}
	}

	public void rankVariations(Terminology termino) {
		termino.getTerms().forEach(t-> {
			final MutableInt vrank = new MutableInt(0);
			termino.getOutboundRelations(t, RelationType.VARIATION)
				.stream()
				.sorted(RelationProperty.VARIANT_SCORE.getComparator(true))
				.forEach(rel -> {
					vrank.increment();
					rel.setProperty(RelationProperty.VARIATION_RANK, vrank.intValue());
				});
		});
	}

	public Set<Term> filterTermsByScores(Terminology termino) {
		Set<Term> remTerms = termino.getTerms().stream()
			.filter(this::filterTermByThresholds)
			.collect(Collectors.toSet());
		remTerms
			.parallelStream()
			.forEach(termino::removeTerm);
		return remTerms;
	}

	public Set<TermRelation> filterVariationsByScores(Terminology termino) {
		Set<TermRelation> remRelations = termino.getRelations(RelationType.VARIATION)
			.filter(this::filterVariation)
			.collect(Collectors.toSet());
		remRelations
			.stream()
			.forEach(termino::removeRelation);
		return remRelations;
	}

	private void mergeTwoOrderVariationPatterns(TerminologyService terminoService) {
		Stopwatch sw = Stopwatch.createStarted();

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
		
		sw.stop();
		LOGGER.debug("Two-order variations detected in {}", sw);
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
						if(LOGGER.isTraceEnabled()) {
							LOGGER.trace("Found order-2 relation in variation set {}-->{}-->{}", t1, t2, t3);
							LOGGER.trace("Removing {}", rtrans);
						}
						if(history.isWatched(t1)) 
							history.saveEvent(t1.getGroupingKey(), this.getClass(), String.format("Removing two-order relation %s because it has a length-2 path %s -> %s -> %s", rtrans, t1, t2, t3));
						if(history.isWatched(t3)) 
							history.saveEvent(t3.getGroupingKey(), this.getClass(), String.format("Removing two-order relation %s because it has a length-2 path %s -> %s -> %s", rtrans, t1, t2, t3));
						rem.add(rtrans);
					})
				;
			});
			
			rem.forEach(rel -> {
				terminoService.removeRelation(rel);
			});
		});

	}


	private void filterExtensionsByThresholds(Terminology termino) {
		Predicate<? super TermRelation> isExtension = rel -> 
				rel.isPropertySet(RelationProperty.IS_EXTENSION) 
				&& rel.getPropertyBooleanValue(RelationProperty.IS_EXTENSION);
		
		Set<TermRelation> remTargets = Sets.newHashSet();

		
		/*
		 *	Remove target term if it has no affix
		 */
		termino
			.getRelations()
			.filter(isExtension)
			.filter(rel -> 
						rel.isPropertySet(RelationProperty.HAS_EXTENSION_AFFIX)
							&&	!rel.getPropertyBooleanValue(RelationProperty.HAS_EXTENSION_AFFIX))
			.forEach(remTargets::add);

		
		/*
		 *	Remove target term if its frequency is 1 
		 */
		termino
			.getRelations()
			.filter(isExtension)
			.filter(rel -> rel.getTo().getFrequency() == 1 && rel.getFrom().getFrequency() > 1)
			.forEach(remTargets::add);
		
		
		
		/*
		 * Actually remove terms
		 */
		remTargets.stream().forEach(rel -> {
			watchTermRemoval(rel.getTo(), String.format("Removing term because it is the poor extension of term %s ", rel.getFrom()));
		});
		
		
		Set<Term> remSet = remTargets.stream().map(TermRelation::getTo).collect(Collectors.toSet());
		LOGGER.debug("Removing {} extension targets from term index", remSet.size());
		remSet.stream().forEach(termino::removeTerm);
	}

	private boolean filterTermByThresholds(Term term) {
		if(StringUtils.getOrthographicScore(term.getLemma()) < this.config.getOrthographicScoreTh()) {
			if(history != null && history.isWatched(term))
				history.saveEvent(
						term.getGroupingKey(), 
						this.getClass(), 
						String.format(
								"Removing term because orthographic score <%.2f> is under threshhold <%.2f>.",
								StringUtils.getOrthographicScore(term.getLemma()),
								this.config.getOrthographicScoreTh()));
			return true;
		}
		else if(term.getPropertyDoubleValue(TermProperty.INDEPENDANCE) < this.config.getTermIndependanceTh()) {
			if(history != null && history.isWatched(term))
				history.saveEvent(
						term.getGroupingKey(), 
						this.getClass(), 
						String.format(
								"Removing term because independence score <%.2f> is under threshhold <%.2f>.",
								term.getPropertyDoubleValue(TermProperty.INDEPENDANCE),
								this.config.getTermIndependanceTh()));
			return true;
		}
		return false;
	}

	private boolean filterVariation(TermRelation relation) {
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
		if(history != null && history.isWatched(base))
			history.saveEvent(
					base.getGroupingKey(), 
					this.getClass(), 
					String.format(
							msg,
							variant,
							score,
							th)
					);
	}

	private void watchTermRemoval(Term term, String msg) {
		if(history != null && history.isWatched(term))
			history.saveEvent(
					term.getGroupingKey(), 
					this.getClass(), 
					msg);
	}

}
