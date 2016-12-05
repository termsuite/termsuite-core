package fr.univnantes.termsuite.engines.gatherer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang.mutable.MutableInt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;

import fr.univnantes.julestar.uima.resources.MultimapFlatResource;
import fr.univnantes.termsuite.metrics.Cosine;
import fr.univnantes.termsuite.metrics.ExplainedValue;
import fr.univnantes.termsuite.metrics.SimilarityDistance;
import fr.univnantes.termsuite.model.RelationProperty;
import fr.univnantes.termsuite.model.RelationType;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.TermIndex;
import fr.univnantes.termsuite.model.TermRelation;
import fr.univnantes.termsuite.model.termino.CustomTermIndex;
import fr.univnantes.termsuite.utils.TermHistory;
import fr.univnantes.termsuite.utils.TermUtils;

public class SemanticTermGatherer extends AbstractGatherer {
	private static final Logger LOGGER = LoggerFactory.getLogger(SemanticTermGatherer.class);
	private Optional<TermHistory> history = Optional.empty();
	private SimilarityDistance distance = new Cosine();
	private double similarityThreshold = 0.40;
	private int nbDistributionalCandidates = 5;
	private MultimapFlatResource dico = new MultimapFlatResource();

	public SemanticTermGatherer setDistance(SimilarityDistance distance) {
		this.distance = distance;
		return this;
	}
	
	public SemanticTermGatherer setSimilarityThreshold(double similarityThreshold) {
		this.similarityThreshold = similarityThreshold;
		return this;
	}
	
	public SemanticTermGatherer setHistory(TermHistory history) {
		if(history != null)
			this.history = Optional.of(history);
		return this;
	}
	
	public SemanticTermGatherer setDictionary(Optional<MultimapFlatResource> dico) {
		this.dico = dico.isPresent() ? dico.get() : new MultimapFlatResource();
		return this;
	}

	@Override
	public void gather(TermIndex termIndex) {
		for(VariantRule rule:this.variantRules)
			gather(termIndex, (SynonymicRule)rule);
	}
	
	public void gather(TermIndex termIndex, SynonymicRule rule) {
		LOGGER.info("Aligning semantic variations {} for rule {}", termIndex.getName(), rule.getName());
		if(termIndex.getTerms().isEmpty())
			return;
		Preconditions.checkNotNull(rule);
		
		Preconditions.checkState(rule.getSynonymSourceWordIndex() != -1);
		
		Term t1, t2, a1, a2;
		List<TermRelation> t1Relations;
		int nbAlignments = 0;
		final MutableInt nbDistribRelationsFound = new MutableInt(0);
		final MutableInt nbDicoRelationFound = new MutableInt(0);
		
		Stopwatch sw = Stopwatch.createUnstarted();
		
		if(!termIndex.getTerms().stream().filter(t->t.getContext()!= null).findAny().isPresent())
			throw new IllegalStateException("Semantic aligner requires a contextualized term index");
		if(!termIndex.getRelations(RelationType.HAS_EXTENSION).findAny().isPresent())
			throw new IllegalStateException("Semantic aligner requires term extension relations");
		
		CustomTermIndex index = termIndex.createCustomIndex("SubSequence"+rule.getName(), rule.getTermProvider());
		
		for(String key:index.keySet()) {
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
							nbDicoRelationFound.increment();
							createDicoRelation(termIndex, t1, t2);
						}
					}
					
					if(a2.getContext() == null) {
						LOGGER.warn("No context vector set for term {}", a1);
						continue;
					} else {
						nbAlignments++;
						sw.start();
						ExplainedValue explainedValue = distance
								.getExplainedValue(a1.getContext(), a2.getContext());
						sw.stop();
						if(explainedValue.getValue() > similarityThreshold) {
							TermRelation rel = new TermRelation(
									RelationType.SYNONYMIC,
									t1,
									t2);
							rel.setProperty(RelationProperty.IS_DISTRIBUTIONAL, true);
							rel.setProperty(RelationProperty.SIMILARITY, explainedValue.getValue());
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
						nbDistribRelationsFound.increment();
						termIndex.addRelation(rel);
						watch(rel.getFrom(), rel.getTo());
					});
			}
		}
		
		LOGGER.debug("Number of context vectors compared: {}, nb distributional synonymic relations found: {}. Total alignment time: {}. Total dico synonyms: {}", 
				nbAlignments, 
				nbDistribRelationsFound, 
				sw,
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
