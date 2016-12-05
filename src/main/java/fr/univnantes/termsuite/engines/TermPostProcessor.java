
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

package fr.univnantes.termsuite.engines;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.lang.mutable.MutableInt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

import fr.univnantes.termsuite.model.RelationProperty;
import fr.univnantes.termsuite.model.RelationType;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.TermIndex;
import fr.univnantes.termsuite.model.TermProperty;
import fr.univnantes.termsuite.model.TermRelation;
import fr.univnantes.termsuite.resources.ScorerConfig;
import fr.univnantes.termsuite.utils.StringUtils;
import fr.univnantes.termsuite.utils.TermHistory;

/**
 * Turn a {@link TermIndex} to a {@link ScoredModel}
 * 
 * @author Damien Cram
 *
 */
public class TermPostProcessor {
	private static final Logger LOGGER = LoggerFactory.getLogger(TermPostProcessor.class);

	private ScorerConfig config;
	
	private TermHistory history;
	
	public TermPostProcessor(ScorerConfig config) {
		super();
		this.config = config;
	}

	public TermPostProcessor setHistory(TermHistory history) {
		this.history = history;
		return this;
	}
	
	public void postprocess(TermIndex termIndex) {
		LOGGER.debug("Post-processing term index {}", termIndex.getName());

		// resets all IS_EXTENSION properies
		new ExtensionDetecter().setIsExtensionProperty(termIndex);
		
		// Score terms 
		new TermScorer().score(termIndex);
		
		// Score variations
		new VariationScorer().score(termIndex);
		
		
		
		// Filter extensions
		filterExtensionsByThresholds(termIndex);
		
		// Filter relations
		Set<TermRelation> remRelations = termIndex.getRelations(RelationType.VARIATIONS)
			.filter(this::filterVariation)
			.collect(Collectors.toSet());
		LOGGER.debug("Filtered {} variants out of {} variants", remRelations.size(), termIndex.getRelations(RelationType.VARIATIONS).count());
		remRelations
			.stream()
			.forEach(termIndex::removeRelation);

		
		/*
		 *  Filter terms with bad orthograph and no independance
		 *  
		 *  IMPORTANT: must occur AFTER detection of 2-order extension variations
		 */
		Set<Term> remTerms = termIndex.getTerms().stream()
			.filter(this::filterTermByThresholds)
			.collect(Collectors.toSet());
		LOGGER.debug("Filtered {} terms out of {} terms", remTerms.size(), termIndex.getTerms().size());
		remTerms
			.stream()
			.forEach(termIndex::removeTerm);
		
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
		 *  IMPORTANT: must occur after term relation filtering. Otherwise:
		 */
		filterTwoOrderVariationPatterns(termIndex);
		
		// Rank variations extensions
		termIndex.getTerms().forEach(t-> {
			final MutableInt vrank = new MutableInt(0);
			termIndex.getOutboundRelations(t, RelationType.VARIATIONS)
				.stream()
				.sorted(RelationProperty.VARIANT_SCORE.getComparator(true))
				.forEach(rel -> {
					vrank.increment();
					rel.setProperty(RelationProperty.VARIATION_RANK, vrank.intValue());
				});
		});

	}

	private void filterTwoOrderVariationPatterns(TermIndex termIndex) {

		/*
		 * Removal pattern n°1: t --extension--> v1 --extension--> v2
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
		filterTwoOrderVariations(termIndex,
				r1 -> r1.getType().isSyntag() && r1.getPropertyBooleanValue(RelationProperty.IS_EXTENSION),
				r2 -> r2.getType().isSyntag() && r2.getPropertyBooleanValue(RelationProperty.IS_EXTENSION)
			);

		/*
		 * Removal pattern n°2: t --extension--> v1 --morph--> v2
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
		filterTwoOrderVariations(termIndex,
				r1 -> r1.getType().isSyntag() && r1.getPropertyBooleanValue(RelationProperty.IS_EXTENSION),
				r2 -> r2.getType() == RelationType.MORPHOLOGICAL
			);
	}
	
	/*
	 * When
	 * 
	 *   baseTerm -r1-> v1
	 *   baseTerm ----> v2
	 *   and v1 -r2-> v2
	 *   
	 * Then remove 
	 *   
	 *   baseTerm ----> v2 
	 * 
	 */
	private void filterTwoOrderVariations(TermIndex termIndex, Predicate<TermRelation> r1, Predicate<TermRelation> r2) {
		termIndex.getTerms().stream()
		.sorted(TermProperty.FREQUENCY.getComparator(true))
		.forEach(term -> {
			final Map<Term, TermRelation> v1Set = new HashMap<>();

			for(TermRelation rel:termIndex.getOutboundRelations(term)) {
				if(r1.test(rel))
					v1Set.put(rel.getTo(), rel);
			}
			
			final Set<TermRelation> order2Rels = new HashSet<>();
			
			v1Set.keySet().forEach(variant-> {
				termIndex
					.getOutboundRelations(variant)
					.stream()
					.filter(r2)
					.filter(order2Rel -> v1Set.containsKey(order2Rel.getTo()))
					.forEach(order2Rels::add)
				;
			});
			
			order2Rels.forEach(rel -> {
				TermRelation r = v1Set.get(rel.getTo());
				if(LOGGER.isTraceEnabled()) {
					LOGGER.trace("Found order-2 relation in variation set {}-->{}-->{}", term, rel.getFrom(), rel.getTo());
					LOGGER.trace("Removing {}", r);
				}
				if(history.isWatched(r.getFrom())) 
					history.saveEvent(r.getFrom().getGroupingKey(), this.getClass(), String.format("Removing two-order relation %s because it has a length-2 path %s -> %s -> %s", rel, term, rel.getFrom(), rel.getTo()));
				if(history.isWatched(r.getTo())) 
					history.saveEvent(r.getTo().getGroupingKey(), this.getClass(), String.format("Removing two-order relation %s because it has a length-2 path %s -> %s -> %s", rel, term, rel.getFrom(), rel.getTo()));
				termIndex.removeRelation(r);
			});
		});

	}


	private void filterExtensionsByThresholds(TermIndex termIndex) {
		Predicate<? super TermRelation> isExtension = rel -> 
				rel.isPropertySet(RelationProperty.IS_EXTENSION) 
				&& rel.getPropertyBooleanValue(RelationProperty.IS_EXTENSION);
		
		Set<TermRelation> remTargets = Sets.newHashSet();

		
		/*
		 *	Remove target term if it has no affix
		 */
		termIndex
			.getRelations()
			.filter(isExtension)
			.filter(rel -> 
						rel.isPropertySet(RelationProperty.HAS_EXTENSION_AFFIX)
							&&	!rel.getPropertyBooleanValue(RelationProperty.HAS_EXTENSION_AFFIX))
			.forEach(remTargets::add);

		
		/*
		 *	Remove target term if its frequency is 1 
		 */
		termIndex
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
		remSet.stream().forEach(termIndex::removeTerm);
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
		Double variantIndependance = relation.getTo().getPropertyDoubleValue(TermProperty.INDEPENDANCE);
		if(variantIndependance < config.getVariantIndependanceTh()) {
			watchVariationRemoval(variant, base, 
					"Removing variant <%s> because the variant independence score <%.2f> is under threshhold <%.2f>.",
					variantIndependance, this.config.getVariantIndependanceTh());
			watchVariationRemoval(base, variant, 
					"Removed as variant of term <%s> because the variant independence score <%.2f> is under threshhold <%.2f>.",
					variantIndependance, this.config.getVariantIndependanceTh());
			return true;
		} else if(relation.getPropertyDoubleValue(RelationProperty.VARIANT_SCORE) < config.getVariationScoreTh()) {
			watchVariationRemoval(variant, base, 
					"Removing variant <%s> because the variation score <%.2f> is under threshhold <%.2f>.",
					relation.getPropertyDoubleValue(RelationProperty.VARIANT_SCORE), this.config.getVariationScoreTh());
			watchVariationRemoval(base, variant, 
					"Removed as variant of term <%s> because the variation score <%.2f> is under threshhold <%.2f>.",
					relation.getPropertyDoubleValue(RelationProperty.VARIANT_SCORE), this.config.getVariationScoreTh());
			return true;
		} else if(relation.getPropertyBooleanValue(RelationProperty.IS_EXTENSION)) {
			if(relation.getPropertyBooleanValue(RelationProperty.HAS_EXTENSION_AFFIX)) {
				if(relation.getPropertyDoubleValue(RelationProperty.AFFIX_GAIN) < config.getExtensionGainTh()) {
					watchVariationRemoval(variant, base, 
							"Removing variant <%s> because the extension gain score <%.2f> is under threshhold <%.2f>.",
							relation.getPropertyDoubleValue(RelationProperty.AFFIX_GAIN), this.config.getExtensionGainTh());
					watchVariationRemoval(base, variant, 
							"Removing as variant of term <%s> because the extension gain score <%.2f> is under threshhold <%.2f>.",
							relation.getPropertyDoubleValue(RelationProperty.AFFIX_GAIN), this.config.getExtensionGainTh());
					
					return true;
				} else if(relation.getPropertyDoubleValue(RelationProperty.AFFIX_SPEC) < config.getExtensionSpecTh()) {
					watchVariationRemoval(variant, base, 
							"Removing variant <%s> because the extension specificity score <%.2f> is under threshhold <%.2f>.",
							relation.getPropertyDoubleValue(RelationProperty.AFFIX_SPEC), this.config.getExtensionSpecTh());
					watchVariationRemoval(base, variant, 
							"Removing as variant of term <%s> because the extension specificity score <%.2f> is under threshhold <%.2f>.",
							relation.getPropertyDoubleValue(RelationProperty.AFFIX_SPEC), this.config.getExtensionSpecTh());
					
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
