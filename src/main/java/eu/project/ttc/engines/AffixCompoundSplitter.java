/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright 2, 2015nership.  The ASF licenses this file
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

import java.util.Iterator;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.ExternalResource;
import org.apache.uima.jcas.JCas;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.project.ttc.models.CompoundType;
import eu.project.ttc.models.Term;
import eu.project.ttc.models.Tree;
import eu.project.ttc.models.Word;
import eu.project.ttc.models.WordBuilder;
import eu.project.ttc.resources.Bank;
import eu.project.ttc.resources.TermIndexResource;
import fr.univnantes.lina.UIMAProfiler;

public class AffixCompoundSplitter extends JCasAnnotator_ImplBase {
	private static final Logger LOGGER = LoggerFactory.getLogger(AffixCompoundSplitter.class);

	public static final String BANK = "Bank";
	@ExternalResource(key = BANK, mandatory = true)
	private Bank bank;

	@ExternalResource(key = TermIndexResource.TERM_INDEX, mandatory = true)
	private TermIndexResource termIndexResource;
	
	public static final String IS_NEO_CLASSICAL = "IsNeoClassical";
	@ConfigurationParameter(name = IS_NEO_CLASSICAL, mandatory = false, defaultValue = "false")
	private Boolean isNeoClassical = false;

	@Override
	public void collectionProcessComplete()
			throws AnalysisEngineProcessException {
		UIMAProfiler.getProfiler("AnalysisEngine").start(this, "process");
		LOGGER.info("Detecting "+ (this.isNeoClassical?"neoclassical":"prefix") +" compounds");				
		Iterator<Term> it = this.termIndexResource.getTermIndex().singleWordTermIterator();
		while (it.hasNext()) {
			Word word = it.next().firstWord().getWord();
			if(word.isCompound() || word.getLemma().length()<=3)
				continue;
			this.findPrefix(word, 0, this.bank.getPrefixTree());
			this.findSuffix(word, word.getLemma().length()-1,this.bank.getSuffixTree());
		}
		UIMAProfiler.getProfiler("AnalysisEngine").stop(this, "process");
	}
	
	@Override
	public void process(JCas cas) throws AnalysisEngineProcessException {
		// nothing to do
	}

	private void findPrefix(Word w, int index, Tree<Character> currentTree) {
		if (index < w.getLemma().length()) {
			char ch = w.getLemma().charAt(index);
			Character c = Character.toLowerCase(ch);
			Tree<Character> tree = currentTree.getSubTree(c);
			if (tree == null) {
				if (currentTree.isLeaf()) { 
					this.addRootComponent(w, 0, index, CompoundType.PREFIX);
					
					// TODO Is it useful to find prefix chaining for termino starting from w[index + 1] ?
					
					/*
					tree = this.bank.getPrefixTree().getSubTree(c);
					if (tree != null) {  
						this.getPrefixes(w,index + 1,tree);					
					}
					*/
				}
			} else {
				this.findPrefix(w,index + 1,tree);
			}
		}
	}
	
	private void findSuffix(Word w, int index, Tree<Character> currentTree) {
		if (index >= 0) {
			char ch = w.getLemma().charAt(index);
			Character c = Character.toLowerCase(ch);
			Tree<Character> tree = currentTree.getSubTree(c);
			if (tree == null) {
				if (currentTree.isLeaf()) {
					this.addRootComponent(w, index, w.getLemma().length(), CompoundType.SUFFIX);
					
					// TODO Is it useful to find suffix chaining for termino starting from w[index + 1] ?
					
					/*
					tree = this.bank.getSuffixTree().getSubTree(c);
					if (tree != null) { 
						this.getSuffixes(cas,begin,index,index - 1,tree);
					}
					*/
				}
			} else {
				this.findSuffix(w,index - 1,tree);
			}
		}
	}
	
	private void addRootComponent(Word w, int from, int to, CompoundType type /* true -> prefix, false -> suffix*/) {
		WordBuilder builder = new WordBuilder(w)
			.addComponent(from, to, w.getLemma().substring(from, to));
		if(type==CompoundType.SUFFIX)
			builder.setCompoundType(CompoundType.SUFFIX);
		else if(type==CompoundType.PREFIX)
			builder.setCompoundType(CompoundType.PREFIX);
		builder.create();
	}
}
