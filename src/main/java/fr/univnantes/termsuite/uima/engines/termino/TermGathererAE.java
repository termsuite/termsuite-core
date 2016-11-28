
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

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright 2, 2015nership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package fr.univnantes.termsuite.uima.engines.termino;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ExternalResource;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;

import fr.univnantes.termsuite.model.RelationProperty;
import fr.univnantes.termsuite.model.RelationType;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.TermIndex;
import fr.univnantes.termsuite.model.TermRelation;
import fr.univnantes.termsuite.model.termino.CustomIndexStats;
import fr.univnantes.termsuite.model.termino.CustomTermIndex;
import fr.univnantes.termsuite.model.termino.TermIndexes;
import fr.univnantes.termsuite.model.termino.TermPair;
import fr.univnantes.termsuite.uima.engines.termino.gathering.VariantRule;
import fr.univnantes.termsuite.uima.engines.termino.gathering.VariantRuleIndex;
import fr.univnantes.termsuite.uima.resources.ObserverResource;
import fr.univnantes.termsuite.uima.resources.ObserverResource.SubTaskObserver;
import fr.univnantes.termsuite.uima.resources.TermHistoryResource;
import fr.univnantes.termsuite.uima.resources.termino.TermIndexResource;
import fr.univnantes.termsuite.uima.resources.termino.YamlVariantRules;

public class TermGathererAE extends JCasAnnotator_ImplBase {
	private static final Logger LOGGER = LoggerFactory.getLogger(TermGathererAE.class);
	public static final String TASK_NAME = "Syntactic variant gathering";
	private static final int OBSERVING_STEP = 1000;
	private static final int WARNING_CRITICAL_SIZE = 2500;
	private static final String M_PREFIX = "M";


	@ExternalResource(key=ObserverResource.OBSERVER, mandatory=false)
	protected ObserverResource observerResource;

	@ExternalResource(key=TermIndexResource.TERM_INDEX, mandatory=true)
	private TermIndexResource termIndexResource;
	
	public static final String YAML_VARIANT_RULES = "YamlVariantRules";
	@ExternalResource(key = YAML_VARIANT_RULES, mandatory = true)
	private YamlVariantRules yamlVariantRules;

	@ExternalResource(key =TermHistoryResource.TERM_HISTORY, mandatory = true)
	private TermHistoryResource historyResource;


	private BigInteger totalComparisons = BigInteger.valueOf(0);
	private int nbComparisons = 0;
	private Optional<SubTaskObserver> taskObserver = Optional.empty();
	private Set<TermPair> foundPairs = new HashSet<>();

	static class RunConfig {
		String indexName;
		VariantRuleIndex variantRuleIndex;
		RunConfig(String indexName, VariantRuleIndex variantRuleIndex) {
			super();
			this.indexName = indexName;
			this.variantRuleIndex = variantRuleIndex;
		}
	}
	
	/*
	 *  Do not deactivate gathering on key_lemma_lemma, otherwise we loose
	 *  morphological gathering based on single-word (with [compound] tag in yaml).
	 *  TODO : understanding why
	 */
	private static final RunConfig[] RUN_CONFIGS = new RunConfig[] {
			new RunConfig(TermIndexes.WORD_COUPLE_LEMMA_LEMMA, VariantRuleIndex.DEFAULT),
			new RunConfig(TermIndexes.WORD_COUPLE_LEMMA_STEM, VariantRuleIndex.DEFAULT),
			new RunConfig(TermIndexes.TERM_HAS_PREFIX_LEMMA, VariantRuleIndex.PREFIX),
			new RunConfig(TermIndexes.TERM_HAS_DERIVATES_LEMMA, VariantRuleIndex.DERIVATION)
	};
	
	@Override
	public void initialize(UimaContext context) throws ResourceInitializationException {
		super.initialize(context);
		this.yamlVariantRules.initialize(this.termIndexResource.getTermIndex());
		if(observerResource != null)
			taskObserver = Optional.of(observerResource.getTaskObserver(TASK_NAME));
	}
	
	@Override
	public void collectionProcessComplete()
			throws AnalysisEngineProcessException {
		LOGGER.info("Starting syntactic term gathering for TermIndex {}", this.termIndexResource.getTermIndex().getName());
		
		TermIndex termIndex = this.termIndexResource.getTermIndex();
		
		if(termIndexResource.getTermIndex().getTerms().isEmpty())
			return;

		/*
		 * Prepare observer and indexes
		 */
		for(RunConfig runConfig:RUN_CONFIGS) {
			CustomTermIndex customIndex = termIndex.getCustomIndex(runConfig.indexName);
			customIndex.cleanSingletonKeys();
			
			// clean biggest classes
			customIndex.cleanEntriesByMaxSize(WARNING_CRITICAL_SIZE);

			CustomIndexStats stats = new CustomIndexStats(customIndex);

			
			// Display class sizes
			Stopwatch sw1 = Stopwatch.createStarted();
			int k = 0;
			LOGGER.debug("Biggest class is {}, size: {}", stats.getBiggestClass(), stats.getBiggestSize());
			
			
			int size;
			for(Integer i:stats.getSizeCounters().keySet()) {
				k ++;
				size = stats.getSizeCounters().get(i).size();
				totalComparisons = totalComparisons.add(BigInteger.valueOf(size * i*(i-1)));
			}
			LOGGER.debug("Number of term pairs to test: " + totalComparisons);
			sw1.stop();
			LOGGER.debug("Time to get the comparisons number: " + sw1.elapsed(TimeUnit.MILLISECONDS));
			LOGGER.debug("Number of classes: " + k);
			if(taskObserver.isPresent())
				taskObserver.get().setTotalTaskWork(totalComparisons.longValue());
		}


		LOGGER.debug("Gathering with default variant rule indexing (source and target patterns)");
		for(RunConfig runConfig:RUN_CONFIGS) {
			gather(runConfig.indexName, runConfig.variantRuleIndex);
			termIndex.dropCustomIndex(runConfig.indexName);
		}
		foundPairs = null;
		
	}
	
	private void gather(final String gatheringKey, VariantRuleIndex variantRuleIndex) {
		LOGGER.debug("Rule-based gathering over the pregathering key {}", gatheringKey);

		// create the index
		CustomTermIndex customIndex = this.termIndexResource.getTermIndex().getCustomIndex(gatheringKey);
		LOGGER.debug("Rule-based gathering over {} classes", customIndex.size());


		// Log the progress every 5 seconds
		Timer progressLoggerTimer = new Timer("Syn. Variant Gathering Timer");
		progressLoggerTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				TermGathererAE.LOGGER.info("progress for key {}: ({}%)",
						gatheringKey,
						String.format("%.2f", ((float)nbComparisons*100)/totalComparisons.longValue())
						);
			}
		}, 5000l, 5000l);
		
		// Do the gathering in each class
		nbComparisons = 0;
		for (String cls : customIndex.keySet()) {
			List<Term> list = customIndex.getTerms(cls);
			List<String> examples = Lists.newLinkedList();
			int cnt =0;
			for(Term t:list) {
				examples.add(t.getGroupingKey());
				cnt++;
				if(cnt > 5)
					break;
			}
			if(list.size() > 1 && LOGGER.isTraceEnabled())
				LOGGER.trace("Rule-based gathering over the '" + cls + "' term class of size " + list.size() + ": " + Joiner.on(" ").join(examples));


			Term source;
			Term target;
			for(ListIterator<Term> sourceIt=list.listIterator(); sourceIt.hasNext();) {
				source=sourceIt.next();
				for(ListIterator<Term> targetIt=list.listIterator(sourceIt.nextIndex()); targetIt.hasNext();) {
					nbComparisons+=2;
					target=targetIt.next();
					
					TermPair pair = new TermPair(source, target);
					if(foundPairs.contains(pair))
						continue;
					
					applyGatheringRules(variantRuleIndex, source, target);
					applyGatheringRules(variantRuleIndex, target, source);
					if(nbComparisons % OBSERVING_STEP == 0) 
						if(taskObserver.isPresent())
							taskObserver.get().work(OBSERVING_STEP);
				}
					
			}
		}
		
		//finalize
		progressLoggerTimer.cancel();
	}

	private void applyGatheringRules(VariantRuleIndex variantRuleIndex, Term source, Term target) {
		VariantRule matchingRule = yamlVariantRules.getMatchingRule(variantRuleIndex, source, target);
		if (matchingRule != null) {
			applyMatchingRule(matchingRule, source, target);
		}
	}

	private void applyMatchingRule(VariantRule matchingRule, Term source, Term target) {
		// Finds the most frequent of both terms
		checkFrequency(source);
		checkFrequency(target);
		
		RelationType relationType = matchingRule.isSynonymicRule() ? 
				RelationType.SYNONYMIC : (
				matchingRule.getName().startsWith(M_PREFIX) ? 
					RelationType.MORPHOLOGICAL : 
					RelationType.SYNTACTICAL);
		TermRelation rel = new TermRelation(
				relationType,
				source,
				target);
		rel.setProperty(RelationProperty.VARIATION_RULE, matchingRule.getName());
		termIndexResource.getTermIndex().addRelation(rel);
		foundPairs.add(new TermPair(source, target));

		watch(source, target, rel);
	}

	private void watch(Term source, Term target, TermRelation tv) {
		if(historyResource.getHistory().isWatched(source.getGroupingKey()))
			historyResource.getHistory().saveEvent(
					source.getGroupingKey(),
					this.getClass(), 
					"Term has a new variation: " + tv);
		
		if(historyResource.getHistory().isWatched(target.getGroupingKey()))
			historyResource.getHistory().saveEvent(
					target.getGroupingKey(),
					this.getClass(), 
					"Term has a new variation base: " + tv);
	}

	private void checkFrequency(Term term) {
		if(term.getFrequency() == 0)
			LOGGER.warn("Frequency of term {} must be greater than 0 before running SyntactticTermGatherer AE", term.getGroupingKey());
	}

	@Override
	public void process(JCas cas) throws AnalysisEngineProcessException {
		// nothing to do at cas level
	}
	
}
