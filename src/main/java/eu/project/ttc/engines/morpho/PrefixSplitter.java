package eu.project.ttc.engines.morpho;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
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

	
	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		// do nothing
	}
	
	@Override
	public void collectionProcessComplete() throws AnalysisEngineProcessException {
		LOGGER.info("Starting {}", TASK_NAME);
		
		int nb = 0;
		for(Word word:termIndexResource.getTermIndex().getWords()) {
			String lemma = word.getLemma();
			String pref = prefixTree.getPrefix(lemma);
			if(pref != null && pref.length() < lemma.length()) {
				if(LOGGER.isTraceEnabled())
					LOGGER.trace("Found prefix: {} for word {}", pref, lemma);
				nb++;
				WordBuilder builder = new WordBuilder(word);
				builder.addComponent(
					0, 
					pref.length(), 
					pref);
				builder.addComponent(
						pref.length(), 
						lemma.length(), 
						lemma.substring(pref.length(),lemma.length()));
				builder.setCompoundType(CompoundType.PREFIX);
				builder.create();
			}
		}
		LOGGER.debug("Number of words with prefix composition: {} out of {}", 
				nb, 
				termIndexResource.getTermIndex().getWords().size());
	}
}
