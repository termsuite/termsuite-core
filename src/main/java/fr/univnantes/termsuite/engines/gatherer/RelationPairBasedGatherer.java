package fr.univnantes.termsuite.engines.gatherer;

import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import com.google.common.base.Stopwatch;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import fr.univnantes.termsuite.framework.Index;
import fr.univnantes.termsuite.index.TermIndex;
import fr.univnantes.termsuite.index.TermIndexType;
import fr.univnantes.termsuite.model.RelationType;
import fr.univnantes.termsuite.model.Term;

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
		
		Stopwatch indexingSw = Stopwatch.createStarted();
		Multimap<String, Term> relationIndex = getRelationIndex();
		indexingSw.stop();
		logger.debug("{} terms indexed for {} in {}ms",
				terminology.termCount(),
				getRelType(),
				indexingSw.elapsed(TimeUnit.MILLISECONDS)
				);
		
		relationIndex.keySet().stream()
			.parallel()
			.forEach(key -> {
				Collection<Term> terms = relationIndex.get(key);
				if(terms.size() > 1)
					gather(terminology, groovyService, terms, key, cnt);
			});
		
		logger.debug("Num of comparisons: {}", cnt);
	}
	
	@Index(type = TermIndexType.SWT_GROUPING_KEYS)
	private TermIndex swtIndex;
	
	
	public Multimap<String, Term> getRelationIndex() {
		Multimap<String, Term> relationIndex = HashMultimap.create();
		
		
		terminology.relations(getRelType())
			.forEach(rel-> {
				String key = String.format(PAIR_FORMAT, rel.getFrom().getLemma(), rel.getTo().getLemma());
				relationIndex.putAll(key, swtIndex.getTerms(rel.getFrom().getGroupingKey()));
				relationIndex.putAll(key, swtIndex.getTerms(rel.getTo().getGroupingKey()));
			});
		return relationIndex;
	}
}
