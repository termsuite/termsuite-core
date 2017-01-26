package fr.univnantes.termsuite.index.providers;

import java.util.Collection;

import com.google.common.collect.ImmutableList;

import fr.univnantes.termsuite.index.AbstractTermIndexValueProvider;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.Terminology;

public class LowercaseTermLemmaProvider extends AbstractTermIndexValueProvider {

	@Override
	public Collection<String> getClasses(Terminology termino, Term term) {
		return ImmutableList.of(term.getLemma().toLowerCase());
	}
}
