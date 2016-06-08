/*******************************************************************************
 * Copyright 2015 - CNRS (Centre National de Recherche Scientifique)
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
package eu.project.ttc.engines.morpho;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.ExternalResource;
import org.apache.uima.jcas.JCas;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.base.MoreObjects;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import eu.project.ttc.engines.cleaner.TermProperty;
import eu.project.ttc.metrics.EditDistance;
import eu.project.ttc.metrics.Levenshtein;
import eu.project.ttc.models.Component;
import eu.project.ttc.models.CompoundType;
import eu.project.ttc.models.Term;
import eu.project.ttc.models.Word;
import eu.project.ttc.models.WordBuilder;
import eu.project.ttc.models.index.CustomTermIndex;
import eu.project.ttc.models.index.TermIndexes;
import eu.project.ttc.models.index.TermMeasure;
import eu.project.ttc.resources.CompostIndex;
import eu.project.ttc.resources.CompostInflectionRules;
import eu.project.ttc.resources.ObserverResource;
import eu.project.ttc.resources.ObserverResource.SubTaskObserver;
import eu.project.ttc.resources.SimpleWordSet;
import eu.project.ttc.resources.TermIndexResource;
import eu.project.ttc.utils.IndexingKey;
import eu.project.ttc.utils.TermSuiteUtils;

/*
 * TODO Apply exceptions for derivational suffixes like -aire, -age, etc.
 */
public class CompostAE extends JCasAnnotator_ImplBase {
	private static final Logger LOGGER = LoggerFactory.getLogger(CompostAE.class);

	public static final String TASK_NAME = "Morphosyntactic analysis (native and neoclassical)";

	@ExternalResource(key=ObserverResource.OBSERVER, mandatory=true)
	protected ObserverResource observerResource;

	@ExternalResource(key=TermIndexResource.TERM_INDEX, mandatory=true)
	private TermIndexResource termIndexResource;
	
	public static final String LANGUAGE_DICO = "LanguageDico";
	@ExternalResource(key=LANGUAGE_DICO, mandatory=true)
	private SimpleWordSet languageDico;

	public static final String INFLECTION_RULES = "InflectionRules";
	@ExternalResource(key=INFLECTION_RULES, mandatory=true, description="Inflection rules for the last segment")
	private CompostInflectionRules inflectionRules;

	public static final String TRANSFORMATION_RULES = "TransformationRules";
	@ExternalResource(key=TRANSFORMATION_RULES, mandatory=true, description="Inflection rules for all but last segments")
	private CompostInflectionRules transformationRules;

	
	public static final String NEOCLASSICAL_PREFIXES = "NeoClassicalPrefixes";
	@ExternalResource(key=NEOCLASSICAL_PREFIXES, mandatory=true)
	private SimpleWordSet neoclassicalPrefixes;

	public static final String STOP_LIST = "StopList";
	@ExternalResource(key=STOP_LIST, mandatory=true)
	private SimpleWordSet stopList;

	public static final String ALPHA = "Alpha";
	@ConfigurationParameter(name=ALPHA, mandatory=true)
	private float alpha;

	public static final String BETA = "Beta";
	@ConfigurationParameter(name=BETA, mandatory=true)
	private float beta;

	public static final String GAMMA = "Gamma";
	@ConfigurationParameter(name=GAMMA, mandatory=true)
	private float gamma;

	public static final String DELTA = "Delta";
	@ConfigurationParameter(name=DELTA, mandatory=true)
	private float delta;

	public static final String SCORE_THRESHOLD = "ScoreThreshold";
	@ConfigurationParameter(name=SCORE_THRESHOLD, mandatory=true)
	private float scoreThreshold;

	public static final String SEGMENT_SIMILARITY_THRESHOLD = "SegmentSimilarityThreshold";
	@ConfigurationParameter(name=SEGMENT_SIMILARITY_THRESHOLD, mandatory=true)
	private float segmentSimilarityThreshold;


	public static final String MIN_COMPONENT_SIZE = "MinimumComponentSize";
	@ConfigurationParameter(name=MIN_COMPONENT_SIZE, mandatory=false, defaultValue = "3")
	private int minComponentSize;

	public static final String MAX_NUMBER_OF_COMPONENTS = "MaxNumberOfComponents";

	@ConfigurationParameter(name=MAX_NUMBER_OF_COMPONENTS, mandatory=false, defaultValue = "3")
	private int maxNumberOfComponents;

	private EditDistance distance = new Levenshtein();

	private CompostIndex compostIndex;
	private static IndexingKey<String, String> similarityIndexingKey = TermSuiteUtils.KEY_THREE_FIRST_LETTERS;
	
	private CustomTermIndex swtLemmaIndex;

	private TermMeasure wrMeasure;

	private LoadingCache<String, SegmentScoreEntry> segmentScoreEntries = CacheBuilder.newBuilder()
				.maximumSize(100000)
				.recordStats()
		       .build(
		           new CacheLoader<String, SegmentScoreEntry>() {
		             public SegmentScoreEntry load(String key) { // no checked exception
		               return computeSegmentScore(key);
		             }
		           });

	private LoadingCache<String, String> segmentLemmaCache = CacheBuilder.newBuilder()
			.maximumSize(100000)
			.recordStats()
	       .build(
	           new CacheLoader<String, String>() {
	             public String load(String segment) { // no checked exception
	               return findSegmentLemma(segment);
	             }
	           });

	
	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
	}
	
	@Override
	public void collectionProcessComplete()
			throws AnalysisEngineProcessException {
		SubTaskObserver observer = observerResource.getTaskObserver(TASK_NAME);
		observer.setTotalTaskWork(termIndexResource.getTermIndex().getWords().size());
		LOGGER.info("Starting morphology analysis");
		LOGGER.debug(this.toString());
		wrMeasure = termIndexResource.getTermIndex().getWRMeasure();
		swtLemmaIndex = termIndexResource.getTermIndex().getCustomIndex(TermIndexes.SINGLE_WORD_LEMMA);
		buildCompostIndex();

		int cnt = 0;
		int observingStep = 100;
		for(Word word:this.termIndexResource.getTermIndex().getWords()) {
			cnt++;
			if(cnt%observingStep == 0) {
				observer.work(observingStep);
			}
			Map<Segmentation, Double> scores = computeScores(word.getLemma());
			if(scores.size() > 0) {
				float bestScore = 0;
				Segmentation bestSegmentation = null;
				for(Segmentation s:scores.keySet()) {
					if(scores.get(s) > bestScore) {
						bestScore = scores.get(s).floatValue();
						bestSegmentation = s;
					}
				}
				
				// build the word component from segmentation
				WordBuilder builder = new WordBuilder(word);
				
				for(Segment seg:bestSegmentation.getSegments()) {
					String lemma = segmentLemmaCache.getUnchecked(seg.getLemma());
					builder.addComponent(
							seg.getBegin(), 
							seg.getEnd(), 
							lemma
							);
					if(seg.isNeoclassical())
						builder.setCompoundType(CompoundType.NEOCLASSICAL);
					else
						builder.setCompoundType(CompoundType.NATIVE);
				}
				builder.create();

				// log the word composition
				if(LOGGER.isTraceEnabled()) {
					List<String> componentStrings = Lists.newArrayList();
					for(Component component:word.getComponents())
						componentStrings.add(component.toString());
					LOGGER.trace("{} [{}]", word.getLemma(), Joiner.on(' ').join(componentStrings));
				}
			}
		}
		LOGGER.debug("segment score cache size: {}", segmentScoreEntries.size());
		LOGGER.debug("segment score hit count: " + segmentScoreEntries.stats().hitCount());
		LOGGER.debug("segment score hit rate: " + segmentScoreEntries.stats().hitRate());
		LOGGER.debug("segment score eviction count: " + segmentScoreEntries.stats().evictionCount());
		termIndexResource.getTermIndex().dropCustomIndex(TermIndexes.SINGLE_WORD_LEMMA);
		segmentScoreEntries.invalidateAll();
		segmentLemmaCache.invalidateAll();
	}
	
	private void buildCompostIndex() {
		LOGGER.debug("Building compost index");

		compostIndex = new CompostIndex(similarityIndexingKey);
		for(String word:languageDico.getElements())
			compostIndex.addDicoWord(word);
		for(String word:neoclassicalPrefixes.getElements())
			compostIndex.addNeoclassicalPrefix(word);
		for(Word w:termIndexResource.getTermIndex().getWords())
			compostIndex.addInCorpus(w.getLemma());
		LOGGER.debug("Compost index size: " + compostIndex.size());
		
	}

	/*
	 * Compute scores for all segmentations of the word
	 */
	private Map<Segmentation, Double> computeScores(String wordStr) {
		Map<Segmentation, Double> scores = Maps.newHashMap();
		List<Segmentation> rawSegmentations = Segmentation.getSegmentations(wordStr, maxNumberOfComponents, minComponentSize);
		for(Segmentation segmentation:rawSegmentations) {
			double segmentationScore = computeSegmentationScore(segmentation);
			if(segmentationScore >= this.scoreThreshold)
				scores.put(segmentation, segmentationScore);
		}
		return scores;
	}
	
	/*
	 * Compute the score of a given segmentation
	 */
	private float computeSegmentationScore(Segmentation segmentation) {
		float sum = 0;
		int index = 0;
		for(Segment s:segmentation.getSegments()) {
			SegmentScoreEntry scoreEntry = index == (segmentation.size() - 1) ? 
					getBestInflectedScoreEntry(s, this.inflectionRules):
						getBestInflectedScoreEntry(s, this.transformationRules);
			sum+=scoreEntry.getScore();
			s.setLemma(scoreEntry.getDicoEntry() == null ? 
					s.getSubstring() : 
						scoreEntry.getDicoEntry().getText());
			index++;
		}
		return sum / segmentation.size();
	}

	
	/*
	 * Returns the best score of a segment considering all its possible inflections or transformations.
	 */
	private SegmentScoreEntry getBestInflectedScoreEntry(Segment s,
			CompostInflectionRules rules) {
		SegmentScoreEntry bestScoreEntry = this.segmentScoreEntries.getUnchecked(s.getSubstring());
		for(String seg:rules.getInflections(s.getSubstring())) {
			SegmentScoreEntry scoreEntry = this.segmentScoreEntries.getUnchecked(seg);
			if(scoreEntry.getScore()>bestScoreEntry.getScore()) 
				bestScoreEntry = scoreEntry;
		}
//		this.segmentScoreEntries.put(s.getSubstring(), bestScoreEntry);
		return bestScoreEntry;
	}
	

	/*
	 * Compute the score of a segment
	 */
	private SegmentScoreEntry computeSegmentScore(String segment) {
		if(this.stopList.contains(segment) )
			return SegmentScoreEntry.SCORE_ZERO;
		CompostIndexEntry closestEntry = compostIndex.getEntry(segment);
		double indexSimilarity = 0.0;
		
		if(closestEntry == null) {
			if(segmentSimilarityThreshold == 1)
				// do not compare similarity of this segment to the index
				return SegmentScoreEntry.SCORE_ZERO;
					
			// Find an entry by similarity
			Iterator<CompostIndexEntry> it = compostIndex.closedEntryCandidateIterator(segment);
			int entryLength = segment.length();
			double dist = 0;
			CompostIndexEntry entry;
			while(it.hasNext()) {
				entry = it.next();
				dist = distance.computeNormalized(segment, entry.getText());
				if(Math.abs(entry.getText().length() - entryLength) <= 3 
						&& dist >= segmentSimilarityThreshold) {
					indexSimilarity = dist;
					closestEntry = entry;
				}
			}
			if(closestEntry == null) {
				// could not find any close entry in the compost index
				return SegmentScoreEntry.SCORE_ZERO;
			}
		} else {
			indexSimilarity = 1f;
		}
		int inCorpus = 0;
		int inDico = closestEntry.isInDico() || closestEntry.isInNeoClassicalPrefix() ? 1 : 0;

		// retrieves all sw terms that have the same lemma
		Collection<Term> corpusTerm = swtLemmaIndex.getTerms(segment);
		float wr = 0f;
		for(Iterator<Term> it = corpusTerm.iterator(); it.hasNext();)
			wr+=wrMeasure.getValue(it.next());
		
		float dataCorpus;
		if(closestEntry.isInCorpus() && !corpusTerm.isEmpty()) {
			dataCorpus = wr / (float)wrMeasure.getMax();
			inCorpus = 1;
		} else {
			dataCorpus = 0;
			inCorpus = closestEntry.isInNeoClassicalPrefix() ? 1 : 0;
		}
		float score = this.alpha * (float)indexSimilarity + this.beta * inDico + this.gamma * inCorpus + this.delta * dataCorpus;
		if(LOGGER.isTraceEnabled()) {
			LOGGER.trace("Score for {} is {} [alpha: {} beta: {} gamma: {} delta: {}]", 
					segment, 
					score,
					indexSimilarity,
					inDico,
					inCorpus,
					dataCorpus);
		}
		return new SegmentScoreEntry(segment, findSegmentLemma(segment), score, closestEntry);
	}
	
	/*
	 * Finds the best lemma for a segment
	 */
	private String findSegmentLemma(String segment) {
		Collection<String> candidates = this.neoclassicalPrefixes.getTranslations(segment);
		if(candidates.isEmpty())
			return segment;
		else {
			TermMeasure wrLog = termIndexResource.getTermIndex().getWRLogMeasure();
			TermProperty property = wrLog.isComputed() ? TermProperty.WR_LOG : TermProperty.FREQUENCY;
			String bestLemma = segment;
			double bestValue = 0d;
			for(String candidateLemma:candidates) {
				for(Term t:swtLemmaIndex.getTerms(candidateLemma)) {
					if(property.getDoubleValue(termIndexResource.getTermIndex(), t) > bestValue) {
						bestValue = property.getDoubleValue(termIndexResource.getTermIndex(), t);
						bestLemma = t.getLemma();
					}
				}
			}
			return bestLemma;
		}
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("a", this.alpha)
				.add("b", this.beta)
				.add("c", this.gamma)
				.add("d", this.delta)
				.add("minCompSize", this.minComponentSize)
				.add("maxCompNum", this.maxNumberOfComponents)
				.add("similarityTh", this.segmentSimilarityThreshold)
				.add("scoreTh", this.scoreThreshold)
				.toString();
				
	}
}
