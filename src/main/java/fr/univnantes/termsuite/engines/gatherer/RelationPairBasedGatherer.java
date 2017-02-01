package fr.univnantes.termsuite.engines.gatherer;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import fr.univnantes.termsuite.framework.service.TerminologyService;
import fr.univnantes.termsuite.index.TermIndex;
import fr.univnantes.termsuite.model.RelationType;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.Relation;
import fr.univnantes.termsuite.model.TermWord;

/**
 * 
 * A special term gatherer based on a index built upon
 * all connections of a given {@link RelationType}
 * 
 * @author Damien Cram
 * 
 * @see PrefixationGatherer
 * @see DerivationGatherer
 *
 */
public abstract class RelationPairBasedGatherer extends VariationTypeGatherer {
	private static final String PAIR_FORMAT = "%s+%s";

	@Override
	protected TermIndex getTermIndex() {
		throw new UnsupportedOperationException("Should never be called");
	}
	
	protected abstract RelationType getRelType();
	
	@Override
	public void execute() {
		if(variantRules.getVariantRules(variationType).isEmpty())
			return;

		AtomicLong cnt = new AtomicLong(0);
		
		Multimap<String, Term> relationIndex = getRelationIndex(terminology, getRelType());
		
		relationIndex.keySet().stream()
			.parallel()
			.forEach(key -> {
				Collection<Term> terms = relationIndex.get(key);
				if(terms.size() > 1)
					gather(terminology, groovyService, terms, key, cnt);
			});
		
		logger.debug("Num of comparisons: {}", cnt);
	}

	public static Multimap<String, Term> getRelationIndex(TerminologyService terminology, RelationType relType) {
		Multimap<String, Term> relationIndex = HashMultimap.create();
		terminology.terms().forEach(term-> {
			Term t;
			Set<Relation> relations = new HashSet<>();
			for(TermWord tw:term.getWords()) {
				if((t = terminology.getTermUnchecked(tw.toGroupingKey())) != null) {
					terminology.inboundRelations(t, relType).forEach(relations::add);
					terminology.outboundRelations(t, relType).forEach(relations::add);
				}
			}
			relations.stream()
				.map(rel -> String.format(PAIR_FORMAT, rel.getFrom().getLemma(), rel.getTo().getLemma()))
				.forEach(key -> relationIndex.put(key, term));

		});
		return relationIndex;
	}
}
