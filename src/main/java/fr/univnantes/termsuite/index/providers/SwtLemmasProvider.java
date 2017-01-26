package fr.univnantes.termsuite.index.providers;

import java.util.Collection;
import java.util.Set;

import com.google.common.collect.Sets;

import fr.univnantes.termsuite.index.AbstractTermIndexValueProvider;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.TermWord;
import fr.univnantes.termsuite.model.Terminology;

/**
 * Provides all lemmas of the terms
 * 
 * @author Damien Cram
 * 
 */
public class SwtLemmasProvider extends AbstractTermIndexValueProvider {
	
	@Override
	public Collection<String> getClasses(Terminology termino, Term term) {
		Set<String> classes = Sets.newHashSet();
		for(TermWord w:term.getWords()) 
			if(w.isSwt())
				classes.add(w.getWord().getLemma().toLowerCase());
		return classes;
	}
}
