package fr.univnantes.termsuite.engines.prepare;

import java.util.Set;
import java.util.stream.Collectors;

import fr.univnantes.termsuite.framework.TerminologyEngine;
import fr.univnantes.termsuite.framework.TerminologyService;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.TermWord;
import fr.univnantes.termsuite.utils.TermUtils;

/**
 * 
 * An engine that sets for each {@link TermWord} it {@link TermWord#isSwt()} flag
 * 
 * @author Damien Cram
 *
 */
public class SWTFlagSetter extends TerminologyEngine {
	
	public void setSWTFlags(TerminologyService termino) {
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
