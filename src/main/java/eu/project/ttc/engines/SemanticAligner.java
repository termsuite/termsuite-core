package eu.project.ttc.engines;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang.mutable.MutableInt;
import org.assertj.core.util.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;

import eu.project.ttc.engines.variant.SynonymicRule;
import eu.project.ttc.engines.variant.VariantRule;
import eu.project.ttc.history.TermHistory;
import eu.project.ttc.metrics.Cosine;
import eu.project.ttc.metrics.ExplainedValue;
import eu.project.ttc.metrics.SimilarityDistance;
import eu.project.ttc.models.RelationProperty;
import eu.project.ttc.models.RelationType;
import eu.project.ttc.models.Term;
import eu.project.ttc.models.TermIndex;
import eu.project.ttc.models.TermRelation;
import eu.project.ttc.models.index.CustomTermIndex;
import eu.project.ttc.utils.TermUtils;
import fr.univnantes.julestar.uima.resources.MultimapFlatResource;

public class SemanticAligner {
	private static final Logger LOGGER = LoggerFactory.getLogger(SemanticAligner.class);
	private Optional<TermHistory> history = Optional.empty();
	private SimilarityDistance distance = new Cosine();
	private double similarityThreshold = 0.40;
	private int nbDistributionalCandidates = 5;
	private SynonymicRule rule;
	private MultimapFlatResource dico = new MultimapFlatResource();

	public SemanticAligner setDistance(SimilarityDistance distance) {
		this.distance = distance;
		return this;
	}
	
	public SemanticAligner setSimilarityThreshold(double similarityThreshold) {
		this.similarityThreshold = similarityThreshold;
		return this;
	}
	
	public SemanticAligner setHistory(TermHistory history) {
		this.history = Optional.of(history);
		return this;
	}
	
	
	public void align(TermIndex termIndex) {
		LOGGER.info("Aligning semantic variations {} for rule {}", termIndex.getName(), this.rule.getName());
		if(termIndex.getTerms().isEmpty())
			return;
		Preconditions.checkNotNull(this.rule);
		
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
				t1Relations = Lists.newArrayList();
				
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

	public SemanticAligner setRule(VariantRule rule) {
		Preconditions.checkArgument(rule.isSynonymicRule(), "Rule must be synonymic: " + rule.getName());
		this.rule = SynonymicRule.parseSynonymicRule(rule);
		return this;
	}


	public SemanticAligner setDictionary(MultimapFlatResource dico) {
		this.dico = dico == null ? new MultimapFlatResource() : dico ;
		return this;
	}

}
