/*******************************************************************************
 * Copyright 2015 - CNRS (Centre National de Recherche Scientifique)
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
package eu.project.ttc.tools;

import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.AggregateBuilder;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.factory.ExternalResourceFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ExternalResourceDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

import eu.project.ttc.engines.AffixCompoundSplitter;
import eu.project.ttc.engines.CasStatCounter;
import eu.project.ttc.engines.CompostAE;
import eu.project.ttc.engines.CompoundSplitter;
import eu.project.ttc.engines.Contextualizer;
import eu.project.ttc.engines.EvalEngine;
import eu.project.ttc.engines.GraphicalVariantGatherer;
import eu.project.ttc.engines.MateLemmaFixer;
import eu.project.ttc.engines.MateLemmatizerTagger;
import eu.project.ttc.engines.PrimaryOccurrenceDetector;
import eu.project.ttc.engines.RegexSpotter;
import eu.project.ttc.engines.StringRegexFilter;
import eu.project.ttc.engines.SyntacticTermGatherer;
import eu.project.ttc.engines.TermClassifier;
import eu.project.ttc.engines.TermIndexBlacklistWordFilterAE;
import eu.project.ttc.engines.TermSpecificityComputer;
import eu.project.ttc.engines.TreeTaggerLemmaFixer;
import eu.project.ttc.engines.cleaner.AbstractTermIndexCleaner;
import eu.project.ttc.engines.cleaner.FilterRules;
import eu.project.ttc.engines.cleaner.TermIndexThresholdCleaner;
import eu.project.ttc.engines.cleaner.TermIndexTopNCleaner;
import eu.project.ttc.engines.cleaner.TermProperty;
import eu.project.ttc.engines.desc.Lang;
import eu.project.ttc.engines.desc.TermSuiteCollection;
import eu.project.ttc.engines.desc.TermSuitePipelineException;
import eu.project.ttc.engines.exporter.EvalExporter;
import eu.project.ttc.engines.exporter.ExportVariationRuleExamples;
import eu.project.ttc.engines.exporter.JsonExporter;
import eu.project.ttc.engines.exporter.SpotterTSVWriter;
import eu.project.ttc.engines.exporter.TBXExporter;
import eu.project.ttc.engines.exporter.TSVExporter;
import eu.project.ttc.engines.exporter.VariantEvalExporter;
import eu.project.ttc.engines.exporter.XmiCasExporter;
import eu.project.ttc.metrics.LogLikelihood;
import eu.project.ttc.models.OccurrenceType;
import eu.project.ttc.models.TermIndex;
import eu.project.ttc.models.index.MemoryTermIndex;
import eu.project.ttc.readers.AbstractToTxtSaxHandler;
import eu.project.ttc.readers.EmptyCollectionReader;
import eu.project.ttc.readers.GenericXMLToTxtCollectionReader;
import eu.project.ttc.readers.StringCollectionReader;
import eu.project.ttc.readers.TeiCollectionReader;
import eu.project.ttc.readers.TxtCollectionReader;
import eu.project.ttc.resources.BankResource;
import eu.project.ttc.resources.CharacterFootprintTermFilter;
import eu.project.ttc.resources.CompostInflectionRules;
import eu.project.ttc.resources.DictionaryResource;
import eu.project.ttc.resources.EvalTrace;
import eu.project.ttc.resources.GeneralLanguageResource;
import eu.project.ttc.resources.MateLemmatizerModel;
import eu.project.ttc.resources.MateTaggerModel;
import eu.project.ttc.resources.MemoryTermIndexManager;
import eu.project.ttc.resources.ReferenceTermList;
import eu.project.ttc.resources.SimpleWordSet;
import eu.project.ttc.resources.TermIndexResource;
import eu.project.ttc.resources.YamlVariantRules;
import eu.project.ttc.types.WordAnnotation;
import eu.project.ttc.utils.OccurrenceBuffer;
import eu.project.ttc.utils.TermSuiteUtils;
import fr.free.rocheteau.jerome.engines.Stemmer;
import fr.univnantes.lina.uima.ChineseSegmenterResourceHelper;
import fr.univnantes.lina.uima.engines.ChineseSegmenter;
import fr.univnantes.lina.uima.engines.TreeTaggerWrapper;
import fr.univnantes.lina.uima.models.ChineseSegmentResource;
import fr.univnantes.lina.uima.models.TreeTaggerParameter;
import fr.univnantes.lina.uima.tkregex.ae.RegexListResource;
import fr.univnantes.lina.uima.tkregex.ae.TokenRegexAE;
import uima.sandbox.filter.resources.DefaultFilterResource;
import uima.sandbox.filter.resources.FilterResource;
import uima.sandbox.lexer.engines.Lexer;
import uima.sandbox.lexer.resources.SegmentBank;
import uima.sandbox.lexer.resources.SegmentBankResource;
import uima.sandbox.mapper.engines.Mapper;
import uima.sandbox.mapper.resources.Mapping;
import uima.sandbox.mapper.resources.MappingResource;

/*
 * TODO Integrates frozen expressions
 * TODO integrate Sonar runner
 * TODO Add functional pipeline TestCases for each collection type and for different pipeline configs
 */


/**
 * A collection reader and ae aggregator (builder pattern) that 
 * creates and runs a full pipeline.
 *  
 * @author Damien Cram
 *
 */
public class TermSuitePipeline {
	private static final Logger LOGGER = LoggerFactory.getLogger(TermSuitePipeline.class);
	private AggregateBuilder aggregateBuilder;
	private TermSuiteResourceHelper resFactory;
	private Optional<String> mateModelsPath = Optional.absent();
	private Optional<String> treeTaggerPath = Optional.absent();
	private Optional<String> syntacticRegexesFilePath = Optional.absent();
	private Optional<String> yamlVariantRulesFilePath = Optional.absent();
	private boolean enableSyntacticLabels = false;
	private boolean exportJsonWithOccurrences = false;
	private boolean exportJsonWithContext = false;
	private Lang lang;
	private CollectionReaderDescription crDescription;
	private float alpha = 0.5f;
	private float beta = 0.1f;
	private float gamma = 0.1f;
	private float delta = 0.3f;
	private float compostScoreThreshold = 0.7f;
	private int compostMinComponentSize = 3;
	private int compostMaxComponentNum = 3;
	private Object compostSegmentSimilarityThreshold = 0.7f;
	private String exportFilteringRule = FilterRules.SpecificityThreshold.name();
	private float exportFilteringThreshold = 0;
	
	private Optional<? extends TermIndex> termIndex = Optional.absent();
	private boolean spotWithOccurrences = true;
	private String contextAssocRateMeasure = LogLikelihood.class.getName();

	/*
	 * Contextualizer options
	 */
	private OccurrenceType contextualizeCoTermsType = OccurrenceType.SINGLE_WORD;
	private boolean contextualizeWithTermClasses = false;
	private int contextualizeWithCoOccurrenceFrequencyThreshhold = 1;
	
	/*
	 * TSVExporter options
	 */
	private String tsvExportProperties = "groupingKey,wr";
	
	
	private Optional<Boolean> logOverlappingRules = Optional.absent();
	private Optional<Float> graphicalVariantSimilarityThreshold = Optional.absent();

	private Optional<String> postProcessingStrategy = Optional.absent();
	
//	private int variantDepth = 3;

	private TermSuitePipeline(String lang) {
		this.lang = Lang.forName(lang);
		this.resFactory = new TermSuiteResourceHelper(this.lang);
		this.aggregateBuilder = new AggregateBuilder();
	}

	public static TermSuitePipeline create(String lang) {
		return new TermSuitePipeline(lang);
	}
	
	public static TermSuitePipeline create(TermIndex termIndex) {
		Preconditions.checkNotNull(termIndex.getName(), "The term index must have a name before it can be used in TermSuitePipeline");
	
		TermSuitePipeline pipeline = create(termIndex.getLang().getCode());
		pipeline.emptyCollection();
		pipeline.setTermIndex(termIndex);
		
		return pipeline;
	}
	
	/**
	 * Runs the pipeline with {@link SimplePipeline} on the {@link CollectionReader} that must have been defined.
	 * 
	 * @throws TermSuitePipelineException if no {@link CollectionReader} has been declared on this pipeline
	 */
	public TermSuitePipeline run() {
		if(crDescription == null)
			throw new TermSuitePipelineException("No collection reader has been declared on this pipeline.");
		else {
			try {
				SimplePipeline.runPipeline(this.crDescription, createDescription());
			} catch (Exception e) {
				throw new TermSuitePipelineException(e);
			}
		}
		return this;
	}
	
	
	
	/**
	 * Runs the pipeline with {@link SimplePipeline} without requiring a {@link CollectionReader}
	 * to be defined.
	 * @param cas the {@link JCas} on which the pipeline operates.
	 */
	public TermSuitePipeline run(JCas cas) {
		try {
			SimplePipeline.runPipeline(cas, createDescription());
			return this;
		} catch (Exception e) {
			throw new TermSuitePipelineException(e);
		}
	}
	
	public TermSuitePipeline setInlineString(String text)  {
		try {
			this.crDescription = CollectionReaderFactory.createReaderDescription(
					StringCollectionReader.class,
					StringCollectionReader.PARAM_TEXT, text,
					StringCollectionReader.PARAM_LANGUAGE, this.lang.getCode()
				);
			return this;
		} catch (Exception e) {
			throw new TermSuitePipelineException(e);
		}
	}
	
	/**
	 * Creates a collection reader for this pipeline.
	 * 
	 * @param termSuiteCollection
	 * @param collectionPath
	 * @param collectionEncoding
	 * @return
	 */
	public TermSuitePipeline setCollection(TermSuiteCollection termSuiteCollection, String collectionPath, String collectionEncoding) {
		Preconditions.checkNotNull(termSuiteCollection);
		Preconditions.checkNotNull(collectionPath);
		Preconditions.checkNotNull(collectionEncoding);
		try {
			switch(termSuiteCollection) {
			case TEI:
				this.crDescription = CollectionReaderFactory.createReaderDescription(
						TeiCollectionReader.class,
						TeiCollectionReader.PARAM_INPUTDIR, collectionPath,
						TxtCollectionReader.PARAM_COLLECTION_TYPE, termSuiteCollection,
						TeiCollectionReader.PARAM_ENCODING, collectionEncoding,
						TeiCollectionReader.PARAM_LANGUAGE, this.lang.getCode()
						);
				break;
			case TXT:
				this.crDescription = CollectionReaderFactory.createReaderDescription(
						TxtCollectionReader.class,
						TxtCollectionReader.PARAM_INPUTDIR, collectionPath,
						TxtCollectionReader.PARAM_COLLECTION_TYPE, termSuiteCollection,
						TxtCollectionReader.PARAM_ENCODING, collectionEncoding,
						TxtCollectionReader.PARAM_LANGUAGE, this.lang.getCode()
						);
				break;
			case EMPTY:
				this.crDescription = CollectionReaderFactory.createReaderDescription(
						EmptyCollectionReader.class
						);
				break;
			default:
				throw new IllegalArgumentException("No such collection: " + termSuiteCollection);
			}
			return this;
		} catch (Exception e) {
			throw new TermSuitePipelineException(e);
		}
	}
	
	/**
	 * Creates a collection reader of type {@link GenericXMLToTxtCollectionReader} for this pipeline.
	 * 
	 * Requires a list of dropped tags and txt tags for collection parsing. 
	 * 
	 * @see AbstractToTxtSaxHandler
	 * 
	 * @param termSuiteCollection
	 * @param collectionPath
	 * @param collectionEncoding
	 * @param droppedTags
	 * @param txtTags
	 * @return
	 */
	public TermSuitePipeline setCollection(TermSuiteCollection termSuiteCollection, String collectionPath, String collectionEncoding, String droppedTags, String txtTags)  {
		try {
			this.crDescription = CollectionReaderFactory.createReaderDescription(
					GenericXMLToTxtCollectionReader.class,
					GenericXMLToTxtCollectionReader.PARAM_COLLECTION_TYPE, termSuiteCollection,
					GenericXMLToTxtCollectionReader.PARAM_DROPPED_TAGS, droppedTags,
					GenericXMLToTxtCollectionReader.PARAM_TXT_TAGS, txtTags,
					GenericXMLToTxtCollectionReader.PARAM_INPUTDIR, collectionPath,
					GenericXMLToTxtCollectionReader.PARAM_ENCODING, collectionEncoding,
					GenericXMLToTxtCollectionReader.PARAM_LANGUAGE, this.lang.getCode()
					);
			return this;
		} catch (Exception e) {
			throw new TermSuitePipelineException(e);
		}
	}
	
	public TermSuitePipeline setResourcePath(String resourcePath) {
		TermSuiteUtils.addToClasspath(resourcePath);
		return this;
	}


	public TermSuitePipeline setContextAssocRateMeasure(String contextAssocRateMeasure) {
		this.contextAssocRateMeasure = contextAssocRateMeasure;
		return this;
	}
	
	public TermSuitePipeline emptyCollection() {
		return setCollection(TermSuiteCollection.EMPTY, "", "UTF-8");
	}

	
	public AnalysisEngineDescription createDescription()  {
		try {
			return this.aggregateBuilder.createAggregateDescription();
		} catch (Exception e) {
			throw new TermSuitePipelineException(e);
		}
	}
	

	public TermSuitePipeline aeWordTokenizer() {
		try {
			AnalysisEngineDescription ae = AnalysisEngineFactory.createEngineDescription(
					Lexer.class, 
					Lexer.PARAM_TYPE, "eu.project.ttc.types.WordAnnotation"
				);
			
			ExternalResourceDescription	segmentBank = ExternalResourceFactory.createExternalResourceDescription(
					SegmentBankResource.class, 
					resFactory.getSegmentBank().toString());

					
			ExternalResourceFactory.bindResource(ae, SegmentBank.KEY_SEGMENT_BANK, segmentBank);


			return aggregateAndReturn(ae);
		} catch (Exception e) {
			throw new TermSuitePipelineException(e);
		}
		
	}

	private TermSuitePipeline aggregateAndReturn(AnalysisEngineDescription ae) {
		this.aggregateBuilder.add(ae);
		return this;
	}

	public TermSuitePipeline aeTreeTagger() {
		try {
			AnalysisEngineDescription ae = AnalysisEngineFactory.createEngineDescription(
					TreeTaggerWrapper.class, 
					TreeTaggerWrapper.PARAM_ANNOTATION_TYPE, "eu.project.ttc.types.WordAnnotation",
					TreeTaggerWrapper.PARAM_TAG_FEATURE, "tag",
					TreeTaggerWrapper.PARAM_LEMMA_FEATURE, "lemma",
					TreeTaggerWrapper.PARAM_UPDATE_ANNOTATION_FEATURES, true,
					TreeTaggerWrapper.PARAM_TT_HOME_DIRECTORY, this.treeTaggerPath.get()
				);
			
			ExternalResourceFactory.createDependencyAndBind(
					ae,
					TreeTaggerParameter.KEY_TT_PARAMETER, 
					TreeTaggerParameter.class, 
					resFactory.getTTParameter().toString());
			return aggregateAndReturn(ae).ttLemmaFixer().ttNormalizer();
		} catch (Exception e) {
			throw new TermSuitePipelineException(e);
		}
	}
	
	public TermSuitePipeline setMateModelPath(String path) {
		this.mateModelsPath = Optional.of(path);
		Preconditions.checkArgument(Files.exists(Paths.get(path)), "Directory %s does not exist", path);
		Preconditions.checkArgument(Files.isDirectory(Paths.get(path)), "File %s is not a directory", path);
		return this;
	}
	
	public TermSuitePipeline aeMateTaggerLemmatizer()  {
		try {
			AnalysisEngineDescription ae = AnalysisEngineFactory.createEngineDescription(
					MateLemmatizerTagger.class
				);
			
			Preconditions.checkState(mateModelsPath.isPresent(), "The path to mate models must be explicitely given. See method #setMateModelPath");
	
			String lemmatizerModel = Paths.get(mateModelsPath.get(), resFactory.getMateLemmatizerModelFileName()).toString();
			String taggerModel = Paths.get(mateModelsPath.get(), resFactory.getMateTaggerModelFileName()).toString();
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
			throw new TermSuitePipelineException(e);
		}
	}
	
	/**
	 * Defines the term properties that appear in tsv export file
	 * 
	 * @see #haeTsvExporter(String)
	 * @param properties
	 * @return
	 */
	public TermSuitePipeline setTsvExportProperties(TermProperty... properties) {
		this.tsvExportProperties = Joiner.on(",").join(properties);
		return this;
	}
	
	/**
	 * Exports the {@link TermIndex} in tsv format
	 * 
	 * @see #setTsvExportProperties(TermProperty...)
	 * @param toFilePath
	 * @return
	 */
	public TermSuitePipeline haeTsvExporter(String toFilePath) {
		try {
			AnalysisEngineDescription ae = AnalysisEngineFactory.createEngineDescription(
					TSVExporter.class, 
					TSVExporter.FILTERING_RULE, exportFilteringRule,
					TSVExporter.FILTERING_THRESHOLD, exportFilteringThreshold,
					TSVExporter.TO_FILE_PATH, toFilePath,
					TSVExporter.TERM_PROPERTIES, this.tsvExportProperties
				);
			ExternalResourceFactory.bindResource(ae, resTermIndex());
	
			return aggregateAndReturn(ae);
		} catch (Exception e) {
			throw new TermSuitePipelineException(e);
		}
	}
	
	/**
	 * 
	 * Exports examples of matching pairs for each variation rule.
	 * 
	 * @param toFilePath
	 * 				the file path where to write the examples for each variation rules
	 * @return the pipeline
	 */
	public TermSuitePipeline haeExportVariationRuleExamples(String toFilePath) {
		try {
			AnalysisEngineDescription ae = AnalysisEngineFactory.createEngineDescription(
					ExportVariationRuleExamples.class, ExportVariationRuleExamples.TO_FILE_PATH, toFilePath);
			ExternalResourceFactory.bindResource(ae, resTermIndex());
			ExternalResourceFactory.bindResource(ae, resSyntacticVariantRules());
			return aggregateAndReturn(ae);
		} catch (Exception e) {
			throw new TermSuitePipelineException(e);
		}
	}
	
	public TermSuitePipeline haeTbxExporter(String toFilePath) {
		try {
			AnalysisEngineDescription ae = AnalysisEngineFactory.createEngineDescription(
					TBXExporter.class, 
					TBXExporter.FILTERING_RULE, exportFilteringRule,
					TBXExporter.FILTERING_THRESHOLD, exportFilteringThreshold,
					TBXExporter.TO_FILE_PATH, toFilePath,
					TBXExporter.LANGUAGE, this.lang
				);
			ExternalResourceFactory.bindResource(ae, resTermIndex());
	
			return aggregateAndReturn(ae);
		} catch (Exception e) {
			throw new TermSuitePipelineException(e);
		}
	}

	public TermSuitePipeline haeEvalExporter(String toFilePath, boolean withVariants) {
		try {
			AnalysisEngineDescription ae = AnalysisEngineFactory.createEngineDescription(
					EvalExporter.class, 
					EvalExporter.FILTERING_RULE, FilterRules.SpecificityThreshold,
					EvalExporter.FILTERING_THRESHOLD, 1.0f,
					EvalExporter.TO_FILE_PATH, toFilePath,
					EvalExporter.WITH_VARIANTS, withVariants
					
				);
			ExternalResourceFactory.bindResource(ae, resTermIndex());
	
			return aggregateAndReturn(ae);
		} catch (Exception e) {
			throw new TermSuitePipelineException(e);
		}
	}
	
	public TermSuitePipeline setExportJsonWithOccurrences(boolean exportJsonWithOccurrences) {
		this.exportJsonWithOccurrences = exportJsonWithOccurrences;
		return this;
	}
	
	public TermSuitePipeline setExportJsonWithContext(boolean b) {
		this.exportJsonWithContext = b;
		return this;
	}

	
	public TermSuitePipeline haeJsonExporter(String toFilePath)  {
		try {
			AnalysisEngineDescription ae = AnalysisEngineFactory.createEngineDescription(
					JsonExporter.class, 
					JsonExporter.TO_FILE_PATH, toFilePath,
					JsonExporter.WITH_OCCURRENCE, exportJsonWithOccurrences,
					JsonExporter.WITH_CONTEXTS, exportJsonWithContext
				);
			ExternalResourceFactory.bindResource(ae, resTermIndex());
	
			return aggregateAndReturn(ae);
		} catch (Exception e) {
			throw new TermSuitePipelineException(e);
		}
	}


	/**
	 * 
	 * Creates a tsv output with :
	 *  - the occurrence list of each term and theirs in-text contexts.
	 *  - a json structure for the evaluation of each variant
	 * 
	 * @param toFilePath
	 * 			The output file path
	 * @param topN
	 * 			The number of variants to keep in the file
	 * @param maxVariantsPerTerm
	 * 			The maximum number of variants to eval for each term
	 * @return
	 */
	public TermSuitePipeline haeVariantEvalExporter(String toFilePath, int topN, int maxVariantsPerTerm)  {
		try {
			AnalysisEngineDescription ae = AnalysisEngineFactory.createEngineDescription(
					VariantEvalExporter.class, 
					VariantEvalExporter.FILTERING_RULE, eu.project.ttc.engines.cleaner.FilterRules.SpecificityThreshold,
					VariantEvalExporter.FILTERING_THRESHOLD, 1.0f,
					VariantEvalExporter.TO_FILE_PATH, toFilePath,
					VariantEvalExporter.TOP_N, topN,
					VariantEvalExporter.NB_VARIANTS_PER_TERM, maxVariantsPerTerm
				);
			
			ExternalResourceFactory.bindResource(ae, resTermIndex());
	
			return aggregateAndReturn(ae);
		} catch (Exception e) {
			throw new TermSuitePipelineException(e);
		}
	}
	
	private void addParameters(AnalysisEngineDescription ae, Object... parameters) {
		if(parameters.length % 2 == 1)
			throw new IllegalArgumentException("Expecting even number of arguements for key-value pairs: " + parameters.length);
		for(int i=0; i<parameters.length; i+=2) 
			ae.getMetaData().getConfigurationParameterSettings().setParameterValue((String)parameters[i], parameters[i+1]);
	}

	private TermSuitePipeline subNormalizer(String target, String mappingFile)  {
		try {
			AnalysisEngineDescription ae = AnalysisEngineFactory.createEngineDescription(
					Mapper.class, 
					Mapper.PARAM_SOURCE, "eu.project.ttc.types.WordAnnotation:tag",
					Mapper.PARAM_TARGET, target,
					Mapper.PARAM_UPDATE, true
				);
			ExternalResourceFactory.createDependencyAndBind(
					ae,
					Mapping.KEY_MAPPING, 
					MappingResource.class, 
					mappingFile);
	
			return aggregateAndReturn(ae);
		} catch (Exception e) {
			throw new TermSuitePipelineException(e);
		}
	}

	private TermSuitePipeline caseNormalizer(String tagger)  {
		return subNormalizer(
				"eu.project.ttc.types.WordAnnotation:case", 
				resFactory.getCaseMapping(tagger).toString());
	}

	private TermSuitePipeline categoryNormalizer(String tagger)  {
		return subNormalizer(
				"eu.project.ttc.types.WordAnnotation:category", 
				resFactory.getCategoryMapping(tagger).toString());
	}

	private TermSuitePipeline tenseNormalizer(String tagger)  {
		return subNormalizer(
				"eu.project.ttc.types.WordAnnotation:tense", 
				resFactory.getTenseMapping(tagger).toString());
	}

	private TermSuitePipeline subCategoryNormalizer(String tagger)  {
		return subNormalizer(
				"eu.project.ttc.types.WordAnnotation:subCategory", 
				resFactory.getSubcategoryMapping(tagger).toString());
	}

	
	private TermSuitePipeline moodNormalizer(String tagger)  {
		return subNormalizer(
				"eu.project.ttc.types.WordAnnotation:mood", 
				resFactory.getMoodMapping(tagger).toString());
	}

	
	private TermSuitePipeline numberNormalizer(String tagger)  {
		return subNormalizer(
				"eu.project.ttc.types.WordAnnotation:number", 
				resFactory.getNumberMapping(tagger).toString());
	}

	
	private TermSuitePipeline genderNormalizer(String tagger)  {
		return subNormalizer(
				"eu.project.ttc.types.WordAnnotation:gender", 
				resFactory.getGenderMapping(tagger).toString());
	}

	private TermSuitePipeline mateNormalizer()  {
		return normalizer("mate");
	}

	private TermSuitePipeline ttNormalizer()  {
		return normalizer("tt");
	}

	private TermSuitePipeline normalizer(String tagger)  {
		categoryNormalizer(tagger);
		subCategoryNormalizer(tagger);
		moodNormalizer(tagger);
		tenseNormalizer(tagger);
		genderNormalizer(tagger);
		numberNormalizer(tagger);
		return caseNormalizer(tagger);
	}
	
	public TermSuitePipeline aeStemmer()  {
		try {
			AnalysisEngineDescription ae = AnalysisEngineFactory.createEngineDescription(
					Stemmer.class,
					Stemmer.PARAM_FEATURE, "eu.project.ttc.types.WordAnnotation:stem",
					Stemmer.PARAM_LANGUAGE, lang,
					Stemmer.PARAM_UPDATE, true
				);
			
			return aggregateAndReturn(ae);
		} catch (Exception e) {
			throw new TermSuitePipelineException(e);
		}
	}
	

	private TermSuitePipeline ttLemmaFixer()  {
		try {
			AnalysisEngineDescription ae = AnalysisEngineFactory.createEngineDescription(
					TreeTaggerLemmaFixer.class,
					TreeTaggerLemmaFixer.LANGUAGE, lang.getCode()
				);
			
			return aggregateAndReturn(ae);
		} catch (Exception e) {
			throw new TermSuitePipelineException(e);
		}
	}
	
	private TermSuitePipeline mateLemmaFixer()  {
		try {
			AnalysisEngineDescription ae = AnalysisEngineFactory.createEngineDescription(
					MateLemmaFixer.class,
					MateLemmaFixer.LANGUAGE, lang.getCode()
				);
			
			return aggregateAndReturn(ae);
		} catch (Exception e) {
			throw new TermSuitePipelineException(e);
		}
	}

		
	/**
	 * The single-word and multi-word term spotter AE
	 * base on UIMA Tokens Regex.
	 * 
	 * @return
	 */
	public TermSuitePipeline aeRegexSpotter()  {
		try {
			Serializable postProcStrategy = this.postProcessingStrategy.isPresent() ? this.postProcessingStrategy.get() : lang.getRegexPostProcessingStrategy();
			AnalysisEngineDescription ae = AnalysisEngineFactory.createEngineDescription(
					RegexSpotter.class,
					TokenRegexAE.PARAM_ALLOW_OVERLAPPING_OCCURRENCES, true,
					RegexSpotter.POST_PROCESSING_STRATEGY, postProcStrategy,
					RegexSpotter.KEEP_OCCURRENCES_IN_TERM_INDEX, this.spotWithOccurrences
				);
			
			if(enableSyntacticLabels)
				addParameters(
						ae, 
						TokenRegexAE.PARAM_SET_LABELS, "labels");
			
			if(logOverlappingRules.isPresent())
				addParameters(
						ae, 
						RegexSpotter.LOG_OVERLAPPING_RULES, logOverlappingRules.get());
			
				
			if(!this.syntacticRegexesFilePath.isPresent() && resFactory.resourceExists(resFactory.getMWRegexes().toString())) {
				LOGGER.warn("File " + resFactory.getMWRegexes() + " does not exist.");
				throw new TermSuitePipelineException("Regex Spotter is not supported for language " + this.lang + ". Please provide a .regex file.");
			}
			
			ExternalResourceFactory.createDependencyAndBind(
					ae,
					RegexListResource.KEY_TOKEN_REGEX_RULES, 
					RegexListResource.class, 
					this.syntacticRegexesFilePath.isPresent() ?  this.syntacticRegexesFilePath.get() : resFactory.getMWRegexes().toString());
	
			ExternalResourceFactory.createDependencyAndBind(
					ae,
					RegexSpotter.CHARACTER_FOOTPRINT_TERM_FILTER, 
					CharacterFootprintTermFilter.class, 
					resFactory.getAllowedChars().toString());
	
			ExternalResourceFactory.bindResource(ae, resTermIndex());
			
			ExternalResourceFactory.createDependencyAndBind(
					ae,
					RegexSpotter.STOP_WORD_FILTER, 
					DefaultFilterResource.class, 
					resFactory.getStopWords().toString());
			
			return aggregateAndReturn(ae);
		} catch (Exception e) {
			throw new TermSuitePipelineException(e);
		}
	}
	
	/**
	 * Naive morphological analysis of compounds based on a 
	 * compound dictionary resource
	 * 
	 * @deprecated 
	 * 		Use {@link #aeCompostSplitter()} instead
	 * @return
	 */
	public TermSuitePipeline aeCompoundSplitter()  {
		try {
			AnalysisEngineDescription ae = AnalysisEngineFactory.createEngineDescription(
					CompoundSplitter.class
				);
			ExternalResourceFactory.createDependencyAndBind(
					ae,
					CompoundSplitter.DICTIONARY, 
					DictionaryResource.class, 
					resFactory.getEmptyDictionary().toString()
				);
			
			ExternalResourceFactory.bindResource(ae, resTermIndex());
			
			return aggregateAndReturn(ae);
		} catch (Exception e) {
			throw new TermSuitePipelineException(e);
		}
	}
	
	/**
	 * Naive morphological analysis of neo-classical compounds based on a 
	 * neo-classical dictionary resource
	 * 
	 * @deprecated 
	 * 		Use {@link #aeCompostSplitter()} instead
	 * @return
	 */
	public TermSuitePipeline aeNeoClassicalSplitter()  {
		return aeAffixCompoundSplitter(true, resFactory.getRootBank().toString());
	}
	
	/**
	 * Naive morphological analysis of prefix compounds based on a 
	 * prefix dictionary resource
	 * 
	 * @deprecated 
	 * 		Use {@link #aeCompostSplitter()} instead
	 * @return
	 */
	public TermSuitePipeline aePrefixSplitter()  {
		return aeAffixCompoundSplitter(false, resFactory.getPrefixBank().toString());
	}

	private TermSuitePipeline aeAffixCompoundSplitter(boolean neoClassical,
			String bankResource)  {
		try {
			AnalysisEngineDescription ae = AnalysisEngineFactory.createEngineDescription(
					AffixCompoundSplitter.class,
					AffixCompoundSplitter.IS_NEO_CLASSICAL, neoClassical
				);
			ExternalResourceFactory.createDependencyAndBind(
					ae,
					AffixCompoundSplitter.BANK, 
					BankResource.class, 
					bankResource
					);
			
			ExternalResourceFactory.bindResource(ae, resTermIndex());
			
			return aggregateAndReturn(ae);
		} catch(Exception e) {
			throw new TermSuitePipelineException(e);
		}
	}
	
	/**
	 * Removes from the term index any term having a 
	 * stop word at its boundaries.
	 * 
	 * @see TermIndexBlacklistWordFilterAE
	 * @return
	 */
	public TermSuitePipeline aeStopWordsFilter()  {
		try {
			AnalysisEngineDescription ae = AnalysisEngineFactory.createEngineDescription(
					TermIndexBlacklistWordFilterAE.class
				);
			
			ExternalResourceFactory.createDependencyAndBind(
					ae,
					FilterResource.KEY_FILTERS, 
					DefaultFilterResource.class, 
					resFactory.getStopWords().toString()
				);
			
			ExternalResourceFactory.bindResource(ae, resTermIndex());
			
			return aggregateAndReturn(ae);
		} catch(Exception e) {
			throw new TermSuitePipelineException(e);
		}
	}

	


	
	/**
	 * Exports all CAS as XMI files to a given directory.
	 * 
	 * @param toDirectoryPath
	 * @return
	 */
	public TermSuitePipeline haeXmiCasExporter(String toDirectoryPath)  {
		try {
			AnalysisEngineDescription ae = AnalysisEngineFactory.createEngineDescription(
					XmiCasExporter.class,
					XmiCasExporter.OUTPUT_DIRECTORY, toDirectoryPath
				);
			
			
			return aggregateAndReturn(ae);
		} catch(Exception e) {
			throw new TermSuitePipelineException(e);
		}
	}


	/**
	 * Export all CAS in TSV format to a given directory.
	 * 
	 * @see SpotterTSVWriter
	 * @param toDirectoryPath
	 * @return
	 */
	public TermSuitePipeline haeSpotterTSVWriter(String toDirectoryPath)  {
		try {
			AnalysisEngineDescription ae = AnalysisEngineFactory.createEngineDescription(
					SpotterTSVWriter.class,
					XmiCasExporter.OUTPUT_DIRECTORY, toDirectoryPath
				);
			return aggregateAndReturn(ae);
		} catch(Exception e) {
			throw new TermSuitePipelineException(e);
		}
	}


	/**
	 * Tokenizer for chinese collections.
	 * @see ChineseSegmenter
	 * 
	 * @return
	 */
	public TermSuitePipeline aeChineseTokenizer()  {
		try {
			AnalysisEngineDescription ae = AnalysisEngineFactory.createEngineDescription(
					ChineseSegmenter.class,
					ChineseSegmenter.ANNOTATION_TYPE, "eu.project.ttc.types.WordAnnotation"
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
			throw new TermSuitePipelineException(e);
		}
	}

	
	private ExternalResourceDescription termIndexResourceDesc;
	private ExternalResourceDescription resTermIndex() {
		if(termIndexResourceDesc == null) {
			if(!termIndex.isPresent())
				emptyTermIndex(UUID.randomUUID().toString());
			
			termIndexResourceDesc = ExternalResourceFactory.createExternalResourceDescription(
					TermIndexResource.class, 
					termIndex.get().getName());
			
			MemoryTermIndexManager manager = MemoryTermIndexManager.getInstance();
			
			// register the term index if not in term index manager
			if(!manager.containsTermIndex(termIndex.get().getName()))
				manager.register(termIndex.get());
		}
		return termIndexResourceDesc;
		
	}
	
	private ExternalResourceDescription syntacticVariantRules;
	private ExternalResourceDescription resSyntacticVariantRules() {
		if(syntacticVariantRules == null) {
			syntacticVariantRules = ExternalResourceFactory.createExternalResourceDescription(
					YamlVariantRules.class, 
					this.yamlVariantRulesFilePath.isPresent() ?  this.yamlVariantRulesFilePath.get() : resFactory.getYamlVariantRules().toString()
				);
		}
		return syntacticVariantRules;

	}


	/**
	 * Returns the term index produced (or last modified) by this pipeline.
	 * @return
	 */
	public TermIndex getTermIndex() {
		return this.termIndex.get();
	}
	
	/**
	 * Sets the term index on which this pipeline will run.
	 * 
	 * @param termIndex
	 */
	public TermSuitePipeline setTermIndex(TermIndex termIndex) {
		this.termIndex = Optional.of(termIndex);
		return this;
	}
	
	/**
	 * Creates a new in-memory {@link TermIndex} on which this 
	 * piepline with run.
	 * 
	 * @param name
	 * 			the name of the new term index
	 * @return
	 * 			this {@link TermSuitePipeline} object
	 */
	public TermSuitePipeline emptyTermIndex(String name) {
		this.termIndex = Optional.of(new MemoryTermIndex(name, this.lang));
		return this;
	}

	
	
	private ExternalResourceDescription generalLanguageResourceDesc;
	private ExternalResourceDescription resGeneralLanguage() {
		if(generalLanguageResourceDesc == null)
			generalLanguageResourceDesc = ExternalResourceFactory.createExternalResourceDescription(
					GeneralLanguageResource.class, 
					resFactory.getGeneralLanguageFrequencies().toString());
		return generalLanguageResourceDesc;
	}
	
	/**
	 * Computes {@link TermProperty#WR} values (and additional 
	 * term properties of type {@link TermProperty} in the future).
	 * 
	 * @see TermSpecificityComputer
	 * @see TermProperty
	 * @return
	 */
	public TermSuitePipeline aeSpecificityComputer()  {
		try {
			AnalysisEngineDescription ae = AnalysisEngineFactory.createEngineDescription(
					TermSpecificityComputer.class
				);
			ExternalResourceFactory.bindResource(ae, resGeneralLanguage());
			ExternalResourceFactory.bindResource(ae, resTermIndex());
			return aggregateAndReturn(ae);
		} catch(Exception e) {
			throw new TermSuitePipelineException(e);
		}
	}

	
	public TermSuitePipeline setContextualizeCoTermsType(
			OccurrenceType contextualizeCoTermsType) {
		this.contextualizeCoTermsType = contextualizeCoTermsType;
		return this;
	}
	
	public TermSuitePipeline setContextualizeWithTermClasses(
			boolean contextualizeWithTermClasses) {
		this.contextualizeWithTermClasses = contextualizeWithTermClasses;
		return this;
	}
	
	public TermSuitePipeline setContextualizeWithCoOccurrenceFrequencyThreshhold(
			int contextualizeWithCoOccurrenceFrequencyThreshhold) {
		this.contextualizeWithCoOccurrenceFrequencyThreshhold = contextualizeWithCoOccurrenceFrequencyThreshhold;
		return this;
	}
	
	/**
	 * Computes the {@link Contextualizer} vector of all 
	 * single-word terms in the term index.
	 * 
	 * @see Contextualizer
	 * @param scope
	 * @param allTerms
	 * @return
	 */
	public TermSuitePipeline aeContextualizer(int scope, boolean allTerms) {
		AnalysisEngineDescription ae;
		try {
			ae = AnalysisEngineFactory.createEngineDescription(
					Contextualizer.class,
					Contextualizer.NORMALIZE_ASSOC_RATE, true,
					Contextualizer.SCOPE, scope,
					Contextualizer.CO_TERMS_TYPE, contextualizeCoTermsType,
					Contextualizer.COMPUTE_CONTEXTS_FOR_ALL_TERMS, allTerms,
					Contextualizer.ASSOCIATION_RATE, contextAssocRateMeasure,
					Contextualizer.USE_TERM_CLASSES, contextualizeWithTermClasses,
					Contextualizer.MINIMUM_COOCC_FREQUENCY_THRESHOLD, contextualizeWithCoOccurrenceFrequencyThreshhold
				);
			ExternalResourceFactory.bindResource(ae, resTermIndex());
			return aggregateAndReturn(ae);
		} catch (Exception e) {
			throw new TermSuitePipelineException(e);
		}
	}
	
	
	public TermSuitePipeline aeThresholdCleaner(TermProperty property, float threshold, boolean keepVariants, boolean isPeriodic, int cleaningPeriod, int termIndexSizeTrigger) {
		try {
			AnalysisEngineDescription ae = AnalysisEngineFactory.createEngineDescription(
				TermIndexThresholdCleaner.class,
				AbstractTermIndexCleaner.CLEANING_PROPERTY, property,
				AbstractTermIndexCleaner.NUM_TERMS_CLEANING_TRIGGER, termIndexSizeTrigger,
				AbstractTermIndexCleaner.KEEP_VARIANTS, keepVariants,
				TermIndexThresholdCleaner.THRESHOLD, threshold
			);
			setPeriodic(isPeriodic, cleaningPeriod, ae);
			
			ExternalResourceFactory.bindResource(ae, resTermIndex());
			return aggregateAndReturn(ae);
		} catch(Exception e) {
			throw new TermSuitePipelineException(e);
		}
	}
	
	public TermSuitePipeline aePrimaryOccurrenceDetector(int detectionStrategy) {
		try {
			AnalysisEngineDescription ae = AnalysisEngineFactory.createEngineDescription(
					PrimaryOccurrenceDetector.class,
					PrimaryOccurrenceDetector.DETECTION_STRATEGY, detectionStrategy
			);
			
			ExternalResourceFactory.bindResource(ae, resTermIndex());
			return aggregateAndReturn(ae);
		} catch(Exception e) {
			throw new TermSuitePipelineException(e);
		}
	}


	private void setPeriodic(boolean isPeriodic, int cleaningPeriod,
			AnalysisEngineDescription ae) {
		if(isPeriodic)
			addParameters(ae, 
					AbstractTermIndexCleaner.PERIODIC_CAS_CLEAN_ON, true,
					AbstractTermIndexCleaner.CLEANING_PERIOD, cleaningPeriod
				);
	}
	
	/**
	 * 
	 * 
	 * 
	 * @param property
	 * @param threshold
	 * @param cleaningPeriod
	 * @return
	 */
	public TermSuitePipeline aeThresholdCleanerPeriodic(TermProperty property, float threshold, int cleaningPeriod)   {
		return aeThresholdCleaner(property, threshold, false, true, cleaningPeriod, 0);
	}

	public TermSuitePipeline aeThresholdCleanerSizeTrigger(TermProperty property, float threshold, int termIndexSizeTrigger)   {
		return aeThresholdCleaner(property, threshold, false, false, 0, termIndexSizeTrigger);
	}

	
	public TermSuitePipeline aeThresholdCleaner(TermProperty property, float threshold) {
		return aeThresholdCleaner(property, threshold, false, false, 0, 0);
	}

	public TermSuitePipeline aeTopNCleaner(TermProperty property, int n)  {
		return aeTopNCleanerPeriodic(property, n, false, 0);
	}
	
	/**
	 * 
	 * @param property
	 * @param n
	 * @param isPeriodic
	 * @param cleaningPeriod
	 * @return
	 */
	public TermSuitePipeline aeTopNCleanerPeriodic(TermProperty property, int n, boolean isPeriodic, int cleaningPeriod)  {
		try {
			AnalysisEngineDescription ae = AnalysisEngineFactory.createEngineDescription(
					TermIndexTopNCleaner.class,
					AbstractTermIndexCleaner.CLEANING_PROPERTY, property,
					TermIndexTopNCleaner.TOP_N, n
					);
			setPeriodic(isPeriodic, cleaningPeriod, ae);
			ExternalResourceFactory.bindResource(ae, resTermIndex());
			return aggregateAndReturn(ae);
		} catch(Exception e) {
			throw new TermSuitePipelineException(e);
		}
	}

	public TermSuitePipeline setGraphicalVariantSimilarityThreshold(float th) {
		this.graphicalVariantSimilarityThreshold = Optional.of(th);
		return this;
	}
	
	public TermSuitePipeline aeGraphicalVariantGatherer()   {
		try {
			AnalysisEngineDescription ae = AnalysisEngineFactory.createEngineDescription(
					GraphicalVariantGatherer.class,
					GraphicalVariantGatherer.LANG, lang.getCode(),
					GraphicalVariantGatherer.SIMILARITY_THRESHOLD, graphicalVariantSimilarityThreshold.isPresent() ? graphicalVariantSimilarityThreshold.get() : 0.9f
				);
			ExternalResourceFactory.bindResource(ae, resTermIndex());
			return aggregateAndReturn(ae);
		} catch(Exception e) {
			throw new TermSuitePipelineException(e);
		}
	}

	/**
	 * Filters out URLs from CAS.
	 * 
	 * @return
	 */
	public TermSuitePipeline aeUrlFilter()   {
		try {
			AnalysisEngineDescription ae = AnalysisEngineFactory.createEngineDescription(
					StringRegexFilter.class
				);
			return aggregateAndReturn(ae);
		} catch(Exception e) {
			throw new TermSuitePipelineException(e);
		}
	}
	
	/**
	 * Gathers terms syntactically.
	 * 
	 * @return
	 */
	public TermSuitePipeline aeSyntacticVariantGatherer()   {
		try {
			AnalysisEngineDescription ae = AnalysisEngineFactory.createEngineDescription(
					SyntacticTermGatherer.class
				);
			
			ExternalResourceFactory.bindResource(ae, resSyntacticVariantRules());
			ExternalResourceFactory.bindResource(ae, resTermIndex());
			return aggregateAndReturn(ae);
		} catch(Exception e) {
			throw new TermSuitePipelineException(e);
		}
	}
	

	public TermSuitePipeline setExportFilteringRule(String exportFilteringRule) {
		this.exportFilteringRule = exportFilteringRule;
		return this;
	}
	public TermSuitePipeline setExportFilteringThreshold(float exportFilteringThreshold) {
		this.exportFilteringThreshold = exportFilteringThreshold;
		return this;
	}

	public TermSuitePipeline setTreeTaggerHome(String treeTaggerPath) {
		this.treeTaggerPath = Optional.of(treeTaggerPath);
		return this;
	}
	public TermSuitePipeline setSyntacticRegexesFilePath(String syntacticRegexesFilePath) {
		this.syntacticRegexesFilePath = Optional.of(syntacticRegexesFilePath);
		return this;
	}
	public TermSuitePipeline haeLogOverlappingRules() {
		this.logOverlappingRules = Optional.of(true);
		return this;
	}
	public TermSuitePipeline enableSyntacticLabels() {
		this.enableSyntacticLabels = true;
		return this;
	}
	
	public TermSuitePipeline setYamlVariantRulesFilePath(String yamlVariantRulesFilePath) {
		this.yamlVariantRulesFilePath = Optional.of(yamlVariantRulesFilePath);
		return this;
	}
	
	public TermSuitePipeline setCompostCoeffs(float alpha, float beta, float gamma, float delta) {
		Preconditions.checkArgument(alpha + beta + gamma + delta == 1.0f, "The sum of coeff must be 1.0");
		this.alpha = alpha;
		this.beta = beta;
		this.gamma = gamma;
		this.delta = delta;
		return this;
	}
	
	public TermSuitePipeline setCompostMaxComponentNum(int compostMaxComponentNum) {
		this.compostMaxComponentNum = compostMaxComponentNum;
		return this;
	}
	
	public TermSuitePipeline setCompostMinComponentSize(int compostMinComponentSize) {
		this.compostMinComponentSize = compostMinComponentSize;
		return this;
	}
	
	public TermSuitePipeline setCompostScoreThreshold(float compostScoreThreshold) {
		this.compostScoreThreshold = compostScoreThreshold;
		return this;
	}
	
	public TermSuitePipeline setCompostSegmentSimilarityThreshold(
			Object compostSegmentSimilarityThreshold) {
		this.compostSegmentSimilarityThreshold = compostSegmentSimilarityThreshold;
		return this;
	}
	
	public TermSuitePipeline aeCompostSplitter()  {
		try {
			AnalysisEngineDescription ae = AnalysisEngineFactory.createEngineDescription(
					CompostAE.class,
					CompostAE.SCORE_THRESHOLD, this.compostScoreThreshold,
					CompostAE.ALPHA, alpha,
					CompostAE.BETA, beta,
					CompostAE.GAMMA, gamma,
					CompostAE.DELTA, delta,
					CompostAE.MIN_COMPONENT_SIZE, this.compostMinComponentSize,
					CompostAE.MAX_NUMBER_OF_COMPONENTS, this.compostMaxComponentNum,
					CompostAE.SEGMENT_SIMILARITY_THRESHOLD, this.compostSegmentSimilarityThreshold
				);
			ExternalResourceFactory.bindResource(ae, resTermIndex());
	//		ExternalResourceFactory.bindResource(ae, resGeneralLanguage());
			ExternalResourceFactory.createDependencyAndBind(
					ae,
					CompostAE.LANGUAGE_DICO, 
					SimpleWordSet.class, 
					resFactory.getLanguageDico().toString());
			ExternalResourceFactory.createDependencyAndBind(
					ae,
					CompostAE.INFLECTION_RULES, 
					CompostInflectionRules.class, 
					resFactory.getCompostInflectionRules().toString());
			ExternalResourceFactory.createDependencyAndBind(
					ae,
					CompostAE.TRANSFORMATION_RULES, 
					CompostInflectionRules.class, 
					resFactory.getCompostTransformationRules().toString());
			ExternalResourceFactory.createDependencyAndBind(
					ae,
					CompostAE.STOP_LIST, 
					SimpleWordSet.class, 
					resFactory.getCompostStopList().toString());
			ExternalResourceFactory.createDependencyAndBind(
					ae,
					CompostAE.NEOCLASSICAL_PREFIXES, 
					SimpleWordSet.class, 
					resFactory.getNeoclassicalPrefixes().toString());
			return aggregateAndReturn(ae);
		} catch(Exception e) {
			throw new TermSuitePipelineException(e);
		}
	}

	public TermSuitePipeline haeCasStatCounter(String statName)  {
		try {
			AnalysisEngineDescription ae = AnalysisEngineFactory.createEngineDescription(
					CasStatCounter.class,
					CasStatCounter.STAT_NAME, statName
				);
			ExternalResourceFactory.bindResource(ae, resTermIndex());
	
			return aggregateAndReturn(ae);
		} catch(Exception e) {
			throw new TermSuitePipelineException(e);
		}
	}
	
	/**
	 * 
	 * Exports time progress to TSV file.
	 * 
	 * Columns are :
	 * <ul>
	 * <li>elapsed time from initialization in milliseconds</li>
	 * <li>number of docs processed</li>
	 * <li>cumulated size of data processed</li>
	 * <li>number of terms in term index</li>
	 * <li>number of {@link WordAnnotation} processed</li>
	 * </ul>
	 * 
	 * 
	 * @param toFile
	 * @return
	 */
	public TermSuitePipeline haeTraceTimePerf(String toFile)  {
		try {
			AnalysisEngineDescription ae = AnalysisEngineFactory.createEngineDescription(
					CasStatCounter.class,
					CasStatCounter.DOCUMENT_PERIOD, 1,
					CasStatCounter.TO_TRACE_FILE, toFile
				);
			ExternalResourceFactory.bindResource(ae, resTermIndex());
	
			return aggregateAndReturn(ae);
		} catch(Exception e) {
			throw new TermSuitePipelineException(e);
		}
	}


	/**
	 * 
	 * @see TermClassifier
	 * @param sortingProperty
	 * 			the term property used to order terms before they are classified. 
	 * 			The first term of a class appearing given this order will be considered 
	 * 			as the head of the class.
	 * @return
	 */
	public TermSuitePipeline aeTermClassifier(TermProperty sortingProperty)  {
		try {
			AnalysisEngineDescription ae = AnalysisEngineFactory.createEngineDescription(
					TermClassifier.class,
					TermClassifier.CLASSIFYING_PROPERTY, sortingProperty
					
				);
			ExternalResourceFactory.bindResource(ae, resTermIndex());
			return aggregateAndReturn(ae);
		} catch(Exception e) {
			throw new TermSuitePipelineException(e);
		}
	}
	
	
	/**
	 * 
	 * @param refFileURI
	 * 			The path to reference termino
	 * @param outputFile
	 * 			The path to output log file
	 * @param customLogHeader
	 * 			A custom string to add in the header of the output log file
	 * @param rFile
	 * 			The path to output r file
	 * @param evalTraceName
	 * 			The name of the eval trace
	 * @param rtlWithVariants
	 * 			true if variants of the reference termino should be kept during the eval
	 * @return
	 */
	public TermSuitePipeline haeEval(String refFileURI, String outputFile, String customLogHeader, String rFile, String evalTraceName, boolean rtlWithVariants)  {
		try {
			AnalysisEngineDescription ae = AnalysisEngineFactory.createEngineDescription(
				EvalEngine.class,
				EvalEngine.OUTPUT_LOG_FILE, outputFile,
				EvalEngine.OUTPUT_R_FILE, rFile,
				EvalEngine.CUSTOM_LOG_HEADER_STRING, customLogHeader,
//				EvalEngine.LC_WITH_VARIANTS, extractedTerminoWithVariants,
				EvalEngine.RTL_WITH_VARIANTS, rtlWithVariants
				
			);
			ExternalResourceFactory.bindResource(ae, resTermIndex());
			ExternalResourceFactory.createDependencyAndBind(
					ae,
					EvalEngine.EVAL_TRACE, 
					EvalTrace.class, 
					evalTraceName);
			ExternalResourceFactory.createDependencyAndBind(
					ae,
					EvalEngine.REFERENCE_LIST, 
					ReferenceTermList.class, 
					"file:" + refFileURI);
			return aggregateAndReturn(ae);
		} catch(Exception e) {
			throw new TermSuitePipelineException(e);
		}
	}

	public TermSuitePipeline setSpotWithOccurrences(boolean b) {
		this.spotWithOccurrences  = b;
		return this;
	}

	/**
	 * Sets the post processing strategy for {@link RegexSpotter} analysis engine
	 * 
	 * @see #aeRegexSpotter()
	 * @see OccurrenceBuffer#NO_CLEANING
	 * @see OccurrenceBuffer#KEEP_PREFIXES
	 * @see OccurrenceBuffer#KEEP_SUFFIXES
	 * 
	 * @param postProcessingStrategy
	 * @return
	 */
	public TermSuitePipeline setPostProcessingStrategy(
			String postProcessingStrategy) {
		
		this.postProcessingStrategy = Optional.of(postProcessingStrategy);
		
		return this;
	}
}
