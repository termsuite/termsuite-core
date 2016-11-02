
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

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import eu.project.ttc.history.TermHistory;
import eu.project.ttc.models.Term;
import eu.project.ttc.models.TermIndex;
import eu.project.ttc.models.scored.ScoredModel;
import eu.project.ttc.models.scored.ScoredTerm;
import eu.project.ttc.models.scored.ScoredVariation;
import eu.project.ttc.utils.StringUtils;

/**
 * Turn a {@link TermIndex} to a {@link ScoredModel}
 * 
 * @author Damien Cram
 *
 */
public class TermVariationScorer {
	private static final Logger LOGGER = LoggerFactory.getLogger(TermVariationScorer.class);

	private ScoredModel scoredModel;

	private VariantScorerConfig config;
	
	private TermHistory history;
	
	public TermVariationScorer(VariantScorerConfig config) {
		super();
		this.config = config;
	}

	public void setHistory(TermHistory history) {
		this.history = history;
	}
	
	private static Comparator<ScoredTerm> wrComparator = new Comparator<ScoredTerm>() {
		@Override
		public int compare(ScoredTerm o1, ScoredTerm o2) {
			return Double.compare(o2.getWRLog(), o1.getWRLog());
		}
	};

	private static Comparator<ScoredVariation> variationScoreComparator = new Comparator<ScoredVariation>() {
		@Override
		public int compare(ScoredVariation o1, ScoredVariation o2) {
			return ComparisonChain.start()
					.compare(o2.getVariationScore(), o1.getVariationScore())
					.compare(o1.getTerm().getGroupingKey(), o2.getTerm().getGroupingKey())
					.compare(o1.getVariant().getTerm().getGroupingKey(), o2.getVariant().getTerm().getGroupingKey())
					.result();
			
		}
	};
	
	public ScoredModel score(TermIndex termIndex) {
		LOGGER.debug("Scorying term index {}", termIndex.getName());

		// Filter terms with bad orthgraph
		doScoredModel(termIndex);

		// rank score model
		int rank = 0;
		for(ScoredTerm t:scoredModel.getTerms()) {
			rank ++;
			t.setRank(rank);
			if(history != null && history.isWatched(t.getTerm()))
				history.saveEvent(
						t.getTerm().getGroupingKey(), 
						this.getClass(), 
						"Set term rank: " + rank);
		}
		return scoredModel;
		
	}

	private void doScoredModel(TermIndex termIndex) {
		scoredModel = new ScoredModel();
		scoredModel.importTermIndex(termIndex);
		scoredModel.sort(wrComparator);
		
		int size = scoredModel.getTerms().size();
		filterTerms();
		LOGGER.debug("Filtered {} terms out of {}", size - scoredModel.getTerms().size(), size);
		
		int sizeBefore = 0;
		int sizeAfter = 0;
		for(ScoredTerm t:scoredModel.getTerms()) {
			if(t.getVariations().isEmpty())
				continue;
			List<ScoredVariation> sv = Lists.newArrayListWithExpectedSize(t.getVariations().size());
			sv.addAll(t.getVariations());
			sizeBefore += sv.size();
			filterVariations(sv);
			sizeAfter += sv.size();
			Collections.sort(sv, variationScoreComparator);
			t.setVariations(sv);
		}
		
		LOGGER.debug("Filtered {} variants out of {}", sizeBefore - sizeAfter, sizeBefore);
		scoredModel.sort(wrComparator);
	}

	private void filterTerms() {
		Set<ScoredTerm> rem = Sets.newHashSet();
		for(ScoredTerm st:scoredModel.getTerms()) {
			if(StringUtils.getOrthographicScore(st.getTerm().getLemma()) < this.config.getOrthographicScoreTh()) {
				rem.add(st);
				
				if(history != null && history.isWatched(st.getTerm()))
					history.saveEvent(
							st.getTerm().getGroupingKey(), 
							this.getClass(), 
							String.format(
									"Removing term because orthographic score <%.2f> is under threshhold <%.2f>.",
									StringUtils.getOrthographicScore(st.getTerm().getLemma()),
									this.config.getOrthographicScoreTh()));

			}
			else if(st.getTermIndependanceScore() < this.config.getTermIndependanceTh()) {
				rem.add(st);
				
				if(history != null && history.isWatched(st.getTerm()))
					history.saveEvent(
							st.getTerm().getGroupingKey(), 
							this.getClass(), 
							String.format(
									"Removing term because independence score <%.2f> is under threshhold <%.2f>.",
									st.getTermIndependanceScore(),
									this.config.getTermIndependanceTh()));
			}
		}
		scoredModel.removeTerms(rem);
	}

	private void filterVariations(List<ScoredVariation>  inputTerms) {
		Iterator<ScoredVariation> it = inputTerms.iterator();
		ScoredVariation v;
		while(it.hasNext()) {
			v = it.next();
			Term variant = v.getTermVariation().getTo();
			Term base = v.getTermVariation().getFrom();
			
			if(v.getVariantIndependanceScore() < config.getVariantIndependanceTh()) {
				watchVariationRemoval(variant, base, 
						"Removing variant <%s> because the variant independence score <%.2f> is under threshhold <%.2f>.",
						v.getVariantIndependanceScore(), this.config.getVariantIndependanceTh());
				watchVariationRemoval(base, variant, 
						"Removed as variant of term <%s> because the variant independence score <%.2f> is under threshhold <%.2f>.",
						v.getVariantIndependanceScore(), this.config.getVariantIndependanceTh());

				it.remove();
			} else if(v.getVariationScore() < config.getVariationScoreTh()) {
				watchVariationRemoval(variant, base, 
						"Removing variant <%s> because the variation score <%.2f> is under threshhold <%.2f>.",
						v.getVariationScore(), this.config.getVariationScoreTh());
				watchVariationRemoval(base, variant, 
						"Removed as variant of term <%s> because the variation score <%.2f> is under threshhold <%.2f>.",
						v.getVariationScore(), this.config.getVariationScoreTh());
				it.remove();
			} else if(v.getExtensionAffix() != null) {
				if(v.getExtensionGainScore() < config.getExtensionGainTh()) {
					watchVariationRemoval(variant, base, 
							"Removing variant <%s> because the extension gain score <%.2f> is under threshhold <%.2f>.",
							v.getExtensionGainScore(), this.config.getExtensionGainTh());
					watchVariationRemoval(base, variant, 
							"Removing as variant of term <%s> because the extension gain score <%.2f> is under threshhold <%.2f>.",
							v.getExtensionGainScore(), this.config.getExtensionGainTh());
					
					it.remove();
				} else if(v.getExtensionSpecScore() < config.getExtensionSpecTh()) {
					watchVariationRemoval(variant, base, 
							"Removing variant <%s> because the extension specificity score <%.2f> is under threshhold <%.2f>.",
							v.getExtensionGainScore(), this.config.getExtensionGainTh());
					watchVariationRemoval(base, variant, 
							"Removing as variant of term <%s> because the extension specificity score <%.2f> is under threshhold <%.2f>.",
							v.getExtensionGainScore(), this.config.getExtensionGainTh());
					
					it.remove();
				}
			}
		}
		
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
