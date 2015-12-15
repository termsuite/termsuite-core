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
package eu.project.ttc.models.index;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import eu.project.ttc.engines.desc.Lang;
import eu.project.ttc.models.Document;
import eu.project.ttc.models.Term;
import eu.project.ttc.models.TermBuilder;
import eu.project.ttc.models.TermClass;
import eu.project.ttc.models.TermIndex;
import eu.project.ttc.models.TermOccurrence;
import eu.project.ttc.models.TermVariation;
import eu.project.ttc.models.TermWord;
import eu.project.ttc.models.Word;
import eu.project.ttc.types.TermOccAnnotation;
import eu.project.ttc.types.WordAnnotation;
import eu.project.ttc.utils.TermSuiteUtils;

/**
 * The in-memory implementation of a {@link TermIndex}.
 * 
 * @author Damien Cram
 *
 */
public class MemoryTermIndex implements TermIndex {
	private static final Logger LOGGER = LoggerFactory.getLogger(MemoryTermIndex.class);
	private static final String MSG_NO_SUCH_PROVIDER = "No such value provider: %s";
	public static final String MSG_NO_SUCH_TERM = "No such term in term index: %s";

	private static final String MEASURE_WR = "wr";
	private static final String MEASURE_WRLOG = "wrLog";
	private static final String MEASURE_FREQUENCY = "frequency";

	/*
	 * The root index of terms. Variants must not be referenced at 
	 * this level of index. They me be indexed from their base-term
	 * instead. 
	 */
	private Map<Integer, Term> termsById = Maps.newHashMap();
	private Map<String, Term> termsByGroupingKey = Maps.newHashMap();
	private Map<String, CustomTermIndex> customIndexes = Maps.newHashMap();
	private Map<String, TermMeasure> termMeasures = Maps.newHashMap();
	private Map<String, Word> wordIndex = Maps.newHashMap();
	private Map<String, Document> documents = Maps.newHashMap();
	private Set<TermClass> termClasses = Sets.newHashSet();
	
	public void inspect() {
		System.out.format("termsById: %d\n", termsById.size());
		System.out.format("termsByGroupingKey: %d\n", termsByGroupingKey.size());
		System.out.format("wordIndex: %d\n", wordIndex.size());
		System.out.format("documents: %d\n", documents.size());
		System.out.format("termClasses: %d\n", termClasses.size());
		System.out.format("customIndexes: %d\n", customIndexes.size());
		for(Document d:documents.values()) {
			d.inspect("\t");
		}
	}

	private String name;
	private Lang lang;
	private String corpusId;
	
	private int currentId = 0;
	private int nbWordAnnotations = 0;
	private int nbSpottedTerms = 0;
	
	public MemoryTermIndex(String name, Lang lang) {
		this.lang = lang;
		this.name = name;
		this.termMeasures.put(MEASURE_WR, new WRMeasure(this));
		this.termMeasures.put(MEASURE_WRLOG, new WRLogMeasure(this));
		this.termMeasures.put(MEASURE_FREQUENCY, new FrequencyMeasure(this));
	}

	@Override
	public void addTerm(Term term) {
		Preconditions.checkArgument(
				!this.termsByGroupingKey.containsKey(term.getGroupingKey()));
		Preconditions.checkNotNull(term.getId());
		Preconditions.checkArgument(!this.termsById.containsKey(term.getId()));

		this.termsByGroupingKey.put(term.getGroupingKey(), term);
		this.termsById.put(term.getId(), term);
		for(CustomTermIndex termIndex:this.customIndexes.values())
			termIndex.indexTerm(term);
		for(TermWord tw:term.getWords())
			privateAddWord(tw.getWord(), false);
	}


	@Override
	public void addWord(Word word) {
		privateAddWord(word, true);
	}

	private void privateAddWord(Word word, boolean failIfAlredyPresent) {
		if(failIfAlredyPresent)
			Preconditions.checkArgument(
					!this.wordIndex.containsKey(word.getLemma()));
		this.wordIndex.put(word.getLemma(), word);
	}

	@Override
	public Collection<Term> getTerms() {
		return Collections.unmodifiableCollection(this.termsByGroupingKey.values());
	}

	@Override
	public Word getWord(String wordId) {
		return this.wordIndex.get(wordId);
	}

	public Word addWord(WordAnnotation anno) {
		String swKey = anno.getLemma();
		Word word = this.wordIndex.get(swKey);
		if(word == null) {
			word = new Word(anno.getLemma(), anno.getStem());
			this.wordIndex.put(swKey, word);
		}
		return word;
	}


	@Override
	public Term addTermOccurrence(TermOccAnnotation annotation, String fileUrl, boolean keepOccurrence) {
		this.nbSpottedTerms++;
		String termGroupingKey = TermSuiteUtils.getGroupingKey(annotation);
		Term term = this.termsByGroupingKey.get(termGroupingKey);
		if(term == null) {
			TermBuilder builder = TermBuilder.start(this);
			for (int i = 0; i < annotation.getWords().size(); i++) {
				WordAnnotation wa = annotation.getWords(i);
				Word w = this.addWord(wa);
				builder.addWord(
						w, 
						annotation.getPattern(i) 
					);
			}
			builder.setSpottingRule(annotation.getSpottingRuleName());
			term = builder.createAndAddToIndex();
		}
		term.addOccurrence(
			new TermOccurrence(
				term, 
				annotation.getCoveredText(), 
				this.getDocument(fileUrl), 
				annotation.getBegin(), 
				annotation.getEnd()),
			keepOccurrence
		);
		return term;
	}

	@Override
	public Iterator<Term> singleWordTermIterator() {
		return new SingleMultiWordIterator(true); 
	}

	@Override
	public Iterator<Term> multiWordTermIterator() {
		return new SingleMultiWordIterator(false); 
	}
	
	@Override
	public Iterator<Term> compoundWordTermIterator() {
		return new CompoundIterator(); 
		};
	
	private abstract class TermIterator extends AbstractIterator<Term> {
		protected Term t;
		protected Iterator<Term> it;
		
		private TermIterator() {
			super();
			this.it = MemoryTermIndex.this.termsByGroupingKey.values().iterator();
		}
	}
	
	private class SingleMultiWordIterator extends TermIterator {
		/*
		 * Single word it if false, multiword if true
		 */
		private boolean singleMultiWordToggle;
		
		private SingleMultiWordIterator(boolean singleMultiWordToogle) {
			super();
			this.singleMultiWordToggle = singleMultiWordToogle;
		}


		@Override
		protected Term computeNext() {
			while(it.hasNext()) {
				if((t = it.next()).isSingleWord() == this.singleMultiWordToggle)
					return t;
			}
			return endOfData();
		}
	}
	
	private class CompoundIterator extends TermIterator {

		@Override
		protected Term computeNext() {
			while(it.hasNext()) {
				if((t = it.next()).isSingleWord() && t.isCompound())
					return t;
			}
			return endOfData();
		}
		
	}

	@Override
	public int newId() {
		while(this.termsById.containsKey(currentId))
			this.currentId++;
		return this.currentId;
	}
	
//	@Override
//	public TermBuilder newTerm(String termId) {
//		return new TermBuilder(this).setGroupingKey(termId);
//	}

	@Override
	public CustomTermIndex getCustomIndex(String indexName) {
		if(this.customIndexes.get(indexName) == null) {
			TermValueProvider valueProvider = TermValueProviders.get(indexName);
			createCustomIndex(indexName, valueProvider);
		}
		return this.customIndexes.get(indexName);
	}

	@Override
	public CustomTermIndex createCustomIndex(String indexName,
			TermValueProvider valueProvider) {
//		Preconditions.checkArgument(
//				!this.customIndexes.containsKey(indexName),
//				String.format("Custom term index %s already exists.", indexName));
		Preconditions.checkArgument(valueProvider != null, 
				MSG_NO_SUCH_PROVIDER,
				indexName);
		CustomTermIndexImpl customIndex = new CustomTermIndexImpl(valueProvider);
		this.customIndexes.put(indexName, customIndex);

		LOGGER.debug("Indexing {} terms to index {}", this.getTerms().size(), indexName);
		for(Term t:this.getTerms()) 
			customIndex.indexTerm(t);
		return customIndex;
	}

	@Override
	public void dropCustomIndex(String indexName) {
		this.customIndexes.remove(indexName);
	}

	@Override
	public Collection<Word> getWords() {
		return Collections.unmodifiableCollection(this.wordIndex.values());
	}

	@Override
	public Term getTermByGroupingKey(String groupingKey) {
//		Preconditions.checkArgument(this.termsByGroupingKey.containsKey(groupingKey),MSG_NO_SUCH_TERM, groupingKey);
		return this.termsByGroupingKey.get(groupingKey);
	}
	
	@Override
	public Term getTermById(int id) {
		return this.termsById.get(id);
	}

	@Override
	public void cleanOrphanWords() {
		Set<String> usedWordLemmas = Sets.newHashSet();
		for(Term t:getTerms()) {
			for(TermWord tw:t.getWords())
				usedWordLemmas.add(tw.getWord().getLemma());
		}
		Iterator<Entry<String, Word>> it = wordIndex.entrySet().iterator();
		Entry<String, Word> entry;
		while (it.hasNext()) {
			entry = it.next();
			if(!usedWordLemmas.contains(entry.getValue().getLemma()))
				it.remove();
		}
	}

	@Override
	public void removeTerm(Term t) {
		
		termsByGroupingKey.remove(t.getGroupingKey());
		termsById.remove(t.getId());
		
		// remove from custom indexes
		for(CustomTermIndex customIndex:customIndexes.values())
			customIndex.removeTerm(t);
		
		// remove from variants
		Set<TermVariation> toRem = Sets.newHashSet();
		for(TermVariation v:t.getVariations()) 
			toRem.add(v);
		for(TermVariation v:toRem)
			t.removeTermVariation(v);
		
		// remove from variants
		toRem = Sets.newHashSet();
		for(TermVariation v:t.getBases()) 
			toRem.add(v);
		for(TermVariation v:toRem)
			v.getBase().removeTermVariation(v);
		
		/*
		 * Removes from context vectors.
		 * 
		 * We assumes that if this term has a context vector 
		 * then all others terms may have this term as co-term,
		 * thus they must be checked from removal.
		 * 
		 */
		if(t.isContextVectorComputed()) {
			for(Term o:termsById.values()) {
				if(o.isContextVectorComputed())
					o.getContextVector().removeCoTerm(t);
			}
		}
	}

	@Override
	public String getName() {
		return this.name;
	}
	
	@Override
	public int hashCode() {
		return this.name.hashCode();
	}

	
	@Override
	public Document getDocument(String url) {
		Document document = documents.get(url);
		if(document == null) {
			document = new Document(url);
			documents.put(url, document);
		}
		return document;
	}
	
	@Override
	public Collection<Document> getDocuments() {
		return this.documents.values();
	}

	@Override
	public void createOccurrenceIndex() {
		for(Term t:this.getTerms()) {
			for(TermOccurrence o:t.getOccurrences()) {
				/*
				 * Explicitely index all occurrences within each source document. The context 
				 * generation would not work without that step.
				 * 
				 * FIXME Move these occurrence indexes inside the present AE (because the 
				 * indexes are never used anywhere else).
				 */
				o.getSourceDocument().indexTermOccurrence(o);
			}
		}
	}
	
	@Override
	public void clearOccurrenceIndex() {
		for(Document d:this.getDocuments())
			d.clearOccurrenceIndex();
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this).addValue(name)
				.add("terms", this.termsById.size())
				.toString();
	}
	
	@Override
	public Lang getLang() {
		return this.lang;
	}
	
	@Override
	public String getCorpusId() {
		return corpusId;
	}
	
	@Override
	public void setCorpusId(String corpusId) {
		this.corpusId = corpusId;
	}

	@Override
	public Collection<TermClass> getTermClasses() {
		return Collections.unmodifiableSet(termClasses);
	}

	@Override
	public void classifyTerms(Term classHead, Iterable<Term> classTerms) {
		Preconditions.checkArgument(Iterables.contains(classTerms, classHead), "head must be contained in class terms");
		TermClass termClass = new TermClass(classHead, classTerms);
		this.termClasses.add(termClass);
		for(Term t2:termClass)
			t2.setTermClass(termClass);
	}

	@Override
	public void setWordAnnotationsNum(int nbWordAnnotations) {
		this.nbWordAnnotations = nbWordAnnotations;
	}

	@Override
	public int getWordAnnotationsNum() {
		return this.nbWordAnnotations;
	}

	@Override
	public TermMeasure getWRMeasure() {
		return this.termMeasures.get(MEASURE_WR);
	}

	@Override
	public TermMeasure getWRLogMeasure() {
		return this.termMeasures.get(MEASURE_WRLOG);
	}

	@Override
	public TermMeasure getFrequencyMeasure() {
		return this.termMeasures.get(MEASURE_FREQUENCY);
	}

	@Override
	public Iterable<TermMeasure> getMeasures() {
		return this.termMeasures.values();
	}
	
	@Override
	public int getSpottedTermsNum() {
		return nbSpottedTerms;
	}
	
	@Override
	public void setSpottedTermsNum(int spottedTermsNum) {
		this.nbSpottedTerms = spottedTermsNum;
	}
}
