package fr.univnantes.termsuite.index.providers;

import java.util.Collection;
import java.util.LinkedList;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;

import fr.univnantes.termsuite.index.TermIndexValueProvider;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.utils.TermSuiteConstants;

public class EqualityIndicesProvider implements TermIndexValueProvider {
	private LinkedList<Integer> eqIndices;
	
	public EqualityIndicesProvider(LinkedList<Integer> eqIndices) {
		super();
		this.eqIndices = eqIndices;
	}

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
}
