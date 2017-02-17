package fr.univnantes.termsuite.engines.gatherer;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.slf4j.Logger;

import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import fr.univnantes.julestar.uima.resources.MultimapFlatResource;
import fr.univnantes.termsuite.api.TermSuiteException;
import fr.univnantes.termsuite.framework.InjectLogger;
import fr.univnantes.termsuite.framework.Parameter;
import fr.univnantes.termsuite.framework.Resource;
import fr.univnantes.termsuite.framework.TermSuiteFactory;
import fr.univnantes.termsuite.framework.service.RelationService;
import fr.univnantes.termsuite.framework.service.TermService;
import fr.univnantes.termsuite.framework.service.TerminologyService;
import fr.univnantes.termsuite.index.TermIndex;
import fr.univnantes.termsuite.metrics.SimilarityDistance;
import fr.univnantes.termsuite.model.Relation;
import fr.univnantes.termsuite.model.RelationProperty;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.uima.ResourceType;
import fr.univnantes.termsuite.utils.Pair;
import fr.univnantes.termsuite.utils.TermUtils;
import fr.univnantes.termsuite.utils.VariationUtils;

public class SemanticGatherer extends VariationTypeGatherer {
	@InjectLogger Logger logger;

	@Parameter
	private GathererOptions options;
	
	@Resource(type=ResourceType.SYNONYMS)
	private MultimapFlatResource dico = new MultimapFlatResource();

	private AtomicInteger nbAlignmentsCounter = new AtomicInteger(0);
	private Stopwatch indexingSw = Stopwatch.createUnstarted();

	private SimilarityDistance distance;
	
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
	public void execute() {
		try {
			distance = options.getSemanticSimilarityDistance().newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new TermSuiteException(e);
		}
		Stopwatch gatherSw = Stopwatch.createStarted();
		for(VariantRule rule:this.variantRules.getVariantRules(VariationType.SEMANTIC))
			gather((SynonymicRule)rule);
		gatherSw.stop();
		logger.debug("Cumulated indexing time: {}", indexingSw);
		
		logger.debug("Alignment cache stats: {}", alignmentScores.stats()); 
		logger.debug("Alignment cache hit rate: {} - load penalty: {}", 
				alignmentScores.stats().hitRate(),
				alignmentScores.stats().averageLoadPenalty()
				);
		alignmentScores.invalidateAll();
		
		logger.debug("Term gathered in {} - Num of alignments: {}", 
				gatherSw, 
				nbAlignmentsCounter);
	}
	
	public void gather(SynonymicRule rule) {
		
		logger.info("Aligning semantic variations for rule {}", rule.getName());
		if(terminology.getTerms().isEmpty())
			return;
		Preconditions.checkNotNull(rule);
		Preconditions.checkState(rule.getSynonymSourceWordIndex() != -1);
		
		if(!terminology.terms().filter(TermService::isContextSet).findAny().isPresent())
			throw new IllegalStateException("Semantic aligner requires a contextualized term index");
		if(!terminology.extensions().findAny().isPresent())
			throw new IllegalStateException("Semantic aligner requires term extension relations");
		
		/*
		 * Create index
		 */
		indexingSw.start();
		TermIndex index = new TermIndex(rule.getTermProvider());
		terminology.terms().map(TermService::getTerm).forEach(index::addToIndex);
		indexingSw.stop();

		/*
		 * Gather semantic variations
		 */
		Stopwatch ruleSw = Stopwatch.createStarted();
		doSemanticVariation(rule, index);
		ruleSw.stop();
		
		/*
		 * Remove exceeding variations
		 */
		removeExceedingVariations();
		

		logger.debug("Semantic alignment finished for rule {} in {}", rule, ruleSw);
	}

	public void removeExceedingVariations() {
		Set<Relation> toRem = new HashSet<Relation>();
		AtomicInteger exceedingVariations = new AtomicInteger(0);
		terminology.terms().forEach(term->{
			if(term.variations(VariationType.SEMANTIC).findAny().isPresent()) {
				List<Relation> termRelations = term.variations(VariationType.SEMANTIC)
						.map(RelationService::getRelation)
						.collect(toList());
				Collections.sort(termRelations, RelationProperty.SEMANTIC_SCORE.getComparator(true));
				for(int i=options.getSemanticNbCandidates(); i< termRelations.size(); i++) {
					toRem.add(termRelations.get(i));
					exceedingVariations.incrementAndGet();
				}
			}
		});
		logger.debug("Removing {} exceeding semantic variations", exceedingVariations);
	}

	public void doSemanticVariation(SynonymicRule rule, TermIndex index) {
		AtomicInteger nbDistribRelationsFound = new AtomicInteger(0);
		AtomicInteger nbDicoRelationFound = new AtomicInteger(0);
		for(String key:index.keySet()) {
			Term t1, t2, a1, a2;
			Pair<Term> pair;
			List<Relation> t1CandidateRelations;
			List<Term> terms = index.getTerms(key).stream()
					.filter(t->rule.getSourcePatterns().contains(t.getPattern()))
					.collect(Collectors.toList());
			
			for(int i=0; i<terms.size();i++) {
				t1 = terms.get(i);
				String akey1 = TermUtils.toGroupingKey(t1.getWords().get(rule.getSynonymSourceWordIndex()));
				if(!terminology.containsTerm(akey1)) 
					continue;
				else
					a1 = terminology.getTerm(akey1).getTerm();

				t1CandidateRelations = new ArrayList<>();
				
				for(int j=0; j<terms.size();j++) {
					if(i==j)
						continue;
					t2 = terms.get(j);
					
					String akey2 = TermUtils.toGroupingKey(t2.getWords().get(rule.getSynonymSourceWordIndex()));
					if(!terminology.containsTerm(akey2)) 
						continue;
					else
						a2 = terminology.getTerm(akey2).getTerm();
					
					Relation rel = null;
					if(areDicoSynonyms(a1, a2)) {
						nbDicoRelationFound.incrementAndGet();
						rel = buildDicoVariation(terminology, t1, t2);
					}
					
					if(a2.getContext() != null && a1.getContext() != null) {
						pair = new Pair<>(a1, a2);
						nbAlignmentsCounter.incrementAndGet();
						Double value = alignmentScores.getUnchecked(pair);
						if(rel == null) {
							rel = buildDistributionalVariation(terminology, t1,t2,value);
						} else {
							rel.setProperty(RelationProperty.IS_DISTRIBUTIONAL, true);
							rel.setProperty(RelationProperty.SEMANTIC_SIMILARITY, value);
						}
					}
					
					if(rel != null) {
						t1CandidateRelations.add(rel);
						rel.setProperty(
								RelationProperty.SEMANTIC_SCORE, 
								computeScore(
									rel.getBoolean(RelationProperty.IS_DICO),
									rel.getDouble(RelationProperty.SEMANTIC_SIMILARITY)
						));
					}
				} // end for j

				
				// Add top distrib candidates to termindex
				t1CandidateRelations
					.stream()
					.sorted(RelationProperty.SEMANTIC_SCORE.getComparator(true))
					.filter(r -> r.getDouble(RelationProperty.SEMANTIC_SCORE) > options.getSemanticSimilarityThreshold())
					.limit(this.options.getSemanticNbCandidates())
					.forEach(candidateRelation -> {
						nbDistribRelationsFound.incrementAndGet();
						RelationService indexedRelation = terminology.createVariation(
								VariationType.SEMANTIC,
								candidateRelation.getFrom(), 
								candidateRelation.getTo());
						VariationUtils.copyRelationPropertyIfSet(
								candidateRelation, 
								indexedRelation.getRelation(), 
								RelationProperty.IS_DICO,
								RelationProperty.SEMANTIC_SCORE,
								RelationProperty.SEMANTIC_SIMILARITY,
								RelationProperty.IS_DISTRIBUTIONAL);
						watch(candidateRelation);
					});
			}
		}
		logger.debug("Nb distributional synonymic relations found: {}. Total dico synonyms: {}", 
				nbDistribRelationsFound, 
				nbDicoRelationFound
				);
	}

	private Comparable<?> computeScore(Boolean isDico, Double semanticSimilarity) {
		double score = isDico ? 0.5d : 0d;
		score += semanticSimilarity == null ? 0 : semanticSimilarity.doubleValue();
		return score;
	}

	private Relation buildDistributionalVariation(TerminologyService terminoService, Term t1, Term t2, Double value) {
		Relation rel = TermSuiteFactory.createVariation(VariationType.SEMANTIC, t1, t2);
		rel.setProperty(RelationProperty.IS_DISTRIBUTIONAL, true);
		rel.setProperty(RelationProperty.IS_DICO, false);
		rel.setProperty(RelationProperty.SEMANTIC_SIMILARITY, value);
		return rel;
	}

	private Relation buildDicoVariation(TerminologyService terminoService, Term t1, Term t2) {
		Relation rel = TermSuiteFactory.createVariation(VariationType.SEMANTIC, t1, t2);
		rel.setProperty(RelationProperty.IS_DICO, true);
		rel.setProperty(RelationProperty.IS_DISTRIBUTIONAL, false);
		return rel;
	}

	private boolean areDicoSynonyms(Term a1, Term a2) {
		return dico.getValues(a1.getLemma()).contains(a2.getLemma())
				|| dico.getValues(a2.getLemma()).contains(a1.getLemma());
	}

	private void watch(Relation rel) {
		if(history.isPresent()) {
			Term t1 = rel.getFrom();
			Term t2 = rel.getTo();
			String label = rel.getBoolean(RelationProperty.IS_DICO) ? "[dico]" : "";
			label += rel.getBoolean(RelationProperty.IS_DISTRIBUTIONAL) ? ("[distrib:"+rel.getDouble(RelationProperty.SEMANTIC_SIMILARITY)+"]") : "";
			if(this.history.get().isTermWatched(t1))
				this.history.get().saveEvent(
						t1,
						this.getClass(), 
						"Term has a new semantic variant: " + t2 + " " + label);
	
			if(this.history.get().isTermWatched(t2))
				this.history.get().saveEvent(
						t2,
						this.getClass(), 
						"Term has new semantic base " + t1 + " " + label);
		}
	}

	@Override
	protected TermIndex getTermIndex() {
		throw new UnsupportedOperationException("Should never be called");
	}
}
