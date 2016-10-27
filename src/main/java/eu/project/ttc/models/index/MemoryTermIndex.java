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
package eu.project.ttc.models.index;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.uima.cas.FSIterator;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import eu.project.ttc.engines.desc.Lang;
import eu.project.ttc.models.Document;
import eu.project.ttc.models.OccurrenceStore;
import eu.project.ttc.models.Term;
import eu.project.ttc.models.TermBuilder;
import eu.project.ttc.models.TermIndex;
import eu.project.ttc.models.TermOccurrence;
import eu.project.ttc.models.TermRelation;
import eu.project.ttc.models.TermWord;
import eu.project.ttc.models.RelationType;
import eu.project.ttc.models.Word;
import eu.project.ttc.models.index.selectors.TermSelector;
import eu.project.ttc.types.SourceDocumentInformation;
import eu.project.ttc.types.TermOccAnnotation;
import eu.project.ttc.types.WordAnnotation;
import eu.project.ttc.utils.JCasUtils;
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

	private static final String MEASURE_WR = "wr";
	private static final String MEASURE_WRLOG = "wrLog";
	private static final String MEASURE_FREQUENCY = "frequency";

	/**
	 * The occurrence store 
	 */
	private OccurrenceStore occurrenceStore;

	/*
	 * The root index of terms. Variants must not be referenced at 
	 * this level of index. They me be indexed from their base-term
	 * instead. 
	 */
	private Map<Integer, Term> termsById = Maps.newHashMap();
	private Map<String, Term> termsByGroupingKey = Maps.newHashMap();
	private Map<String, CustomTermIndex> customIndexes = Maps.newHashMap();
	private Map<String, Word> wordIndex = Maps.newHashMap();
	private Map<String, Document> documents = Maps.newHashMap();
	private Multimap<Term, TermRelation> outboundVariations = HashMultimap.create();
	private Multimap<Term, TermRelation> inboundVariations = HashMultimap.create();

	private String name;
	private Lang lang;
	private String corpusId;
	
	private int currentDocumentId = 0;
	private int currentId = 0;
	private int nbWordAnnotations = 0;
	private int nbSpottedTerms = 0;
	
	public MemoryTermIndex(String name, Lang lang, OccurrenceStore occurrenceStore) {
		this.lang = lang;
		this.name = name;
		this.occurrenceStore = occurrenceStore;
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
			termIndex.indexTerm(this, term);
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

	private Word addWord(WordAnnotation anno) {
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
	

	@Override
	public CustomTermIndex getCustomIndex(String indexName) {
		if(this.customIndexes.get(indexName) == null) {
			TermValueProvider valueProvider = TermValueProviders.get(indexName, this);
			createCustomIndex(indexName, valueProvider);
		}
		return this.customIndexes.get(indexName);
	}

	@Override
	public CustomTermIndex createCustomIndex(String indexName,
			TermValueProvider valueProvider) {
		Preconditions.checkArgument(valueProvider != null, 
				MSG_NO_SUCH_PROVIDER,
				indexName);
		CustomTermIndexImpl customIndex = new CustomTermIndexImpl(valueProvider);
		this.customIndexes.put(indexName, customIndex);

		LOGGER.debug("Indexing {} terms to index {}", this.getTerms().size(), indexName);
		for(Term t:this.getTerms()) 
			customIndex.indexTerm(this, t);
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
		
		removeTermOnly(t);
		occurrenceStore.removeTerm(t);
	}

	private void removeTermOnly(Term t) {
		termsByGroupingKey.remove(t.getGroupingKey());
		termsById.remove(t.getId());
		
		
		
		// remove from custom indexes
		for(CustomTermIndex customIndex:customIndexes.values())
			customIndex.removeTerm(this, t);
		
		// remove from variants
		List<TermRelation> toRem = Lists.newLinkedList();
		toRem.addAll(this.outboundVariations.removeAll(t));
		toRem.addAll(this.inboundVariations.removeAll(t));
		toRem.forEach(this::removeRelation);
		
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
			document = new Document(currentDocumentId++, url);
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
	public void setWordAnnotationsNum(int nbWordAnnotations) {
		this.nbWordAnnotations = nbWordAnnotations;
	}

	@Override
	public int getWordAnnotationsNum() {
		return this.nbWordAnnotations;
	}
	@Override
	public int getSpottedTermsNum() {
		return nbSpottedTerms;
	}
	
	@Override
	public void setSpottedTermsNum(int spottedTermsNum) {
		this.nbSpottedTerms = spottedTermsNum;
	}
	
	@Override
	public OccurrenceStore getOccurrenceStore() {
		return this.occurrenceStore;
	}

	@Override
	public void deleteMany(TermSelector selector) {
		List<Term> rem = Lists.newArrayList();
		for(Term t:termsById.values()) {
			if(selector.select(t))
				rem.add(t);
		}
		for(Term t:rem)
			removeTermOnly(t);
		occurrenceStore.deleteMany(selector);
		
	}

	@Override
	public void importCas(JCas cas, boolean keepOccurrence) {
		SourceDocumentInformation sdi = JCasUtils.getSourceDocumentAnnotation(cas).get();
		FSIterator<Annotation> iterator = cas.getAnnotationIndex().iterator();
		while(iterator.hasNext()) {
			Annotation anno = iterator.next();
			if(anno instanceof WordAnnotation) {
				this.nbWordAnnotations++;
			}
			else if(anno instanceof TermOccAnnotation) {
				addTermOccurrence((TermOccAnnotation)anno, sdi.getUri(), keepOccurrence);
			} 
		}
	}

	@Override
	public Stream<TermRelation> getRelations(RelationType... types) {
		if(types.length == 0)
			return this.inboundVariations.entries().stream().map(e -> (TermRelation)e.getValue());
		else {
			Set<RelationType> typeSet = Sets.newHashSet(types);
			return this.inboundVariations.entries().stream().map(e -> (TermRelation)e.getValue())
					.filter(tv -> typeSet.contains(tv.getType()));
		}
	}

	@Override
	public Collection<TermRelation> getOutboundRelations(Term base, RelationType... types) {
		if(types.length == 0)
			return this.outboundVariations.get(base);
		else {
			Set<RelationType> typeSet = Sets.newHashSet(types);
			return this.outboundVariations.get(base).stream()
					.filter(tv -> typeSet.contains(tv.getType()))
					.collect(Collectors.toSet());
		}
	}

	@Override
	public Collection<TermRelation> getInboundTerRelat(Term variant, RelationType... types) {
		if(types.length == 0)
			return this.inboundVariations.get(variant);
		else {
			Set<RelationType> typeSet = Sets.newHashSet(types);
			List<TermRelation> list = this.inboundVariations.get(variant).stream()
					.filter(tv -> typeSet.contains(tv.getType()))
					.collect(Collectors.toList());
			Collections.sort(list);
			return list;
		}
	}
	
	@Override
	public TermRelation addRelation(Term base, Term variant, RelationType type, Object info) {
		TermRelation tv = new TermRelation(type, base, variant, info);
		this.addRelation(tv);
		return tv;
	}
	
	@Override
	public void addRelation(TermRelation termVariation) {
		this.outboundVariations.put(termVariation.getFrom(), termVariation);
		this.inboundVariations.put(termVariation.getTo(), termVariation);
		
	}

	@Override
	public void removeRelation(TermRelation variation) {
		this.outboundVariations.remove(variation.getFrom(), variation);
		this.inboundVariations.remove(variation.getTo(), variation);
	}
}
