package fr.univnantes.termsuite.projection;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import fr.univnantes.termsuite.model.Term;

public class DocumentProjection {
	
	private Map<String, Term> terms;
	
	public DocumentProjection(Collection<Term> documentTerms) {
		terms = new HashMap<>();
		documentTerms.forEach(t-> terms.put(t.getGroupingKey(), t));
	}
	
	public Map<String, Term> getTerms() {
		return terms;
	}
	
}
