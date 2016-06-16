package eu.project.ttc.engines.morpho;

import java.util.Map;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.ExternalResource;
import org.apache.uima.jcas.JCas;
import org.assertj.core.util.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.project.ttc.models.Term;
import eu.project.ttc.models.VariationType;
import eu.project.ttc.models.Word;
import eu.project.ttc.resources.PrefixTree;
import eu.project.ttc.resources.TermIndexResource;
import eu.project.ttc.utils.TermSuiteConstants;

public class PrefixSplitter extends JCasAnnotator_ImplBase {
	private static final Logger LOGGER = LoggerFactory.getLogger(PrefixSplitter.class);

	public static final String TASK_NAME = "Morphosyntactic analysis (prefix)";

	@ExternalResource(key=TermIndexResource.TERM_INDEX, mandatory=true)
	private TermIndexResource termIndexResource;
	
	@ExternalResource(key=PrefixTree.PREFIX_TREE, mandatory=true)
	private PrefixTree prefixTree;

	
	public static final String CHECK_IF_MORPHO_EXTENSION_IS_IN_CORPUS = "CheckIfMorphoExtensionInCorpus";
	@ConfigurationParameter(name=CHECK_IF_MORPHO_EXTENSION_IS_IN_CORPUS, mandatory=false, defaultValue = "true")
	private boolean checkIfMorphoExtensionInCorpus;
	
	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		// do nothing
	}
	
	@Override
	public void collectionProcessComplete() throws AnalysisEngineProcessException {
		LOGGER.info("Starting {}", TASK_NAME);
		Map<String, Term> lemmaIndex = Maps.newHashMap();
		int nb = 0;
		String prefixExtension, lemma, pref;
		for(Term swt:termIndexResource.getTermIndex().getTerms()) {
			if(!swt.isSingleWord())
				continue;
			else {
				lemmaIndex.put(swt.getLemma(), swt);
			}
			
			Word word = swt.getWords().get(0).getWord();
			lemma = word.getLemma();
			pref = prefixTree.getPrefix(lemma);
			if(pref != null && pref.length() < lemma.length()) {
				prefixExtension = lemma.substring(pref.length(),lemma.length());
				if(LOGGER.isTraceEnabled())
					LOGGER.trace("Found prefix: {} for word {}", pref, lemma);
				if(checkIfMorphoExtensionInCorpus) {
					if(!lemmaIndex.containsKey(prefixExtension)) {
						if(LOGGER.isTraceEnabled())
							LOGGER.trace("Prefix extension: {} for word {} is not found in corpus. Aborting composition for this word.", prefixExtension, lemma);
						continue;
					} else {
						Term target = lemmaIndex.get(prefixExtension);
						swt.addTermVariation(target, VariationType.IS_PREFIX_OF, TermSuiteConstants.EMPTY_STRING);
					}
				}
				nb++;
			}
		}
		LOGGER.debug("Number of words with prefix composition: {} out of {}", 
				nb, 
				termIndexResource.getTermIndex().getWords().size());
	}
}
