package fr.univnantes.termsuite.engines.gatherer;

import java.util.Collection;
import java.util.LinkedList;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;

import fr.univnantes.termsuite.index.TermIndexValueProvider;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.utils.TermSuiteConstants;

public class SynonymicRule extends VariantRule {
	private int synonymSourceWordIndex = -1;
	private TermIndexValueProvider equalityProvider;
	private LinkedList<Integer> eqIndices = Lists.newLinkedList();
	
	public SynonymicRule(String ruleName) {
		super(ruleName);
	}

	public int getSynonymSourceWordIndex() {
		return synonymSourceWordIndex;
	}
	
	public String getIndexingKey(Term t) {
		return equalityProvider.getClasses(t).iterator().next();
	}

	public TermIndexValueProvider getTermProvider() {
		return equalityProvider;
	}
	
	public LinkedList<Integer> getEqIndices() {
		return eqIndices;
	}
	
	public void setSynonymSourceWordIndex(int synonymSourceWordIndex) {
		this.synonymSourceWordIndex = synonymSourceWordIndex;
	}
	
	public void setEqIndices(LinkedList<Integer> eqIndices) {
		this.eqIndices = eqIndices;
		initEqualityProvider(eqIndices);
	}

	public void initEqualityProvider(LinkedList<Integer> eqIndices) {
		this.equalityProvider = new TermIndexValueProvider() {
			@Override
			public Collection<String> getClasses(Term term) {
				if(eqIndices.getLast() < term.getWords().size()) {
					return Lists.newArrayList(term.getWords().subList(
							eqIndices.getFirst(), 
							eqIndices.getLast() + 1).stream()
						.map(tw -> tw.getWord().getLemma())
						.collect(Collectors.joining(TermSuiteConstants.COLONS)));
				} else
					return Lists.newArrayList();
			}
		};
	}
}
