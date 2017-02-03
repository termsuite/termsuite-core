package fr.univnantes.termsuite.index.providers;

import java.util.Collection;
import java.util.Collections;

import fr.univnantes.termsuite.model.Term;

public class SwtLemmasSwtTermsOnlyProvider extends SwtLemmasProvider {

	
	@SuppressWarnings("unchecked")
	@Override
	public Collection<String> getClasses(Term term) {
		if(term.getWords().size() == 1)
			return super.getClasses(term);
		else
			return Collections.EMPTY_SET;
	}
}
