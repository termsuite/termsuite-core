package fr.univnantes.termsuite.engines.prepare;

import java.util.Set;

import com.google.common.collect.Sets;

import fr.univnantes.termsuite.framework.TerminologyEngine;
import fr.univnantes.termsuite.framework.TerminologyService;
import fr.univnantes.termsuite.model.Term;
import uima.sandbox.filter.resources.FilterResource;

public class StopWordCleaner extends TerminologyEngine {

	private FilterResource filter;

	public void clean(TerminologyService terminology) {

		Set<String> filters = filter.getFilters();
		
		Set<Term> toRem = Sets.newHashSet();
		for(Term t:terminology.getTerms()) {
			if(filters.contains(t.getWords().get(0).getWord().getLemma())) {
				// first word of term is a filter word
				toRem.add(t);
			} else if(t.isMultiWord() && filters.contains(t.getWords().get(t.getWords().size() - 1).getWord().getLemma())) {
				// last word of term is a filter word
				toRem.add(t);
			}
		}
		
		for(Term t:toRem) {
			terminology.removeTerm(t);
		}
	}
}
