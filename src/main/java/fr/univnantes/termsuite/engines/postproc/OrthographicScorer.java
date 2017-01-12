package fr.univnantes.termsuite.engines.postproc;

import fr.univnantes.termsuite.framework.Execute;
import fr.univnantes.termsuite.framework.TerminologyEngine;
import fr.univnantes.termsuite.framework.TerminologyService;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.TermProperty;
import fr.univnantes.termsuite.utils.StringUtils;
import fr.univnantes.termsuite.utils.TermSuiteConstants;

public class OrthographicScorer extends TerminologyEngine {

	@Execute
	public void scoreOrthographic(TerminologyService index) {
		for(Term term:index.getTerms()) 
			term.setProperty(
					TermProperty.ORTHOGRAPHIC_SCORE, 
					StringUtils.getOrthographicScore(term.getLemma().replaceAll(
							TermSuiteConstants.WHITESPACE_STRING, 
							TermSuiteConstants.EMPTY_STRING)));
	}

}
