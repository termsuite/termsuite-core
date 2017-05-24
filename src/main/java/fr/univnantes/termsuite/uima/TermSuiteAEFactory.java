package fr.univnantes.termsuite.uima;

import java.nio.file.Path;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;

import fr.univnantes.termsuite.api.ResourceConfig;
import fr.univnantes.termsuite.model.Lang;
import fr.univnantes.termsuite.model.Tagger;
import fr.univnantes.termsuite.types.FixedExpression;


/**
 * 
 * A factory for TermSuite UIMA Analysis Engines with 
 * default resource configuration (i.e. linguistic 
 * resources are taken from embedded resources).
 * 
 * @author Damien Cram
 *
 */
public class TermSuiteAEFactory {

	private static final ResourceConfig DEFAULT_RESOURCE_CONFIG = new ResourceConfig();

	public static AnalysisEngineDescription createStemmerAEDesc(Lang lang) {
		return CustomResourceTermSuiteAEFactory.createStemmerAEDesc(DEFAULT_RESOURCE_CONFIG, lang);
	}

	public static AnalysisEngineDescription createNormalizerAEDesc(Lang lang, Tagger tagger) {
		return CustomResourceTermSuiteAEFactory.createNormalizerAEDesc(DEFAULT_RESOURCE_CONFIG, lang, tagger);
	}

	public static AnalysisEngineDescription createWordTokenizerAEDesc(Lang lang) {
		return CustomResourceTermSuiteAEFactory.createWordTokenizerAEDesc(DEFAULT_RESOURCE_CONFIG, lang);
	}

	public static AnalysisEngineDescription createMateAEDesc(Lang lang, Path mateModelPath) {
		return CustomResourceTermSuiteAEFactory.createMateAEDesc(DEFAULT_RESOURCE_CONFIG, lang, mateModelPath);
	}
	
	public static AnalysisEngineDescription createDocumentLoggerAEDesc(long nbDocument, long corpusSize) {
		return CustomResourceTermSuiteAEFactory.createDocumentLoggerAEDesc(nbDocument, corpusSize);
	}
	
	public static AnalysisEngineDescription createURLFilterAEDesc() {
		return CustomResourceTermSuiteAEFactory.createURLFilterAEDesc();
	}

	/**
	 * Spots fixed expressions in the CAS an creates {@link FixedExpression}
	 * annotation whenever one is found.
	 * 
	 * @return
	 */
	public static AnalysisEngineDescription createFixedExpressionSpotterAEDesc(Lang lang)  {
		return CustomResourceTermSuiteAEFactory.createFixedExpressionSpotterAEDesc(DEFAULT_RESOURCE_CONFIG, lang);
	}

	public static AnalysisEngineDescription createChineseTokenizerAEDesc()  {
		return CustomResourceTermSuiteAEFactory.createChineseTokenizerAEDesc();		
	}

	public static AnalysisEngineDescription createCasStatCounterAEDesc(String statName)  {
		return CustomResourceTermSuiteAEFactory.createCasStatCounterAEDesc(statName);
	}

	public static AnalysisEngineDescription createTreeTaggerAEDesc(Lang lang, Path treeTaggerPath) {
		return CustomResourceTermSuiteAEFactory.createTreeTaggerAEDesc(DEFAULT_RESOURCE_CONFIG, lang, treeTaggerPath);
	}
	public static AnalysisEngineDescription createRegexSpotterAEDesc(Lang lang) {
		return CustomResourceTermSuiteAEFactory.createRegexSpotterAEDesc(DEFAULT_RESOURCE_CONFIG, lang);
	}

}
