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
package eu.project.ttc.engines;

import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ExternalResource;
import org.apache.uima.jcas.JCas;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.base.Stopwatch;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Lists;

import eu.project.ttc.engines.variant.VariantRule;
import eu.project.ttc.models.Term;
import eu.project.ttc.models.VariationType;
import eu.project.ttc.models.index.CustomTermIndex;
import eu.project.ttc.models.index.IndexStats;
import eu.project.ttc.models.index.TermIndexes;
import eu.project.ttc.models.index.TermValueProviders;
import eu.project.ttc.resources.TermIndexResource;
import eu.project.ttc.resources.YamlVariantRules;
import fr.univnantes.lina.UIMAProfiler;

public class SyntacticTermGatherer extends JCasAnnotator_ImplBase {
	private static final Logger LOGGER = LoggerFactory.getLogger(SyntacticTermGatherer.class);
	
	private static final String M_PREFIX = "M";

	@ExternalResource(key=TermIndexResource.TERM_INDEX, mandatory=true)
	private TermIndexResource termIndexResource;
	
	public static final String YAML_VARIANT_RULES = "YamlVariantRules";
	@ExternalResource(key = YAML_VARIANT_RULES, mandatory = true)
	private YamlVariantRules yamlVariantRules;

	@Override
	public void collectionProcessComplete()
			throws AnalysisEngineProcessException {
		UIMAProfiler.getProfiler("AnalysisEngine").start(this, "process");
		LOGGER.info("Start syntactic term gathering");
		
		for(VariantRule rule:this.yamlVariantRules.getVariantRules())
			UIMAProfiler.getProfiler("Gathering stats").initHit(rule.getName());
		
		/*
		 *  Do not deactivate gathering on key_lemma_lemma, otherwise we loose
		 *  morphological gathering based on single-word (with [compound] tag in yaml).
		 *  TODO : understanding why
		 */
		gather(TermIndexes.WORD_COUPLE_LEMMA_LEMMA);
		gather(TermIndexes.WORD_COUPLE_LEMMA_STEM);
		UIMAProfiler.getProfiler("AnalysisEngine").stop(this, "process");
	}
	
	private int totalComparisons = 0;
	private int nbComparisons = 0;

	private static final int WARNING_CRITICAL_SIZE = 10000;
	
	private void gather(final String gatheringKey) {
		LOGGER.debug("Rule-based gathering over the pregathering key {}", gatheringKey);

		// create the index
		CustomTermIndex customIndex = this.termIndexResource.getTermIndex().createCustomIndex(
				gatheringKey,
				TermValueProviders.get(gatheringKey));
		LOGGER.debug("Rule-based gathering over {} classes", customIndex.size());

		// clean singleton classes
		LOGGER.debug("Cleaning singleton keys");
		customIndex.cleanSingletonKeys();

		// clean biggest classes
		customIndex.dropBiggerEntries(WARNING_CRITICAL_SIZE, true);
		
		IndexStats stats = new IndexStats(customIndex);

		
		// Display class sizes
		Stopwatch sw1 = Stopwatch.createStarted();
		Stopwatch sw2 = Stopwatch.createUnstarted();
		int k = 0;
		LOGGER.info("Biggest class is {}, size: {}", stats.getBiggestClass(), stats.getBiggestSize());
		
		
		int size;
		for(Integer i:stats.getSizeCounters().keySet()) {
			k ++;
			size = stats.getSizeCounters().get(i).size();
			sw2.start();
			LOGGER.trace("Number of classes of size " + i + ": " + size);
			sw2.stop();
			totalComparisons += size * i*(i-1);
		}
		LOGGER.info("Number of term pairs to test: " + totalComparisons);
		sw1.stop();
		LOGGER.debug("Time to get the comparisons number: " + sw1.elapsed(TimeUnit.MILLISECONDS));
		LOGGER.debug("Time to get class sizes: " + sw2.elapsed(TimeUnit.MILLISECONDS));
		LOGGER.debug("Number of classes: " + k);
		
		// Log the progress every 5 seconds
		Timer progressLoggerTimer = new Timer("Syn. Variant Gathering Timer");
		progressLoggerTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				SyntacticTermGatherer.LOGGER.info("progress for key {}: ({}%)",
						gatheringKey,
						String.format("%.2f", ((float)nbComparisons*100)/totalComparisons)
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
			if(list.size() > 1) {
				LOGGER.trace("Rule-based gathering over the '" + cls + "' term class of size " + list.size() + ": " + Joiner.on(" ").join(examples));
			}


			Term source;
			Term target;
			for(ListIterator<Term> sourceIt=list.listIterator(); sourceIt.hasNext();) {
				source=sourceIt.next();
				for(ListIterator<Term> targetIt=list.listIterator(sourceIt.nextIndex()); targetIt.hasNext();) {
					nbComparisons+=2;
					target=targetIt.next();
					applyGatheringRules(source, target);
					applyGatheringRules(target, source);
				}
					
			}
		}
		
		//finalize
		this.termIndexResource.getTermIndex().dropCustomIndex(gatheringKey);
		progressLoggerTimer.cancel();
	}

	private void applyGatheringRules(Term source, Term target) {
		VariantRule matchingRule = yamlVariantRules.getMatchingRule(source,target);
		if (matchingRule != null) {
			applyMatchingRule(matchingRule, source, target);
		}
	}

	private void applyMatchingRule(VariantRule matchingRule, Term source, Term target) {
		// Finds the most frequent of both terms
		checkFrequency(source);
		checkFrequency(target);
		if(baseTargetComparator.compare(source, target) > 0) {
			// swaps terms, sets the most frequent  and shortest as the source
			Term aux = source;
			source = target;
			target = aux;
		}
		
		source.addTermVariation(
				target, 
				matchingRule.getName().startsWith(M_PREFIX) ? VariationType.MORPHOLOGICAL : VariationType.SYNTACTICAL,
				matchingRule.getName());
		UIMAProfiler.getProfiler("Gathering stats").hit(matchingRule.getName(), source.getPilot() + " || " + target.getPilot());
	}

	private void checkFrequency(Term term) {
		if(term.getFrequency() == 0)
			LOGGER.warn("Frequency of term {} must be greater than 0 before running SyntactticTermGatherer AE", term.getGroupingKey());
	}

	@Override
	public void process(JCas cas) throws AnalysisEngineProcessException {
		// nothing to do at cas level
	}
	
	
	private static Comparator<Term> baseTargetComparator = new Comparator<Term>() {
		public int compare(Term a, Term b) {
		     return ComparisonChain.start()
		         .compare(b.getFrequency(), a.getFrequency())
		         .compare(a.getWords().size(), b.getWords().size())
		         .result();
		   }
	};
	
}
