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
import org.apache.uima.resource.ResourceInitializationException;

import com.google.common.base.Preconditions;

import fr.univnantes.termsuite.api.PipelineListener;
import fr.univnantes.termsuite.api.ResourceConfig;
import fr.univnantes.termsuite.model.Lang;
import fr.univnantes.termsuite.model.Tagger;
import fr.univnantes.termsuite.uima.CustomResourceTermSuiteAEFactory;
import fr.univnantes.termsuite.uima.PipelineListenerAE;
import fr.univnantes.termsuite.uima.PipelineResourceMgrs;
import fr.univnantes.termsuite.uima.PreparationPipelineException;
import fr.univnantes.termsuite.uima.TermSuiteAEFactory;
import fr.univnantes.termsuite.utils.TermHistory;

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
	private Optional<Long> nbDocuments = Optional.empty();
	private Optional<Long> corpusSize = Optional.empty();
	private List<AnalysisEngineDescription> customAEs = new ArrayList<>();
	private Path taggerPath;
	private List<PipelineListener> userPipelineListeners = new ArrayList<>();
	private ResourceConfig resourceConfig = new ResourceConfig();
	private Tagger tagger = Tagger.TREE_TAGGER;
	private boolean fixedExpressionEnabled = false;
	private boolean documentLoggingEnabled = true;

	
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
	
	public void terminates() {
		PipelineResourceMgrs.clearPipeline(pipelineId);
	}

	public AnalysisEngineDescription create() {
		if(documentLoggingEnabled)
			aggregateAndReturn(
					CustomResourceTermSuiteAEFactory.createDocumentLoggerAEDesc(
						nbDocuments.orElse(0l), 
						corpusSize.orElse(0l)));
		
		if(lang == Lang.ZH)
			aggregateAndReturn(TermSuiteAEFactory.createChineseTokenizerAEDesc());
		else
			aggregateAndReturn(CustomResourceTermSuiteAEFactory.createWordTokenizerAEDesc(resourceConfig, lang));
		
		switch (tagger) {
		case TREE_TAGGER:
			aggregateAndReturn(CustomResourceTermSuiteAEFactory.createTreeTaggerAEDesc(resourceConfig, lang, taggerPath));
			break;
		case MATE:
			aggregateAndReturn(CustomResourceTermSuiteAEFactory.createMateAEDesc(resourceConfig, lang, taggerPath));
		default:
			throw new UnsupportedOperationException("Unknown tagger: " + tagger);
		}
		
		aggregateAndReturn(CustomResourceTermSuiteAEFactory.createURLFilterAEDesc());
		
		aggregateAndReturn(CustomResourceTermSuiteAEFactory.createStemmerAEDesc(resourceConfig, lang));
		
		aggregateAndReturn(CustomResourceTermSuiteAEFactory.createRegexSpotterAEDesc(resourceConfig, lang));
		
		if(fixedExpressionEnabled) 
			aggregateAndReturn(CustomResourceTermSuiteAEFactory.createFixedExpressionSpotterAEDesc(resourceConfig, lang));
		
		for(AnalysisEngineDescription ae:customAEs)
			aggregateAndReturn(ae);
		
		aggregateAndReturn(CustomResourceTermSuiteAEFactory.createCasStatCounterAEDesc("At end of preparation"));
		
		aePipelineListener();
		
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


	private PreprocessingPipelineBuilder aggregateAndReturn(AnalysisEngineDescription ae) {
		this.aggregateBuilder.add(ae);
		return this;
	}

	public PreprocessingPipelineBuilder setResourceConfig(ResourceConfig resourceConfig) {
		this.resourceConfig = resourceConfig;
		return this;
	}
	

	public PreprocessingPipelineBuilder setMateModelPath(String path) {
		Preconditions.checkArgument(Files.exists(Paths.get(path)), "Directory %s does not exist", path);
		Preconditions.checkArgument(Files.isDirectory(Paths.get(path)), "File %s is not a directory", path);
		return this;
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

	public PreprocessingPipelineBuilder setTagger(Tagger tagger) {
		this.tagger = tagger;
		return this;
	}
	
	
	public PreprocessingPipelineBuilder setDocumentLoggingEnabled(boolean documentLoggingEnabled) {
		this.documentLoggingEnabled = documentLoggingEnabled;
		return this;
	}
	
	public PreprocessingPipelineBuilder setFixedExpressionEnabled(boolean fixedExpressionEnabled) {
		this.fixedExpressionEnabled = fixedExpressionEnabled;
		return this;
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