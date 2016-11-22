
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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.primitives.Doubles;

import eu.project.ttc.engines.ExtensionDetecter;
import eu.project.ttc.models.RelationProperty;
import eu.project.ttc.models.RelationType;
import eu.project.ttc.models.Term;
import eu.project.ttc.models.TermIndex;
import eu.project.ttc.models.TermProperty;
import eu.project.ttc.models.TermRelation;
import eu.project.ttc.models.TermWord;
import eu.project.ttc.utils.StringUtils;
import eu.project.ttc.utils.TermUtils;

/**
 * Scores all variations in a {@link TermIndex}
 * 
 * @author Damien Cram
 *
 */
public class VariationScorer {
	private static final Logger LOGGER = LoggerFactory.getLogger(VariationScorer.class);
	
	public void score(TermIndex termIndex) {
		LOGGER.debug("Scorying variations in term index {}", termIndex.getName());

		doExtensionScores(termIndex);
		doFrequencyScores(termIndex);
		doStrictnessScores(termIndex);
		doVariantIndependanceScore(termIndex);
		doVariantScores(termIndex);
	}

	public void doVariantScores(TermIndex termIndex) {
		termIndex.getRelations(RelationType.VARIATIONS).forEach( relation -> {
			double extensionScore = relation.getPropertyDoubleValue(RelationProperty.EXTENSION_SCORE) != null ?
					0.75*relation.getPropertyDoubleValue(RelationProperty.EXTENSION_SCORE) + 0.25*relation.getPropertyDoubleValue(RelationProperty.FREQUENCY_RATIO) :
						1;
			double score = relation.getPropertyDoubleValue(RelationProperty.STRICTNESS) == 1d ?
					0.9 + 0.1*relation.getPropertyDoubleValue(RelationProperty.FREQUENCY_RATIO) :
						extensionScore;
			relation.setProperty(
					RelationProperty.VARIANT_SCORE, 
					score);
		});
	}

	public void doVariantIndependanceScore(TermIndex termIndex) {
		for(Term term:termIndex.getTerms()) {
			Collection<TermRelation> variations = termIndex.getOutboundRelations(term, RelationType.VARIATIONS);
			
			for(TermRelation variation:variations) {
				int inclCount = 0;
				for(TermRelation otherVariation:variations) {
					if(otherVariation == variation)
						continue;
					if(TermUtils.isIncludedIn(variation.getTo(), otherVariation.getTo()))
						inclCount += otherVariation.getTo().getFrequency();
				}
				double variantIndependance = 1 - Doubles.min(1d,((double)inclCount)/variation.getTo().getFrequency());
				variation.setProperty(RelationProperty.VARIANT_INDEPENDANCE, variantIndependance);
			}
		}
	}

	public void doStrictnessScores(TermIndex termIndex) {
		termIndex.getRelations(RelationType.VARIATIONS).forEach( relation -> {
			relation.setProperty(RelationProperty.STRICTNESS, 
					TermUtils.getStrictness(
					termIndex.getOccurrenceStore(), 
					relation.getTo(), 
					relation.getFrom()));
		});
	}

	public void doFrequencyScores(TermIndex termIndex) {
		Map<Term, Integer> maxVariantFrequencies = new HashMap<>();
		for(Term t:termIndex.getTerms()) {
			Optional<Integer> max = termIndex.getOutboundRelations(t, RelationType.VARIATIONS)
				.stream()
				.map(r -> r.getTo().getFrequency())
				.reduce(Integer::max);
			
			if(max.isPresent())
				maxVariantFrequencies.put(
					t, 
					max.get()
				);
		}
		
		termIndex.getRelations(RelationType.VARIATIONS).forEach( relation -> {
			double frequencyRatio = (double)relation.getTo().getFrequency()/maxVariantFrequencies.get(relation.getFrom());

			relation.setProperty(RelationProperty.FREQUENCY_RATIO, frequencyRatio);
		});
	}

	public void doExtensionScores(TermIndex termIndex) {
		if(!termIndex.getRelations(RelationType.HAS_EXTENSION).findAny().isPresent()) {
			LOGGER.info("No {} relation set. Computing extension detection.", RelationType.HAS_EXTENSION);
			new ExtensionDetecter().detectExtensions(termIndex);
		}
		
		Term affix;
		Set<Term> extensions;
		for(Term from:termIndex.getTerms()) {
			extensions = termIndex.getOutboundRelations(from, RelationType.HAS_EXTENSION)
					.stream()
					.map(TermRelation::getTo)
					.collect(Collectors.toSet());
			for(TermRelation variation:termIndex.getOutboundRelations(from, RelationType.VARIATIONS)) {
				if(extensions.contains(variation.getTo())) {
					affix = TermUtils.getExtensionAffix(
							termIndex,
							from, 
							variation.getTo());
					
					if(affix == null) {
						variation.setProperty(
								RelationProperty.HAS_EXTENSION_AFFIX, 
								false);
						
						continue;
					} else 
						variation.setProperty(
								RelationProperty.HAS_EXTENSION_AFFIX, 
								true);
					
					double affixGain = (double)from.getFrequency()/affix.getFrequency();
					variation.setProperty(RelationProperty.AFFIX_GAIN, 
							affixGain);

					
					double affixSpec = (double)affix.getPropertyValue(TermProperty.SPECIFICITY);
					variation.setProperty(RelationProperty.AFFIX_SPEC, 
							affixSpec);
					
					
					double orthographicScore = 1d;
					for(TermWord tw:affix.getWords())
						orthographicScore = orthographicScore * StringUtils.getOrthographicScore(tw.getWord().getLemma());
					variation.setProperty(RelationProperty.AFFIX_ORTHOGRAPHIC_SCORE, 
							orthographicScore);
					
					
					double root = 2d;
					double w = 3d; // gain weight
					double extensionScore = orthographicScore * Math.pow((w*Math.pow(affixGain, root) + Math.pow(affixSpec, root))/(1+w),1d/root);

					variation.setProperty(RelationProperty.EXTENSION_SCORE, 
							extensionScore);
				} 
			}
		}
		
		
		
	}
}
