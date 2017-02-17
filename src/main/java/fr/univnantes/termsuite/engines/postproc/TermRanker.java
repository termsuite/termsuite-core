package fr.univnantes.termsuite.engines.postproc;

import static java.util.stream.Collectors.toList;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import fr.univnantes.termsuite.engines.SimpleEngine;
import fr.univnantes.termsuite.framework.Parameter;
import fr.univnantes.termsuite.framework.service.TermService;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.TermProperty;

public class TermRanker extends SimpleEngine {
	
	@Parameter
	private TermRankingOptions config;
	
	@Override
	public void execute() {
		List<Term> ranked = terminology.terms().map(TermService::getTerm).collect(toList());
		Comparator<Term> comparator = config.getRankingProperty().getComparator(config.isDesc());
		Collections.sort(ranked, comparator);
		for(int index = 0; index < ranked.size(); index++) {
			ranked.get(index).setProperty(TermProperty.RANK, index + 1);
			watch(ranked, index);
		}
	}

	private void watch(List<Term> ranked, int index) {
		if(history.isPresent()) {
			if(history.get().isTermWatched(ranked.get(index)))
				history.get().saveEvent(
						ranked.get(index), 
						this.getClass(), 
						"Set term rank: " + (index+1));
		}
	}
}
