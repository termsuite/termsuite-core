package fr.univnantes.termsuite.index.providers;

import java.util.Collection;
import java.util.Set;

import com.google.common.collect.Sets;

import fr.univnantes.termsuite.index.TermIndexValueProvider;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.TermWord;

/**
 * Provides all lemmas of the terms
 * 
 * @author Damien Cram
 * 
 */
public class WordLemmasProvider implements TermIndexValueProvider {
	
	@Override
	public Collection<String> getClasses(Term term) {
		Set<String> classes = Sets.newHashSet();
		for(TermWord w:term.getWords()) 
			classes.add(w.getWord().getLemma().toLowerCase());
		return classes;
	}
}
