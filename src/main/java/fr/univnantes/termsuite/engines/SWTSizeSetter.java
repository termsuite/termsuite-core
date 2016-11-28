package fr.univnantes.termsuite.engines;

import java.util.Set;
import java.util.stream.Collectors;

import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.TermIndex;
import fr.univnantes.termsuite.model.TermProperty;
import fr.univnantes.termsuite.utils.TermUtils;

/**
 * 
 * An engine that sets for each term the its number of included SWTs
 * 
 * @author Damien Cram
 *
 */
public class SWTSizeSetter {
	
	
	public void setSWTNumber(TermIndex termIndex) {
		Set<String> swts = termIndex.getTerms().stream()
				.filter(t -> t.getWords().size() == 1)
				.map(Term::getGroupingKey)
				.collect(Collectors.toSet());
		for(Term t:termIndex.getTerms()) {
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
