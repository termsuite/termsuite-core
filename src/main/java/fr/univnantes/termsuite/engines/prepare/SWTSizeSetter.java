package fr.univnantes.termsuite.engines.prepare;

import java.util.Set;
import java.util.stream.Collectors;

import fr.univnantes.termsuite.framework.TerminologyEngine;
import fr.univnantes.termsuite.framework.TerminologyService;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.TermProperty;
import fr.univnantes.termsuite.utils.TermUtils;

/**
 * 
 * An engine that sets for each term the its number of included SWTs
 * 
 * @author Damien Cram
 *
 */
public class SWTSizeSetter extends TerminologyEngine {
	
	
	public void setSWTNumber(TerminologyService termino) {
		Set<String> swts = termino.terms()
				.filter(t -> t.getWords().size() == 1)
				.map(Term::getGroupingKey)
				.collect(Collectors.toSet());
		for(Term t:termino.getTerms()) {
			long cnt = t.getWords()
				.stream()
				.filter( tw -> swts.contains(TermUtils.toGroupingKey(tw)))
				.count();
			t.setProperty(TermProperty.SWT_SIZE, (int)cnt);
			if(cnt == 1)
				t.setProperty(TermProperty.IS_SINGLE_WORD, true);
		}
		
	}

}
