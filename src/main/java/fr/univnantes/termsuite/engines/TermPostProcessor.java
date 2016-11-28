
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

		
		// Filter terms with bad orthograph and no independance
		Set<Term> remTerms = termIndex.getTerms().stream()
			.filter(this::filterTermByThresholds)
			.collect(Collectors.toSet());
		LOGGER.debug("Filtered {} terms out of {} terms", remTerms.size(), termIndex.getTerms().size());
		remTerms
			.stream()
			.forEach(termIndex::removeTerm);
		
		// Detect 2-order extension variations
		filterTwoOrderVariations(termIndex);
		
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

	private void filterTwoOrderVariations(TermIndex termIndex) {
		termIndex.getTerms().stream()
			.sorted(TermProperty.FREQUENCY.getComparator(true))
			.forEach(term -> {
				final Map<Term, TermRelation> variants = new HashMap<>();

				for(TermRelation rel:termIndex.getOutboundRelations(term, RelationType.SYNTAG_VARIATIONS)) {
					if(rel.getPropertyBooleanValue(RelationProperty.IS_EXTENSION))
						variants.put(rel.getTo(), rel);
				}
				
				final Set<TermRelation> order2Rels = new HashSet<>();
				
				variants.keySet().forEach(variant-> {
					termIndex
						.getOutboundRelations(variant, RelationType.SYNTAG_VARIATIONS)
						.stream()
						.filter(order2Rel -> order2Rel.getPropertyBooleanValue(RelationProperty.IS_EXTENSION))
						.filter(order2Rel -> variants.containsKey(order2Rel.getTo()))
						.forEach(order2Rels::add)
					;
				});
				
				order2Rels.forEach(rel -> {
					TermRelation r = variants.get(rel.getTo());
					if(LOGGER.isDebugEnabled()) {
						LOGGER.debug("Found order-2 relation in variation set {}-->{}-->{}", term, rel.getFrom(), rel.getTo());
						LOGGER.debug("Removing {}", r);
					}
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
		
		LOGGER.debug("Removing {} extension targets from term index", remTargets.size());
		remTargets.stream().map(TermRelation::getTo).forEach(termIndex::removeTerm);
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
