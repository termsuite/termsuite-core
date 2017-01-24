package fr.univnantes.termsuite.engines.splitter;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toSet;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.lang.mutable.MutableLong;
import org.slf4j.Logger;

import com.google.common.base.Joiner;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import fr.univnantes.termsuite.framework.InjectLogger;
import fr.univnantes.termsuite.framework.Parameter;
import fr.univnantes.termsuite.framework.Resource;
import fr.univnantes.termsuite.framework.TerminologyEngine;
import fr.univnantes.termsuite.metrics.EditDistance;
import fr.univnantes.termsuite.metrics.Levenshtein;
import fr.univnantes.termsuite.model.Component;
import fr.univnantes.termsuite.model.CompoundType;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.TermProperty;
import fr.univnantes.termsuite.model.Word;
import fr.univnantes.termsuite.model.WordBuilder;
import fr.univnantes.termsuite.model.termino.CustomTermIndex;
import fr.univnantes.termsuite.model.termino.TermIndexes;
import fr.univnantes.termsuite.resources.CompostIndex;
import fr.univnantes.termsuite.uima.ResourceType;
import fr.univnantes.termsuite.uima.resources.preproc.SimpleWordSet;
import fr.univnantes.termsuite.uima.resources.termino.CompostInflectionRules;
import fr.univnantes.termsuite.utils.IndexingKey;
import fr.univnantes.termsuite.utils.TermSuiteUtils;

public class NativeSplitter extends TerminologyEngine {
	@InjectLogger Logger logger;

	@Parameter
	private MorphologicalOptions opt;

	@Resource(type=ResourceType.COMPOST_INFLECTION_RULES)
	private CompostInflectionRules inflectionRules;
	
	@Resource(type=ResourceType.COMPOST_TRANSFORMATION_RULES)
	private CompostInflectionRules transformationRules;
	
	@Resource(type=ResourceType.DICO)
	private SimpleWordSet languageDico;

	@Resource(type=ResourceType.NEOCLASSICAL_PREFIXES)
	private SimpleWordSet neoclassicalPrefixes;
	
	@Resource(type=ResourceType.COMPOST_STOP_LIST)
	private SimpleWordSet stopList;

	private CompostIndex compostIndex;
	private static IndexingKey<String, String> similarityIndexingKey = TermSuiteUtils.KEY_THREE_FIRST_LETTERS;
	private CustomTermIndex swtLemmaIndex;

	private EditDistance distance = new Levenshtein();


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
	public void execute() {
		logger.info("Starting morphologyical compound detection for termino");
		swtLemmaIndex = terminology.getTerminology().getCustomIndex(TermIndexes.SINGLE_WORD_LEMMA);
		buildCompostIndex();

		
		final MutableLong cnt = new MutableLong(0);
		
		Timer progressLoggerTimer = new Timer("Morphosyntactic splitter AE");
		progressLoggerTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				int total = terminology.getWords().size();
				logger.info("Progress: {}% ({} on {})",
						String.format("%.2f", ((double)cnt.longValue()*100)/total),
						cnt.longValue(),
						total);
			}
		}, 5000l, 5000l);

		
//		int observingStep = 100;
		Set<Word> words = terminology.getTerms()
				.parallelStream()
				.filter(Term::isSingleWord)
				.map(swt -> swt.getWords().get(0).getWord())
				/*
				 * Do not do native morphology splitting 
				 * if a composition already exists.
				 */
				.filter(word -> !word.isCompound())
				.collect(toSet());

		logger.debug("Num of words: {}", words.size());
		logger.debug("Num of compound words before native splitting: {}", words.stream().filter(Word::isCompound).count());

		words
			.parallelStream()
			.forEach( word -> {
				
				cnt.increment();
				
				Map<Segmentation, Double> scores = computeScores(word.getLemma());
				watchScores(word, scores);
				if(scores.size() > 0) {
	
					List<Segmentation> segmentations = Lists.newArrayList(scores.keySet());
					
					/*
					 *  compare segmentations in a deterministic way.
					 */
					segmentations.sort(new Comparator<Segmentation>() {
						@Override
						public int compare(Segmentation o1, Segmentation o2) {
							int comp = Double.compare(scores.get(o2), scores.get(o1));
							if(comp != 0)
								return comp;
							comp = Integer.compare(o1.getSegments().size(), o2.getSegments().size());
							if(comp != 0)
								return comp;	
							for(int i = 0; i< o1.getSegments().size(); i++) {
								comp = Integer.compare(
									o2.getSegments().get(i).getEnd(), 
									o1.getSegments().get(i).getEnd()
								);
								if(comp != 0)
									return comp;
							}
							return 0;
						}
					});
					
					Segmentation bestSegmentation = segmentations.get(0);
					
					// build the word component from segmentation
					WordBuilder builder = new WordBuilder(word);
					
					boolean isNeoclassical = false;
					for(Segment seg:bestSegmentation.getSegments()) {
						String lemma = segmentLemmaCache.getUnchecked(seg.getLemma());
						if(lemma == null)
							builder.addComponent(
								seg.getBegin(), 
								seg.getEnd(), 
								seg.getSubstring()
							);
						else
							builder.addComponent(
									seg.getBegin(), 
									seg.getEnd(), 
									seg.getSubstring(),
									lemma
								);
						
						if(compostIndex.isNeoclassical(seg.getSubstring())) {
							isNeoclassical = true;
							builder.setNeoclassicalAffix(seg.getBegin(), seg.getEnd());
						} 
					}
					builder.setCompoundType(isNeoclassical ? CompoundType.NEOCLASSICAL : CompoundType.NATIVE);
					builder.create();
					
					watchComposition(word, true);
					// log the word composition
					if(logger.isTraceEnabled()) {
						List<String> componentStrings = Lists.newArrayList();
						for(Component component:word.getComponents())
							componentStrings.add(component.toString());
						logger.trace("{} [{}]", word.getLemma(), Joiner.on(' ').join(componentStrings));
					}
				}
			});

		//finalize
		progressLoggerTimer.cancel();

		logger.debug("Num of compound words after native splitting: {}", words.stream().filter(Word::isCompound).count());
			
		
		
		
		logger.debug("segment score cache size: {}", segmentScoreEntries.size());
		logger.debug("segment score hit count: " + segmentScoreEntries.stats().hitCount());
		logger.debug("segment score hit rate: " + segmentScoreEntries.stats().hitRate());
		logger.debug("segment score eviction count: " + segmentScoreEntries.stats().evictionCount());
		terminology.getTerminology().dropCustomIndex(TermIndexes.SINGLE_WORD_LEMMA);
		segmentScoreEntries.invalidateAll();
		segmentLemmaCache.invalidateAll();
	}
	
	private void watchComposition(Word word, boolean newlyCreated) {
		if(history.isPresent()) {
			if(history.get().isLemmaWatched(word.getLemma())) {
				history.get().saveEventByLemma(
						word.getLemma(), 
						NativeSplitter.class, 
						String.format("%sSegmentation of swt is [%s]%s", 
								!newlyCreated ? "[word already segmented] " : "",
								word.getCompoundType().getShortName(),
								word.getComponents().stream().map(Component::getSubstring).collect(joining("+"))
							));
			}
		}
	}

	private void watchScores(Word word, Map<Segmentation, Double> scores) {
		if(history.isPresent()) {
			if(history.get().isLemmaWatched(word.getLemma())) {
				String scoreStr = scores.entrySet().stream().sorted(new Comparator<Entry<Segmentation, Double>>() {
					@Override
					public int compare(Entry<Segmentation, Double> o1, Entry<Segmentation, Double> o2) {
						return Double.compare(o2.getValue(), o1.getValue());
					}
				}).map(e -> String.format("%s[%.3f]", e.getKey(), e.getValue()))
				.collect(joining(", "));
				
				history.get().saveEventByLemma(
						word.getLemma(), 
						NativeSplitter.class, 
						"Segmentation scores are: " + scoreStr);
			}
		}
	}

	private void buildCompostIndex() {
		logger.debug("Building compost index");

		compostIndex = new CompostIndex(similarityIndexingKey);
		for(String word:languageDico.getElements())
			compostIndex.addDicoWord(word);
		for(String word:neoclassicalPrefixes.getElements())
			compostIndex.addNeoclassicalPrefix(word);
		for(Word w:terminology.getWords())
			compostIndex.addInCorpus(w.getLemma());
		logger.debug("Compost index size: " + compostIndex.size());
	}

	/*
	 * Compute scores for all segmentations of the word
	 */
	private Map<Segmentation, Double> computeScores(String wordStr) {
		Map<Segmentation, Double> scores = Maps.newHashMap();
		List<Segmentation> rawSegmentations = Segmentation.getSegmentations(wordStr, this.opt.getMaxNumberOfComponents(), this.opt.getMinComponentSize());
		for(Segmentation segmentation:rawSegmentations) {
			double segmentationScore = computeSegmentationScore(segmentation);
			if(segmentationScore >= this.opt.getScoreThreshold())
				scores.put(segmentation, segmentationScore);
		}
		return scores;
	}
	
	/*
	 * Compute the score of a given segmentation
	 */
	private double computeSegmentationScore(Segmentation segmentation) {
		double sum = 0;
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
			if(this.opt.getSegmentSimilarityThreshold() == 1)
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
						&& dist >= this.opt.getSegmentSimilarityThreshold()) {
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
		Collection<Term> corpusTerms = swtLemmaIndex.getTerms(segment);
		double wr = 0f;
		for(Iterator<Term> it = corpusTerms.iterator(); it.hasNext();)
			wr+=Math.pow(10, it.next().getSpecificity());
		
		double dataCorpus;
		if(closestEntry.isInCorpus() && !corpusTerms.isEmpty()) {
			dataCorpus = wr / getMaxSpec();
			inCorpus = 1;
		} else {
			dataCorpus = 0;
			inCorpus = closestEntry.isInNeoClassicalPrefix() ? 1 : 0;
		}
		double score = this.opt.getAlpha() * indexSimilarity + this.opt.getBeta() * inDico + this.opt.getGamma() * inCorpus + this.opt.getDelta() * dataCorpus;
		if(logger.isTraceEnabled()) {
			logger.trace("Score for {} is {} [alpha: {} beta: {} gamma: {} delta: {}]", 
					segment, 
					score,
					indexSimilarity,
					inDico,
					inCorpus,
					dataCorpus);
		}
		return new SegmentScoreEntry(segment, findSegmentLemma(segment), score, closestEntry);
	}

	private Optional<Double> maxSpec = Optional.empty();

	public double getMaxSpec() {
		if(!maxSpec.isPresent()) {
			Comparator<Term> specComparator = TermProperty.SPECIFICITY.getComparator(false);
			double wrLog = terminology.terms().max(specComparator).get().getSpecificity();
			maxSpec = Optional.of((double)Math.pow(10, wrLog));
		}
		return maxSpec.get();
	}
	
	/*
	 * Finds the best lemma for a segment
	 */
	private String findSegmentLemma(String segment) {
		Collection<String> candidates = this.neoclassicalPrefixes.getTranslations(segment);
		if(candidates.isEmpty())
			return segment;
		else {
			String bestLemma = segment;
			double bestValue = 0d;
			for(String candidateLemma:candidates) {
				for(Term t:swtLemmaIndex.getTerms(candidateLemma)) {
					if(t.getSpecificity() > bestValue) {
						bestValue = t.getSpecificity();
						bestLemma = t.getLemma();
					}
				}
			}
			return bestLemma;
		}
	}

}
