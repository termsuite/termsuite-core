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
package eu.project.ttc.utils;

import java.io.PrintStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import com.google.common.base.Optional;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import eu.project.ttc.engines.desc.Lang;
import eu.project.ttc.engines.desc.TermSuiteResourceException;
import eu.project.ttc.models.ContextVector;
import eu.project.ttc.models.SyntacticVariation;
import eu.project.ttc.models.Term;
import eu.project.ttc.models.TermIndex;
import eu.project.ttc.resources.GeneralLanguageResource;
import eu.project.ttc.tools.TermSuiteResourceHelper;

public class TermUtils {
	
	public static Comparator<Term> frequencyComparator = new Comparator<Term>() {
		@Override
		public int compare(Term o1, Term o2) {
			return ComparisonChain.start()
					.compare(o2.getFrequency(), o1.getFrequency())
					.result();
		}
	};
	
	public static Comparator<Term> specificityComparator = new Comparator<Term>() {
		@Override
		public int compare(Term o1, Term o2) {
			return ComparisonChain.start()
					.compare(o2.getWR(), o1.getWR())
					.result();
		}
	};
	
	public static void showIndex(TermIndex index, PrintStream stream) {
		Optional<Pattern> watchExpression = Optional.absent();
		showIndex(index, stream, watchExpression);
	}
		
	public static void showIndex(TermIndex index, PrintStream stream, Optional<Pattern> watchExpression) {
		for(Term term:index.getTerms()) {
			if(!watchExpression.isPresent()
					|| (watchExpression.isPresent() && watchExpression.get().matcher(term.getGroupingKey()).find())
					) {
				stream.println(term);
				for(Term t:term.getGraphicalVariants()) 
					stream.format("\tgraphical: %s\n" , t.getGroupingKey());
				for(SyntacticVariation variation:term.getSyntacticVariants()) 
					stream.format("\tsyntactic: %s\n" , variation.getTarget().getGroupingKey());
			}
		}
	}

	public static void showTopNTermsBy(TermIndex index, PrintStream out, int n) {
		List<Term> terms = Lists.newArrayList(index.getTerms());
		Collections.sort(terms, specificityComparator);
		int i = 0;
		for(Term t:terms) {
			out.println(t);
			if(i++ > n)
				break;
		}
	}

	public static void showCompounds(TermIndex index, PrintStream out, int threshhold) {
		List<Term> terms = Lists.newArrayList();
		for(Term term:index.getTerms()) {
			if(term.isCompound() && term.getFrequency() >= threshhold)
				terms.add(term);
		}
		Collections.sort(terms, frequencyComparator);
		for(Term term:terms) 
			out.println(term);
	}

	public static String collapseText(String coveredText) {
		char[] charArray = coveredText.toCharArray();
		if(charArray.length == 0)
			return "";
		char last = charArray[0];
		StringBuilder builder = new StringBuilder();
		builder.append(last);
		for(int i=1;i<charArray.length; i++) {
			char c = charArray[i];
			if(Character.isWhitespace(c)) {
				c = TermSuiteConstants.WHITESPACE;
				if(Character.isWhitespace(last))
					continue;
			}
			builder.append(c);
			last = c;
		}
		return builder.toString().trim();
	}
	
	public static void showContextVector(ContextVector contextVector, int topN) {
		Set<ContextVector.Entry> entries = Sets.newTreeSet(contextVector.getEntries());
		int i = 0;
		for(ContextVector.Entry e:entries) {
			i++;
			if(i>topN)
				break;
			System.out.format("\t%-12s: %.3f\n", e.getCoTerm().getLemma(), e.getNbCooccs());
		}
	}
	
	/**
	 * 
	 * @param l
	 * @param t
	 * @return
	 */
	public static int getGeneralFrequency(Lang l, Term t) {
		String resName = new TermSuiteResourceHelper(l).getGeneralLanguageFrequencies().toString().replaceFirst("file:", "");
		GeneralLanguageResource generalLanguage = new GeneralLanguageResource();
		try {
			generalLanguage.load(TermUtils.class.getClassLoader().getResourceAsStream(resName));
			return generalLanguage.getFrequency(t.getLemma(), t.getPattern());
		} catch (Exception e) {
			throw new TermSuiteResourceException("Could not read resource " + resName, e);
		}
	}

}
