package fr.univnantes.termsuite.index;

import java.util.Collection;
import java.util.Collections;

import fr.univnantes.termsuite.index.providers.SwtLemmasProvider;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.Terminology;

public class SwtLemmasSwtTermsOnlyProvider extends SwtLemmasProvider {

	
	@SuppressWarnings("unchecked")
	@Override
	public Collection<String> getClasses(Terminology termino, Term term) {
		if(term.isSingleWord())
			return super.getClasses(termino, term);
		else
			return Collections.EMPTY_SET;
	}
}
