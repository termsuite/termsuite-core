/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright 2, 2013nership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package eu.project.ttc.engines;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasMultiplier_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.AbstractCas;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.CasCopier;

import eu.project.ttc.types.WordAnnotation;

public class TildeTokenizer extends JCasMultiplier_ImplBase {
	
	private List<Token> tokens;
	
	private void setTokens() {
		this.tokens = new ArrayList<Token>();
	}
	
	private List<Token> getTokens() {
		return this.tokens;
	}
 	
	@Override
	public void initialize(UimaContext context) throws ResourceInitializationException {
		super.initialize(context);
		if (this.getTokens() == null) {
			this.setTokens();
		}
	}
	
	@Override
	public void process(JCas cas) throws AnalysisEngineProcessException {
		try {
			String text = cas.getDocumentText();
			Scanner scanner = new Scanner(text);
			String delimiter = System.getProperty("line.separator");
			scanner.useDelimiter(delimiter);
			while (scanner.hasNext()) {
				String line = scanner.next();
				String[] items = line.split("\t");
				if (items.length == 4) {
					String word = items[0].trim();
					// String tag = items[1];
					String lemma = items[2].trim();
					String tag = items[3].trim();
					Token token = new Token(word, tag, lemma);
					this.getTokens().add(token);
				} 
			}
			scanner.close();
			this.cas = cas;
			this.enableHasNext(true);
		} catch (Exception e) {
			throw new AnalysisEngineProcessException(e);
		}
	}
	
	private JCas cas;

	private boolean hasNext;
	
	private void enableHasNext(boolean enabled) {
		this.hasNext = enabled;
	}

	@Override
	public boolean hasNext() throws AnalysisEngineProcessException {
		return this.hasNext;
	}

	@Override
	public AbstractCas next() throws AnalysisEngineProcessException {
		this.enableHasNext(false);
		JCas cas = this.getEmptyJCas();
		try {
			CasCopier.copyCas(this.cas.getCas(), cas.getCas(), false);
			StringBuilder builder = new StringBuilder();
			int begin = 0;
			int end = 0;
			for (Token token : this.getTokens()) {
				begin = builder.length();
				builder.append(token.word());
				end = builder.length();
				builder.append(' ');
				WordAnnotation annotation = new WordAnnotation(cas, begin, end);
				annotation.setTag(token.tag());
				annotation.setLemma(token.lemma());
				annotation.addToIndexes();
			}
			cas.setDocumentText(builder.toString());
			cas.setDocumentLanguage("lv");
			this.getTokens().clear();
			return cas;
		} catch (Exception e) {
			cas.release();
			throw new AnalysisEngineProcessException(e);
		}
	}
	
	private class Token {
		
		private String word;
		
		public String word() {
			return this.word;
		}
		
		private String tag;
		
		public String tag() {
			return this.tag;
		}
		
		private String lemma;
		
		public String lemma() {
			return this.lemma;
		}
		
		public Token(String word, String tag, String lemma) {
			this.word = word;
			this.tag = tag;
			this.lemma = lemma;
		}
		
		public String toString() {
			return this.word + " " + this.tag + " " + this.lemma;
		}
		
	}
	
}
