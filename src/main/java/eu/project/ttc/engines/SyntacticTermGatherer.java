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

import java.math.BigInteger;
import java.util.List;
import java.util.ListIterator;
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
import com.google.common.base.Optional;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;

import eu.project.ttc.engines.variant.VariantRule;
import eu.project.ttc.engines.variant.VariantRuleIndex;
import eu.project.ttc.models.Term;
import eu.project.ttc.models.TermWord;
import eu.project.ttc.models.VariationType;
import eu.project.ttc.models.index.CustomIndexStats;
import eu.project.ttc.models.index.CustomTermIndex;
import eu.project.ttc.models.index.TermIndexes;
import eu.project.ttc.resources.ObserverResource;
import eu.project.ttc.resources.ObserverResource.SubTaskObserver;
import eu.project.ttc.resources.TermIndexResource;
import eu.project.ttc.resources.YamlVariantRules;
import eu.project.ttc.utils.TermUtils;

public class SyntacticTermGatherer extends JCasAnnotator_ImplBase {
	private static final Logger LOGGER = LoggerFactory.getLogger(SyntacticTermGatherer.class);
	public static final String TASK_NAME = "Syntactic variant gathering";
	private static final int OBSERVING_STEP = 1000;

	private static final String M_PREFIX = "M";

	@ExternalResource(key=ObserverResource.OBSERVER, mandatory=false)
	protected ObserverResource observerResource;

	@ExternalResource(key=TermIndexResource.TERM_INDEX, mandatory=true)
	private TermIndexResource termIndexResource;
	
	public static final String YAML_VARIANT_RULES = "YamlVariantRules";
	@ExternalResource(key = YAML_VARIANT_RULES, mandatory = true)
	private YamlVariantRules yamlVariantRules;

	private Optional<SubTaskObserver> taskObserver = Optional.absent();

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
		LOGGER.info("Start syntactic term gathering");
		
		/*
		 *  Do not deactivate gathering on key_lemma_lemma, otherwise we loose
		 *  morphological gathering based on single-word (with [compound] tag in yaml).
		 *  TODO : understanding why
		 */
		String[] gatheringKeys = new String[] {
				TermIndexes.WORD_COUPLE_LEMMA_LEMMA,
				TermIndexes.WORD_COUPLE_LEMMA_STEM
			};

		// prepare observer and indexes
		for(String key:gatheringKeys) {
			CustomTermIndex customIndex = this.termIndexResource.getTermIndex().getCustomIndex(key);
			customIndex.cleanSingletonKeys();
			// clean biggest classes
			customIndex.cleanEntriesByMaxSize(WARNING_CRITICAL_SIZE);

			CustomIndexStats stats = new CustomIndexStats(customIndex);

			
			// Display class sizes
			Stopwatch sw1 = Stopwatch.createStarted();
			int k = 0;
			LOGGER.info("Biggest class is {}, size: {}", stats.getBiggestClass(), stats.getBiggestSize());
			
			
			int size;
			for(Integer i:stats.getSizeCounters().keySet()) {
				k ++;
				size = stats.getSizeCounters().get(i).size();
				totalComparisons = totalComparisons.add(BigInteger.valueOf(size * i*(i-1)));
			}
			LOGGER.info("Number of term pairs to test: " + totalComparisons);
			sw1.stop();
			LOGGER.debug("Time to get the comparisons number: " + sw1.elapsed(TimeUnit.MILLISECONDS));
			LOGGER.debug("Number of classes: " + k);
			if(taskObserver.isPresent())
				taskObserver.get().setTotalTaskWork(totalComparisons.longValue());
		}


		LOGGER.debug("Gathering with default variant rule indexing (source and target patterns)");
		for(String key:gatheringKeys)
			gatherDefault(key);
		
		LOGGER.debug("Gathering with derivation variant rule indexing (source and target patterns)");
		gatherManualClass(VariantRuleIndex.DERIVATION, selectTerms(VariationType.DERIVES_INTO));
		
		LOGGER.debug("Gathering with prefix variant rule indexing (source and target patterns)");
		gatherManualClass(VariantRuleIndex.PREFIX, selectTerms(VariationType.IS_PREFIX_OF));
		
	}
	
	/**
	 * Returns the swt associated with the TermWord
	 * if and only if it has at least one connection of given
	 * {@link VariationType}.
	 * 
	 * Returns null otherwise.
	 * 
	 * @param tw
	 * @param variantType
	 * @return
	 */
	private Term toSpecialSWTTerm(TermWord tw, VariationType variantType) {
		String sourceGroupingKey = TermUtils.toGroupingKey(tw);
		Term sourceTerm = this.termIndexResource.getTermIndex().getTermByGroupingKey(sourceGroupingKey);
		if(sourceTerm != null) {
			if(sourceTerm.getVariations(variantType).iterator().hasNext()
					|| sourceTerm.getBases(variantType).iterator().hasNext())
				return sourceTerm;
		}
		return null;
	}
	
	private List<Term> selectTerms(VariationType variantType) {
		Term swt;
		List<Term> terms = Lists.newArrayList();
		for (Term t : termIndexResource.getTermIndex().getTerms()) {
			for (TermWord termWord : t.getWords()) {
				swt = toSpecialSWTTerm(termWord, variantType);
				if (swt != null) {
					terms.add(t);
					break;
				}
			}
		}
		return terms;
	}
	
	
	private void gatherManualClass(VariantRuleIndex variantRuleIndex, List<Term> terms) {
		Term source;
		Term target;
		for(ListIterator<Term> sourceIt=terms.listIterator(); sourceIt.hasNext();) {
			source=sourceIt.next();
			for(ListIterator<Term> targetIt=terms.listIterator(sourceIt.nextIndex()); targetIt.hasNext();) {
				target=targetIt.next();
				applyGatheringRules(variantRuleIndex, source, target);
				applyGatheringRules(variantRuleIndex, target, source);
			}
		}
	}

	private BigInteger totalComparisons = BigInteger.valueOf(0);
	private int nbComparisons = 0;

	private static final int WARNING_CRITICAL_SIZE = 2500;
	
	private void gatherDefault(final String gatheringKey) {
		LOGGER.debug("Rule-based gathering over the pregathering key {}", gatheringKey);

		// create the index
		CustomTermIndex customIndex = this.termIndexResource.getTermIndex().getCustomIndex(gatheringKey);
		LOGGER.debug("Rule-based gathering over {} classes", customIndex.size());


		
		// Log the progress every 5 seconds
		Timer progressLoggerTimer = new Timer("Syn. Variant Gathering Timer");
		progressLoggerTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				SyntacticTermGatherer.LOGGER.info("progress for key {}: ({}%)",
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
					applyGatheringRules(VariantRuleIndex.DEFAULT, source, target);
					applyGatheringRules(VariantRuleIndex.DEFAULT, target, source);
					if(nbComparisons % OBSERVING_STEP == 0) 
						if(taskObserver.isPresent())
							taskObserver.get().work(OBSERVING_STEP);
				}
					
			}
		}
		
		//finalize
		this.termIndexResource.getTermIndex().dropCustomIndex(gatheringKey);
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
		
//		/*
//		 * Reverse the variation when target frequency is
//		 * bigger than source frequency
//		 */
//		if(baseTargetComparator.compare(source, target) > 0) {
//			// swaps terms, sets the most frequent  and shortest as the source
//			Term aux = source;
//			source = target;
//			target = aux;
//		}
		
		source.addTermVariation(
				target, 
				matchingRule.getName().startsWith(M_PREFIX) ? VariationType.MORPHOLOGICAL : VariationType.SYNTACTICAL,
				matchingRule.getName());
	}

	private void checkFrequency(Term term) {
		if(term.getFrequency() == 0)
			LOGGER.warn("Frequency of term {} must be greater than 0 before running SyntactticTermGatherer AE", term.getGroupingKey());
	}

	@Override
	public void process(JCas cas) throws AnalysisEngineProcessException {
		// nothing to do at cas level
	}
	
	
//	private static Comparator<Term> baseTargetComparator = new Comparator<Term>() {
//		public int compare(Term a, Term b) {
//		     return ComparisonChain.start()
//		         .compare(b.getFrequency(), a.getFrequency())
//		         .compare(a.getWords().size(), b.getWords().size())
//		         .result();
//		   }
//	};
	
}
