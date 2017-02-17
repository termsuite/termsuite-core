package fr.univnantes.termsuite.engines.gatherer;

import java.util.LinkedList;

import com.google.common.collect.Lists;

import fr.univnantes.termsuite.index.TermIndexValueProvider;
import fr.univnantes.termsuite.index.providers.EqualityIndicesProvider;
import fr.univnantes.termsuite.model.Term;

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
		this.equalityProvider = new EqualityIndicesProvider(eqIndices);
	}
}
