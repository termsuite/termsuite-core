package fr.univnantes.termsuite.engines.prepare;

import java.util.Set;

import com.google.common.collect.Sets;

import fr.univnantes.termsuite.engines.SimpleEngine;
import fr.univnantes.termsuite.framework.Resource;
import fr.univnantes.termsuite.framework.service.TermService;
import fr.univnantes.termsuite.uima.ResourceType;
import uima.sandbox.filter.resources.FilterResource;

public class StopWordCleaner extends SimpleEngine {

	@Resource(type=ResourceType.STOP_WORDS_FILTER)
	private FilterResource filter;

	@Override
	public void execute() {

		Set<String> filters = filter.getFilters();
		
		Set<TermService> toRem = Sets.newHashSet();
		for(TermService t:terminology.getTerms()) {
			if(filters.contains(t.getWords().get(0).getWord().getLemma())) {
				// first word of term is a filter word
				toRem.add(t);
			} else if(t.isMultiWord() && filters.contains(t.getWords().get(t.getWords().size() - 1).getWord().getLemma())) {
				// last word of term is a filter word
				toRem.add(t);
			}
		}
		
		for(TermService t:toRem) {
			terminology.removeTerm(t);
		}
	}
}
