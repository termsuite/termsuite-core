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
package fr.univnantes.termsuite.uima.engines.termino;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.ExternalResource;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.univnantes.julestar.uima.resources.MultimapFlatResource;
import fr.univnantes.termsuite.engines.splitter.MorphologicalAnalyzer;
import fr.univnantes.termsuite.engines.splitter.MorphologicalOptions;
import fr.univnantes.termsuite.engines.splitter.NativeSplitter;
import fr.univnantes.termsuite.uima.resources.ObserverResource;
import fr.univnantes.termsuite.uima.resources.TermHistoryResource;
import fr.univnantes.termsuite.uima.resources.preproc.ManualSegmentationResource;
import fr.univnantes.termsuite.uima.resources.preproc.PrefixTree;
import fr.univnantes.termsuite.uima.resources.preproc.SimpleWordSet;
import fr.univnantes.termsuite.uima.resources.termino.CompostInflectionRules;
import fr.univnantes.termsuite.uima.resources.termino.SuffixDerivationList;
import fr.univnantes.termsuite.uima.resources.termino.TerminologyResource;

/**
 * A UIMA wrapper for {@link NativeSplitter}
 * 
 * @author Damien Cram
 *
 */
public class MorphologicalAnalyzerAE extends JCasAnnotator_ImplBase {
	private static final Logger LOGGER = LoggerFactory.getLogger(MorphologicalAnalyzerAE.class);

	public static final String TASK_NAME = "Morphosyntactic analysis (native and neoclassical)";

	@ExternalResource(key=ObserverResource.OBSERVER, mandatory=true)
	protected ObserverResource observerResource;

	@ExternalResource(key=TerminologyResource.TERMINOLOGY, mandatory=true)
	private TerminologyResource terminoResource;
	
	public static final String LANGUAGE_DICO = "LanguageDico";
	@ExternalResource(key=LANGUAGE_DICO, mandatory=true)
	private SimpleWordSet languageDico;

	public static final String INFLECTION_RULES = "InflectionRules";
	@ExternalResource(key=INFLECTION_RULES, mandatory=true, description="Inflection rules for the last segment")
	private CompostInflectionRules inflectionRules;

	public static final String TRANSFORMATION_RULES = "TransformationRules";
	@ExternalResource(key=TRANSFORMATION_RULES, mandatory=true, description="Inflection rules for all but last segments")
	private CompostInflectionRules transformationRules;

	public static final String PREFIX_EXCEPTIONS = "PrefixExceptions";
	@ExternalResource(key=PREFIX_EXCEPTIONS, mandatory=true)
	private ManualSegmentationResource prefixExceptions;

	public static final String NEOCLASSICAL_PREFIXES = "NeoClassicalPrefixes";
	@ExternalResource(key=NEOCLASSICAL_PREFIXES, mandatory=true)
	private SimpleWordSet neoclassicalPrefixes;

	public static final String STOP_LIST = "StopList";
	@ExternalResource(key=STOP_LIST, mandatory=true)
	private SimpleWordSet stopList;

	@ExternalResource(key =TermHistoryResource.TERM_HISTORY, mandatory = true)
	private TermHistoryResource historyResource;

	
	public static final String ALPHA = "Alpha";
	@ConfigurationParameter(name=ALPHA, mandatory=true)
	private float alpha;

	public static final String BETA = "Beta";
	@ConfigurationParameter(name=BETA, mandatory=true)
	private float beta;

	public static final String GAMMA = "Gamma";
	@ConfigurationParameter(name=GAMMA, mandatory=true)
	private float gamma;

	public static final String DELTA = "Delta";
	@ConfigurationParameter(name=DELTA, mandatory=true)
	private float delta;

	public static final String SCORE_THRESHOLD = "ScoreThreshold";
	@ConfigurationParameter(name=SCORE_THRESHOLD, mandatory=true)
	private float scoreThreshold;

	public static final String SEGMENT_SIMILARITY_THRESHOLD = "SegmentSimilarityThreshold";
	@ConfigurationParameter(name=SEGMENT_SIMILARITY_THRESHOLD, mandatory=false, defaultValue="1")
	private float segmentSimilarityThreshold = 1f;

	public static final String MIN_COMPONENT_SIZE = "MinimumComponentSize";
	@ConfigurationParameter(name=MIN_COMPONENT_SIZE, mandatory=false, defaultValue = "3")
	private int minComponentSize;

	public static final String MAX_NUMBER_OF_COMPONENTS = "MaxNumberOfComponents";
	@ConfigurationParameter(name=MAX_NUMBER_OF_COMPONENTS, mandatory=false, defaultValue = "3")
	private int maxNumberOfComponents;


	@ExternalResource(key=PrefixTree.PREFIX_TREE, mandatory=true)
	private PrefixTree prefixTree;

	public static final String CHECK_IF_MORPHO_EXTENSION_IS_IN_CORPUS = "CheckIfMorphoExtensionInCorpus";
	@ConfigurationParameter(name=CHECK_IF_MORPHO_EXTENSION_IS_IN_CORPUS, mandatory=false, defaultValue = "true")
	private boolean checkIfMorphoExtensionInCorpus;
	
	
	public static final String MANUAL_COMPOSITION_LIST = "ManualCompositionList";
	@ExternalResource(key=MANUAL_COMPOSITION_LIST, mandatory=true)
	private ManualSegmentationResource manualCompositions;

	@ExternalResource(key=SuffixDerivationList.SUFFIX_DERIVATIONS, mandatory=true)
	private SuffixDerivationList suffixDerivationList;

	public static final String SUFFIX_DERIVATION_EXCEPTION = "SuffixDerivationExceptions";
	@ExternalResource(key=SUFFIX_DERIVATION_EXCEPTION, mandatory=true)
	private MultimapFlatResource exceptionsByDerivateExceptionForms;

	public void initialize(UimaContext context) throws ResourceInitializationException {
		super.initialize(context);
		if(segmentSimilarityThreshold != 1.0) 
			LOGGER.warn("segmentSimilarityThreshold is set to {}. Another value than 1 can make this AE very long to execute.", 
					segmentSimilarityThreshold);
	};
	
	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
	}
	
	@Override
	public void collectionProcessComplete() throws AnalysisEngineProcessException {
		MorphologicalOptions opt = new MorphologicalOptions()
				.setAlpha(this.alpha)
				.setBeta(this.beta)
				.setGamma(this.gamma)
				.setDelta(this.delta)
				.setMaxNumberOfComponents(this.maxNumberOfComponents)
				.setMinComponentSize(this.minComponentSize)
				.setScoreThreshold(this.scoreThreshold)
				.setSegmentSimilarityThreshold(this.segmentSimilarityThreshold);
		
		new MorphologicalAnalyzer()
				.setOptions(opt)
				.setHistory(historyResource.getHistory())
				.setInflectionRules(inflectionRules)
				.setLanguageDico(languageDico)
				.setManualCompositions(manualCompositions)
				.setManualSuffixDerivations(this.exceptionsByDerivateExceptionForms)
				.setNeoclassicalPrefixes(neoclassicalPrefixes)
				.setPrefixExceptions(prefixExceptions)
				.setPrefixTree(prefixTree)
				.setStopList(stopList)
				.setSuffixDerivationList(suffixDerivationList)
				.setTransformationRules(transformationRules)
				.analyze(terminoResource.getTerminology());
	}
}
