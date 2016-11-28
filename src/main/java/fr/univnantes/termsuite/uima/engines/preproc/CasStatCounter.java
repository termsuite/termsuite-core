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
package fr.univnantes.termsuite.uima.engines.preproc;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.mutable.MutableInt;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.ExternalResource;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Functions;
import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;

import fr.univnantes.termsuite.model.RelationType;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.TermIndex;
import fr.univnantes.termsuite.model.TermOccurrence;
import fr.univnantes.termsuite.types.SourceDocumentInformation;
import fr.univnantes.termsuite.types.WordAnnotation;
import fr.univnantes.termsuite.uima.resources.termino.TermIndexResource;
import fr.univnantes.termsuite.utils.JCasUtils;

/**
 * Compiles and logs CAS stats.
 * 
 * @author Damien Cram
 *
 */
public class CasStatCounter extends JCasAnnotator_ImplBase {
	private static final Logger LOGGER = LoggerFactory.getLogger(CasStatCounter.class);
	
	private Map<String, MutableInt> counters = Maps.newHashMap();
	
	public static final String STAT_NAME = "StatName";
	@ConfigurationParameter(name=STAT_NAME, mandatory=false)
	private String statName;

	public static final String DOCUMENT_PERIOD = "DocumentPeriod";
	@ConfigurationParameter(name=DOCUMENT_PERIOD, mandatory=false, defaultValue = "-1")
	private int docPeriod;
	private boolean periodicStatEnabled = false;
	private int docIt;
	private long cumulatedFileSize;
	
	public static final String TO_TRACE_FILE = "ToTraceFile";
	@ConfigurationParameter(name=TO_TRACE_FILE, mandatory=false)
	private String traceFileName;
	private Writer fileWriter;

	private static final String TSV_LINE_FORMAT="%d\t%d\t%d\t%d\t%d\n";
	
	@ExternalResource(key=TermIndexResource.TERM_INDEX, mandatory=true)
	private TermIndexResource termIndexResource;

	private Stopwatch sw;
	@Override
	public void initialize(UimaContext context) throws ResourceInitializationException {
		super.initialize(context);
		this.sw = Stopwatch.createStarted();
		if(traceFileName != null) {
			File file = new File(traceFileName);
			try {
				this.fileWriter = new FileWriter(file);
			} catch (IOException e) {
				LOGGER.error("Could not create a writer to file {}", traceFileName);
				throw new ResourceInitializationException(e);
			}
			this.periodicStatEnabled = docPeriod > 0;
			LOGGER.info("Tracing time performance to file {}", file.getAbsolutePath());
		}
	}
	
	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		this.docIt++;
		Optional<SourceDocumentInformation> sourceDocumentAnnotation = JCasUtils.getSourceDocumentAnnotation(aJCas);
		if(sourceDocumentAnnotation.isPresent())
			this.cumulatedFileSize += sourceDocumentAnnotation.get().getDocumentSize();
		FSIterator<Annotation> it =  aJCas.getAnnotationIndex().iterator();
		Annotation a;
		MutableInt i;
		while(it.hasNext()) {
			a = it.next();
			i = counters.get(a.getType().getShortName());
			if(i == null) 
				counters.put(a.getType().getShortName(), new MutableInt(1));
			else
				i.increment();
		}
		if(periodicStatEnabled && this.docIt % this.docPeriod == 0)
			try {
				traceToFile();
			} catch (IOException e) {
				throw new AnalysisEngineProcessException(e);
			}
	}
	
	private void traceToFile() throws IOException {
		String line = String.format(TSV_LINE_FORMAT,
			this.sw.elapsed(TimeUnit.MILLISECONDS),
			this.docIt,
			this.cumulatedFileSize,
			this.termIndexResource.getTermIndex().getTerms().size(),
			this.counters.get(WordAnnotation.class.getSimpleName()).intValue()
		);
		LOGGER.debug(line);
		this.fileWriter.write(line);
		this.fileWriter.flush();
	}

	@Override
	protected void finalize() throws Throwable {
		this.fileWriter.close();
		super.finalize();
	}
	@Override
	public void collectionProcessComplete()
			throws AnalysisEngineProcessException {
		if(statName != null)
			logStats();
	}

	private void logStats() {
		Ordering<String> a = Ordering.natural().reverse().onResultOf(Functions.forMap(counters)).compound(Ordering.natural());
		Map<String, MutableInt> map = ImmutableSortedMap.copyOf(counters, a);
		
		Iterator<Entry<String, MutableInt>> it = map.entrySet().iterator();
		if(it.hasNext()) {// it will be empty if pipeline is run on empty collection
			Entry<String, MutableInt> mostFrequentAnno = it.next();
			LOGGER.info("[{}] {}: {} ", statName, mostFrequentAnno.getKey(), mostFrequentAnno.getValue().intValue());
		}
		int nbSyntacticVariants = 0;
		int nbMorphologicalVariants = 0;
		int nbGraphicalVariants = 0;
		int nbSynonymicVariants = 0;
		int nbOccurrences = 0;
		TermIndex termIndex = termIndexResource.getTermIndex();
		TermIndex tIndex = termIndex;
		for(Term t:tIndex.getTerms()) {
			nbMorphologicalVariants+=Iterables.size(termIndex.getOutboundRelations(t,RelationType.MORPHOLOGICAL));
			nbSyntacticVariants+=Iterables.size(termIndex.getOutboundRelations(t,RelationType.SYNTACTICAL));
			nbGraphicalVariants+=Iterables.size(termIndex.getOutboundRelations(t,RelationType.GRAPHICAL));
			nbSynonymicVariants+=Iterables.size(termIndex.getOutboundRelations(t,RelationType.SYNONYMIC));
			Collection<TermOccurrence> occurrences = termIndex.getOccurrenceStore().getOccurrences(t);
			nbOccurrences+=occurrences.size();
		}
		// graphical variants are bidirectional
		nbGraphicalVariants/=2;
		
		LOGGER.info("[{}] Nb terms:    {} [sw: {}, mw: {}]", statName, 
				tIndex.getTerms().size(), 
				Iterators.size(tIndex.singleWordTermIterator()),
				Iterators.size(tIndex.multiWordTermIterator()));
		LOGGER.info("[{}] Nb words:    {} [compounds: {}]", statName, 
				tIndex.getWords().size(), 
				Iterators.size(tIndex.compoundWordTermIterator()));
		LOGGER.info("[{}] Nb occurrences: {}", statName, 
				nbOccurrences);
		LOGGER.info("[{}] Nb variants: {} [morph: {}, syntactic: {}, graph: {}, synonyms: {}]", statName, 
				nbMorphologicalVariants + nbSyntacticVariants + nbGraphicalVariants, 
				nbMorphologicalVariants, 
				nbSyntacticVariants, 
				nbGraphicalVariants,
				nbSynonymicVariants);
	}
}
