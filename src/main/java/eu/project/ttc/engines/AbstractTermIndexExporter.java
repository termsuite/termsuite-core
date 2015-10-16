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
package eu.project.ttc.engines;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeSet;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.ExternalResource;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

import eu.project.ttc.engines.cleaner.FilterRules;
import eu.project.ttc.models.Term;
import eu.project.ttc.models.TermIndex;
import eu.project.ttc.resources.TermIndexResource;
import eu.project.ttc.tools.utils.TermPredicate;
import eu.project.ttc.tools.utils.TermPredicates;
import fr.univnantes.lina.UIMAProfiler;

/**
 * Exports a {@link TermIndex} in TSV format
 * 
 * @author Damien Cram
 *
 */
public abstract class AbstractTermIndexExporter extends JCasAnnotator_ImplBase {
	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractTermIndexExporter.class);

	/*
	 *  AE resources
	 */
	@ExternalResource(key=TermIndexResource.TERM_INDEX, mandatory=true)
	protected TermIndexResource termIndexResource;

	
	/*
	 * AE parameters
	 */
	public static final String FILTERING_THRESHOLD = "FilteringThreshold";
	@ConfigurationParameter(name=FILTERING_THRESHOLD, mandatory=false, defaultValue="0")
	private float filteringThreshold;
	
	public static final String FILTERING_RULE = "FilteringRule";
	@ConfigurationParameter(name=FILTERING_RULE, mandatory=false, defaultValue = "SpecificityThreshold")
	private String filterRule = null;
	
	public static final String TO_FILE_PATH = "TsvFilePath";
	@ConfigurationParameter(name=TO_FILE_PATH, mandatory=true)
	protected String toFilePath;

	
	/*
	 * Internal fields
	 */
	/** Initial predicate */
	private TermPredicate acceptPredicate;

	/** Term sorter in TBX output */
	private Comparator<Term> outputComparator;

	/** The destination file **/
	protected File toFile;
	
	protected FileWriter writer;

	@Override
	public void initialize(UimaContext context)
			throws ResourceInitializationException {
		super.initialize(context);
		this.toFile = new File(this.toFilePath);
		Preconditions.checkNotNull(this.toFile.getAbsoluteFile().getParentFile(), String.format("Invalid path %s.", this.toFilePath));
		Preconditions.checkState(this.toFile.getAbsoluteFile().getParentFile().canWrite(), String.format("Cannot write to directory %s.", this.toFile.getAbsoluteFile().getParentFile().getPath()));
		try {
			this.writer = new FileWriter(toFile, false);
		} catch (IOException e) {
			LOGGER.error("Could not initialize write to file {}", toFile.getAbsolutePath());
			throw new ResourceInitializationException(e);
		}
		initFilteringAndSorting();
	}
	
	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		// do nothing
	}
	
	@Override
	public void collectionProcessComplete()
			throws AnalysisEngineProcessException {
		LOGGER.info("Exporting the term index {} to file {} with filtering rule {} and filtering threshold {}. ",
				termIndexResource.getTermIndex().getName(),
				toFilePath,
				this.filterRule,
				filteringThreshold
				);
		UIMAProfiler.getProfiler("AnalysisEngine").start(this, "process");
		Term term;
		Iterator<Term> it = this.termIndexResource.getTermIndex().getTerms().iterator();
		TreeSet<Term> acceptedTerms = Sets.newTreeSet(outputComparator);
		while(it.hasNext()) {
			term = it.next();
			if (acceptPredicate.accept(term)) 
				acceptedTerms.add(term);
		}
		processAcceptedTerms(acceptedTerms);
		UIMAProfiler.getProfiler("AnalysisEngine").stop(this, "process");
	}
	
	
	protected abstract void processAcceptedTerms(TreeSet<Term> acceptedTerms) throws AnalysisEngineProcessException;

	/**
	 * Initialize the terms filtering based on the parameters.
	 * The initialization is done as a side effect of the method on the class instance.
	 */
	private void initFilteringAndSorting() {

        // Add the filtering rule
        FilterRules rule = FilterRules.valueOf(filterRule);
		switch (rule) {
        case None:
            outputComparator = termIndexResource.getTermIndex().getWRMeasure().getTermComparator(true);
            acceptPredicate = TermPredicates.TRIVIAL_ACCEPTOR;
            return;

        case OccurrenceThreshold:
            outputComparator = TermPredicates.DESCENDING_OCCURRENCE_ORDER;
            acceptPredicate = TermPredicates.createOccurrencesPredicate((int)Math.floor(filteringThreshold));
            return;

        case SpecificityThreshold:
            outputComparator = termIndexResource.getTermIndex().getWRMeasure().getTermComparator(true);
            acceptPredicate = TermPredicates.createMeasurePredicate(filteringThreshold, termIndexResource.getTermIndex().getWRMeasure());
            return;

        case TopNByOccurrence:
            outputComparator = TermPredicates.DESCENDING_OCCURRENCE_ORDER;
            acceptPredicate = TermPredicates.createTopNByOccurrencesPredicate((int)Math.floor(filteringThreshold));
            return;

        case TopNBySpecificity:
            outputComparator = termIndexResource.getTermIndex().getWRMeasure().getTermComparator(true);
            acceptPredicate = TermPredicates.createTopNByTermMeasurePredicate((int)Math.floor(filteringThreshold), termIndexResource.getTermIndex().getWRMeasure());
            return;

        default:
            throw new IllegalArgumentException("Unknown filtering rule " + filterRule);
		}
	}
	
	@Override
	public void destroy() {
		super.destroy();
		try {
			writer.flush();
			writer.close();
		} catch (IOException e) {
			LOGGER.error("Could not close writer to file.", e);
		}

	}
}
