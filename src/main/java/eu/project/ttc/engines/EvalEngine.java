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
package eu.project.ttc.engines;

import java.io.IOException;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.ExternalResource;
import org.apache.uima.jcas.JCas;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.base.Stopwatch;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import eu.project.ttc.engines.cleaner.TermProperty;
import eu.project.ttc.models.Term;
import eu.project.ttc.models.TermRelation;
import eu.project.ttc.resources.EvalTrace;
import eu.project.ttc.resources.EvalTrace.RecPoint;
import eu.project.ttc.resources.ReferenceTermList;
import eu.project.ttc.resources.ReferenceTermList.RTLTerm;
import eu.project.ttc.resources.TermIndexResource;
import eu.project.ttc.utils.FileUtils;

/**
 * 
 * An engine for the measurement of precision and recall of 
 * an extracted termino against a reference list.
 * 
 * @author Damien Cram
 *
 */
public class EvalEngine  extends JCasAnnotator_ImplBase {
	private static final Logger LOGGER = LoggerFactory.getLogger(EvalEngine.class);

	public static final String HORIZONTAL_RULE = "------------------------------------------------------------";

	public static final String REFERENCE_LIST = "ReferenceList";
	@ExternalResource(key = REFERENCE_LIST, mandatory = true)
	private ReferenceTermList rtl;

	@ExternalResource(key = TermIndexResource.TERM_INDEX, mandatory = true)
	private TermIndexResource termIndexResource;
	
	public static final String EVAL_TRACE = "EvalTrace";
	@ExternalResource(key=EVAL_TRACE, mandatory=true)
	private EvalTrace evalTrace;
	
	public static final String OUTPUT_LOG_FILE = "OutputLogFile";
	@ConfigurationParameter(name=OUTPUT_LOG_FILE, mandatory=false)
	private String outputLogFile;

	public static final String OUTPUT_R_FILE = "OutputRFile";
	@ConfigurationParameter(name=OUTPUT_R_FILE, mandatory=false)
	private String outputRFile;

	public static final String RTL_WITH_VARIANTS = "RTLWithVariants";

	@ConfigurationParameter(name=RTL_WITH_VARIANTS, mandatory=true)
	private boolean rtlV;
	
	public static final String CUSTOM_LOG_HEADER_STRING = "CustomLogHeaderString";
	@ConfigurationParameter(name=CUSTOM_LOG_HEADER_STRING, mandatory=false, defaultValue="")
	private String customLogHeaderString;

	public static final Collection<Integer> CHART_AXIS_POINTS = ImmutableSet.of(10, 50, 100, 200, 250, 500, 1000, 2000, 5000, 10000, 20000);
	
	private Set<RTLTerm> rtlTermsNotFound;
	private DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	private Stopwatch sw = Stopwatch.createUnstarted();
	
	@Override
	public void process(JCas arg0) throws AnalysisEngineProcessException {
		// do nothing
	}

	
	
	@Override
	public void collectionProcessComplete()
			throws AnalysisEngineProcessException {
		// init the eval trace with the ref list size, otherwise the recall cannot be computed
		this.evalTrace.setRtlSize(rtl.getTerms().size());

		evaluate();
		writeToLogFile();
		writeToRFile();
	}

	private void writeToRFile() {
		if(outputRFile != null) {
			try {
				PrintStream stream = new PrintStream(outputRFile);
				
				writePrecisionRecallMatrix(stream);
				
				stream.flush();
				stream.close();		
			} catch (IOException e) {
				LOGGER.error("File error", e);
				LOGGER.error("Could not write R chart to file {}", outputRFile);
			}
		}
	}

	private void writePrecisionRecallMatrix(PrintStream stream) throws IOException {
		for(RecPoint p:evalTrace.getChartAxisPoints(true)) {
			stream.format("%d\t%.2f\t%.2f\n", 
				p.getRank(),
				p.getPrecision(),
				p.getRecall()
			);
		}
	}

	private void writeToLogFile() {
		if(outputLogFile != null) {
			try {
				int numVariationPaths = 0;
				for(Term lcTerm:termIndexResource.getTermIndex().getTerms())
					numVariationPaths += termIndexResource.getTermIndex().getOutboundRelations(lcTerm).size();
				PrintStream stream = new PrintStream(outputLogFile);
				stream.println(HORIZONTAL_RULE);
				if(!customLogHeaderString.isEmpty())
					stream.println(customLogHeaderString);
//				stream.format("RTL path: %s\n", rtl.getPath());
//				stream.format("Variant depth: %d\n", 10);
				
				stream.format("RTL Mode: %s\n", getModeString());
				stream.format("LC term index: %s\n", termIndexResource.getTermIndex().getName());
				stream.format("Num. LC terms: %d\tIncl. variants: %d\n", termIndexResource.getTermIndex().getTerms().size(), numVariationPaths);
				
				for(int i:new int[]{10,100,1000}) {
					RecPoint p = evalTrace.getAtRank(i);
					String str = String.format("R_%d: %.2f  (p=%.2f)", i, p.getRecall()*100, p.getPrecision()*100);
					stream.println(str.replaceAll(",", "."));
				}
				stream.format("R_max: %.2f\n", evalTrace.getMaxRecall()*100);
				stream.format("Date: %s\n", dateFormat.format(new Date()));
				stream.format("Generation time: %s\n", sw.elapsed(TimeUnit.SECONDS));
				stream.println();
				stream.println(HORIZONTAL_RULE);
				stream.println("Extracted terms found in reference list");
				stream.println(HORIZONTAL_RULE);
				stream.println();

				String termLineFormat = "%-8s%-40s\n";
				stream.format(String.format(termLineFormat, "Rank", "terms"));
				stream.format(termLineFormat, "---", "---");
				for(RecPoint p:evalTrace.getChartAxisPoints(true)) {
										
					for(int i=0; i<p.getLcTermsFound().size(); i++)
						stream.format(termLineFormat, 
								"",
								p.getLcTermsFound().get(i).toStringWithVar()
							);
					stream.format(termLineFormat, 
							Integer.toString(p.getRank()),
							"---"
						);

				}
				
				stream.println();
				stream.println(HORIZONTAL_RULE);
				stream.println("Reference terms not found in extracted terms");
				stream.println(HORIZONTAL_RULE);
				stream.println();
				
				stream.println("Id\tVar?\tTerm");
				stream.println("---");
				Set<RTLTerm> sortedRefTermsNotFound = new TreeSet<RTLTerm>(new Comparator<RTLTerm>() {
					@Override
					public int compare(RTLTerm o1, RTLTerm o2) {
						return ComparisonChain.start().compare(
								o1.getId(), o2.getId()
								).result();
					}
				});
				sortedRefTermsNotFound.addAll(rtlTermsNotFound);
				 
				for(RTLTerm refTerm:sortedRefTermsNotFound)
					stream.println(refTerm.toTSVString());

				stream.println();
				stream.println(HORIZONTAL_RULE);
				stream.println("Precision/Recall");
				stream.println(HORIZONTAL_RULE);
				stream.println();
				
				writePrecisionRecallMatrix(stream);

				stream.flush();
				stream.close();		
			} catch (IOException e) {
				LOGGER.error("File error", e);
				LOGGER.error("Could not write eval logs to file {}", outputLogFile);
			}		
		}
	}

	private String getModeString() {
		return String.format("%s\t(RTL %s variants)",
				rtlV ? "RTLv" : "RTL",
				rtlV ? "with" : "without"
		);
	}

	private void evaluate() {
		LOGGER.info("Evaluating extracted terms against file {} [RTL with variants: {}]", 
				FileUtils.getFileName(rtl.getPath()),
				rtlV);
		rtlTermsNotFound = Sets.newHashSet(rtl.asList());
		List<Term> lc = Lists.newArrayList(termIndexResource.getTermIndex().getTerms());
		Collections.sort(lc, TermProperty.SPECIFICITY.getComparator(true));
		generateRecPointIndexes(lc.size());
		
		List<RTLTerm> rtlTermsFound = Lists.newArrayList();
		Term term;
		int tp=0;
		int rank = 0;
		while(rank < lc.size()) {
			term = lc.get(rank);
			sw.start();
			for(RTLTerm rtlTerm:getMatchingRTLTerms(rtlTermsNotFound, term)) {
				rtlTermsFound.add(rtlTerm);
				rtlTermsNotFound.remove(rtlTerm);
				tp++;
				LOGGER.debug("For term \"{}\", found reference term \"{}\" ({})", term, rtlTerm, tp);
			}
			sw.stop();
			evalTrace.trace(rank, tp, rtlTermsFound);
			rtlTermsFound = Lists.newArrayList();
			if(LOGGER.isDebugEnabled()) 
				LOGGER.debug("Top {} extracted terms against reference list {}: p={} and r={}",
					rank,
					FileUtils.getFileName(rtl.getPath()),
					String.format("%.2f", evalTrace.getLast().getPrecision()),
					String.format("%.2f", evalTrace.getLast().getRecall())
				);
			rank ++;
		}
		
		LOGGER.info("Max recall after all {} extracted terms compared against ref list {}: {} (Eval time: {})",
			evalTrace.getLast().getRank(),
			FileUtils.getFileName(rtl.getPath()),
			String.format("%.2f", evalTrace.getLast().getRecall()),
			sw.elapsed(TimeUnit.MILLISECONDS)
			);
	}
	
	
	private Set<Integer> recPointIndexes;
	private void generateRecPointIndexes(int maxIndex) {
		recPointIndexes = Sets.newHashSet();
		for(int i:CHART_AXIS_POINTS)
			if(i<maxIndex)
				recPointIndexes.add(i);
		int i = 0;
		while(i++ < maxIndex - 1) {
			if(i <= 10)
				recPointIndexes.add(i);
			else if(i < 100 && i%10==0)
				recPointIndexes.add(i);
			else if(i < 1000 && i%100==0)
				recPointIndexes.add(i);
			else if(i%1000==0)
				recPointIndexes.add(i);
		}
		recPointIndexes.add(maxIndex - 1);
		
	}

	private Collection<RTLTerm> getMatchingRTLTerms(Collection<RTLTerm> rtl,
			Term lcTerm) {
		Collection<RTLTerm> matchingRTLTerms = Sets.newHashSet();
		for(RTLTerm rtlTerm:rtl) {
			if(isMatch(rtlTerm, lcTerm))
				matchingRTLTerms.add(rtlTerm);
		}
		return matchingRTLTerms;
	}

	/**
	 * 
	 * @param rtlTerm
	 * @param lcTerm
	 * @return
	 */
	private boolean isMatch(RTLTerm rtlTerm, Term lcTerm) {
		Set<RTLTerm> rtlTerms = Sets.newHashSet(rtlTerm);
		if(rtlV)
			rtlTerms.addAll(rtlTerm.getVariants());
			
		Set<Term> lc = Sets.newHashSet();
		lc.add(lcTerm);
		for(TermRelation tv:termIndexResource.getTermIndex().getOutboundRelations(lcTerm))
				lc.add(tv.getTo());
		
		
		if(LOGGER.isTraceEnabled()) {
			LOGGER.trace("lc terms:  {}", Joiner.on(',').join(lc));
			LOGGER.trace("rtl terms: {}", Joiner.on(',').join(rtlTerms));
		}
		for(RTLTerm rtlTerm2:rtlTerms) {
			for(Term lcTerm2:lc) {
				if(isTermMatch(rtlTerm2, lcTerm2))
					return true;
			}
		}
		
		return false;
	}


	private boolean isTermMatch(RTLTerm refTerm, Term lcTerm) {
		
		// 1- test again lemma concatenation
		if(refTerm.getString().equalsIgnoreCase(lcTerm.getLemma()))
			return true;
		
		return false;
	}
}
