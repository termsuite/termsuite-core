package fr.univnantes.termsuite.uima;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.ExternalResourceFactory;
import org.apache.uima.resource.ExternalResourceDescription;

import com.google.common.base.Preconditions;

import fr.free.rocheteau.jerome.engines.Stemmer;
import fr.univnantes.lina.uima.ChineseSegmenterFactory;
import fr.univnantes.lina.uima.engines.TreeTaggerWrapper;
import fr.univnantes.lina.uima.models.TreeTaggerParameter;
import fr.univnantes.lina.uima.tkregex.ae.RegexListResource;
import fr.univnantes.lina.uima.tkregex.ae.TokenRegexAE;
import fr.univnantes.termsuite.api.ResourceConfig;
import fr.univnantes.termsuite.api.TermSuiteException;
import fr.univnantes.termsuite.framework.service.TermSuiteResourceManager;
import fr.univnantes.termsuite.model.Lang;
import fr.univnantes.termsuite.model.Tagger;
import fr.univnantes.termsuite.types.FixedExpression;
import fr.univnantes.termsuite.uima.engines.preproc.CasStatCounter;
import fr.univnantes.termsuite.uima.engines.preproc.FixedExpressionSpotter;
import fr.univnantes.termsuite.uima.engines.preproc.MateLemmaFixer;
import fr.univnantes.termsuite.uima.engines.preproc.MateLemmatizerTagger;
import fr.univnantes.termsuite.uima.engines.preproc.RegexSpotter;
import fr.univnantes.termsuite.uima.engines.preproc.StringRegexFilter;
import fr.univnantes.termsuite.uima.engines.preproc.TreeTaggerLemmaFixer;
import fr.univnantes.termsuite.uima.resources.preproc.CharacterFootprintTermFilter;
import fr.univnantes.termsuite.uima.resources.preproc.FixedExpressionResource;
import fr.univnantes.termsuite.uima.resources.preproc.MateLemmatizerModel;
import fr.univnantes.termsuite.uima.resources.preproc.MateTaggerModel;
import uima.sandbox.filter.resources.DefaultFilterResource;
import uima.sandbox.lexer.engines.Lexer;
import uima.sandbox.lexer.resources.SegmentBank;
import uima.sandbox.lexer.resources.SegmentBankResource;
import uima.sandbox.mapper.engines.Mapper;
import uima.sandbox.mapper.resources.Mapping;
import uima.sandbox.mapper.resources.MappingResource;


/**
 * 
 * A factory for TermSuite UIMA Analysis Engines.
 * 
 * @author Damien Cram
 *
 */
public class CustomResourceTermSuiteAEFactory {


	public static AnalysisEngineDescription createStemmerAEDesc(ResourceConfig resourceConfig, Lang lang) {
		try {
			AnalysisEngineDescription ae = AnalysisEngineFactory.createEngineDescription(
				Stemmer.class,
				Stemmer.PARAM_FEATURE, "fr.univnantes.termsuite.types.WordAnnotation:stem",
				Stemmer.PARAM_LANGUAGE, lang,
				Stemmer.PARAM_UPDATE, true
			);
			return ae;
		} catch (Exception e) {
			throw new TermSuiteResourceException(e);
		}
	}

	public static AnalysisEngineDescription createNormalizerAEDesc(ResourceConfig resourceConfig, Lang lang, Tagger tagger) {
		AnalysisEngineDescription ae;
		try {
			ae = AnalysisEngineFactory.createEngineDescription(
					Lexer.class, 
					Lexer.PARAM_TYPE, "fr.univnantes.termsuite.types.WordAnnotation"
				);
		
			ExternalResourceDescription	segmentBank = ExternalResourceFactory.createExternalResourceDescription(
					SegmentBankResource.class,
					getResourceURL(resourceConfig, ResourceType.SEGMENT_BANK, lang)
				);
					
			ExternalResourceFactory.bindResource(
					ae, 
					SegmentBank.KEY_SEGMENT_BANK, 
					segmentBank);
			return ae;	
		} catch (Exception e) {
			throw new TermSuiteException(e);
		}
	}

	public static AnalysisEngineDescription createWordTokenizerAEDesc(ResourceConfig resourceConfig, Lang lang) {
		AnalysisEngineDescription ae;
		try {
			ae = AnalysisEngineFactory.createEngineDescription(
					Lexer.class, 
					Lexer.PARAM_TYPE, "fr.univnantes.termsuite.types.WordAnnotation"
				);
		
			ExternalResourceDescription	segmentBank = ExternalResourceFactory.createExternalResourceDescription(
					SegmentBankResource.class,
					getResourceURL(resourceConfig, ResourceType.SEGMENT_BANK, lang)
				);
					
			ExternalResourceFactory.bindResource(
					ae, 
					SegmentBank.KEY_SEGMENT_BANK, 
					segmentBank);
			return ae;	
		} catch (Exception e) {
			throw new TermSuiteException(e);
		}
	}

	public static URL getResourceURL(ResourceConfig resourceConfig, ResourceType resourceType, Lang lang, Tagger tagger) {
		for(URL urlPrefix:resourceConfig.getURLPrefixes()) {
			URL candidateURL = resourceType.fromUrlPrefix(urlPrefix, lang, tagger);
			if(TermSuiteResourceManager.resourceExists(resourceType, urlPrefix, candidateURL))
				return candidateURL;
		}
		return resourceType.fromClasspath(lang, tagger);

	}

	public static URL getResourceURL(ResourceConfig resourceConfig, ResourceType resourceType, Lang lang) {
		return getResourceURL(resourceConfig, resourceType, lang, null);
	}

	public static AnalysisEngineDescription createMateAEDesc(ResourceConfig resourceConfig, Lang lang, Path mateModelPath) {
		try {
			AnalysisEngineDescription mateTaggerAE = AnalysisEngineFactory.createEngineDescription(
				MateLemmatizerTagger.class
			);
			
			String lemmatizerModel = mateModelPath.resolve("mate-lemma-"+lang.getCode()+".model").toString();
			String taggerModel = mateModelPath.resolve("mate-pos-"+lang.getCode()+".model").toString();
			Preconditions.checkArgument(Files.exists(Paths.get(lemmatizerModel)), "Lemmatizer model does not exist: %s", lemmatizerModel);
			Preconditions.checkArgument(Files.exists(Paths.get(taggerModel)), "Tagger model does not exist: %s", taggerModel);
	
			ExternalResourceFactory.createDependencyAndBind(
					mateTaggerAE,
					MateLemmatizerTagger.LEMMATIZER, 
					MateLemmatizerModel.class, 
					lemmatizerModel);
			ExternalResourceFactory.createDependencyAndBind(
					mateTaggerAE,
					MateLemmatizerTagger.TAGGER, 
					MateTaggerModel.class, 
					taggerModel);
			
			AnalysisEngineDescription lemmaFixerAE = AnalysisEngineFactory.createEngineDescription(
					MateLemmaFixer.class,
					MateLemmaFixer.LANGUAGE, lang.getCode()
				);

			AnalysisEngineDescription normalizerAE = createNormalizerAE(resourceConfig, lang, Tagger.MATE);

			return AnalysisEngineFactory.createEngineDescription(
					mateTaggerAE,
					lemmaFixerAE, 
					normalizerAE);

		} catch (Exception e) {
			throw new TermSuiteException(e);
		}
	}
	
	public static AnalysisEngineDescription createDocumentLoggerAEDesc(long nbDocument, long corpusSize) {
		try {
			AnalysisEngineDescription ae = AnalysisEngineFactory.createEngineDescription(
					DocumentLogger.class,
					DocumentLogger.NB_DOCUMENTS, nbDocument,
					DocumentLogger.CORPUS_SIZE, corpusSize
				);

			return ae;
		} catch(Exception e) {
			throw new TermSuiteException(e);
		}

	}
	
	public static AnalysisEngineDescription createURLFilterAEDesc() {
		try {
			return AnalysisEngineFactory.createEngineDescription(
				StringRegexFilter.class
				);
		} catch(Exception e) {
			throw new TermSuiteException(e);
		}
	}

	/**
	 * Spots fixed expressions in the CAS an creates {@link FixedExpression}
	 * annotation whenever one is found.
	 * 
	 * @return
	 */
	public static AnalysisEngineDescription createFixedExpressionSpotterAEDesc(ResourceConfig resourceConfig, Lang lang)  {
		try {
			AnalysisEngineDescription ae = AnalysisEngineFactory.createEngineDescription(
					FixedExpressionSpotter.class,
					FixedExpressionSpotter.FIXED_EXPRESSION_MAX_SIZE, 5,
					FixedExpressionSpotter.REMOVE_WORD_ANNOTATIONS_FROM_CAS, false,
					FixedExpressionSpotter.REMOVE_TERM_OCC_ANNOTATIONS_FROM_CAS, true
				);
			
			ExternalResourceDescription fixedExprRes = ExternalResourceFactory.createExternalResourceDescription(
					FixedExpressionResource.class, 
					getResourceURL(resourceConfig, ResourceType.FIXED_EXPRESSIONS, lang));
			
			ExternalResourceFactory.bindResource(
					ae,
					FixedExpressionResource.FIXED_EXPRESSION_RESOURCE, 
					fixedExprRes
				);
			
			return ae;
		} catch (Exception e) {
			throw new PreparationPipelineException(e);
		}
	}

	public static AnalysisEngineDescription createCasStatCounterAEDesc(String statName)  {
		try {
			return AnalysisEngineFactory.createEngineDescription(
					CasStatCounter.class,
					CasStatCounter.STAT_NAME, statName
				);
		} catch(Exception e) {
			throw new PreparationPipelineException(e);
		}
	}

	public static AnalysisEngineDescription createTreeTaggerAEDesc(ResourceConfig resourceConfig, Lang lang, Path treeTaggerPath) {
		try {
			AnalysisEngineDescription treeTaggerAE = AnalysisEngineFactory.createEngineDescription(
					TreeTaggerWrapper.class, 
					TreeTaggerWrapper.PARAM_ANNOTATION_TYPE, "fr.univnantes.termsuite.types.WordAnnotation",
					TreeTaggerWrapper.PARAM_TAG_FEATURE, "tag",
					TreeTaggerWrapper.PARAM_LEMMA_FEATURE, "lemma",
					TreeTaggerWrapper.PARAM_UPDATE_ANNOTATION_FEATURES, true,
					TreeTaggerWrapper.PARAM_TT_HOME_DIRECTORY, treeTaggerPath.toString()
				);
			
			ExternalResourceDescription ttParam = ExternalResourceFactory.createExternalResourceDescription(
					TreeTaggerParameter.class,
					getResourceURL(resourceConfig, ResourceType.TREETAGGER_CONFIG, lang, Tagger.TREE_TAGGER)
				);
			
			ExternalResourceFactory.bindResource(
					treeTaggerAE,
					TreeTaggerParameter.KEY_TT_PARAMETER, 
					ttParam 
				);
			
			AnalysisEngineDescription lemmaFixerAE = AnalysisEngineFactory.createEngineDescription(
					TreeTaggerLemmaFixer.class,
					TreeTaggerLemmaFixer.LANGUAGE, lang.getCode()
				);

			
			AnalysisEngineDescription normalizerAE = createNormalizerAE(resourceConfig, lang, Tagger.TREE_TAGGER);
			
			return AnalysisEngineFactory.createEngineDescription(
					treeTaggerAE,
					lemmaFixerAE, 
					normalizerAE);
		} catch (Exception e) {
			throw new TermSuiteException(e);
		}
	}

	private static AnalysisEngineDescription createSubNormalizerAEDesc(String target, URL mappingFile)  {
		try {
			AnalysisEngineDescription ae = AnalysisEngineFactory.createEngineDescription(
					Mapper.class, 
					Mapper.PARAM_SOURCE, "fr.univnantes.termsuite.types.WordAnnotation:tag",
					Mapper.PARAM_TARGET, target,
					Mapper.PARAM_UPDATE, true
				);
			
			ExternalResourceDescription mappingRes = ExternalResourceFactory.createExternalResourceDescription(
					MappingResource.class,
					mappingFile
				);
			
			ExternalResourceFactory.bindResource(
					ae,
					Mapping.KEY_MAPPING, 
					mappingRes 
				);

			return ae;
		} catch (Exception e) {
			throw new PreparationPipelineException(e);
		}
	}

	private static AnalysisEngineDescription createNormalizerAE(ResourceConfig resourceConfig, Lang lang, Tagger tagger) {
		try {
			return AnalysisEngineFactory.createEngineDescription(
					createSubNormalizerAEDesc(
							"fr.univnantes.termsuite.types.WordAnnotation:case", 
							getResourceURL(resourceConfig, ResourceType.TAGGER_CASE_MAPPING, lang, tagger)),
					createSubNormalizerAEDesc(
							"fr.univnantes.termsuite.types.WordAnnotation:category", 
							getResourceURL(resourceConfig, ResourceType.TAGGER_CATEGORY_MAPPING, lang, tagger)),
					createSubNormalizerAEDesc(
							"fr.univnantes.termsuite.types.WordAnnotation:tense", 
							getResourceURL(resourceConfig, ResourceType.TAGGER_TENSE_MAPPING, lang, tagger)),
					createSubNormalizerAEDesc(
							"fr.univnantes.termsuite.types.WordAnnotation:subCategory", 
							getResourceURL(resourceConfig, ResourceType.TAGGER_SUBCATEGORY_MAPPING, lang, tagger)),
					createSubNormalizerAEDesc(
							"fr.univnantes.termsuite.types.WordAnnotation:mood", 
							getResourceURL(resourceConfig, ResourceType.TAGGER_MOOD_MAPPING, lang, tagger)),
					createSubNormalizerAEDesc(
							"fr.univnantes.termsuite.types.WordAnnotation:number", 
							getResourceURL(resourceConfig, ResourceType.TAGGER_NUMBER_MAPPING, lang, tagger)),
					createSubNormalizerAEDesc(
							"fr.univnantes.termsuite.types.WordAnnotation:gender", 
							getResourceURL(resourceConfig, ResourceType.TAGGER_GENDER_MAPPING, lang, tagger))
				);
		} catch (Exception e) {
			throw new PreparationPipelineException(e);
		}

	}

	public static AnalysisEngineDescription createRegexSpotterAEDesc(ResourceConfig resourceConfig, Lang lang) {
		try {
			AnalysisEngineDescription ae = AnalysisEngineFactory.createEngineDescription(
					RegexSpotter.class,
					TokenRegexAE.PARAM_ALLOW_OVERLAPPING_OCCURRENCES, true
				);
			
			
			addParameters(
					ae, 
					RegexSpotter.LOG_OVERLAPPING_RULES, false);
			
			
			ExternalResourceDescription mwtRules = ExternalResourceFactory.createExternalResourceDescription(
					RegexListResource.class, 
					getResourceURL(resourceConfig, ResourceType.MWT_RULES, lang));
			
			ExternalResourceFactory.bindResource(
					ae,
					RegexListResource.KEY_TOKEN_REGEX_RULES, 
					mwtRules
				);

			if(lang != Lang.ZH) {
				ExternalResourceDescription allowedCharsRes = ExternalResourceFactory.createExternalResourceDescription(
						CharacterFootprintTermFilter.class, 
						getResourceURL(resourceConfig, ResourceType.ALLOWED_CHARS, lang));
				
				ExternalResourceFactory.bindResource(
						ae,
						RegexSpotter.CHARACTER_FOOTPRINT_TERM_FILTER, 
						allowedCharsRes
						);
			}

			ExternalResourceDescription stopWordsRes = ExternalResourceFactory.createExternalResourceDescription(
					DefaultFilterResource.class, 
					getResourceURL(resourceConfig, ResourceType.STOP_WORDS_FILTER, lang));
			
			ExternalResourceFactory.bindResource(
					ae,
					RegexSpotter.STOP_WORD_FILTER, 
					stopWordsRes
				);
			return ae;
		} catch(Exception e) {
			throw new TermSuiteException(e);
		}
	}
		
	private static void addParameters(AnalysisEngineDescription ae, Object... parameters) {
		if(parameters.length % 2 == 1)
			throw new IllegalArgumentException("Expecting even number of arguements for key-value pairs: " + parameters.length);
		for(int i=0; i<parameters.length; i+=2) 
			ae.getMetaData().getConfigurationParameterSettings().setParameterValue((String)parameters[i], parameters[i+1]);
	}

}
