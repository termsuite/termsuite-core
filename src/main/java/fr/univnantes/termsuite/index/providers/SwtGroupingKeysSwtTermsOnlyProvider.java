package fr.univnantes.termsuite.index.providers;

import java.util.Collection;
import java.util.Collections;

import fr.univnantes.termsuite.model.Term;

public class SwtGroupingKeysSwtTermsOnlyProvider extends SwtGroupingKeysProvider {

	@SuppressWarnings("unchecked")
	@Override
	public Collection<String> getClasses(Term term) {
		if(term.isSingleWord())
			return super.getClasses(term);
		else
			return Collections.EMPTY_SET;
	}

}
