
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

package eu.project.ttc.termino.engines;

import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.project.ttc.history.TermHistory;
import eu.project.ttc.models.RelationProperty;
import eu.project.ttc.models.RelationType;
import eu.project.ttc.models.Term;
import eu.project.ttc.models.TermIndex;
import eu.project.ttc.models.TermProperty;
import eu.project.ttc.models.TermRelation;
import eu.project.ttc.utils.StringUtils;

/**
 * Turn a {@link TermIndex} to a {@link ScoredModel}
 * 
 * @author Damien Cram
 *
 */
public class TermPostProcessor {
	private static final Logger LOGGER = LoggerFactory.getLogger(TermPostProcessor.class);

	private VariantScorerConfig config;
	
	private TermHistory history;
	
	public TermPostProcessor(VariantScorerConfig config) {
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
		
		// Filter terms with bad orthograph and no independance
		Set<Term> remTerms = termIndex.getTerms().stream()
			.filter(this::filterTerm)
			.collect(Collectors.toSet());
		LOGGER.debug("Filtered {} terms out of {} terms", remTerms.size(), termIndex.getTerms().size());
		remTerms
			.stream()
			.forEach(termIndex::removeTerm);

		// Score variations
		new VariationScorer().score(termIndex);
		
		// Filter variations
		Set<TermRelation> remRelations = termIndex.getRelations(RelationType.VARIATIONS)
			.filter(this::filterVariation)
			.collect(Collectors.toSet());
		LOGGER.debug("Filtered {} variants out of {} variants", remRelations.size(), termIndex.getRelations(RelationType.VARIATIONS).count());
		
		remRelations
			.stream()
			.forEach(termIndex::removeRelation);

		
		// rank score model
//		for(ScoredTerm t:scoredModel.getTerms()) {
//			rank ++;
//			t.setRank(rank);
//			if(history != null && history.isWatched(t.getTerm()))
//				history.saveEvent(
//						t.getTerm().getGroupingKey(), 
//						this.getClass(), 
//						"Set term rank: " + rank);
//		}
	}

	private boolean filterTerm(Term term) {
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
		if(relation.getPropertyDoubleValue(RelationProperty.VARIANT_INDEPENDANCE) < config.getVariantIndependanceTh()) {
			watchVariationRemoval(variant, base, 
					"Removing variant <%s> because the variant independence score <%.2f> is under threshhold <%.2f>.",
					relation.getPropertyDoubleValue(RelationProperty.VARIANT_INDEPENDANCE), this.config.getVariantIndependanceTh());
			watchVariationRemoval(base, variant, 
					"Removed as variant of term <%s> because the variant independence score <%.2f> is under threshhold <%.2f>.",
					relation.getPropertyDoubleValue(RelationProperty.VARIANT_INDEPENDANCE), this.config.getVariantIndependanceTh());
			return true;
		} else if(relation.getPropertyDoubleValue(RelationProperty.VARIANT_SCORE) < config.getVariationScoreTh()) {
			watchVariationRemoval(variant, base, 
					"Removing variant <%s> because the variation score <%.2f> is under threshhold <%.2f>.",
					relation.getPropertyDoubleValue(RelationProperty.VARIANT_SCORE), this.config.getVariationScoreTh());
			watchVariationRemoval(base, variant, 
					"Removed as variant of term <%s> because the variation score <%.2f> is under threshhold <%.2f>.",
					relation.getPropertyDoubleValue(RelationProperty.VARIANT_SCORE), this.config.getVariationScoreTh());
			return true;
		} else if(relation.isPropertySet(RelationProperty.AFFIX_GAIN)) {
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
}
