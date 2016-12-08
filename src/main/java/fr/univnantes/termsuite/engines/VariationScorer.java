
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

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Stopwatch;

import fr.univnantes.termsuite.metrics.LinearNormalizer;
import fr.univnantes.termsuite.metrics.MinMaxNormalizer;
import fr.univnantes.termsuite.metrics.Normalizer;
import fr.univnantes.termsuite.model.RelationProperty;
import fr.univnantes.termsuite.model.RelationType;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.TermIndex;
import fr.univnantes.termsuite.model.TermProperty;
import fr.univnantes.termsuite.model.TermRelation;
import fr.univnantes.termsuite.model.TermWord;
import fr.univnantes.termsuite.utils.StringUtils;
import fr.univnantes.termsuite.utils.TermUtils;

/**
 * Scores all variations in a {@link TermIndex}
 * 
 * @author Damien Cram
 *
 */
public class VariationScorer {
	private static final Logger LOGGER = LoggerFactory.getLogger(VariationScorer.class);
	
	public void score(TermIndex termIndex) {
		LOGGER.info("Computing scores for variations");
		Stopwatch sw = Stopwatch.createStarted();

		doRelationScores(termIndex);
		normalizeSourceGain(termIndex);
		doExtensionScores(termIndex);
		normalizeExtensionScores(termIndex);
		doVariantScores(termIndex);
		sw.stop();
		LOGGER.debug("Scores computed in {}", sw);
	}

	private void normalizeSourceGain(TermIndex termIndex) {
		final Normalizer normalizer = new LinearNormalizer(0.33, 1);
		
		termIndex.getRelations()
			.filter(r -> r.isPropertySet(RelationProperty.SOURCE_GAIN))
			.forEach(r -> 
				r.setProperty(RelationProperty.NORMALIZED_SOURCE_GAIN, 
						normalizer.normalize(r.getPropertyDoubleValue(RelationProperty.SOURCE_GAIN))));
	}

	private void normalizeExtensionScores(TermIndex termIndex) {
		final Normalizer normalizer = new MinMaxNormalizer(termIndex
				.getRelations()
				.filter(r -> r.isPropertySet(RelationProperty.EXTENSION_SCORE))
				.map(r -> r.getPropertyDoubleValue(RelationProperty.EXTENSION_SCORE))
				.collect(Collectors.toList()));
		
		termIndex.getRelations()
			.filter(r -> r.isPropertySet(RelationProperty.EXTENSION_SCORE))
			.forEach(r -> 
					r.setProperty(RelationProperty.NORMALIZED_EXTENSION_SCORE, 
							normalizer.normalize(r.getPropertyDoubleValue(RelationProperty.EXTENSION_SCORE))));
	}

	public void doVariantScores(TermIndex termIndex) {
		termIndex.getRelations(RelationType.VARIATIONS).forEach( relation -> {
			double score = relation.isPropertySet(RelationProperty.NORMALIZED_EXTENSION_SCORE) ?
							0.91*relation.getPropertyDoubleValue(RelationProperty.NORMALIZED_EXTENSION_SCORE) :
							0.89 + 0.1*relation.getPropertyDoubleValue(RelationProperty.NORMALIZED_SOURCE_GAIN);
			relation.setProperty(
					RelationProperty.VARIANT_SCORE, 
					score);
		});
	}

	public void doRelationScores(TermIndex termIndex) {
		termIndex.getRelations(RelationType.VARIATIONS).forEach( relation -> {
			double sourceGain = Math.log10((double)relation.getTo().getFrequency()/relation.getFrom().getFrequency());
			relation.setProperty(RelationProperty.SOURCE_GAIN, 
					sourceGain);

			relation.setProperty(RelationProperty.STRICTNESS, 
					TermUtils.getStrictness(
					termIndex.getOccurrenceStore(), 
					relation.getTo(), 
					relation.getFrom()));
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
		List<Double> affixScores = termIndex
				.getRelations()
				.filter(r -> r.isPropertySet(RelationProperty.AFFIX_SCORE))
				.map(r -> r.getPropertyDoubleValue(RelationProperty.AFFIX_SCORE))
				.collect(Collectors.toList());
		if(!affixScores.isEmpty()) {
			final Normalizer affixScoreNormalizer = new MinMaxNormalizer(affixScores);
			
			termIndex
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
		termIndex.getRelations()
			.filter(rel -> rel.isPropertySet(RelationProperty.HAS_EXTENSION_AFFIX) && rel.getPropertyBooleanValue(RelationProperty.HAS_EXTENSION_AFFIX))
			.forEach(rel -> {
				double extensionScore = (rel.getPropertyDoubleValue(RelationProperty.NORMALIZED_SOURCE_GAIN)
								+ 0.3 * rel.getPropertyDoubleValue(RelationProperty.NORMALIZED_AFFIX_SCORE));
					
				rel.setProperty(RelationProperty.EXTENSION_SCORE, 
						extensionScore);
		});
	}
}
