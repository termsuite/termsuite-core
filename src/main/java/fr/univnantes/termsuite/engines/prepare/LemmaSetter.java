package fr.univnantes.termsuite.engines.prepare;

import fr.univnantes.termsuite.engines.SimpleEngine;
import fr.univnantes.termsuite.model.TermProperty;
import fr.univnantes.termsuite.utils.TermUtils;

/**
 * 
 * An engine that sets for each term the its number of included SWTs
 * 
 * @author Damien Cram
 *
 */
public class LemmaSetter extends SimpleEngine {
	
	@Override
	public void execute() {
		terminology.terms().forEach(t-> {
			t.setProperty(TermProperty.LEMMA, TermUtils.getTermLemma(t.getTerm()));
		});
	}
}
