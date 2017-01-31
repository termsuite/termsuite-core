package fr.univnantes.termsuite.index.providers;

import java.util.Collection;

import com.google.common.collect.ImmutableList;

import fr.univnantes.termsuite.index.TermIndexValueProvider;
import fr.univnantes.termsuite.model.Term;

public class LowercaseTermLemmaProvider implements TermIndexValueProvider {

	@Override
	public Collection<String> getClasses(Term term) {
		return ImmutableList.of(term.getLemma().toLowerCase());
	}
}
