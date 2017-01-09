
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
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Stopwatch;

import fr.univnantes.termsuite.engines.gatherer.VariationType;
import fr.univnantes.termsuite.framework.TerminologyService;
import fr.univnantes.termsuite.metrics.LinearNormalizer;
import fr.univnantes.termsuite.metrics.MinMaxNormalizer;
import fr.univnantes.termsuite.metrics.Normalizer;
import fr.univnantes.termsuite.model.RelationProperty;
import fr.univnantes.termsuite.model.RelationType;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.TermProperty;
import fr.univnantes.termsuite.model.TermRelation;
import fr.univnantes.termsuite.model.TermWord;
import fr.univnantes.termsuite.model.Terminology;
import fr.univnantes.termsuite.utils.StringUtils;
import fr.univnantes.termsuite.utils.TermUtils;

/**
 * Scores all variations in a {@link Terminology}
 * 
 * @author Damien Cram
 *
 */
public class VariationScorer {
	private static final Logger LOGGER = LoggerFactory.getLogger(VariationScorer.class);
	private static final Predicate<? super TermRelation> NOT_SEMANTIC = v -> 
			v.get(RelationProperty.VARIATION_TYPE) != VariationType.SEMANTIC 
			&& (v.get(RelationProperty.VARIATION_TYPE) != VariationType.INFERENCE 
					|| !v.getPropertyBooleanValue(RelationProperty.IS_SEMANTIC));
	private static final Predicate<? super TermRelation> SEMANTIC = v -> 
		v.get(RelationProperty.VARIATION_TYPE) == VariationType.SEMANTIC 
		|| (v.get(RelationProperty.VARIATION_TYPE) == VariationType.INFERENCE 
				&& v.getPropertyBooleanValue(RelationProperty.IS_SEMANTIC));

	public void score(Terminology termino) {
		LOGGER.info("Computing scores for variations");
		Stopwatch sw = Stopwatch.createStarted();

		TerminologyService terminoService = new TerminologyService(termino);
		
//		LOGGER.debug("Computing {}", RelationProperty.STRICTNESS);
//		doStrictnesses(terminoService);
		
		LOGGER.debug("Computing {}", RelationProperty.VARIANT_BAG_FREQUENCY);
		doVariationFrenquencies(terminoService);

		LOGGER.debug("Computing {}", RelationProperty.SOURCE_GAIN);
		doSourceGains(terminoService);
		
		LOGGER.debug("Computing {}", RelationProperty.NORMALIZED_SOURCE_GAIN);
		normalizeSourceGain(termino);
		
		LOGGER.debug("Computing {}", RelationProperty.EXTENSION_SCORE);
		scoreExtensions(termino);

		LOGGER.debug("Computing {}", RelationProperty.NORMALIZED_EXTENSION_SCORE);
		normalizeExtensionScores(termino);
		
		LOGGER.debug("Computing {}", RelationProperty.VARIANT_SCORE);
		doVariantScores(terminoService);
		
		sw.stop();
		LOGGER.debug("Scores computed in {}", sw);
	}

	private void normalizeSourceGain(Terminology termino) {
		final Normalizer normalizer = new LinearNormalizer(0.33, 1);
		
		termino.getRelations()
			.filter(r -> r.isPropertySet(RelationProperty.SOURCE_GAIN))
			.forEach(r -> 
				r.setProperty(RelationProperty.NORMALIZED_SOURCE_GAIN, 
						normalizer.normalize(r.getPropertyDoubleValue(RelationProperty.SOURCE_GAIN))));
	}

	private void normalizeExtensionScores(Terminology termino) {
		final Normalizer normalizer = new MinMaxNormalizer(termino
				.getRelations()
				.filter(r -> r.isPropertySet(RelationProperty.EXTENSION_SCORE))
				.map(r -> r.getPropertyDoubleValue(RelationProperty.EXTENSION_SCORE))
				.collect(Collectors.toList()));
		
		termino.getRelations()
			.filter(r -> r.isPropertySet(RelationProperty.EXTENSION_SCORE))
			.forEach(r -> 
					r.setProperty(RelationProperty.NORMALIZED_EXTENSION_SCORE, 
							normalizer.normalize(r.getPropertyDoubleValue(RelationProperty.EXTENSION_SCORE))));
	}

	public void doVariantScores(TerminologyService termino) {
		
		/*
		 * Non-semantic variations
		 */
		termino.variations()
			.filter(NOT_SEMANTIC)
			.forEach( variation -> {
				double score = variation.isPropertySet(RelationProperty.NORMALIZED_EXTENSION_SCORE) ?
							0.91*variation.getPropertyDoubleValue(RelationProperty.NORMALIZED_EXTENSION_SCORE) :
							0.89 + 0.1*variation.getPropertyDoubleValue(RelationProperty.NORMALIZED_SOURCE_GAIN);
				variation.setProperty(
					RelationProperty.VARIANT_SCORE, 
					score);
		});
		
		/*
		 * Semantic variations
		 */
		termino.variations()
			.filter(SEMANTIC)
			.forEach( variation -> {
				/*
				 * The score of a semantic variation is :
				 *  - 0.75 if the variation is dico
				 *  - 0.75*similarity otherwise
				 * 
				 */
				
				double score = 0.75;
				if(!variation.isPropertySet(RelationProperty.IS_DISTRIBUTIONAL))
					System.out.println(variation.getProperties());
				if(variation.getPropertyBooleanValue(RelationProperty.IS_DISTRIBUTIONAL)) {
					score *= variation.getPropertyDoubleValue(RelationProperty.SEMANTIC_SIMILARITY);
				}
				
				variation.setProperty(
					RelationProperty.VARIANT_SCORE, 
					score);
			});

	}

	
	private int recursiveDoVariationFrenquencies(TerminologyService termino, TermRelation rel, Set<Term> visitedTerms) {
		Term term = rel.getTo();
		if(visitedTerms.contains(term)) {
			return 0;
		} else {
			Set<Term> newVisitedTerms = new HashSet<>();
			newVisitedTerms.addAll(visitedTerms);
			newVisitedTerms.add(term);
			newVisitedTerms.add(rel.getFrom());
			AtomicInteger sum = new AtomicInteger(term.getFrequency());
			termino.variationsFrom(term)
				// exclude semantic variations, otherwise there are too many variation pathes
				.filter(NOT_SEMANTIC)
				.forEach(v2 -> {
					if(!v2.getPropertyBooleanValue(RelationProperty.IS_EXTENSION)) {
						sum.addAndGet(recursiveDoVariationFrenquencies(termino, v2, newVisitedTerms));
					}
			});
			return sum.get();
		}
	}

	public void doVariationFrenquencies(TerminologyService termino) {
		termino.variations()
			// exclude semantic variations, otherwise there are too many variation pathes
			.filter(NOT_SEMANTIC)
			.forEach(v1 -> {
				v1.setProperty(
					RelationProperty.VARIANT_BAG_FREQUENCY, 
					recursiveDoVariationFrenquencies(termino, v1, new HashSet<>()));
		});
	}

//	public void doStrictnesses(TerminologyService termino) {
//		termino.variations()
//			.filter(NOT_SEMANTIC)
//			.forEach( relation -> {
//				relation.setProperty(RelationProperty.STRICTNESS, 
//					TermUtils.getStrictness(
//							termino.getTerminology().getOccurrenceStore(), 
//					relation.getTo(), 
//					relation.getFrom()));
//		});
//		
//	}
	public void doSourceGains(TerminologyService termino) {
		termino.variations()
			.forEach( relation -> {
				double sourceGain = Math.log10((double)relation.getPropertyIntegerValue(RelationProperty.VARIANT_BAG_FREQUENCY)/relation.getFrom().getFrequency());
				relation.setProperty(RelationProperty.SOURCE_GAIN, 
					sourceGain);

		});
	}

	public void scoreExtensions(Terminology termino) {
		if(!termino.getRelations(RelationType.HAS_EXTENSION).findAny().isPresent()) {
			LOGGER.warn("No {} relation set. Cannot score extensions.", RelationType.HAS_EXTENSION);
			return;
		}
		
		Term affix;
		Set<Term> extensions;
		for(Term from:termino.getTerms()) {
			extensions = termino.getOutboundRelations(from, RelationType.HAS_EXTENSION)
					.stream()
					.map(TermRelation::getTo)
					.collect(Collectors.toSet());
			for(TermRelation variation:termino.getOutboundRelations(from, RelationType.VARIATION)) {
				if(extensions.contains(variation.getTo())) {
					affix = TermUtils.getExtensionAffix(
							termino,
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
					
					double affixGain = Math.log10((double)variation.getTo().getFrequency()/affix.getFrequency());
					variation.setProperty(RelationProperty.AFFIX_GAIN, 
							affixGain);

					double affixRatio = Math.log10((double)affix.getFrequency()/from.getFrequency());
					variation.setProperty(RelationProperty.AFFIX_RATIO, 
							affixRatio);

					
					double affixSpec = (Double)affix.getPropertyValue(TermProperty.SPECIFICITY);
					variation.setProperty(RelationProperty.AFFIX_SPEC, 
							affixSpec);

					double affixScore = (3*affixSpec + 2*affixGain) / 5;
					variation.setProperty(RelationProperty.AFFIX_SCORE, 
							affixScore);

					double orthographicScore = 1d;
					for(TermWord tw:affix.getWords())
						orthographicScore = orthographicScore * StringUtils.getOrthographicScore(tw.getWord().getLemma());
					variation.setProperty(RelationProperty.AFFIX_ORTHOGRAPHIC_SCORE, 
							orthographicScore);
					
					
				} 
			}
		} // end for terms
		

		// normalize affix score
		List<Double> affixScores = termino
				.getRelations()
				.filter(r -> r.isPropertySet(RelationProperty.AFFIX_SCORE))
				.map(r -> r.getPropertyDoubleValue(RelationProperty.AFFIX_SCORE))
				.collect(Collectors.toList());
		if(!affixScores.isEmpty()) {
			final Normalizer affixScoreNormalizer = new MinMaxNormalizer(affixScores);
			
			termino
				.getRelations()
				.filter(r -> r.isPropertySet(RelationProperty.AFFIX_SCORE))
				.forEach(r -> {
						r.setProperty(
									RelationProperty.NORMALIZED_AFFIX_SCORE, 
									affixScoreNormalizer.normalize(r.getPropertyDoubleValue(RelationProperty.AFFIX_SCORE)));
					}
				);
		}

		// compute global extension score
		termino.getRelations()
			.filter(rel -> rel.isPropertySet(RelationProperty.HAS_EXTENSION_AFFIX) && rel.getPropertyBooleanValue(RelationProperty.HAS_EXTENSION_AFFIX))
			.forEach(rel -> {
				double extensionScore = (rel.getPropertyDoubleValue(RelationProperty.NORMALIZED_SOURCE_GAIN)
								+ 0.3 * rel.getPropertyDoubleValue(RelationProperty.NORMALIZED_AFFIX_SCORE));
					
				rel.setProperty(RelationProperty.EXTENSION_SCORE, 
						extensionScore);
		});
	}
}
