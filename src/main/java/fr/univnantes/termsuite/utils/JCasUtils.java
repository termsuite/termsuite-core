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
package fr.univnantes.termsuite.utils;

import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import org.apache.commons.lang.mutable.MutableInt;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.StringArray;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.metadata.NameValuePair;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;

import fr.univnantes.termsuite.types.SourceDocumentInformation;
import fr.univnantes.termsuite.types.TermOccAnnotation;
import fr.univnantes.termsuite.types.WordAnnotation;

public class JCasUtils {
	public static void showTermFreq(JCas jcas, int num) {
		FSIterator<Annotation> it = jcas.getAnnotationIndex(TermOccAnnotation.type).iterator();
		int cnt = 0;
		while (it.hasNext()) {
			cnt += 1;
			TermOccAnnotation annotation = (TermOccAnnotation) it.next();
			if(cnt == num) {
				System.out.println("TermOccAnnotation n°"+num+": " + annotation);
				break;
			}
		}
	}
	
	public static SourceDocumentInformation initJCasSDI(JCas jCas, String language, String text, String uri, long documentSize) {
		SourceDocumentInformation sdi;
		sdi = new SourceDocumentInformation(jCas);
		sdi.setBegin(0);
		sdi.setUri(uri);
		sdi.setEnd(text.length());
		sdi.setOffsetInSource(0);
		sdi.addToIndexes();
		sdi.setDocumentSize(documentSize);
		return sdi;
	}

	public static String getTermSuiteCasFileName(JCas jcas) {
		FSIterator<Annotation> it = jcas.getAnnotationIndex(SourceDocumentInformation.type).iterator();
		if(it.hasNext()) {
			SourceDocumentInformation sdi = (SourceDocumentInformation) it.next();
			Iterator<String> iterator = Splitter.on("/").split(sdi.getUri() == null ? "(no uri)" : sdi.getUri()).iterator();
			String name = null;
			while(iterator.hasNext())
				name = iterator.next();
			return name;
		} else
			return null;
			
	}
	
	public static void showSdiWithCategory(JCas jcas) {
		FSIterator<Annotation> it = jcas.getAnnotationIndex(WordAnnotation.type).iterator();
		int wordCnt = 0;
		while(it.hasNext()) {
			wordCnt++;
			WordAnnotation a = (WordAnnotation) it.next();
			System.out.print(a.getCoveredText() + "_" + a.getTag());
			if(wordCnt < 12) {
				System.out.print(" ");
			} else {
				System.out.println();
				wordCnt = 0;
			}
				
		}
		System.out.println(Joiner.on(" ").join(it));
	}
	
	public static void showSdiWithCategory2(JCas jcas) {
		String wordsLine = "";
		String catsLine = "";
		int cnt = 0;
		FSIterator<Annotation> it = jcas.getAnnotationIndex(WordAnnotation.type).iterator();
		while(it.hasNext()) {
			cnt += 1;
			WordAnnotation a = (WordAnnotation) it.next();
			
			String[] strings = center(a.getCoveredText(), a.getTag());
			wordsLine+=strings[0] + " ";
			catsLine+=strings[1] + " ";
			if(cnt == 20) {
				System.out.println(wordsLine);
				System.out.println(catsLine);
				System.out.println();
				
				wordsLine = "";
				catsLine = "";
				cnt = 0;
			} 
		}
		if(cnt>0) {
			System.out.println(wordsLine);
			System.out.println(catsLine);
		}
	}
	
	public static String[] center(String str1, String str2) {
		if(str1.length() >= str2.length()) {
			boolean right = true;
			while(str2.length() < str1.length()) {
				str2 = right ? str2 + " " : " " + str2;
				right = !right;
			}
			return new String[]{str1,str2};
		} else {
			String[] strings = center(str2, str1);
			return new String[]{strings[1],strings[0]};
		}
	}

	
	public static int countType(JCas jcas, int type) {
		FSIterator<Annotation> it = jcas.getAnnotationIndex(type).iterator();
		int cnt = 0;
		while(it.hasNext()) {
			cnt++;
			it.next();
		}
		return cnt;
	}
	

	
	public static String arrayToString(StringArray stringArray) {
		return Joiner.on(' ').join(stringArray.toArray());
	}


	public static void showJCas(JCas jcas) {
		FSIterator<Annotation> it = jcas.getAnnotationIndex().iterator();
		Map<String, MutableInt> counters = new TreeMap<String, MutableInt>();
		int total = 0;
		while (it.hasNext()) {
			total+=1;
			String annoType = "rien";
			try {
				Annotation annotation = (Annotation) it.next();
				annoType = annotation.getType().getName();
			} catch (NullPointerException e) {
				it.moveToNext();
				annoType = e.getClass().getCanonicalName();
			}
			if(counters.get(annoType) == null) {
				counters.put(annoType, new MutableInt(1));
			} else {
				counters.get(annoType).increment();
			}
		}
		System.out.println("Total annotation in JCas (ID: "+System.identityHashCode(jcas)+"): " + total);
		for (String annoType:counters.keySet()) {
			System.out.println(annoType+ ": " + counters.get(annoType));
		}
	}

	public static void showAEConfig(AnalysisEngine analysisEngine) {
		for(NameValuePair pair:analysisEngine.getMetaData().getConfigurationParameterSettings().getParameterSettings()) {
			System.out.println("** " + pair.getName() + ": " + pair.getValue());
		}
	}

	public static Optional<SourceDocumentInformation> getSourceDocumentAnnotation(JCas jCas) {
		FSIterator<Annotation> iterator = jCas.getAnnotationIndex(SourceDocumentInformation.type).iterator();
		if(iterator.hasNext())
			return Optional.of((SourceDocumentInformation)iterator.next());
		else
			return Optional.empty();
	}

	public static boolean containsStrictly(Annotation container, Annotation subAnnotation) {
		return contains(container, subAnnotation) 
				&& !sameIndexes(container, subAnnotation);
	}

	public static boolean contains(Annotation container, Annotation subAnnotation) {
		return container.getBegin() <= subAnnotation.getBegin() && container.getEnd()>= subAnnotation.getEnd();
	}

	public static boolean sameIndexes(Annotation anno1, Annotation anno2) {
		return anno1.getBegin() == anno2.getBegin() && anno1.getEnd() == anno2.getEnd();		
	}

}
