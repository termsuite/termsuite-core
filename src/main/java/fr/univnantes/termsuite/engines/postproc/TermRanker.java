package fr.univnantes.termsuite.engines.postproc;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;

import com.google.common.collect.Lists;

import fr.univnantes.termsuite.framework.Execute;
import fr.univnantes.termsuite.framework.TerminologyEngine;
import fr.univnantes.termsuite.framework.TerminologyService;
import fr.univnantes.termsuite.model.Term;

public class TermRanker extends TerminologyEngine {
	
	@Inject
	private TermRankingOptions config = new TermRankingOptions();
	
	@Execute
	public void rank(TerminologyService service) {
		List<Term> ranked = Lists.newArrayList(service.getTerms());
		Comparator<Term> comparator = config.getRankingProperty().getComparator(config.isDesc());
		Collections.sort(ranked, comparator);
		for(int index = 0; index < ranked.size(); index++) {
			ranked.get(index).setRank(index + 1);
			watch(ranked, index);
		}

	}

	private void watch(List<Term> ranked, int index) {
		if(history.isPresent()) {
			if(history.get().isWatched(ranked.get(index)))
				history.get().saveEvent(
						ranked.get(index).getGroupingKey(), 
						this.getClass(), 
						"Set term rank: " + (index+1));
		}
	}
}
