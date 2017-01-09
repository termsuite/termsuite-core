package fr.univnantes.termsuite.engines.postproc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Stopwatch;

import fr.univnantes.termsuite.framework.TerminologyService;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.TermProperty;
import fr.univnantes.termsuite.model.Terminology;
import fr.univnantes.termsuite.utils.StringUtils;
import fr.univnantes.termsuite.utils.TermSuiteConstants;

public class TermScorer {
	private static final Logger LOGGER = LoggerFactory.getLogger(TermScorer.class);

	public void score(Terminology termino) {
		LOGGER.info("Computing scores for terms");
		Stopwatch sw = Stopwatch.createStarted();
		
		
		LOGGER.debug("Computing independance for all terms");
		new IndependanceScorer().setIndependance(new TerminologyService(termino));

		LOGGER.debug("Computing orthographic scores");
		scoreOrthographic(termino);
		sw.stop();
		LOGGER.debug("Scores computed in {}", sw);
	}
	
	
	public void scoreOrthographic(Terminology index) {
		for(Term term:index.getTerms()) 
			term.setProperty(
					TermProperty.ORTHOGRAPHIC_SCORE, 
					StringUtils.getOrthographicScore(term.getLemma().replaceAll(
							TermSuiteConstants.WHITESPACE_STRING, 
							TermSuiteConstants.EMPTY_STRING)));
	}

}
