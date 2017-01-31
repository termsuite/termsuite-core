/*******************************************************************************
 * Copyright 2015-2016 - CNRS (Centre National de Recherche Scientifique)
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *******************************************************************************/
package fr.univnantes.termsuite.framework;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.fit.factory.AggregateBuilder;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.ExternalResourceFactory;
import org.apache.uima.resource.ExternalResourceDescription;
import org.apache.uima.resource.ResourceInitializationException;

import com.google.common.base.Preconditions;

import fr.free.rocheteau.jerome.engines.Stemmer;
import fr.univnantes.lina.uima.ChineseSegmenterResourceHelper;
import fr.univnantes.lina.uima.engines.ChineseSegmenter;
import fr.univnantes.lina.uima.engines.TreeTaggerWrapper;
import fr.univnantes.lina.uima.models.ChineseSegmentResource;
import fr.univnantes.lina.uima.models.TreeTaggerParameter;
import fr.univnantes.lina.uima.tkregex.ae.RegexListResource;
import fr.univnantes.lina.uima.tkregex.ae.TokenRegexAE;
import fr.univnantes.termsuite.api.ResourceConfig;
import fr.univnantes.termsuite.framework.service.TermSuiteResourceManager;
import fr.univnantes.termsuite.model.Lang;
import fr.univnantes.termsuite.model.Tagger;
import fr.univnantes.termsuite.types.FixedExpression;
import fr.univnantes.termsuite.uima.DocumentLogger;
import fr.univnantes.termsuite.uima.PipelineClosingAE;
import fr.univnantes.termsuite.uima.PipelineListener;
import fr.univnantes.termsuite.uima.PipelineListenerAE;
import fr.univnantes.termsuite.uima.PipelineResourceMgrs;
import fr.univnantes.termsuite.uima.PreparationPipelineException;
import fr.univnantes.termsuite.uima.PreparationPipelineOptions;
import fr.univnantes.termsuite.uima.ResourceType;
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
import fr.univnantes.termsuite.utils.TermHistory;
import uima.sandbox.filter.resources.DefaultFilterResource;
import uima.sandbox.lexer.engines.Lexer;
import uima.sandbox.lexer.resources.SegmentBank;
import uima.sandbox.lexer.resources.SegmentBankResource;
import uima.sandbox.mapper.engines.Mapper;
import uima.sandbox.mapper.resources.Mapping;
import uima.sandbox.mapper.resources.MappingResource;

/**
 * A builder and launcher for preparation pipeline.
 *  
 * @author Damien Cram
 *
 */
public class PreprocessingPipelineBuilder {
	private final String pipelineId = UUID.randomUUID().toString();

	/* The Logger */
	private AggregateBuilder aggregateBuilder;
	
	/* ******************************
	 * MAIN PIPELINE PARAMETERS
	 */
	private Lang lang;
	private PreparationPipelineOptions options = new PreparationPipelineOptions();
	private Optional<Long> nbDocuments = Optional.empty();
	private Optional<Long> corpusSize = Optional.empty();
	private List<AnalysisEngineDescription> customAEs = new ArrayList<>();
	private Path taggerPath;
	private List<PipelineListener> userPipelineListeners = new ArrayList<>();
	private ResourceConfig resourceConfig = new ResourceConfig();

	
	/* *******************
	 * CONSTRUCTORS
	 */
	private PreprocessingPipelineBuilder(Lang lang, Path taggerPath) {
		this.aggregateBuilder = new AggregateBuilder();
		this.lang = lang;
		this.taggerPath = taggerPath;
	}


	/**
	 * 
	 * Starts a chaining {@link PreprocessingPipelineBuilder} builder. 
	 * 
	 * @param lang
	 * 			The 
	 * @return
	 * 			The chaining builder.
	 * 
	 */
	public static PreprocessingPipelineBuilder create(Lang lang, Path taggerPath) {
		return new PreprocessingPipelineBuilder(lang, taggerPath);
	}
	

	/**
	 * Registers a pipeline listener.
	 * 
	 * @param pipelineListener
	 * @return
	 * 		This chaining {@link PreprocessingPipelineBuilder} builder object
	 */
	public PreprocessingPipelineBuilder addPipelineListener(PipelineListener pipelineListener) {
		this.userPipelineListeners.add(pipelineListener);
		return this;
	}
	
	public void setOptions(PreparationPipelineOptions options) {
		this.options = options;
	}
	
	public void terminates() {
		PipelineResourceMgrs.clearPipeline(pipelineId);
	}

	public AnalysisEngineDescription create() {
		if(options.isDocumentLoggingEnabled())
			aeDocumentLogger(nbDocuments.orElse(0l), corpusSize.orElse(0l));
		
		if(lang == Lang.ZH)
			aeChineseTokenizer();
		else
			aeWordTokenizer();
		
		switch (options.getTagger()) {
		case TREE_TAGGER:
			aeTreeTagger(taggerPath.toString());
			break;
		case MATE:
			aeMateTaggerLemmatizer(taggerPath.toString());
		default:
			throw new UnsupportedOperationException("Unknown tagger: " + options.getTagger());
		}
		
		aeUrlFilter();
		
		aeStemmer();
		
		aeRegexSpotter();
		
		if(options.isFixedExpressionEnabled()) 
			aeFixedExpressionSpotter();
		
		for(AnalysisEngineDescription ae:customAEs)
			aggregateAndReturn(ae);
		
		
		haeCasStatCounter("At end of preparation");
		
		aePipelineListener();

//		aePipelineClosingAE();
		
		try {
			return this.aggregateBuilder.createAggregateDescription();
		} catch (ResourceInitializationException e) {
			throw new PreparationPipelineException(e);
		}
		
	}
	

	public PreprocessingPipelineBuilder setHistory(TermHistory history) {
		PipelineResourceMgrs.getResourceMgr(pipelineId).register(TermHistory.class, history);
		return this;
	}

	private PreprocessingPipelineBuilder aeWordTokenizer() {
		try {
			AnalysisEngineDescription ae = AnalysisEngineFactory.createEngineDescription(
					Lexer.class, 
					Lexer.PARAM_TYPE, "fr.univnantes.termsuite.types.WordAnnotation"
				);
			
			ExternalResourceDescription	segmentBank = ExternalResourceFactory.createExternalResourceDescription(
					SegmentBankResource.class,
					getResourceURL(ResourceType.SEGMENT_BANK)
				);
			

					
			ExternalResourceFactory.bindResource(
					ae, 
					SegmentBank.KEY_SEGMENT_BANK, 
					segmentBank);

			return aggregateAndReturn(ae);
		} catch (Exception e) {
			throw new PreparationPipelineException(e);
		}
		
	}

	private PreprocessingPipelineBuilder aggregateAndReturn(AnalysisEngineDescription ae) {
		this.aggregateBuilder.add(ae);
		return this;
	}

	private PreprocessingPipelineBuilder aeTreeTagger(String taggerPath) {
		try {
			AnalysisEngineDescription ae = AnalysisEngineFactory.createEngineDescription(
					TreeTaggerWrapper.class, 
					TreeTaggerWrapper.PARAM_ANNOTATION_TYPE, "fr.univnantes.termsuite.types.WordAnnotation",
					TreeTaggerWrapper.PARAM_TAG_FEATURE, "tag",
					TreeTaggerWrapper.PARAM_LEMMA_FEATURE, "lemma",
					TreeTaggerWrapper.PARAM_UPDATE_ANNOTATION_FEATURES, true,
					TreeTaggerWrapper.PARAM_TT_HOME_DIRECTORY, taggerPath
				);
			
			ExternalResourceDescription ttParam = ExternalResourceFactory.createExternalResourceDescription(
					TreeTaggerParameter.class,
					getResourceURL(ResourceType.TREETAGGER_CONFIG, Tagger.TREE_TAGGER)
				);
			
			ExternalResourceFactory.bindResource(
					ae,
					TreeTaggerParameter.KEY_TT_PARAMETER, 
					ttParam 
				);

			return aggregateAndReturn(ae).ttLemmaFixer().ttNormalizer();
		} catch (Exception e) {
			throw new PreparationPipelineException(e);
		}
	}




	
	public PreprocessingPipelineBuilder setResourceConfig(ResourceConfig resourceConfig) {
		this.resourceConfig = resourceConfig;
		return this;
	}
	
	public URL getResourceURL(ResourceType resourceType) {
		return getResourceURL(resourceType, null);
	}
	
	public URL getResourceURL(ResourceType resourceType, Tagger tagger) {
		for(URL urlPrefix:resourceConfig.getURLPrefixes()) {
			URL candidateURL = resourceType.fromUrlPrefix(urlPrefix, lang, tagger);
			if(TermSuiteResourceManager.resourceExists(resourceType, urlPrefix, candidateURL))
				return candidateURL;
		}
		return resourceType.fromClasspath(lang, tagger);
	}


	public PreprocessingPipelineBuilder setMateModelPath(String path) {
		Preconditions.checkArgument(Files.exists(Paths.get(path)), "Directory %s does not exist", path);
		Preconditions.checkArgument(Files.isDirectory(Paths.get(path)), "File %s is not a directory", path);
		return this;
	}

	
	private PreprocessingPipelineBuilder aeMateTaggerLemmatizer(String mateModelPath)  {
		try {
			AnalysisEngineDescription ae = AnalysisEngineFactory.createEngineDescription(
					MateLemmatizerTagger.class
				);
			
			String lemmatizerModel = Paths.get(mateModelPath, "mate-lemma-"+lang.getCode()+".model").toString();
			String taggerModel = Paths.get(mateModelPath, "mate-pos-"+lang.getCode()+".model").toString();
			Preconditions.checkArgument(Files.exists(Paths.get(lemmatizerModel)), "Lemmatizer model does not exist: %s", lemmatizerModel);
			Preconditions.checkArgument(Files.exists(Paths.get(taggerModel)), "Tagger model does not exist: %s", taggerModel);
	
			ExternalResourceFactory.createDependencyAndBind(
					ae,
					MateLemmatizerTagger.LEMMATIZER, 
					MateLemmatizerModel.class, 
					lemmatizerModel);
			ExternalResourceFactory.createDependencyAndBind(
					ae,
					MateLemmatizerTagger.TAGGER, 
					MateTaggerModel.class, 
					taggerModel);
	
			return aggregateAndReturn(ae)
					.mateLemmaFixer()
					.mateNormalizer();
		} catch (Exception e) {
			throw new PreparationPipelineException(e);
		}
	}
	
	private void addParameters(AnalysisEngineDescription ae, Object... parameters) {
		if(parameters.length % 2 == 1)
			throw new IllegalArgumentException("Expecting even number of arguements for key-value pairs: " + parameters.length);
		for(int i=0; i<parameters.length; i+=2) 
			ae.getMetaData().getConfigurationParameterSettings().setParameterValue((String)parameters[i], parameters[i+1]);
	}

	private PreprocessingPipelineBuilder subNormalizer(String target, URL mappingFile)  {
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

			return aggregateAndReturn(ae);
		} catch (Exception e) {
			throw new PreparationPipelineException(e);
		}
	}

	private PreprocessingPipelineBuilder caseNormalizer(Tagger tagger)  {
		return subNormalizer(
				"fr.univnantes.termsuite.types.WordAnnotation:case", 
				getResourceURL(ResourceType.TAGGER_CASE_MAPPING, tagger));
	}

	private PreprocessingPipelineBuilder categoryNormalizer(Tagger tagger)  {
		return subNormalizer(
				"fr.univnantes.termsuite.types.WordAnnotation:category", 
				getResourceURL(ResourceType.TAGGER_CATEGORY_MAPPING, tagger));
	}

	private PreprocessingPipelineBuilder tenseNormalizer(Tagger tagger)  {
		return subNormalizer(
				"fr.univnantes.termsuite.types.WordAnnotation:tense", 
				getResourceURL(ResourceType.TAGGER_TENSE_MAPPING, tagger));
	}

	private PreprocessingPipelineBuilder subCategoryNormalizer(Tagger tagger)  {
		return subNormalizer(
				"fr.univnantes.termsuite.types.WordAnnotation:subCategory", 
				getResourceURL(ResourceType.TAGGER_SUBCATEGORY_MAPPING, tagger));
	}

	
	private PreprocessingPipelineBuilder moodNormalizer(Tagger tagger)  {
		return subNormalizer(
				"fr.univnantes.termsuite.types.WordAnnotation:mood", 
				getResourceURL(ResourceType.TAGGER_MOOD_MAPPING, tagger));
	}

	
	private PreprocessingPipelineBuilder numberNormalizer(Tagger tagger)  {
		return subNormalizer(
				"fr.univnantes.termsuite.types.WordAnnotation:number", 
				getResourceURL(ResourceType.TAGGER_NUMBER_MAPPING, tagger));
	}

	
	private PreprocessingPipelineBuilder genderNormalizer(Tagger tagger)  {
		return subNormalizer(
				"fr.univnantes.termsuite.types.WordAnnotation:gender", 
				getResourceURL(ResourceType.TAGGER_GENDER_MAPPING, tagger));
	}

	private PreprocessingPipelineBuilder mateNormalizer()  {
		return normalizer(Tagger.MATE);
	}

	private PreprocessingPipelineBuilder ttNormalizer()  {
		return normalizer(Tagger.TREE_TAGGER);
	}

	private PreprocessingPipelineBuilder normalizer(Tagger tagger)  {
		categoryNormalizer(tagger);
		subCategoryNormalizer(tagger);
		moodNormalizer(tagger);
		tenseNormalizer(tagger);
		genderNormalizer(tagger);
		numberNormalizer(tagger);
		return caseNormalizer(tagger);
	}
	
	private PreprocessingPipelineBuilder aeStemmer()  {
		try {
			AnalysisEngineDescription ae = AnalysisEngineFactory.createEngineDescription(
					Stemmer.class,
					Stemmer.PARAM_FEATURE, "fr.univnantes.termsuite.types.WordAnnotation:stem",
					Stemmer.PARAM_LANGUAGE, lang,
					Stemmer.PARAM_UPDATE, true
				);

			return aggregateAndReturn(ae);
		} catch (Exception e) {
			throw new PreparationPipelineException(e);
		}
	}
	

	private PreprocessingPipelineBuilder ttLemmaFixer()  {
		try {
			AnalysisEngineDescription ae = AnalysisEngineFactory.createEngineDescription(
					TreeTaggerLemmaFixer.class,
					TreeTaggerLemmaFixer.LANGUAGE, lang.getCode()
				);
			

			return aggregateAndReturn(ae);
		} catch (Exception e) {
			throw new PreparationPipelineException(e);
		}
	}
	
	private PreprocessingPipelineBuilder mateLemmaFixer()  {
		try {
			AnalysisEngineDescription ae = AnalysisEngineFactory.createEngineDescription(
					MateLemmaFixer.class,
					MateLemmaFixer.LANGUAGE, lang.getCode()
				);

			return aggregateAndReturn(ae);
		} catch (Exception e) {
			throw new PreparationPipelineException(e);
		}
	}

	private PipelineListener rootPipelineListener = new PipelineListener() {
		@Override
		public void statusUpdated(double progress, String msg) {
			for(PipelineListener l:userPipelineListeners)
				l.statusUpdated(progress, msg);
		}
	};
	
	private PreprocessingPipelineBuilder aePipelineListener()  {
		try {
			PipelineResourceMgrs.getResourceMgr(pipelineId).register(PipelineListener.class, rootPipelineListener);
			AnalysisEngineDescription ae = AnalysisEngineFactory.createEngineDescription(
					PipelineListenerAE.class,
					PipelineListenerAE.PIPELINE_ID, pipelineId
				);

			return aggregateAndReturn(ae);
		} catch (Exception e) {
			throw new PreparationPipelineException(e);
		}
	}

	private PreprocessingPipelineBuilder aePipelineClosingAE()  {
		try {
			PipelineResourceMgrs.getResourceMgr(pipelineId).register(PipelineListener.class, rootPipelineListener);
			AnalysisEngineDescription ae = AnalysisEngineFactory.createEngineDescription(
					PipelineClosingAE.class,
					PipelineClosingAE.PIPELINE_ID, pipelineId
				);

			return aggregateAndReturn(ae);
		} catch (Exception e) {
			throw new PreparationPipelineException(e);
		}
	}


	/**
	 * Spots fixed expressions in the CAS an creates {@link FixedExpression}
	 * annotation whenever one is found.
	 * 
	 * @return
	 * 		This chaining {@link PreprocessingPipelineBuilder} builder object
	 */
	private PreprocessingPipelineBuilder aeFixedExpressionSpotter()  {
		try {
			AnalysisEngineDescription ae = AnalysisEngineFactory.createEngineDescription(
					FixedExpressionSpotter.class,
					FixedExpressionSpotter.FIXED_EXPRESSION_MAX_SIZE, 5,
					FixedExpressionSpotter.REMOVE_WORD_ANNOTATIONS_FROM_CAS, false,
					FixedExpressionSpotter.REMOVE_TERM_OCC_ANNOTATIONS_FROM_CAS, true
				);
			
			ExternalResourceDescription fixedExprRes = ExternalResourceFactory.createExternalResourceDescription(
					FixedExpressionResource.class, 
					getResourceURL(ResourceType.FIXED_EXPRESSIONS));
			
			ExternalResourceFactory.bindResource(
					ae,
					FixedExpressionResource.FIXED_EXPRESSION_RESOURCE, 
					fixedExprRes
				);
			
			return aggregateAndReturn(ae);
		} catch (Exception e) {
			throw new PreparationPipelineException(e);
		}
	}
	
	/**
	 * The single-word and multi-word term spotter AE
	 * base on UIMA Tokens Regex.
	 * 
	 * @return
	 * 		This chaining {@link PreprocessingPipelineBuilder} builder object
	 */
	private PreprocessingPipelineBuilder aeRegexSpotter()  {
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
					getResourceURL(ResourceType.MWT_RULES));
			
			ExternalResourceFactory.bindResource(
					ae,
					RegexListResource.KEY_TOKEN_REGEX_RULES, 
					mwtRules
				);

			ExternalResourceDescription allowedCharsRes = ExternalResourceFactory.createExternalResourceDescription(
					CharacterFootprintTermFilter.class, 
					getResourceURL(ResourceType.ALLOWED_CHARS));
			
			ExternalResourceFactory.bindResource(
					ae,
					RegexSpotter.CHARACTER_FOOTPRINT_TERM_FILTER, 
					allowedCharsRes
				);

			ExternalResourceDescription stopWordsRes = ExternalResourceFactory.createExternalResourceDescription(
					DefaultFilterResource.class, 
					getResourceURL(ResourceType.STOP_WORDS_FILTER));
			
			ExternalResourceFactory.bindResource(
					ae,
					RegexSpotter.STOP_WORD_FILTER, 
					stopWordsRes
				);

			PreprocessingPipelineBuilder aggAE = aggregateAndReturn(ae);
			return aggAE;
		} catch (Exception e) {
			throw new PreparationPipelineException(e);
		}
	}
	
	

	private PreprocessingPipelineBuilder aeDocumentLogger(long nbDocument, long corpusSize)  {
		
		try {
			AnalysisEngineDescription ae = AnalysisEngineFactory.createEngineDescription(
					DocumentLogger.class,
					DocumentLogger.NB_DOCUMENTS, nbDocument,
					DocumentLogger.CORPUS_SIZE, corpusSize
				);

			return aggregateAndReturn(ae);
		} catch(Exception e) {
			throw new PreparationPipelineException(e);
		}
	}

	/**
	 * Tokenizer for chinese collections.
	 * @see ChineseSegmenter
	 * 
	 * @return
	 * 		This chaining {@link PreprocessingPipelineBuilder} builder object
	 */
	private PreprocessingPipelineBuilder aeChineseTokenizer()  {
		try {
			AnalysisEngineDescription ae = AnalysisEngineFactory.createEngineDescription(
					ChineseSegmenter.class,
					ChineseSegmenter.ANNOTATION_TYPE, "fr.univnantes.termsuite.types.WordAnnotation"
				);
			ExternalResourceFactory.createDependencyAndBind(
					ae,
					ChineseSegmenter.CHINESE_WORD_SEGMENTS, 
					ChineseSegmentResource.class, 
					ChineseSegmenterResourceHelper.getChineseWordSegments());
			ExternalResourceFactory.createDependencyAndBind(
					ae,
					ChineseSegmenter.CHINESE_FOREIGN_NAME_SEGMENTS, 
					ChineseSegmentResource.class, 
					ChineseSegmenterResourceHelper.getForeignNameSegments());
			ExternalResourceFactory.createDependencyAndBind(
					ae,
					ChineseSegmenter.CHINESE_NUMBER_SEGMENTS, 
					ChineseSegmentResource.class, 
					ChineseSegmenterResourceHelper.getNumberSegments());

			return aggregateAndReturn(ae);
		} catch(Exception e) {
			throw new PreparationPipelineException(e);
		}
	}

	/**
	 * Filters out URLs from CAS.
	 * 
	 * @return
	 * 		This chaining {@link PreprocessingPipelineBuilder} builder object
	 */
	private PreprocessingPipelineBuilder aeUrlFilter()   {
		try {
			AnalysisEngineDescription ae = AnalysisEngineFactory.createEngineDescription(
					StringRegexFilter.class
				);

			return aggregateAndReturn(ae);
		} catch(Exception e) {
			throw new PreparationPipelineException(e);
		}
	}
	
	private PreprocessingPipelineBuilder haeCasStatCounter(String statName)  {
		try {
			AnalysisEngineDescription ae = AnalysisEngineFactory.createEngineDescription(
					CasStatCounter.class,
					CasStatCounter.STAT_NAME, statName
				);

			return aggregateAndReturn(ae);
		} catch(Exception e) {
			throw new PreparationPipelineException(e);
		}
	}
	
	/**
	 * 
	 * Aggregates an AE to the TS pipeline.
	 * 
	 * @param ae
	 * 			the ae description of the added pipeline.
	 * @return
	 * 		This chaining {@link PreprocessingPipelineBuilder} builder object
	 * 			
	 */
	public PreprocessingPipelineBuilder addCustomAE(AnalysisEngineDescription ae) {
		customAEs.add(ae);
		return this;
	}
	
	public PreprocessingPipelineBuilder setNbDocuments(long nbDocuments) {
		this.nbDocuments = Optional.of(nbDocuments);
		return this;
	}


	public PreprocessingPipelineBuilder setCorpusSize(long corpusSize) {
		this.corpusSize = Optional.of(corpusSize);
		return this;
	}
}