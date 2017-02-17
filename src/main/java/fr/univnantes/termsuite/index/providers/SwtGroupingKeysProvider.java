package fr.univnantes.termsuite.index.providers;

import static java.util.stream.Collectors.toSet;

import java.util.Collection;

import fr.univnantes.termsuite.index.TermIndexValueProvider;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.TermWord;

public class SwtGroupingKeysProvider implements TermIndexValueProvider {
	@Override
	public Collection<String> getClasses(Term term) {
		return term.getWords().stream()
				.filter(TermWord::isSwt)
				.map(tw -> tw.toGroupingKey())
				.collect(toSet())
				;
	}
}
	