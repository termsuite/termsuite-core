package fr.univnantes.termsuite.engines;

import java.util.Set;
import java.util.stream.Collectors;

import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.TermWord;
import fr.univnantes.termsuite.model.Terminology;
import fr.univnantes.termsuite.utils.TermUtils;

/**
 * 
 * An engine that sets for each {@link TermWord} it {@link TermWord#isSwt()} flag
 * 
 * @author Damien Cram
 *
 */
public class SWTFlagSetter {
	
	public void setSWTFlags(Terminology termino) {
		Set<String> swts = termino.getTerms().stream()
				.filter(t -> t.getWords().size() == 1)
				.map(Term::getGroupingKey)
				.collect(Collectors.toSet());
		for(Term t:termino.getTerms()) {
			for(TermWord tw:t.getWords()) 
				tw.setSwt(swts.contains(TermUtils.toGroupingKey(tw)));
		}
		
	}

}
