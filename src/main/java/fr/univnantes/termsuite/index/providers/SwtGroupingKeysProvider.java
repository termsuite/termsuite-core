package fr.univnantes.termsuite.index.providers;

import static java.util.stream.Collectors.toSet;

import java.util.Collection;

import fr.univnantes.termsuite.index.AbstractTermIndexValueProvider;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.TermWord;
import fr.univnantes.termsuite.model.Terminology;

public class SwtGroupingKeysProvider extends AbstractTermIndexValueProvider {
	@Override
	public Collection<String> getClasses(Terminology termino, Term term) {
		return term.getWords().stream()
				.filter(TermWord::isSwt)
				.map(tw -> tw.toGroupingKey())
				.collect(toSet())
				;
	}
}
