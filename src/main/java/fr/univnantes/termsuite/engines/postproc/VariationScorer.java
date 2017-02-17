
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

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.slf4j.Logger;

import fr.univnantes.termsuite.engines.SimpleEngine;
import fr.univnantes.termsuite.framework.InjectLogger;
import fr.univnantes.termsuite.framework.service.RelationService;
import fr.univnantes.termsuite.framework.service.TermService;
import fr.univnantes.termsuite.index.Terminology;
import fr.univnantes.termsuite.metrics.LinearNormalizer;
import fr.univnantes.termsuite.metrics.MinMaxNormalizer;
import fr.univnantes.termsuite.metrics.Normalizer;
import fr.univnantes.termsuite.model.RelationProperty;
import fr.univnantes.termsuite.model.RelationType;
import fr.univnantes.termsuite.model.TermWord;
import fr.univnantes.termsuite.utils.StringUtils;

/**
 * Scores all variations in a {@link Terminology}
 * 
 * @author Damien Cram
 *
 */
public class VariationScorer extends SimpleEngine {
	@InjectLogger Logger logger;

	@Override
	public void execute() {

		logger.debug("Computing {}", RelationProperty.VARIANT_BAG_FREQUENCY);
		doVariationFrenquencies();

		logger.debug("Computing {}", RelationProperty.SOURCE_GAIN);
		doSourceGains();
		
		logger.debug("Computing {}", RelationProperty.NORMALIZED_SOURCE_GAIN);
		normalizeSourceGain();
		
		logger.debug("Computing {}", RelationProperty.EXTENSION_SCORE);
		scoreExtensions();

		logger.debug("Computing {}", RelationProperty.NORMALIZED_EXTENSION_SCORE);
		normalizeExtensionScores();
		
		logger.debug("Computing {}", RelationProperty.VARIANT_SCORE);
		doVariantScores();
	}

	private void normalizeSourceGain() {
		final Normalizer normalizer = new LinearNormalizer(0.33, 1);
		
		terminology.relations()
			.filter(r -> r.isPropertySet(RelationProperty.SOURCE_GAIN))
			.forEach(r -> 
				r.setProperty(RelationProperty.NORMALIZED_SOURCE_GAIN, 
						normalizer.normalize(r.getDouble(RelationProperty.SOURCE_GAIN))));
	}

	private void normalizeExtensionScores() {
		Predicate<? super RelationService> extensionSet = r -> r.isPropertySet(RelationProperty.EXTENSION_SCORE);
		if(!terminology.relations().filter(extensionSet).findFirst().isPresent()) {
			logger.warn("Found no relation with property {} set.", extensionSet);
			return;
		}
			
		final Normalizer normalizer = new MinMaxNormalizer(terminology
				.relations()
				.filter(extensionSet)
				.map(r -> r.getDouble(RelationProperty.EXTENSION_SCORE))
				.collect(Collectors.toList()));
		
		terminology
			.relations()
			.filter(r -> r.isPropertySet(RelationProperty.EXTENSION_SCORE))
			.forEach(r -> 
					r.setProperty(RelationProperty.NORMALIZED_EXTENSION_SCORE, 
							normalizer.normalize(r.getDouble(RelationProperty.EXTENSION_SCORE))));
	}

	public void doVariantScores() {
		
		/*
		 * Non-semantic variations
		 */
		terminology.variations()
			.filter(RelationService::notSemantic)
			.forEach( variation -> {
				double score = variation.isPropertySet(RelationProperty.NORMALIZED_EXTENSION_SCORE) ?
							0.91*variation.getDouble(RelationProperty.NORMALIZED_EXTENSION_SCORE) :
							0.89 + 0.1*variation.getDouble(RelationProperty.NORMALIZED_SOURCE_GAIN);
				variation.setProperty(
					RelationProperty.VARIANT_SCORE, 
					score);
		});
		
		/*
		 * Semantic variations
		 */
		terminology.variations()
			.filter(RelationService::isSemantic)
			.forEach( variation -> {
				/*
				 * The score of a semantic variation is :
				 *  - 0.75 if the variation is dico
				 *  - 0.75*similarity otherwise
				 * 
				 */
				
				double score = 0.75;
				if(variation.getBoolean(RelationProperty.IS_DISTRIBUTIONAL)) {
					score *= variation.getDouble(RelationProperty.SEMANTIC_SIMILARITY);
				}
				
				variation.setProperty(
					RelationProperty.VARIANT_SCORE, 
					score);
			});

	}

	
	private int recursiveDoVariationFrenquencies(RelationService rel, Set<TermService> visitedTerms) {
		TermService term = rel.getTo();
		if(visitedTerms.contains(term)) {
			return 0;
		} else {
			Set<TermService> newVisitedTerms = new HashSet<>();
			newVisitedTerms.addAll(visitedTerms);
			newVisitedTerms.add(term);
			newVisitedTerms.add(rel.getFrom());
			AtomicInteger sum = new AtomicInteger(term.getFrequency());
			term.variations()
				// exclude semantic variations, otherwise there are too many variation pathes
				.filter(RelationService::notSemantic)
				.forEach(v2 -> {
					if(!v2.getBoolean(RelationProperty.IS_EXTENSION)) {
						sum.addAndGet(recursiveDoVariationFrenquencies(v2, newVisitedTerms));
					}
			});
			return sum.get();
		}
	}

	public void doVariationFrenquencies() {
		terminology.variations()
			.forEach(r -> r.setProperty(RelationProperty.VARIANT_BAG_FREQUENCY, r.getTo().getFrequency()));

		terminology.variations()
			// exclude semantic variations, otherwise there are too many variation pathes
			.filter(RelationService::notSemantic)
			.forEach(v1 -> {
				v1.setProperty(
					RelationProperty.VARIANT_BAG_FREQUENCY, 
					recursiveDoVariationFrenquencies(v1, new HashSet<>()));
		});
	}

	public void doSourceGains() {
		terminology.variations()
			.forEach( relation -> {
				double sourceGain = Math.log10((double)relation.getInteger(RelationProperty.VARIANT_BAG_FREQUENCY)/relation.getFrom().getFrequency());
				relation.setProperty(RelationProperty.SOURCE_GAIN, 
					sourceGain);

		});
	}

	public void scoreExtensions() {
		if(!terminology.extensions().findAny().isPresent()) {
			logger.warn("No {} relation set. Cannot score extensions.", RelationType.HAS_EXTENSION);
			return;
		}
		
		for(TermService from:terminology.getTerms()) {
			Set<TermService> extensions = from
					.outboundRelations(RelationType.HAS_EXTENSION)
					.map(RelationService::getTo)
					.collect(Collectors.toSet());
			from.variations().forEach( variation -> {
				if(extensions.contains(variation.getTo())) {
					Optional<TermService> affix = terminology.getExtensionAffix(
							from.getTerm(), 
							variation.getTo().getTerm());
					
					if(!affix.isPresent()) {
						variation.setProperty(
								RelationProperty.HAS_EXTENSION_AFFIX, 
								false);
						
						return;
					} else 
						variation.setProperty(
								RelationProperty.HAS_EXTENSION_AFFIX, 
								true);
					
					double affixGain = Math.log10((double)variation.getTo().getFrequency()/affix.get().getFrequency());
					variation.setProperty(RelationProperty.AFFIX_GAIN, 
							affixGain);

					double affixRatio = Math.log10((double)affix.get().getFrequency()/from.getFrequency());
					variation.setProperty(RelationProperty.AFFIX_RATIO, 
							affixRatio);

					
					double affixSpec = affix.get().getSpecificity();
					variation.setProperty(RelationProperty.AFFIX_SPEC, 
							affixSpec);

					double affixScore = (3*affixSpec + 2*affixGain) / 5;
					variation.setProperty(RelationProperty.AFFIX_SCORE, 
							affixScore);

					double orthographicScore = 1d;
					for(TermWord tw:affix.get().getWords())
						orthographicScore = orthographicScore * StringUtils.getOrthographicScore(tw.getWord().getLemma());
					variation.setProperty(RelationProperty.AFFIX_ORTHOGRAPHIC_SCORE, 
							orthographicScore);
					
					
				} 
			});
		} // end for terms
		

		// normalize affix score
		List<Double> affixScores = terminology
				.relations()
				.filter(r -> r.isPropertySet(RelationProperty.AFFIX_SCORE))
				.map(r -> r.getDouble(RelationProperty.AFFIX_SCORE))
				.collect(Collectors.toList());
		if(!affixScores.isEmpty()) {
			final Normalizer affixScoreNormalizer = new MinMaxNormalizer(affixScores);
			
			terminology
				.relations()
				.filter(r -> r.isPropertySet(RelationProperty.AFFIX_SCORE))
				.forEach(r -> {
						r.setProperty(
									RelationProperty.NORMALIZED_AFFIX_SCORE, 
									affixScoreNormalizer.normalize(r.getDouble(RelationProperty.AFFIX_SCORE)));
					}
				);
		}

		// compute global extension score
		terminology.relations()
			.filter(rel -> rel.isPropertySet(RelationProperty.HAS_EXTENSION_AFFIX) && rel.getBoolean(RelationProperty.HAS_EXTENSION_AFFIX))
			.forEach(rel -> {
				double extensionScore = (rel.getDouble(RelationProperty.NORMALIZED_SOURCE_GAIN)
								+ 0.3 * rel.getDouble(RelationProperty.NORMALIZED_AFFIX_SCORE));
					
				rel.setProperty(RelationProperty.EXTENSION_SCORE, 
						extensionScore);
		});
	}
}
