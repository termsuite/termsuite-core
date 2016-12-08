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
package fr.univnantes.termsuite.uima;

import java.io.File;
import java.io.Serializable;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;

import org.apache.commons.lang.mutable.MutableInt;
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
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

import fr.free.rocheteau.jerome.engines.Stemmer;
import fr.univnantes.julestar.uima.resources.MultimapFlatResource;
import fr.univnantes.lina.uima.ChineseSegmenterResourceHelper;
import fr.univnantes.lina.uima.engines.ChineseSegmenter;
import fr.univnantes.lina.uima.engines.TreeTaggerWrapper;
import fr.univnantes.lina.uima.models.ChineseSegmentResource;
import fr.univnantes.lina.uima.models.TreeTaggerParameter;
import fr.univnantes.lina.uima.tkregex.ae.RegexListResource;
import fr.univnantes.lina.uima.tkregex.ae.TokenRegexAE;
import fr.univnantes.termsuite.api.stream.CasConsumer;
import fr.univnantes.termsuite.api.stream.ConsumerRegistry;
import fr.univnantes.termsuite.api.stream.DocumentProvider;
import fr.univnantes.termsuite.api.stream.DocumentStream;
import fr.univnantes.termsuite.api.stream.StreamingCasConsumer;
import fr.univnantes.termsuite.engines.ExtensionVariantGatherer;
import fr.univnantes.termsuite.engines.contextualizer.Contextualizer;
import fr.univnantes.termsuite.engines.contextualizer.ContextualizerOptions;
import fr.univnantes.termsuite.model.Lang;
import fr.univnantes.termsuite.model.OccurrenceStore;
import fr.univnantes.termsuite.model.RelationType;
import fr.univnantes.termsuite.model.Tagger;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.TermProperty;
import fr.univnantes.termsuite.model.TermSuiteCollection;
import fr.univnantes.termsuite.model.Terminology;
import fr.univnantes.termsuite.model.occurrences.MemoryOccurrenceStore;
import fr.univnantes.termsuite.model.occurrences.MongoDBOccurrenceStore;
import fr.univnantes.termsuite.model.termino.MemoryTerminology;
import fr.univnantes.termsuite.resources.ScorerConfig;
import fr.univnantes.termsuite.resources.TermSuitePipelineObserver;
import fr.univnantes.termsuite.types.FixedExpression;
import fr.univnantes.termsuite.types.TermOccAnnotation;
import fr.univnantes.termsuite.types.WordAnnotation;
import fr.univnantes.termsuite.uima.engines.PipelineListener;
import fr.univnantes.termsuite.uima.engines.PipelineObserver;
import fr.univnantes.termsuite.uima.engines.export.CompoundExporterAE;
import fr.univnantes.termsuite.uima.engines.export.EvalExporterAE;
import fr.univnantes.termsuite.uima.engines.export.ExportVariationRuleExamplesAE;
import fr.univnantes.termsuite.uima.engines.export.JsonCasExporter;
import fr.univnantes.termsuite.uima.engines.export.JsonExporterAE;
import fr.univnantes.termsuite.uima.engines.export.SpotterTSVWriter;
import fr.univnantes.termsuite.uima.engines.export.TSVExporterAE;
import fr.univnantes.termsuite.uima.engines.export.TbxExporterAE;
import fr.univnantes.termsuite.uima.engines.export.TermsuiteJsonCasExporter;
import fr.univnantes.termsuite.uima.engines.export.VariantEvalExporterAE;
import fr.univnantes.termsuite.uima.engines.export.VariationExporterAE;
import fr.univnantes.termsuite.uima.engines.export.XmiCasExporter;
import fr.univnantes.termsuite.uima.engines.preproc.CasStatCounter;
import fr.univnantes.termsuite.uima.engines.preproc.DocumentLogger;
import fr.univnantes.termsuite.uima.engines.preproc.FixedExpressionSpotter;
import fr.univnantes.termsuite.uima.engines.preproc.FixedExpressionTermMarker;
import fr.univnantes.termsuite.uima.engines.preproc.MateLemmaFixer;
import fr.univnantes.termsuite.uima.engines.preproc.MateLemmatizerTagger;
import fr.univnantes.termsuite.uima.engines.preproc.RegexSpotter;
import fr.univnantes.termsuite.uima.engines.preproc.StringRegexFilter;
import fr.univnantes.termsuite.uima.engines.preproc.TermOccAnnotationImporter;
import fr.univnantes.termsuite.uima.engines.preproc.TerminologyBlacklistWordFilterAE;
import fr.univnantes.termsuite.uima.engines.preproc.TreeTaggerLemmaFixer;
import fr.univnantes.termsuite.uima.engines.termino.ContextualizerAE;
import fr.univnantes.termsuite.uima.engines.termino.DocumentFrequencySetterAE;
import fr.univnantes.termsuite.uima.engines.termino.EvalEngine;
import fr.univnantes.termsuite.uima.engines.termino.ExtensionDetecterAE;
import fr.univnantes.termsuite.uima.engines.termino.ExtensionVariantGathererAE;
import fr.univnantes.termsuite.uima.engines.termino.MergerAE;
import fr.univnantes.termsuite.uima.engines.termino.MorphologicalAnalyzerAE;
import fr.univnantes.termsuite.uima.engines.termino.PilotSetterAE;
import fr.univnantes.termsuite.uima.engines.termino.PostProcessorAE;
import fr.univnantes.termsuite.uima.engines.termino.Ranker;
import fr.univnantes.termsuite.uima.engines.termino.SWTSizeSetterAE;
import fr.univnantes.termsuite.uima.engines.termino.TermGathererAE;
import fr.univnantes.termsuite.uima.engines.termino.TermSpecificityComputer;
import fr.univnantes.termsuite.uima.engines.termino.cleaning.AbstractTerminologyCleaner;
import fr.univnantes.termsuite.uima.engines.termino.cleaning.MaxSizeThresholdCleaner;
import fr.univnantes.termsuite.uima.engines.termino.cleaning.TerminologyThresholdCleaner;
import fr.univnantes.termsuite.uima.engines.termino.cleaning.TerminologyTopNCleaner;
import fr.univnantes.termsuite.uima.readers.AbstractToTxtSaxHandler;
import fr.univnantes.termsuite.uima.readers.CollectionDocument;
import fr.univnantes.termsuite.uima.readers.EmptyCollectionReader;
import fr.univnantes.termsuite.uima.readers.GenericXMLToTxtCollectionReader;
import fr.univnantes.termsuite.uima.readers.IstexCollectionReader;
import fr.univnantes.termsuite.uima.readers.JsonCollectionReader;
import fr.univnantes.termsuite.uima.readers.QueueRegistry;
import fr.univnantes.termsuite.uima.readers.StreamingCollectionReader;
import fr.univnantes.termsuite.uima.readers.StringCollectionReader;
import fr.univnantes.termsuite.uima.readers.TxtCollectionReader;
import fr.univnantes.termsuite.uima.readers.XmiCollectionReader;
import fr.univnantes.termsuite.uima.resources.ObserverResource;
import fr.univnantes.termsuite.uima.resources.TermHistoryResource;
import fr.univnantes.termsuite.uima.resources.TermSuiteMemoryUIMAResource;
import fr.univnantes.termsuite.uima.resources.preproc.CharacterFootprintTermFilter;
import fr.univnantes.termsuite.uima.resources.preproc.EvalTrace;
import fr.univnantes.termsuite.uima.resources.preproc.FixedExpressionResource;
import fr.univnantes.termsuite.uima.resources.preproc.ManualSegmentationResource;
import fr.univnantes.termsuite.uima.resources.preproc.MateLemmatizerModel;
import fr.univnantes.termsuite.uima.resources.preproc.MateTaggerModel;
import fr.univnantes.termsuite.uima.resources.preproc.PrefixTree;
import fr.univnantes.termsuite.uima.resources.preproc.SimpleWordSet;
import fr.univnantes.termsuite.uima.resources.termino.CompostInflectionRules;
import fr.univnantes.termsuite.uima.resources.termino.GeneralLanguageResource;
import fr.univnantes.termsuite.uima.resources.termino.ReferenceTermList;
import fr.univnantes.termsuite.uima.resources.termino.SuffixDerivationList;
import fr.univnantes.termsuite.uima.resources.termino.TerminologyResource;
import fr.univnantes.termsuite.uima.resources.termino.YamlRuleSetResource;
import fr.univnantes.termsuite.utils.FileUtils;
import fr.univnantes.termsuite.utils.OccurrenceBuffer;
import fr.univnantes.termsuite.utils.TermHistory;
import fr.univnantes.termsuite.utils.TermSuiteResourceManager;
import uima.sandbox.filter.resources.DefaultFilterResource;
import uima.sandbox.filter.resources.FilterResource;
import uima.sandbox.lexer.engines.Lexer;
import uima.sandbox.lexer.resources.SegmentBank;
import uima.sandbox.lexer.resources.SegmentBankResource;
import uima.sandbox.mapper.engines.Mapper;
import uima.sandbox.mapper.resources.Mapping;
import uima.sandbox.mapper.resources.MappingResource;

/**
 * A collection reader and ae aggregator (builder pattern) that 
 * creates and runs a full pipeline.
 *  
 * @author Damien Cram
 *
 */
public class TermSuitePipeline {

	/* The Logger */
	private static final Logger LOGGER = LoggerFactory.getLogger(TermSuitePipeline.class);
	
	/* ******************************
	 * MAIN PIPELINE PARAMETERS
	 */
	private OccurrenceStore occurrenceStore = new MemoryOccurrenceStore();
	private Optional<? extends Terminology> termino = Optional.empty();
	private Lang lang;
	private CollectionReaderDescription crDescription;
	private String pipelineObserverName;
	private AggregateBuilder aggregateBuilder;
	private String termHistoryResourceName = "PipelineHistory";

	
	/*
	 * POS Tagger parameters
	 */
	private Optional<String> mateModelsPath = Optional.empty();
	private Optional<String> treeTaggerPath = Optional.empty();
	

	/*
	 * Regex Spotter params
	 */
	private boolean addSpottedAnnoToTermino = false;
	private boolean spotWithOccurrences = true;
	private Optional<Boolean> logOverlappingRules = Optional.empty();
	private Optional<String> postProcessingStrategy = Optional.empty();
	private boolean enableSyntacticLabels = false;

	/*
	 * Cleaner properties
	 */
	private boolean keepVariantsWhileCleaning = false;
	
	/*
	 * Compost Params
	 */
	private Optional<Double> alpha = Optional.empty();
	private Optional<Double> beta = Optional.empty();
	private Optional<Double> gamma = Optional.empty();
	private Optional<Double> delta = Optional.empty();
	private Optional<Double> compostScoreThreshold = Optional.empty();
	private Optional<Integer> compostMinComponentSize = Optional.empty();
	private Optional<Integer> compostMaxComponentNum = Optional.empty();
	private Optional<Double> compostSegmentSimilarityThreshold = Optional.of(1d);

	/*
	 * Graphical Variant Gatherer parameters
	 */
	private Optional<Double> graphicalVariantSimilarityThreshold = Optional.empty();
	
	/* JSON */
	private boolean exportJsonWithOccurrences = true;
	private boolean exportJsonWithContext = false;
	private boolean linkMongoStore = false;
	/* TSV */
	private String tsvExportProperties = "groupingKey,wr";
	private boolean tsvWithVariantScores = false;
	private boolean tsvWithHeaders = true;
	
	/*
	 * Streaming parameters
	 */
	private Thread streamThread = null;
	private DocumentProvider documentProvider;


	/* *******************
	 * CONSTRUCTORS
	 */
	private TermSuitePipeline(String lang, String urlPrefix) {
		this.lang = Lang.forName(lang);
		this.aggregateBuilder = new AggregateBuilder();
		this.pipelineObserverName = PipelineObserver.class.getSimpleName() + "-" + Thread.currentThread().getId() + "-" + System.currentTimeMillis();

		TermSuiteResourceManager.getInstance().register(pipelineObserverName, new TermSuitePipelineObserver(2,1));
		
		this.termHistoryResourceName = TermHistory.class.getSimpleName() + "-" + Thread.currentThread().getId() + "-" + System.currentTimeMillis();
		TermSuiteResourceManager.getInstance().register(termHistoryResourceName, new TermHistory());
		
		initUIMALogging();
	}

	
	private void initUIMALogging() {
		System.setProperty("org.apache.uima.logger.class", UIMASlf4jWrapperLogger.class.getName());
	}


	/**
	 * 
	 * Starts a chaining {@link TermSuitePipeline} builder. 
	 * 
	 * @param lang
	 * 			The 
	 * @return
	 * 			The chaining builder.
	 * 
	 */
	public static TermSuitePipeline create(String lang) {
		return new TermSuitePipeline(lang, null);
	}
	

	public static TermSuitePipeline create(Terminology termino) {
		Preconditions.checkNotNull(termino.getName(), "The term index must have a name before it can be used in TermSuitePipeline");
		
		if(!TermSuiteResourceManager.getInstance().contains(termino.getName()))
			TermSuiteResourceManager.getInstance().register(termino.getName(), termino);
		
		TermSuitePipeline pipeline = create(termino.getLang().getCode());
		pipeline.emptyCollection();
		pipeline.setTermino(termino);
		
		return pipeline;
	}
	
	/* *******************************
	 * RUNNERS
	 */
	
	/**
	 * Runs the pipeline with {@link SimplePipeline} on the {@link CollectionReader} that must have been defined.
	 * 
	 * @throws TermSuitePipelineException if no {@link CollectionReader} has been declared on this pipeline
	 */
	public TermSuitePipeline run() {
		checkCR();
		runPipeline();
		return this;
	}
	
	private void runPipeline() {
		try {
			SimplePipeline.runPipeline(this.crDescription, createDescription());
			terminates();
		} catch (Exception e) {
			throw new TermSuitePipelineException(e);
		}
	}
	
	public DocumentStream stream(CasConsumer consumer) {
		try {
			String id = new BigInteger(130, new SecureRandom()).toString(8);
			String casConsumerName = "pipeline-"+id+"-consumer";
			ConsumerRegistry.getInstance().registerConsumer(casConsumerName, consumer);
			String queueName = "pipeline-"+id+"-queue";
			final BlockingQueue<CollectionDocument> q = QueueRegistry.getInstance().registerQueue(queueName, 10);
			
			/*
			 * 1- Creates the streaming collection reader desc
			 */
			this.crDescription = CollectionReaderFactory.createReaderDescription(
					StreamingCollectionReader.class,
					StreamingCollectionReader.PARAM_LANGUAGE, this.lang.getCode(),
					StreamingCollectionReader.PARAM_NAME, queueName,
					StreamingCollectionReader.PARAM_QUEUE_NAME, queueName
					);
			
			/*
			 * 2- Aggregate the consumer AE
			 */
			AnalysisEngineDescription consumerAE = AnalysisEngineFactory.createEngineDescription(
					StreamingCasConsumer.class, 
					StreamingCasConsumer.PARAM_CONSUMER_NAME, casConsumerName
				);
			this.aggregateBuilder.add(consumerAE);
			
			/*
			 * 3- Starts the pipeline in a separate Thread 
			 */
			this.streamThread = new Thread() {
				@Override
				public void run() {
					runPipeline();
				}
			};
			this.streamThread.start();
			
			/*
			 * 4- Bind user inputs to the queue
			 */
			documentProvider = new DocumentProvider() {
				@Override
				public void provide(CollectionDocument doc) {
					try {
						q.put(doc);
					} catch (InterruptedException e) {
						LOGGER.warn("Interrupted while there were more documents waiting.");
					}
				}
			};
			return new DocumentStream(streamThread, documentProvider, consumer, queueName);
		} catch (Exception e) {
			throw new TermSuitePipelineException(e);
		}
	}

	public Thread getStreamThread() {
		return streamThread;
	}
	
	private void checkCR() {
		if(crDescription == null)
			throw new TermSuitePipelineException("No collection reader has been declared on this pipeline.");
	}

		
	private void terminates() {
		if(termino.isPresent() && termino.get().getOccurrenceStore() instanceof MongoDBOccurrenceStore) 
			((MongoDBOccurrenceStore)termino.get().getOccurrenceStore()).close();
			
	}

	/**
	 * Registers a pipeline listener.
	 * 
	 * @param pipelineListener
	 * @return
	 * 		This chaining {@link TermSuitePipeline} builder object
	 */
	public TermSuitePipeline addPipelineListener(PipelineListener pipelineListener) {
		TermSuiteResourceManager manager = TermSuiteResourceManager.getInstance();
		((TermSuitePipelineObserver)manager.get(pipelineObserverName)).registerListener(pipelineListener);
		return this;
	}

	
	/**
	 * Runs the pipeline with {@link SimplePipeline} without requiring a {@link CollectionReader}
	 * to be defined.
	 * @param cas the {@link JCas} on which the pipeline operates.
	 * @return
	 * 		This chaining {@link TermSuitePipeline} builder object
	 */
	public TermSuitePipeline run(JCas cas) {
		try {
			SimplePipeline.runPipeline(cas, createDescription());
			terminates();
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
	
	
	public TermSuitePipeline setIstexCollection(String apiURL, List<String> documentsIds) {
		try {
			this.crDescription = CollectionReaderFactory.createReaderDescription(
				IstexCollectionReader.class,
				IstexCollectionReader.PARAM_IGNORE_LANGUAGE_ERRORS, true,
				IstexCollectionReader.PARAM_LANGUAGE, this.lang.getCode(),
				IstexCollectionReader.PARAM_ID_LIST, Joiner.on(",").join(documentsIds),
				IstexCollectionReader.PARAM_API_URL, apiURL
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
	 * 		This chaining {@link TermSuitePipeline} builder object
	 */
	public TermSuitePipeline setCollection(TermSuiteCollection termSuiteCollection, String collectionPath, String collectionEncoding) {
		Preconditions.checkNotNull(termSuiteCollection);
		Preconditions.checkNotNull(collectionPath);
		Preconditions.checkNotNull(collectionEncoding);
		try {
			switch(termSuiteCollection) {
			case TXT:
				this.crDescription = CollectionReaderFactory.createReaderDescription(
						TxtCollectionReader.class,
						TxtCollectionReader.PARAM_INPUTDIR, collectionPath,
						TxtCollectionReader.PARAM_COLLECTION_TYPE, termSuiteCollection,
						TxtCollectionReader.PARAM_ENCODING, collectionEncoding,
						TxtCollectionReader.PARAM_LANGUAGE, this.lang.getCode()
						);
				break;
			case XMI:
				this.crDescription = CollectionReaderFactory.createReaderDescription(
						XmiCollectionReader.class,
						XmiCollectionReader.PARAM_INPUTDIR, collectionPath,
						XmiCollectionReader.PARAM_COLLECTION_TYPE, termSuiteCollection,
						XmiCollectionReader.PARAM_ENCODING, collectionEncoding,
						XmiCollectionReader.PARAM_LANGUAGE, this.lang.getCode()
						);
				break;
			case JSON:
				this.crDescription = CollectionReaderFactory.createReaderDescription(
						JsonCollectionReader.class,
						JsonCollectionReader.PARAM_INPUTDIR, collectionPath,
						JsonCollectionReader.PARAM_COLLECTION_TYPE, termSuiteCollection,
						JsonCollectionReader.PARAM_ENCODING, collectionEncoding,
						JsonCollectionReader.PARAM_LANGUAGE, this.lang.getCode()
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
	 * 		This chaining {@link TermSuitePipeline} builder object
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
	
	/**
	 * Invoke this method if TermSuite resources are accessible via 
	 * a "file:/path/to/res/" url, i.e. they can be found locally.
	 * 
	 * @param resourceDir
	 * @return
	 */
	public TermSuitePipeline setResourceDir(String resourceDir) {
		Preconditions.checkArgument(new File(resourceDir).isDirectory(), 
				"Not a directory: %s", resourceDir);
		
		if(!resourceDir.endsWith(File.separator))
			resourceDir = resourceDir + File.separator;
//		TermSuiteUtils.addToClasspath(resourceDir);
		try {
			this.resourceUrlPrefix = Optional.of(new URL("file:" + resourceDir));
			LOGGER.info("Resource URL prefix is: {}", this.resourceUrlPrefix.get());
		} catch (MalformedURLException e) {
			throw new TermSuitePipelineException(e);
		}
		return this;
	}
	
	public TermSuitePipeline setResourceJar(String resourceJar) {
		Preconditions.checkArgument(FileUtils.isJar(resourceJar), 
				"Not a jar file: %s", resourceJar);
		try {
			this.resourceUrlPrefix = Optional.of(new URL("jar:file:"+resourceJar+"!/"));
			LOGGER.info("Resource URL prefix is: {}", this.resourceUrlPrefix.get());
		} catch (MalformedURLException e) {
			throw new TermSuitePipelineException(e);
		}
		return this;		
	}

	
	
	private Optional<URL> resourceUrlPrefix = Optional.empty();
	
	
	public TermSuitePipeline setResourceUrlPrefix(String urlPrefix) {
		try {
			this.resourceUrlPrefix = Optional.of(new URL(urlPrefix));
		} catch (MalformedURLException e) {
			throw new TermSuitePipelineException("Bad url: " + urlPrefix, e);
		}
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

	public TermSuitePipeline setHistory(TermHistory history) {
		TermSuiteResourceManager.getInstance().remove(termHistoryResourceName);
		TermSuiteResourceManager.getInstance().register(termHistoryResourceName, history);
		return this;
	}

	public TermSuitePipeline watch(String... termKeys) {
		TermHistory termHistory = (TermHistory)TermSuiteResourceManager.getInstance().get(termHistoryResourceName);
		termHistory.addWatchedTerms(termKeys);
		return this;
	}

	public String getHistoryResourceName() {
		return termHistoryResourceName;
	}
		
	public TermSuitePipeline aeWordTokenizer() {
		try {
			AnalysisEngineDescription ae = AnalysisEngineFactory.createEngineDescription(
					Lexer.class, 
					Lexer.PARAM_TYPE, "fr.univnantes.termsuite.types.WordAnnotation"
				);
			
			ExternalResourceDescription	segmentBank = ExternalResourceFactory.createExternalResourceDescription(
					SegmentBankResource.class,
					getResUrl(TermSuiteResource.SEGMENT_BANK)
				);
			

					
			ExternalResourceFactory.bindResource(
					ae, 
					SegmentBank.KEY_SEGMENT_BANK, 
					segmentBank);

			return aggregateAndReturn(ae, "Word tokenizer", 0);
		} catch (Exception e) {
			throw new TermSuitePipelineException(e);
		}
		
	}

//	private TermSuitePipeline aggregateAndReturn(AnalysisEngineDescription ae) {
//		return aggregateAndReturn(ae, null, 0);
//	}

	private Map<String, MutableInt> taskNumbers = Maps.newHashMap();
	private String getNumberedTaskName(String taskName) {
		if(!taskNumbers.containsKey(taskName))
			taskNumbers.put(taskName, new MutableInt(0));
		taskNumbers.get(taskName).increment();
		return String.format("%s-%d", taskName, taskNumbers.get(taskName).intValue());
	}
	
	private TermSuitePipeline aggregateAndReturn(AnalysisEngineDescription ae, String taskName, int ccWeight) {
		Preconditions.checkNotNull(taskName);

		// Add the pre-task observer
		this.aggregateBuilder.add(aeObserver(taskName, ccWeight, PipelineObserver.TASK_STARTED));
		
		// Add the ae itself
		this.aggregateBuilder.add(ae);
		
		// Add the post-task observer
		this.aggregateBuilder.add(aeObserver(taskName, ccWeight, PipelineObserver.TASK_ENDED));
		return this;
	}


	private AnalysisEngineDescription aeObserver(String taskName, int weight, String hook) {
		try {
			AnalysisEngineDescription ae = AnalysisEngineFactory.createEngineDescription(
					PipelineObserver.class, 
					PipelineObserver.TASK_NAME, taskName,
					PipelineObserver.HOOK, hook,
					PipelineObserver.WEIGHT, weight
				);
			
			ExternalResourceFactory.bindResource(ae, resObserver());

			return ae;
		} catch (Exception e) {
			throw new TermSuitePipelineException(e);
		}
		
	}
	public TermSuitePipeline aeTreeTagger() {
		try {
			AnalysisEngineDescription ae = AnalysisEngineFactory.createEngineDescription(
					TreeTaggerWrapper.class, 
					TreeTaggerWrapper.PARAM_ANNOTATION_TYPE, "fr.univnantes.termsuite.types.WordAnnotation",
					TreeTaggerWrapper.PARAM_TAG_FEATURE, "tag",
					TreeTaggerWrapper.PARAM_LEMMA_FEATURE, "lemma",
					TreeTaggerWrapper.PARAM_UPDATE_ANNOTATION_FEATURES, true,
					TreeTaggerWrapper.PARAM_TT_HOME_DIRECTORY, this.treeTaggerPath.get()
				);
			
			ExternalResourceDescription ttParam = ExternalResourceFactory.createExternalResourceDescription(
					TreeTaggerParameter.class,
					getResUrl(TermSuiteResource.TREETAGGER_CONFIG, Tagger.TREE_TAGGER)
				);
			
			ExternalResourceFactory.bindResource(
					ae,
					TreeTaggerParameter.KEY_TT_PARAMETER, 
					ttParam 
				);

			return aggregateAndReturn(ae, "POS Tagging (TreeTagger)", 0).ttLemmaFixer().ttNormalizer();
		} catch (Exception e) {
			throw new TermSuitePipelineException(e);
		}
	}


	/*
	 * Builds the resource url for this pipeline
	 */
	private URL getResUrl(TermSuiteResource tsResource, Tagger tagger) {
		if(!resourceUrlPrefix.isPresent())
			return tsResource.fromClasspath(lang, tagger);
		else
			return tsResource.fromUrlPrefix(this.resourceUrlPrefix.get(), lang, tagger);		
		
	}


	/*
	 * Builds the resource url for this pipeline	 * 
	 */
	private URL getResUrl(TermSuiteResource tsResource) {
		if(!resourceUrlPrefix.isPresent()) {
			URL fromClasspath = tsResource.fromClasspath(lang);
			return fromClasspath;
		} else {
			URL fromUrlPrefix = tsResource.fromUrlPrefix(this.resourceUrlPrefix.get(), lang);
			return fromUrlPrefix;
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
			String lemmatizerModel = Paths.get(mateModelsPath.get(), "mate-lemma-"+lang.getCode()+".model").toString();
			String taggerModel = Paths.get(mateModelsPath.get(), "mate-pos-"+lang.getCode()+".model").toString();
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
	
			return aggregateAndReturn(ae, "POS Tagging (Mate)", 0)
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
	 * 		This chaining {@link TermSuitePipeline} builder object
	 */
	public TermSuitePipeline setTsvExportProperties(TermProperty... properties) {
		this.tsvExportProperties = Joiner.on(",").join(properties);
		return this;
	}
	
	/**
	 * Exports the {@link Terminology} in tsv format
	 * 
	 * @see #setTsvExportProperties(TermProperty...)
	 * @param toFilePath
	 * @return
	 * 		This chaining {@link TermSuitePipeline} builder object
	 */
	public TermSuitePipeline haeTsvExporter(String toFilePath) {
		try {
			AnalysisEngineDescription ae = AnalysisEngineFactory.createEngineDescription(
					TSVExporterAE.class, 
					TSVExporterAE.TO_FILE_PATH, toFilePath,
					TSVExporterAE.TERM_PROPERTIES, this.tsvExportProperties,
					TSVExporterAE.SHOW_HEADERS, tsvWithHeaders,
					TSVExporterAE.SHOW_SCORES, tsvWithVariantScores
				);
			ExternalResourceFactory.bindResource(ae, resTermino());


			return aggregateAndReturn(ae, getNumberedTaskName("Exporting the terminology to " + toFilePath), 1);
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
	 * @return
	 * 		This chaining {@link TermSuitePipeline} builder object
	 */
	public TermSuitePipeline haeExportVariationRuleExamples(String toFilePath) {
		try {
			AnalysisEngineDescription ae = AnalysisEngineFactory.createEngineDescription(
					ExportVariationRuleExamplesAE.class, ExportVariationRuleExamplesAE.TO_FILE_PATH, toFilePath);
			ExternalResourceFactory.bindResource(ae, resTermino());
			ExternalResourceFactory.bindResource(ae, resSyntacticVariantRules());

			return aggregateAndReturn(ae, "Exporting variation rules examples", 0);
		} catch (Exception e) {
			throw new TermSuitePipelineException(e);
		}
	}
	
	/**
	 * 
	 * Exports all compound words of the terminology to given file path.
	 * 
	 * @param toFilePath
	 * @return
	 * 		This chaining {@link TermSuitePipeline} builder object
	 */
	public TermSuitePipeline haeCompoundExporter(String toFilePath) {
		try {
			AnalysisEngineDescription ae = AnalysisEngineFactory.createEngineDescription(
					CompoundExporterAE.class, 
					CompoundExporterAE.TO_FILE_PATH, 
					toFilePath);
			ExternalResourceFactory.bindResource(ae, resTermino());

			return aggregateAndReturn(ae, "Exporting compounds", 0);
		} catch (Exception e) {
			throw new TermSuitePipelineException(e);
		}
	}

	public TermSuitePipeline haeVariationExporter(String toFilePath, RelationType... vTypes) {
		try {
			String typeStrings = Joiner.on(",").join(vTypes);
			AnalysisEngineDescription ae = AnalysisEngineFactory.createEngineDescription(
					VariationExporterAE.class, 
					VariationExporterAE.TO_FILE_PATH, toFilePath,
					VariationExporterAE.VARIATION_TYPES, typeStrings 
					);
			ExternalResourceFactory.bindResource(ae, resTermino());

			String taskName = "Exporting variations " + typeStrings + " to file " + toFilePath;
			return aggregateAndReturn(ae, taskName, 0);
		} catch (Exception e) {
			throw new TermSuitePipelineException(e);
		}
	}
		
	public TermSuitePipeline haeTbxExporter(String toFilePath) {
		try {
			AnalysisEngineDescription ae = AnalysisEngineFactory.createEngineDescription(
					TbxExporterAE.class, 
					TbxExporterAE.TO_FILE_PATH, toFilePath
				);
			ExternalResourceFactory.bindResource(ae, resTermino());

			return aggregateAndReturn(ae, getNumberedTaskName("Exporting the terminology to " + toFilePath), 1);
		} catch (Exception e) {
			throw new TermSuitePipelineException(e);
		}
	}

	public TermSuitePipeline haeEvalExporter(String toFilePath, boolean withVariants) {
		try {
			AnalysisEngineDescription ae = AnalysisEngineFactory.createEngineDescription(
					EvalExporterAE.class, 
					EvalExporterAE.TO_FILE_PATH, toFilePath,
					EvalExporterAE.WITH_VARIANTS, withVariants
					
				);
			ExternalResourceFactory.bindResource(ae, resTermino());

			return aggregateAndReturn(ae, "Exporting evaluation files", 0);
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
					JsonExporterAE.class, 
					JsonExporterAE.TO_FILE_PATH, toFilePath,
					JsonExporterAE.WITH_OCCURRENCE, exportJsonWithOccurrences,
					JsonExporterAE.WITH_CONTEXTS, exportJsonWithContext,
					JsonExporterAE.LINKED_MONGO_STORE, this.linkMongoStore
				);
			ExternalResourceFactory.bindResource(ae, resTermino());

			return aggregateAndReturn(ae, getNumberedTaskName("Exporting the terminology to " + toFilePath), 1);
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
	 * 		This chaining {@link TermSuitePipeline} builder object
	 */
	public TermSuitePipeline haeVariantEvalExporter(String toFilePath, int topN, int maxVariantsPerTerm)  {
		try {
			AnalysisEngineDescription ae = AnalysisEngineFactory.createEngineDescription(
					VariantEvalExporterAE.class, 
					VariantEvalExporterAE.TO_FILE_PATH, toFilePath,
					VariantEvalExporterAE.TOP_N, topN,
					VariantEvalExporterAE.NB_VARIANTS_PER_TERM, maxVariantsPerTerm
				);
			
			ExternalResourceFactory.bindResource(ae, resTermino());

			return aggregateAndReturn(ae, "Exporting variant evaluation files", 0);
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

	private TermSuitePipeline subNormalizer(String target, URL mappingFile)  {
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

			return aggregateAndReturn(ae, "Normalizing " + mappingFile, 0);
		} catch (Exception e) {
			throw new TermSuitePipelineException(e);
		}
	}

	private TermSuitePipeline caseNormalizer(Tagger tagger)  {
		return subNormalizer(
				"fr.univnantes.termsuite.types.WordAnnotation:case", 
				getResUrl(TermSuiteResource.TAGGER_CASE_MAPPING, tagger));
	}

	private TermSuitePipeline categoryNormalizer(Tagger tagger)  {
		return subNormalizer(
				"fr.univnantes.termsuite.types.WordAnnotation:category", 
				getResUrl(TermSuiteResource.TAGGER_CATEGORY_MAPPING, tagger));
	}

	private TermSuitePipeline tenseNormalizer(Tagger tagger)  {
		return subNormalizer(
				"fr.univnantes.termsuite.types.WordAnnotation:tense", 
				getResUrl(TermSuiteResource.TAGGER_TENSE_MAPPING, tagger));
	}

	private TermSuitePipeline subCategoryNormalizer(Tagger tagger)  {
		return subNormalizer(
				"fr.univnantes.termsuite.types.WordAnnotation:subCategory", 
				getResUrl(TermSuiteResource.TAGGER_SUBCATEGORY_MAPPING, tagger));
	}

	
	private TermSuitePipeline moodNormalizer(Tagger tagger)  {
		return subNormalizer(
				"fr.univnantes.termsuite.types.WordAnnotation:mood", 
				getResUrl(TermSuiteResource.TAGGER_MOOD_MAPPING, tagger));
	}

	
	private TermSuitePipeline numberNormalizer(Tagger tagger)  {
		return subNormalizer(
				"fr.univnantes.termsuite.types.WordAnnotation:number", 
				getResUrl(TermSuiteResource.TAGGER_NUMBER_MAPPING, tagger));
	}

	
	private TermSuitePipeline genderNormalizer(Tagger tagger)  {
		return subNormalizer(
				"fr.univnantes.termsuite.types.WordAnnotation:gender", 
				getResUrl(TermSuiteResource.TAGGER_GENDER_MAPPING, tagger));
	}

	private TermSuitePipeline mateNormalizer()  {
		return normalizer(Tagger.MATE);
	}

	private TermSuitePipeline ttNormalizer()  {
		return normalizer(Tagger.TREE_TAGGER);
	}

	private TermSuitePipeline normalizer(Tagger tagger)  {
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
					Stemmer.PARAM_FEATURE, "fr.univnantes.termsuite.types.WordAnnotation:stem",
					Stemmer.PARAM_LANGUAGE, lang,
					Stemmer.PARAM_UPDATE, true
				);

			return aggregateAndReturn(ae, "Stemming", 0);
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
			

			return aggregateAndReturn(ae, "Fixing lemmas", 0);
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

			return aggregateAndReturn(ae, "Fixing lemmas", 0);
		} catch (Exception e) {
			throw new TermSuitePipelineException(e);
		}
	}

	/**
	 * Iterates over the {@link Terminology} and mark terms as
	 * "fixed expressions" when their lemmas are found in the 
	 * {@link FixedExpressionResource}.
	 * 
	 * @return
	 * 		This chaining {@link TermSuitePipeline} builder object
	 */
	public TermSuitePipeline aeFixedExpressionTermMarker()  {
		/*
		 * TODO Check if resource is present for that current language.
		 */
		try {
			AnalysisEngineDescription ae = AnalysisEngineFactory.createEngineDescription(
					FixedExpressionTermMarker.class
				);
			
			ExternalResourceDescription fixedExprRes = ExternalResourceFactory.createExternalResourceDescription(
					FixedExpressionResource.class, 
					getResUrl(TermSuiteResource.FIXED_EXPRESSIONS));
			
			ExternalResourceFactory.bindResource(
					ae,
					FixedExpressionResource.FIXED_EXPRESSION_RESOURCE, 
					fixedExprRes
				);
			

			ExternalResourceFactory.bindResource(ae, resTermino());

			return aggregateAndReturn(ae, "Marking fixed expression terms", 0);
		} catch (Exception e) {
			throw new TermSuitePipelineException(e);
		}
	}
	
	/**
	 * Spots fixed expressions in the CAS an creates {@link FixedExpression}
	 * annotation whenever one is found.
	 * 
	 * @return
	 * 		This chaining {@link TermSuitePipeline} builder object
	 */
	public TermSuitePipeline aeFixedExpressionSpotter()  {
		/*
		 * TODO Check if resource is present for that current language.
		 */
		try {
			AnalysisEngineDescription ae = AnalysisEngineFactory.createEngineDescription(
					FixedExpressionSpotter.class,
					FixedExpressionSpotter.FIXED_EXPRESSION_MAX_SIZE, 5,
					FixedExpressionSpotter.REMOVE_WORD_ANNOTATIONS_FROM_CAS, false,
					FixedExpressionSpotter.REMOVE_TERM_OCC_ANNOTATIONS_FROM_CAS, true
				);
			
			

			ExternalResourceDescription fixedExprRes = ExternalResourceFactory.createExternalResourceDescription(
					FixedExpressionResource.class, 
					getResUrl(TermSuiteResource.FIXED_EXPRESSIONS));
			
			ExternalResourceFactory.bindResource(
					ae,
					FixedExpressionResource.FIXED_EXPRESSION_RESOURCE, 
					fixedExprRes
				);
			
			return aggregateAndReturn(ae, "Spotting fixed expressions", 0);
		} catch (Exception e) {
			throw new TermSuitePipelineException(e);
		}
	}
	
	/**
	 * The single-word and multi-word term spotter AE
	 * base on UIMA Tokens Regex.
	 * 
	 * @return
	 * 		This chaining {@link TermSuitePipeline} builder object
	 */
	public TermSuitePipeline aeRegexSpotter()  {
		try {
			Serializable postProcStrategy = this.postProcessingStrategy.isPresent() ? this.postProcessingStrategy.get() : lang.getRegexPostProcessingStrategy();
			AnalysisEngineDescription ae = AnalysisEngineFactory.createEngineDescription(
					RegexSpotter.class,
					TokenRegexAE.PARAM_ALLOW_OVERLAPPING_OCCURRENCES, true,
					RegexSpotter.POST_PROCESSING_STRATEGY, postProcStrategy
				);
			
			if(enableSyntacticLabels)
				addParameters(
						ae, 
						TokenRegexAE.PARAM_SET_LABELS, "labels");
			
			if(logOverlappingRules.isPresent())
				addParameters(
						ae, 
						RegexSpotter.LOG_OVERLAPPING_RULES, logOverlappingRules.get());
			
			
			ExternalResourceDescription mwtRules = ExternalResourceFactory.createExternalResourceDescription(
					RegexListResource.class, 
					getResUrl(TermSuiteResource.MWT_RULES));
			
			ExternalResourceFactory.bindResource(
					ae,
					RegexListResource.KEY_TOKEN_REGEX_RULES, 
					mwtRules
				);

			ExternalResourceFactory.bindResource(
					ae, resHistory());

	
			ExternalResourceDescription allowedCharsRes = ExternalResourceFactory.createExternalResourceDescription(
					CharacterFootprintTermFilter.class, 
					getResUrl(TermSuiteResource.ALLOWED_CHARS));
			
			ExternalResourceFactory.bindResource(
					ae,
					RegexSpotter.CHARACTER_FOOTPRINT_TERM_FILTER, 
					allowedCharsRes
				);

			if(this.addSpottedAnnoToTermino)
				ExternalResourceFactory.bindResource(ae, resTermino());

			ExternalResourceDescription stopWordsRes = ExternalResourceFactory.createExternalResourceDescription(
					DefaultFilterResource.class, 
					getResUrl(TermSuiteResource.STOP_WORDS_FILTER));
			
			ExternalResourceFactory.bindResource(
					ae,
					RegexSpotter.STOP_WORD_FILTER, 
					stopWordsRes
				);

			TermSuitePipeline aggAE = aggregateAndReturn(ae, "Spotting terms", 0);
			if(this.addSpottedAnnoToTermino)
				aggAE.aeTermOccAnnotationImporter();
			return aggAE;
		} catch (Exception e) {
			throw new TermSuitePipelineException(e);
		}
	}
	
	
	/**
	 * An AE thats imports all {@link TermOccAnnotation} in CAS to a {@link Terminology}.
	 * 
	 * @return
	 * 		This chaining {@link TermSuitePipeline} builder object
	 */
	public TermSuitePipeline aeTermOccAnnotationImporter()  {
		try {
			AnalysisEngineDescription ae = AnalysisEngineFactory.createEngineDescription(
					TermOccAnnotationImporter.class,
					TermOccAnnotationImporter.KEEP_OCCURRENCES_IN_TERMINOLOGY, spotWithOccurrences
				);
			ExternalResourceFactory.bindResource(ae, resTermino());
			ExternalResourceFactory.bindResource(ae, resHistory());

			return aggregateAndReturn(ae, "TermOccAnnotation importer", 0)
						.aePilotSetter()
						.aeDocumentFrequencySetter()
						.aeSWTSizeSetter()
						;
		} catch (Exception e) {
			throw new TermSuitePipelineException(e);
		}
	}

	private TermSuitePipeline aePilotSetter()  {
		try {
			AnalysisEngineDescription ae = AnalysisEngineFactory.createEngineDescription(
					PilotSetterAE.class
				);
			ExternalResourceFactory.bindResource(ae, resTermino());

			return aggregateAndReturn(ae, PilotSetterAE.TASK_NAME, 0);
		} catch (Exception e) {
			throw new TermSuitePipelineException(e);
		}		
	}
	
	private TermSuitePipeline aeDocumentFrequencySetter()  {
		try {
			AnalysisEngineDescription ae = AnalysisEngineFactory.createEngineDescription(
					DocumentFrequencySetterAE.class
				);
			ExternalResourceFactory.bindResource(ae, resTermino());

			return aggregateAndReturn(ae, DocumentFrequencySetterAE.TASK_NAME, 0);
		} catch (Exception e) {
			throw new TermSuitePipelineException(e);
		}		
	}

	private TermSuitePipeline aeSWTSizeSetter()  {
		try {
			AnalysisEngineDescription ae = AnalysisEngineFactory.createEngineDescription(
					SWTSizeSetterAE.class
				);
			ExternalResourceFactory.bindResource(ae, resTermino());

			return aggregateAndReturn(ae, SWTSizeSetterAE.TASK_NAME, 0);
		} catch (Exception e) {
			throw new TermSuitePipelineException(e);
		}		
	}

	
	/**
	 * Removes from the term index any term having a 
	 * stop word at its boundaries.
	 * 
	 * @see TerminologyBlacklistWordFilterAE
	 * @return
	 * 		This chaining {@link TermSuitePipeline} builder object
	 */
	public TermSuitePipeline aeStopWordsFilter()  {
		try {
			AnalysisEngineDescription ae = AnalysisEngineFactory.createEngineDescription(
					TerminologyBlacklistWordFilterAE.class
				);
			
			ExternalResourceDescription stopWordsFilterResourceRes = ExternalResourceFactory.createExternalResourceDescription(
					DefaultFilterResource.class, 
					getResUrl(TermSuiteResource.STOP_WORDS_FILTER));
			
			ExternalResourceFactory.bindResource(
					ae,
					FilterResource.KEY_FILTERS, 
					stopWordsFilterResourceRes
				);

			ExternalResourceFactory.bindResource(ae, resTermino());

			return aggregateAndReturn(ae, "Filtering stop words", 0);
		} catch(Exception e) {
			throw new TermSuitePipelineException(e);
		}
	}

	


	
	/**
	 * Exports all CAS as XMI files to a given directory.
	 * 
	 * @param toDirectoryPath
	 * @return
	 * 		This chaining {@link TermSuitePipeline} builder object
	 */
	public TermSuitePipeline haeXmiCasExporter(String toDirectoryPath)  {
		try {
			AnalysisEngineDescription ae = AnalysisEngineFactory.createEngineDescription(
					XmiCasExporter.class,
					XmiCasExporter.OUTPUT_DIRECTORY, toDirectoryPath
				);

			return aggregateAndReturn(ae, "Exporting XMI Cas files", 0);
		} catch(Exception e) {
			throw new TermSuitePipelineException(e);
		}
	}

	/**
	 * Exports all CAS as JSON files to a given directory.
	 *
	 * @param toDirectoryPath
	 * @return
	 * 		This chaining {@link TermSuitePipeline} builder object
	 */
	public TermSuitePipeline haeTermsuiteJsonCasExporter(String toDirectoryPath)  {
		try {
			AnalysisEngineDescription ae = AnalysisEngineFactory.createEngineDescription(
					TermsuiteJsonCasExporter.class,
					TermsuiteJsonCasExporter.OUTPUT_DIRECTORY, toDirectoryPath
			);

			return aggregateAndReturn(ae, "Exporting Json Cas files", 0);
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
	 * 		This chaining {@link TermSuitePipeline} builder object
	 */
	public TermSuitePipeline haeSpotterTSVWriter(String toDirectoryPath)  {
		try {
			AnalysisEngineDescription ae = AnalysisEngineFactory.createEngineDescription(
					SpotterTSVWriter.class,
					XmiCasExporter.OUTPUT_DIRECTORY, toDirectoryPath
				);

			return aggregateAndReturn(ae, "Exporting annotations in TSV to " + toDirectoryPath, 0);
		} catch(Exception e) {
			throw new TermSuitePipelineException(e);
		}
	}

	public TermSuitePipeline aeDocumentLogger(long nbDocument)  {
		
		try {
			AnalysisEngineDescription ae = AnalysisEngineFactory.createEngineDescription(
					DocumentLogger.class,
					DocumentLogger.NB_DOCUMENTS, nbDocument
				);

			return aggregateAndReturn(ae, "Document logging", 0);
		} catch(Exception e) {
			throw new TermSuitePipelineException(e);
		}
	}

	/**
	 * Tokenizer for chinese collections.
	 * @see ChineseSegmenter
	 * 
	 * @return
	 * 		This chaining {@link TermSuitePipeline} builder object
	 */
	public TermSuitePipeline aeChineseTokenizer()  {
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

			return aggregateAndReturn(ae, "Word tokenizing", 0);
		} catch(Exception e) {
			throw new TermSuitePipelineException(e);
		}
	}

	private ExternalResourceDescription termSynonymDesc;
	public ExternalResourceDescription resSynonyms() {
		if(termSynonymDesc == null) {
			termSynonymDesc = ExternalResourceFactory.createExternalResourceDescription(
					MultimapFlatResource.class, 
					getResUrl(TermSuiteResource.SYNONYMS));
		}
		return termSynonymDesc;
	}

	private ExternalResourceDescription terminoResourceDesc;
	public ExternalResourceDescription resTermino() {
		if(terminoResourceDesc == null) {
			if(!termino.isPresent())
				emptyTermino(UUID.randomUUID().toString());
			
			terminoResourceDesc = ExternalResourceFactory.createExternalResourceDescription(
					TerminologyResource.class, 
					termino.get().getName());
			
			TermSuiteResourceManager manager = TermSuiteResourceManager.getInstance();
			
			// register the term index if not in term index manager
			if(!manager.contains(termino.get().getName()))
				manager.register(termino.get().getName(), termino.get());
		}
		return terminoResourceDesc;
		
	}
	
	private ExternalResourceDescription pipelineObserverResource;
	public ExternalResourceDescription resObserver() {
		if(pipelineObserverResource == null) {
			pipelineObserverResource = ExternalResourceFactory.createExternalResourceDescription(
					ObserverResource.class, this.pipelineObserverName);
		}
		return pipelineObserverResource;

	}
	
	private ExternalResourceDescription termHistoryResource;
	public ExternalResourceDescription resHistory() {
		if(termHistoryResource == null) {
			termHistoryResource = ExternalResourceFactory.createExternalResourceDescription(
					TermHistoryResource.class, this.termHistoryResourceName);
		}
		return termHistoryResource;

	}

	
	private ExternalResourceDescription syntacticVariantRules;
	public ExternalResourceDescription resSyntacticVariantRules() {
		if(syntacticVariantRules == null) {
			syntacticVariantRules = ExternalResourceFactory.createExternalResourceDescription(
					YamlRuleSetResource.class, 
					getResUrl(TermSuiteResource.VARIANTS)
				);
		}
		return syntacticVariantRules;

	}


	/**
	 * Returns the term index produced (or last modified) by this pipeline.
	 * @return
	 * 		The term index processed by this pipeline
	 */
	public Terminology getTerminology() {
		return this.termino.get();
	}
	
	/**
	 * Sets the term index on which this pipeline will run.
	 * 
	 * @param termino
	 * @return
	 * 		This chaining {@link TermSuitePipeline} builder object
	 */
	public TermSuitePipeline setTermino(Terminology termino) {
		this.termino = Optional.of(termino);
		return this;
	}
	
	/**
	 * Creates a new in-memory {@link Terminology} on which this 
	 * piepline with run.
	 * 
	 * @param name
	 * 			the name of the new term index
	 * @return
	 * 		This chaining {@link TermSuitePipeline} builder object
	 */
	public TermSuitePipeline emptyTermino(String name) {
		MemoryTerminology termino = new MemoryTerminology(name, this.lang, this.occurrenceStore);
		LOGGER.info("Creating Terminology {}", termino.getName());
		this.termino = Optional.of(termino);
		return this;
	}

	
	
	private ExternalResourceDescription generalLanguageResourceDesc;
	private ExternalResourceDescription resGeneralLanguage() {
		if(generalLanguageResourceDesc == null)
			generalLanguageResourceDesc = ExternalResourceFactory.createExternalResourceDescription(
					GeneralLanguageResource.class, 
					getResUrl(TermSuiteResource.GENERAL_LANGUAGE));
		return generalLanguageResourceDesc;
	}
	
	/**
	 * Computes {@link TermProperty#WR} values (and additional 
	 * term properties of type {@link TermProperty} in the future).
	 * 
	 * @see TermSpecificityComputer
	 * @see TermProperty
	 * @return
	 * 		This chaining {@link TermSuitePipeline} builder object
	 */
	public TermSuitePipeline aeSpecificityComputer()  {
		try {
			AnalysisEngineDescription ae = AnalysisEngineFactory.createEngineDescription(
					TermSpecificityComputer.class
				);
			ExternalResourceFactory.bindResource(ae, resGeneralLanguage());
			ExternalResourceFactory.bindResource(ae, resTermino());
			ExternalResourceFactory.bindResource(ae, resHistory());

			return aggregateAndReturn(ae, "Computing term specificities", 0);
		} catch(Exception e) {
			throw new TermSuitePipelineException(e);
		}
	}

	/**
	 * Computes the {@link Contextualizer} vector of all 
	 * single-word terms in the term index.
	 * 
	 * @see Contextualizer
	 * @param scope
	 * @param allTerms
	 * @return
	 * 		This chaining {@link TermSuitePipeline} builder object
	 */
	public TermSuitePipeline aeContextualizer(ContextualizerOptions options) {
		AnalysisEngineDescription ae;
		try {
			ae = AnalysisEngineFactory.createEngineDescription(
					ContextualizerAE.class,
					ContextualizerAE.SCOPE, options.getScope(),
					ContextualizerAE.CO_TERMS_TYPE, options.getCoTermType(),
					ContextualizerAE.ASSOCIATION_RATE, options.getAssociationRate().getName(),
					ContextualizerAE.MINIMUM_COOCC_FREQUENCY_THRESHOLD, options.getMinimumCooccFrequencyThreshold()
				);
			ExternalResourceFactory.bindResource(ae, resTermino());

			return aggregateAndReturn(ae, "Build context vectors", 1);
		} catch (Exception e) {
			throw new TermSuitePipelineException(e);
		}
	}
	
	public TermSuitePipeline aeMaxSizeThresholdCleaner(TermProperty property, int maxSize) {
		try {
			AnalysisEngineDescription ae = AnalysisEngineFactory.createEngineDescription(
				MaxSizeThresholdCleaner.class,
				AbstractTerminologyCleaner.CLEANING_PROPERTY, property,	
				MaxSizeThresholdCleaner.MAX_SIZE, maxSize
			);
			ExternalResourceFactory.bindResource(ae, resTermino());

			return aggregateAndReturn(ae, "Cleaning Terminology on property "+property.toString().toLowerCase()+" with maxSize=" + maxSize, 0);
		} catch(Exception e) {
			throw new TermSuitePipelineException(e);
		}
		
	}

	
	public TermSuitePipeline aeThresholdCleaner(TermProperty property, double threshold, boolean isPeriodic, int cleaningPeriod, int terminoSizeTrigger) {
		try {
			AnalysisEngineDescription ae = AnalysisEngineFactory.createEngineDescription(
				TerminologyThresholdCleaner.class,
				AbstractTerminologyCleaner.CLEANING_PROPERTY, property,
				AbstractTerminologyCleaner.NUM_TERMS_CLEANING_TRIGGER, terminoSizeTrigger,
				AbstractTerminologyCleaner.KEEP_VARIANTS, this.keepVariantsWhileCleaning,
				TerminologyThresholdCleaner.THRESHOLD, (float)threshold
			);
			setPeriodic(isPeriodic, cleaningPeriod, ae);
			
			ExternalResourceFactory.bindResource(ae, resTermino());
			ExternalResourceFactory.bindResource(ae, resHistory());

			return aggregateAndReturn(ae, getNumberedTaskName("Cleaning"), 0);
		} catch(Exception e) {
			throw new TermSuitePipelineException(e);
		}
	}
	
	private void setPeriodic(boolean isPeriodic, int cleaningPeriod,
			AnalysisEngineDescription ae) {
		if(isPeriodic)
			addParameters(ae, 
					AbstractTerminologyCleaner.PERIODIC_CAS_CLEAN_ON, true,
					AbstractTerminologyCleaner.CLEANING_PERIOD, cleaningPeriod
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
	 * 		This chaining {@link TermSuitePipeline} builder object
	 */
	public TermSuitePipeline aeThresholdCleanerPeriodic(TermProperty property, double threshold, int cleaningPeriod)   {
		return aeThresholdCleaner(property, threshold, true, cleaningPeriod, 0);
	}

	public TermSuitePipeline aeThresholdCleanerSizeTrigger(TermProperty property, double threshold, int terminoSizeTrigger)   {
		return aeThresholdCleaner(property, threshold, false, 0, terminoSizeTrigger);
	}

	
	public TermSuitePipeline setKeepVariantsWhileCleaning(boolean keepVariantsWhileCleaning) {
		this.keepVariantsWhileCleaning = keepVariantsWhileCleaning;
		return this;
	}
	
	public TermSuitePipeline aeThresholdCleaner(TermProperty property, double threshold) {
		return aeThresholdCleaner(property, threshold, false, 0, 0);
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
	 * 		This chaining {@link TermSuitePipeline} builder object
	 */
	public TermSuitePipeline aeTopNCleanerPeriodic(TermProperty property, int n, boolean isPeriodic, int cleaningPeriod)  {
		try {
			AnalysisEngineDescription ae = AnalysisEngineFactory.createEngineDescription(
					TerminologyTopNCleaner.class,
					AbstractTerminologyCleaner.CLEANING_PROPERTY, property,
					TerminologyTopNCleaner.TOP_N, n
					);
			setPeriodic(isPeriodic, cleaningPeriod, ae);
			ExternalResourceFactory.bindResource(ae, resTermino());
			ExternalResourceFactory.bindResource(ae, resHistory());

			return aggregateAndReturn(ae, "Cleaning termino. Keepings only top " + n + " terms on property " + property.toString().toLowerCase(), 0);
		} catch(Exception e) {
			throw new TermSuitePipelineException(e);
		}
	}

	public TermSuitePipeline setGraphicalVariantSimilarityThreshold(double th) {
		this.graphicalVariantSimilarityThreshold = Optional.of(th);
		return this;
	}
	
	/**
	 * Filters out URLs from CAS.
	 * 
	 * @return
	 * 		This chaining {@link TermSuitePipeline} builder object
	 */
	public TermSuitePipeline aeUrlFilter()   {
		try {
			AnalysisEngineDescription ae = AnalysisEngineFactory.createEngineDescription(
					StringRegexFilter.class
				);

			return aggregateAndReturn(ae, "Filtering URLs", 0);
		} catch(Exception e) {
			throw new TermSuitePipelineException(e);
		}
	}
	
	/**
	 * Gathers terms according to their syntactic structures.
	 * 
	 * @return
	 * 		This chaining {@link TermSuitePipeline} builder object
	 */
	public TermSuitePipeline aeTermVariantGatherer(boolean semanticAlignmentEnabled)   {
		try {
			float th = graphicalVariantSimilarityThreshold.isPresent() ? 
					(float)graphicalVariantSimilarityThreshold.get().doubleValue() 
						: 0.9f;

			AnalysisEngineDescription ae = AnalysisEngineFactory.createEngineDescription(
					TermGathererAE.class,
					TermGathererAE.SEMANTIC_ALIGNER_ENABLED, semanticAlignmentEnabled,
					TermGathererAE.LANG, lang.getCode(),
					TermGathererAE.SIMILARITY_THRESHOLD, th
				);
			
			ExternalResourceFactory.bindResource(ae, resSyntacticVariantRules());
			ExternalResourceFactory.bindResource(ae, resTermino());
			if(TermSuiteResource.SYNONYMS.exists(this.lang))
				ExternalResourceFactory.bindResource(ae, resSynonyms());
			ExternalResourceFactory.bindResource(ae, resObserver());
			ExternalResourceFactory.bindResource(ae, resHistory());

			return aggregateAndReturn(ae, TermGathererAE.TASK_NAME, 1);
		} catch(Exception e) {
			throw new TermSuitePipelineException(e);
		}
	}
	
	
	/**
	 * Detects all inclusion/extension relation between terms that have size >= 2.
	 * 
	 * @return
	 * 		This chaining {@link TermSuitePipeline} builder object
	 */
	public TermSuitePipeline aeExtensionDetector()   {
		try {
			AnalysisEngineDescription ae = AnalysisEngineFactory.createEngineDescription(
					ExtensionDetecterAE.class
				);
			
			ExternalResourceFactory.bindResource(ae, resTermino());
			ExternalResourceFactory.bindResource(ae, resHistory());

			return aggregateAndReturn(ae, "Detecting term extensions", 1);
		} catch(Exception e) {
			throw new TermSuitePipelineException(e);
		}
	}
	
	/**
	 * Invokes {@link ExtensionVariantGatherer} on current term index.
	 * 
	 * @return
	 * 		This chaining {@link TermSuitePipeline} builder object
	 * 
	 * @see ExtensionVariantGatherer
	 */
	public TermSuitePipeline aeExtensionVariantGatherer() {
		try {
			AnalysisEngineDescription ae = AnalysisEngineFactory.createEngineDescription(
					ExtensionVariantGathererAE.class
				);
			
			ExternalResourceFactory.bindResource(ae, resTermino());
			ExternalResourceFactory.bindResource(ae, resHistory());
			return aggregateAndReturn(ae, "Infering variations on term extensions", 1);
		} catch(Exception e) {
			throw new TermSuitePipelineException(e);
		}

	}

	/**
	 * Transforms the {@link Terminology} into a flat one-n scored model.
	 * 
	 * 
	 * @return
	 * 		This chaining {@link TermSuitePipeline} builder object
	 */
	public TermSuitePipeline aeScorer(ScorerConfig scorerConfig)   {
		try {
			AnalysisEngineDescription ae = AnalysisEngineFactory.createEngineDescription(
					PostProcessorAE.class					
				);
			
			ExternalResourceFactory.bindResource(ae, resTermino());
			ExternalResourceFactory.bindResource(ae, resObserver());
			ExternalResourceFactory.bindResource(ae, resHistory());
			
			TermSuiteResourceManager.getInstance().register("ScorerConfigResourceURI", scorerConfig);
			ExternalResourceDescription scorerConfigDescription = ExternalResourceFactory.createExternalResourceDescription(
					TermSuiteMemoryUIMAResource.class,
					"ScorerConfigResourceURI"
				);
			ExternalResourceFactory.bindResource(
					ae,
					PostProcessorAE.SCORER_CONFIG, 
					scorerConfigDescription 
				);


			return aggregateAndReturn(ae, PostProcessorAE.TASK_NAME, 1);
		} catch(Exception e) {
			throw new TermSuitePipelineException(e);
		}
	}

	/**
	 *  Merges the variants (only those who are extensions of the base term) 
	 *  of a terms by graphical variation.
	 *  
	 * @return
	 * 		This chaining {@link TermSuitePipeline} builder object
	 */
	public TermSuitePipeline aeMerger()   {
		try {
			AnalysisEngineDescription ae = AnalysisEngineFactory.createEngineDescription(
					MergerAE.class
				);
			
			ExternalResourceFactory.bindResource(ae, resTermino());
			ExternalResourceFactory.bindResource(ae, resObserver());

			return aggregateAndReturn(ae, MergerAE.TASK_NAME, 1);
		} catch(Exception e) {
			throw new TermSuitePipelineException(e);
		}
	}

	
	/**
	 * 
	 * Sets the {@link Term#setRank(int)} of all terms of the {@link Terminology}
	 * given a {@link TermProperty}.
	 * 
	 * @param property
	 * @param desc
	 * @return
	 */
	public TermSuitePipeline aeRanker(TermProperty property, boolean desc)   {
		Preconditions.checkArgument(property != TermProperty.RANK, "Cannot rank on property %s", TermProperty.RANK);
		try {
			AnalysisEngineDescription ae = AnalysisEngineFactory.createEngineDescription(
					Ranker.class,
					Ranker.RANKING_PROPERTY, property,	
					Ranker.DESC, desc
				);
				ExternalResourceFactory.bindResource(ae, resTermino());
				ExternalResourceFactory.bindResource(ae, resObserver());
				ExternalResourceFactory.bindResource(ae, resHistory());


			return aggregateAndReturn(ae, Ranker.TASK_NAME, 1);
		} catch(Exception e) {
			throw new TermSuitePipelineException(e);
		}
	}
	
	public TermSuitePipeline setTreeTaggerHome(String treeTaggerPath) {
		this.treeTaggerPath = Optional.of(treeTaggerPath);
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
	
	public TermSuitePipeline setCompostCoeffs(double alpha, double beta, double gamma, double delta) {
		Preconditions.checkArgument(alpha + beta + gamma + delta == 1.0f, "The sum of coeff must be 1.0");
		this.alpha = Optional.of(alpha);
		this.beta = Optional.of(beta);
		this.gamma = Optional.of(gamma);
		this.delta = Optional.of(delta);
		return this;
	}
	
	public TermSuitePipeline setCompostMaxComponentNum(int compostMaxComponentNum) {
		this.compostMaxComponentNum = Optional.of(compostMaxComponentNum);
		return this;
	}
	
	public TermSuitePipeline setCompostMinComponentSize(int compostMinComponentSize) {
		this.compostMinComponentSize = Optional.of(compostMinComponentSize);
		return this;
	}
	
	public TermSuitePipeline setCompostScoreThreshold(double compostScoreThreshold) {
		this.compostScoreThreshold = Optional.of(compostScoreThreshold);
		return this;
	}
	
	public TermSuitePipeline setCompostSegmentSimilarityThreshold(
			double compostSegmentSimilarityThreshold) {
		this.compostSegmentSimilarityThreshold = Optional.of(compostSegmentSimilarityThreshold);
		return this;
	}
	
	public TermSuitePipeline aeMorphologicalAnalyzer()  {
		try {
			AnalysisEngineDescription ae = AnalysisEngineFactory.createEngineDescription(
					MorphologicalAnalyzerAE.class,
					MorphologicalAnalyzerAE.SCORE_THRESHOLD, (float) (this.compostScoreThreshold.isPresent() ? this.compostScoreThreshold.get() : this.lang.getCompostScoreThreshold()),
					MorphologicalAnalyzerAE.ALPHA, (float) (alpha.isPresent() ? alpha.get() : lang.getCompostAlpha()),
					MorphologicalAnalyzerAE.BETA, (float) (beta.isPresent() ? beta.get() : lang.getCompostBeta()),
					MorphologicalAnalyzerAE.GAMMA, (float) (gamma.isPresent() ? gamma.get() : lang.getCompostGamma()),
					MorphologicalAnalyzerAE.DELTA, (float) (delta.isPresent() ? delta.get() : lang.getCompostDelta()),
					MorphologicalAnalyzerAE.MIN_COMPONENT_SIZE, this.compostMinComponentSize.isPresent() ? this.compostMinComponentSize.get() : this.lang.getCompostMinComponentSize(),
					MorphologicalAnalyzerAE.MAX_NUMBER_OF_COMPONENTS, this.compostMaxComponentNum.isPresent() ? this.compostMaxComponentNum.get() : this.lang.getCompostMaxComponentNumber(),
					MorphologicalAnalyzerAE.SEGMENT_SIMILARITY_THRESHOLD, (float) this.compostSegmentSimilarityThreshold.get().doubleValue()
				);
			ExternalResourceFactory.bindResource(ae, resTermino());
			ExternalResourceFactory.bindResource(ae, resObserver());

			
			ExternalResourceDescription prefixExceptionsRes = ExternalResourceFactory.createExternalResourceDescription(
					ManualSegmentationResource.class,
					getResUrl(TermSuiteResource.PREFIX_EXCEPTIONS));
			
			ExternalResourceFactory.bindResource(
					ae,
					MorphologicalAnalyzerAE.PREFIX_EXCEPTIONS, 
					prefixExceptionsRes
				);


			
			ExternalResourceDescription manualCompositionListRes = ExternalResourceFactory.createExternalResourceDescription(
					ManualSegmentationResource.class,
					getResUrl(TermSuiteResource.MANUAL_COMPOSITIONS));
			
			ExternalResourceFactory.bindResource(
					ae,
					MorphologicalAnalyzerAE.MANUAL_COMPOSITION_LIST, 
					manualCompositionListRes
				);


			ExternalResourceDescription suffixDerivationsExceptionsRes = ExternalResourceFactory.createExternalResourceDescription(
					MultimapFlatResource.class,
					getResUrl(TermSuiteResource.SUFFIX_DERIVATION_EXCEPTIONS));
			
			ExternalResourceFactory.bindResource(
					ae,
					MorphologicalAnalyzerAE.SUFFIX_DERIVATION_EXCEPTION, 
					suffixDerivationsExceptionsRes
				);


			ExternalResourceDescription suffixDerivationsRes = ExternalResourceFactory.createExternalResourceDescription(
					SuffixDerivationList.class,
					getResUrl(TermSuiteResource.SUFFIX_DERIVATIONS));
			
			ExternalResourceFactory.bindResource(
					ae,
					SuffixDerivationList.SUFFIX_DERIVATIONS, 
					suffixDerivationsRes
				);

			
			ExternalResourceDescription prefixTreeRes = ExternalResourceFactory.createExternalResourceDescription(
					PrefixTree.class, 
					getResUrl(TermSuiteResource.PREFIX_BANK));
			
			ExternalResourceFactory.bindResource(
					ae,
					PrefixTree.PREFIX_TREE, 
					prefixTreeRes
				);

			ExternalResourceDescription langDicoRes = ExternalResourceFactory.createExternalResourceDescription(
					SimpleWordSet.class, 
					getResUrl(TermSuiteResource.DICO));
			
			ExternalResourceFactory.bindResource(
					ae,
					MorphologicalAnalyzerAE.LANGUAGE_DICO, 
					langDicoRes
				);
			
			
			ExternalResourceDescription compostInflectionRulesRes = ExternalResourceFactory.createExternalResourceDescription(
					CompostInflectionRules.class, 
					getResUrl(TermSuiteResource.COMPOST_INFLECTION_RULES));
			
			ExternalResourceFactory.bindResource(
					ae,
					MorphologicalAnalyzerAE.INFLECTION_RULES, 
					compostInflectionRulesRes
				);
			
			
			ExternalResourceDescription transformationRulesRes = ExternalResourceFactory.createExternalResourceDescription(
					CompostInflectionRules.class, 
					getResUrl(TermSuiteResource.COMPOST_TRANSFORMATION_RULES));
			
			ExternalResourceFactory.bindResource(
					ae,
					MorphologicalAnalyzerAE.TRANSFORMATION_RULES, 
					transformationRulesRes
				);
			
			ExternalResourceDescription compostStopListRes = ExternalResourceFactory.createExternalResourceDescription(
					SimpleWordSet.class, 
					getResUrl(TermSuiteResource.COMPOST_STOP_LIST));
			
			ExternalResourceFactory.bindResource(
					ae,
					MorphologicalAnalyzerAE.STOP_LIST, 
					compostStopListRes
				);
			
			
			ExternalResourceDescription neoClassicalPrefixesRes = ExternalResourceFactory.createExternalResourceDescription(
					SimpleWordSet.class, 
					getResUrl(TermSuiteResource.NEOCLASSICAL_PREFIXES));
			
			ExternalResourceFactory.bindResource(
					ae,
					MorphologicalAnalyzerAE.NEOCLASSICAL_PREFIXES, 
					neoClassicalPrefixesRes
				);
			
			ExternalResourceFactory.bindResource(ae, resHistory());

			
			return aggregateAndReturn(ae, MorphologicalAnalyzerAE.TASK_NAME, 2);
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
			ExternalResourceFactory.bindResource(ae, resTermino());

			return aggregateAndReturn(ae, getNumberedTaskName("Counting stats ["+statName+"]"), 0);
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
	 * 		This chaining {@link TermSuitePipeline} builder object
	 */
	public TermSuitePipeline haeTraceTimePerf(String toFile)  {
		try {
			AnalysisEngineDescription ae = AnalysisEngineFactory.createEngineDescription(
					CasStatCounter.class,
					CasStatCounter.DOCUMENT_PERIOD, 1,
					CasStatCounter.TO_TRACE_FILE, toFile
				);
			ExternalResourceFactory.bindResource(ae, resTermino());

			return aggregateAndReturn(ae, "Exporting time performances to file " + toFile, 0);
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
	 * 		This chaining {@link TermSuitePipeline} builder object
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
			ExternalResourceFactory.bindResource(ae, resTermino());
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

			return aggregateAndReturn(ae, "Evaluating " + evalTraceName, 0);
		} catch(Exception e) {
			throw new TermSuitePipelineException(e);
		}
	}
	
	/**
	 * 
	 * Stores occurrences to MongoDB
	 * 
	 * @param mongoDBUri
	 * 			the mongo db connection uri
	 * @return
	 * 		This chaining {@link TermSuitePipeline} builder object
	 */
	public TermSuitePipeline setMongoDBOccurrenceStore(String mongoDBUri) {
		this.occurrenceStore = new MongoDBOccurrenceStore(mongoDBUri);
		return this;
	}

	
	/**
	 * @deprecated Use TermSuitePipeline#setOccurrenceStoreMode instead.
	 * 
	 * @param activate
	 * @return
	 * 		This chaining {@link TermSuitePipeline} builder object
	 * 
	 */
	@Deprecated
	public TermSuitePipeline setSpotWithOccurrences(boolean activate) {
		this.spotWithOccurrences = activate;
		return this;
	}
	
	/**
	 * Configures {@link RegexSpotter}. If <code>true</code>, 
	 * adds all spotted occurrences to the {@link Terminology}.
	 * 
	 * @see #aeRegexSpotter()
	 * 
	 * @param addToTermino
	 * 			the value of the parameter
	 * @return
	 * 		This chaining {@link TermSuitePipeline} builder object
	 */
	public TermSuitePipeline setAddSpottedAnnoToTermino(boolean addToTermino) {
		this.addSpottedAnnoToTermino = addToTermino;
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
	 * 		This chaining {@link TermSuitePipeline} builder object
	 */
	public TermSuitePipeline setPostProcessingStrategy(
			String postProcessingStrategy) {
		
		this.postProcessingStrategy = Optional.of(postProcessingStrategy);
		
		return this;
	}
	
	/**
	 * Configures tsvExporter to (not) show headers on the 
	 * first line.
	 * 
	 * @param tsvWithHeaders
	 * 			the flag
	 * @return
	 * 		This chaining {@link TermSuitePipeline} builder object
	 */
	public TermSuitePipeline setTsvShowHeaders(boolean tsvWithHeaders) {
		this.tsvWithHeaders = tsvWithHeaders;
		return this;
	}
	
	/**
	 * Configures tsvExporter to (not) show variant scores with the
	 * "V" label
	 * 
	 * @param tsvWithVariantScores
	 * 			the flag
	 * @return
	 * 		This chaining {@link TermSuitePipeline} builder object
		 */
	public TermSuitePipeline setTsvShowScores(boolean tsvWithVariantScores) {
		this.tsvWithVariantScores = tsvWithVariantScores;
		return this;
	}

	public TermSuitePipeline haeJsonCasExporter(String toDirectoryPath ) {

		try {
			AnalysisEngineDescription ae = AnalysisEngineFactory.createEngineDescription(
					JsonCasExporter.class,
					JsonCasExporter.OUTPUT_DIRECTORY, toDirectoryPath
			);
			return aggregateAndReturn(ae, getNumberedTaskName("Exporting CAS to JSON files"), 0);
		} catch(Exception e) {
			throw new TermSuitePipelineException(e);
		}
	}

	/**
	 * 
	 * Configures the {@link JsonExporterAE} to not embed the occurrences 
	 * in the json file, but to link the mongodb occurrence store instead.
	 * 
	 * 
	 * 
	 * @see #haeJsonExporter(String) 
	 * @return
	 * 		This chaining {@link TermSuitePipeline} builder object
	 */
	public TermSuitePipeline linkMongoStore() {
		this.linkMongoStore = true;
		return this;
	}
	

	/**
	 * 
	 * Aggregates an AE to the TS pipeline.
	 * 
	 * @param ae
	 * 			the ae description of the added pipeline.
	 * @param taskName
	 * 			a user-readable name for the AE task (intended to 
	 * 			be displayed in progress views)
	 * @return
	 * 		This chaining {@link TermSuitePipeline} builder object
	 * 			
	 */
	public TermSuitePipeline customAE(AnalysisEngineDescription ae, String taskName) {
		try {
			return aggregateAndReturn(ae, taskName, 0);
		} catch(Exception e) {
			throw new TermSuitePipelineException(e);
		}
	}

}