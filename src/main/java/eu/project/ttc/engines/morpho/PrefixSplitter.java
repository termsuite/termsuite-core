package eu.project.ttc.engines.morpho;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.ExternalResource;
import org.apache.uima.jcas.JCas;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.project.ttc.models.CompoundType;
import eu.project.ttc.models.Word;
import eu.project.ttc.models.WordBuilder;
import eu.project.ttc.resources.PrefixTree;
import eu.project.ttc.resources.TermIndexResource;

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
		
		int nb = 0;
		String prefixExtension, lemma, pref;
		for(Word word:termIndexResource.getTermIndex().getWords()) {
			lemma = word.getLemma();
			pref = prefixTree.getPrefix(lemma);
			if(pref != null && pref.length() < lemma.length()) {
				prefixExtension = lemma.substring(pref.length(),lemma.length());
				if(LOGGER.isTraceEnabled())
					LOGGER.trace("Found prefix: {} for word {}", pref, lemma);
				if(checkIfMorphoExtensionInCorpus) {
					if(termIndexResource.getTermIndex().getWord(prefixExtension) == null) {
						if(LOGGER.isTraceEnabled())
							LOGGER.trace("Prefix extension: {} for word {} is not found in corpus. Aborting composition for this word.", prefixExtension, lemma);
						continue;
					}
				}
				nb++;
				WordBuilder builder = new WordBuilder(word);
				builder.addComponent(
					0, 
					pref.length(), 
					pref);
				builder.addComponent(
						pref.length(), 
						lemma.length(), 
						prefixExtension);
				builder.setCompoundType(CompoundType.PREFIX);
				builder.create();
			}
		}
		LOGGER.debug("Number of words with prefix composition: {} out of {}", 
				nb, 
				termIndexResource.getTermIndex().getWords().size());
	}
}
