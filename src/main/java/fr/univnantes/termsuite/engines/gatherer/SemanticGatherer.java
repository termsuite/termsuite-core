package fr.univnantes.termsuite.engines.gatherer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import fr.univnantes.julestar.uima.resources.MultimapFlatResource;
import fr.univnantes.termsuite.metrics.Cosine;
import fr.univnantes.termsuite.metrics.SimilarityDistance;
import fr.univnantes.termsuite.model.RelationProperty;
import fr.univnantes.termsuite.model.RelationType;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.TermIndex;
import fr.univnantes.termsuite.model.TermRelation;
import fr.univnantes.termsuite.model.termino.CustomTermIndex;
import fr.univnantes.termsuite.utils.Pair;
import fr.univnantes.termsuite.utils.TermHistory;
import fr.univnantes.termsuite.utils.TermUtils;

public class SemanticGatherer extends AbstractGatherer {
	private static final Logger LOGGER = LoggerFactory.getLogger(SemanticGatherer.class);
	private Optional<TermHistory> history = Optional.empty();
	private SimilarityDistance distance = new Cosine();
	private double similarityThreshold = 0.40;
	private int nbDistributionalCandidates = 5;
	private MultimapFlatResource dico = new MultimapFlatResource();

	public SemanticGatherer setDistance(SimilarityDistance distance) {
		this.distance = distance;
		return this;
	}
	
	public SemanticGatherer setSimilarityThreshold(double similarityThreshold) {
		this.similarityThreshold = similarityThreshold;
		return this;
	}
	
	public SemanticGatherer setHistory(TermHistory history) {
		if(history != null)
			this.history = Optional.of(history);
		return this;
	}
	
	public SemanticGatherer setDictionary(Optional<MultimapFlatResource> dico) {
		this.dico = dico.isPresent() ? dico.get() : new MultimapFlatResource();
		return this;
	}

	private AtomicInteger nbAlignments = new AtomicInteger(0);
	private Stopwatch indexingSw = Stopwatch.createUnstarted();

	private LoadingCache<Pair<Term>, Double> alignmentScores = CacheBuilder.newBuilder()
			.initialCapacity(10000)
			.maximumSize(100000)
			.recordStats()
			.build(new CacheLoader<Pair<Term>, Double>() {
				@Override
				public Double load(Pair<Term> pair) throws Exception {
					return distance
							.getValue(
									pair.getElement1().getContext(), 
									pair.getElement2().getContext());
				}
			});
	
	
	@Override
	public void gather(TermIndex termIndex) {
		Stopwatch gatherSw = Stopwatch.createStarted();
		for(VariantRule rule:this.variantRules)
			gather(termIndex, (SynonymicRule)rule);
		gatherSw.stop();
		LOGGER.debug("Cumulated indexing time: {}", indexingSw);
		
		LOGGER.debug("Alignment cache stats: {}", alignmentScores.stats()); 
		LOGGER.debug("Alignment cache hit rate: {} - load penalty: {}", 
				alignmentScores.stats().hitRate(),
				alignmentScores.stats().averageLoadPenalty()
				);
		alignmentScores.invalidateAll();
		
		LOGGER.debug("Term gathered in {} - Num of alignments: {}", 
				gatherSw, 
				nbAlignments);
	}
	
	public void gather(TermIndex termIndex, SynonymicRule rule) {
		LOGGER.info("Aligning semantic variations for rule {}", rule.getName());
		if(termIndex.getTerms().isEmpty())
			return;
		Preconditions.checkNotNull(rule);
		
		Preconditions.checkState(rule.getSynonymSourceWordIndex() != -1);
		
		AtomicInteger nbDistribRelationsFound = new AtomicInteger(0);
		AtomicInteger nbDicoRelationFound = new AtomicInteger(0);
		
		
		if(!termIndex.getTerms().stream().filter(t->t.getContext()!= null).findAny().isPresent())
			throw new IllegalStateException("Semantic aligner requires a contextualized term index");
		if(!termIndex.getRelations(RelationType.HAS_EXTENSION).findAny().isPresent())
			throw new IllegalStateException("Semantic aligner requires term extension relations");
		
		String indexName = "SubSequence"+rule.getName();

		indexingSw.start();
		CustomTermIndex index = termIndex.createCustomIndex(indexName, rule.getTermProvider());
		indexingSw.stop();

		Stopwatch ruleSw = Stopwatch.createStarted();
		
		index.keySet().parallelStream().forEach(key ->{
			Term t1, t2, a1, a2;
			Pair<Term> pair;
			List<TermRelation> t1Relations;
			List<Term> terms = index.getTerms(key).stream()
										.filter(t->rule.getSourcePatterns().contains(t.getPattern()))
										.collect(Collectors.toList());
			
			for(int i=0; i<terms.size();i++) {
				t1 = terms.get(i);
				String akey1 = TermUtils.toGroupingKey(t1.getWords().get(rule.getSynonymSourceWordIndex()));
				a1 = termIndex.getTermByGroupingKey(akey1);
				if(a1 == null) {
					continue;
				} else if(a1.getContext() == null) {
					LOGGER.warn("No context vector set for term {}", a1);
					continue;
				}
				t1Relations = new ArrayList<>();
				
				for(int j=0; j<terms.size();j++) {
					if(i==j)
						continue;
					t2 = terms.get(j);
					String akey2 = TermUtils.toGroupingKey(t2.getWords().get(rule.getSynonymSourceWordIndex()));
					a2 = termIndex.getTermByGroupingKey(akey2);
					if(a2 == null) {
						continue;
					} 
					
					if(!dico.getValues(a1.getLemma()).isEmpty()) {
						// test all synonyms from dico
						if(dico.getValues(a1.getLemma()).contains(a2.getLemma())) {
							nbDicoRelationFound.incrementAndGet();
							createDicoRelation(termIndex, t1, t2);
						}
					}
					
					if(a2.getContext() == null) {
						LOGGER.warn("No context vector set for term {}", a1);
						continue;
					} else {
						pair = new Pair<>(a1, a2);
						nbAlignments.incrementAndGet();
						Double value = alignmentScores.getUnchecked(pair);
						if(value > similarityThreshold) {
							TermRelation rel = new TermRelation(
									RelationType.SYNONYMIC,
									t1,
									t2);
							rel.setProperty(RelationProperty.IS_DISTRIBUTIONAL, true);
							rel.setProperty(RelationProperty.SIMILARITY, value);
							t1Relations.add(rel);
						}
					}
				} // end for j

				
				// Add top distrib candidates to termindex
				t1Relations
					.stream()
					.sorted(RelationProperty.SIMILARITY.getComparator(true))
					.limit(this.nbDistributionalCandidates)
					.forEach(rel -> {
						nbDistribRelationsFound.incrementAndGet();
						termIndex.addRelation(rel);
						watch(rel.getFrom(), rel.getTo());
					});
			}
		});
		ruleSw.stop();

		termIndex.dropCustomIndex(indexName);
		LOGGER.debug("Semantic alignment finished for rule {} in {}", rule, ruleSw);
		LOGGER.debug("Nb distributional synonymic relations found: {}. Total dico synonyms: {}", 
				nbDistribRelationsFound, 
				nbDicoRelationFound
				);
	}

	public void createDicoRelation(TermIndex termIndex, Term t1, Term t2) {
		TermRelation rel = new TermRelation(
				RelationType.SYNONYMIC,
				t1,
				t2);
		rel.setProperty(RelationProperty.IS_DISTRIBUTIONAL, false);
		termIndex.addRelation(rel);
		watch(t1, t2);
	}

	private void watch(Term t1, Term t2) {
		if(history.isPresent()) {
			if(this.history.get().isWatched(t1.getGroupingKey()))
				this.history.get().saveEvent(
						t1.getGroupingKey(),
						this.getClass(), 
						"Term has a new semantic variant: " + t2);
	
			if(this.history.get().isWatched(t2.getGroupingKey()))
				this.history.get().saveEvent(
						t2.getGroupingKey(),
						this.getClass(), 
						"Term has a new semantic variant " + t1);
		}
	}
}
