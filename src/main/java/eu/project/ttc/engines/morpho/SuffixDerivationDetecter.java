package eu.project.ttc.engines.morpho;

import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ExternalResource;
import org.apache.uima.jcas.JCas;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.project.ttc.models.Term;
import eu.project.ttc.models.TermIndex;
import eu.project.ttc.models.TermWord;
import eu.project.ttc.models.VariationType;
import eu.project.ttc.models.index.CustomTermIndex;
import eu.project.ttc.models.index.TermValueProviders;
import eu.project.ttc.resources.SuffixDerivation;
import eu.project.ttc.resources.SuffixDerivationList;
import eu.project.ttc.resources.TermIndexResource;

public class SuffixDerivationDetecter extends JCasAnnotator_ImplBase {
	private static final Logger LOGGER = LoggerFactory.getLogger(SuffixDerivationDetecter.class);

	private static final String LEMMA_INDEX = "LemmaIndex";
	public static final String TASK_NAME = "Morphosyntactic analysis (suffix derivation)";

	@ExternalResource(key=TermIndexResource.TERM_INDEX, mandatory=true)
	private TermIndexResource termIndexResource;
	
	@ExternalResource(key=SuffixDerivationList.SUFFIX_DERIVATIONS, mandatory=true)
	private SuffixDerivationList suffixDerivationList;

	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		// do nothing
	}
	
	@Override
	public void collectionProcessComplete() throws AnalysisEngineProcessException {
		LOGGER.info("Starting {}", TASK_NAME);
		TermIndex termIndex = termIndexResource.getTermIndex();
		CustomTermIndex lemmaIndex = termIndex.createCustomIndex(
				LEMMA_INDEX, 
				TermValueProviders.TERM_LEMMA_LOWER_CASE_PROVIDER);
		
		int nbDerivations = 0, nbSwt = 0;
		TermWord termWord, derivateTermWord;
		for(Term swt:termIndex.getTerms()) {
			if(!swt.isSingleWord())
				continue;
			nbSwt++;
			termWord = swt.getWords().get(0);
			List<SuffixDerivation> derivations = suffixDerivationList.getSuffixDerivations(termWord);
			for(SuffixDerivation suffixDerivation:derivations) {
				if(suffixDerivation.canDerivate(termWord)) {
					derivateTermWord = suffixDerivation.derivate(termWord);
					List<Term> derivateTerms = lemmaIndex.getTerms(derivateTermWord.getWord().getLemma());
					for(Term derivateTerm:derivateTerms) {
						if(derivateTerm.getWords().get(0).equals(derivateTermWord)) {
							nbDerivations++;
							if(LOGGER.isTraceEnabled())
								LOGGER.trace("Found suffix derivation: {} for word {}", derivateTerm, swt);
							swt.addTermVariation(derivateTerm, VariationType.DERIVATIONAL, suffixDerivation.getType());
						}
					}
				}
			}
		}
		
		termIndex.dropCustomIndex(LEMMA_INDEX);
		
		LOGGER.debug("Number of derivations found: {} out of {} SWTs", 
				nbDerivations, 
				nbSwt);
	}
}
