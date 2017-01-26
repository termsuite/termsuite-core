package fr.univnantes.termsuite.index.providers;

import java.util.Collection;

import fr.univnantes.termsuite.index.AbstractTermIndexValueProvider;
import fr.univnantes.termsuite.model.RelationType;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.Terminology;

public class PrefixationLemmasProvider extends AbstractTermIndexValueProvider {

	@Override
	public Collection<String> getClasses(Terminology termino, Term term) {
		return toRelationPairs(termino, term, RelationType.IS_PREFIX_OF);
	}

}
