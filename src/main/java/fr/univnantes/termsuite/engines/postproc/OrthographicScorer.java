package fr.univnantes.termsuite.engines.postproc;

import fr.univnantes.termsuite.SimpleEngine;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.TermProperty;
import fr.univnantes.termsuite.utils.StringUtils;
import fr.univnantes.termsuite.utils.TermSuiteConstants;

public class OrthographicScorer extends SimpleEngine {
	
	@Override
	public void execute() {
		for(Term term:terminology.getTerms()) 
			term.setProperty(
					TermProperty.ORTHOGRAPHIC_SCORE, 
					StringUtils.getOrthographicScore(term.getLemma().replaceAll(
							TermSuiteConstants.WHITESPACE_STRING, 
							TermSuiteConstants.EMPTY_STRING)));
	}

}
