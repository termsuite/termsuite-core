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
package fr.univnantes.termsuite.api;

import static java.util.stream.Collectors.toSet;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.inject.Guice;
import com.google.inject.Injector;

import fr.univnantes.termsuite.alignment.BilingualAlignmentService;
import fr.univnantes.termsuite.framework.TermSuiteFactory;
import fr.univnantes.termsuite.framework.modules.AlignerModule;
import fr.univnantes.termsuite.index.Terminology;
import fr.univnantes.termsuite.metrics.Cosine;
import fr.univnantes.termsuite.metrics.Jaccard;
import fr.univnantes.termsuite.metrics.SimilarityDistance;
import fr.univnantes.termsuite.model.IndexedCorpus;
import fr.univnantes.termsuite.model.Lang;
import fr.univnantes.termsuite.model.OccurrenceStore;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.resources.BilingualDictionary;

public class BilingualAligner {
	private Optional<IndexedCorpus> sourceCorpus = Optional.empty();
	private Optional<IndexedCorpus> targetCorpus = Optional.empty();
	private Optional<BilingualDictionary> dico = Optional.empty();
	private Class<? extends SimilarityDistance> distance = Cosine.class;
	
	
	public BilingualAligner setSourceTerminology(Terminology sourceTerminology) {
		Lang lang = sourceTerminology.getLang();
		OccurrenceStore occStore = TermSuiteFactory.createMemoryOccurrenceStore(lang);
		return setSourceTerminology(TermSuiteFactory.createIndexedCorpus(sourceTerminology, occStore));
	}

	public BilingualAligner setTargetTerminology(Terminology targetTerminology) {
		Lang lang = targetTerminology.getLang();
		OccurrenceStore occStore = TermSuiteFactory.createMemoryOccurrenceStore(lang);
		return setTargetTerminology(TermSuiteFactory.createIndexedCorpus(targetTerminology, occStore));
	}

	public BilingualAligner setSourceTerminology(IndexedCorpus sourceCorpus) {
		checkCorpus(sourceCorpus);
		checkLang();
		this.sourceCorpus = Optional.of(sourceCorpus);
		return this;
	}
	
	private void checkCorpus(IndexedCorpus corpus) {
		Collection<Term> terms = corpus.getTerminology().getTerms().values();
		Set<Term> contextualizedSwts = terms.stream()
					.filter(t->t.getWords().size() == 1)
					.filter(t->t.getContext() != null)
					.collect(toSet());
		
		Preconditions.checkArgument(!contextualizedSwts.isEmpty(), 
				"Corpus %s are not contextualized", 
				corpus
				)
		;
	}

	private void checkLang() {
		if(sourceCorpus.isPresent() && targetCorpus.isPresent())
			Preconditions.checkArgument(sourceCorpus.get().getTerminology().getLang() != targetCorpus.get().getTerminology().getLang(),
					"Langues of source and target terminologies must be different. Got twice: " + targetCorpus.get().getTerminology().getLang());
	}

	public BilingualAligner setDistance(Class<? extends SimilarityDistance> distance) {
		this.distance = distance;
		return this;
	}
	
	public BilingualAligner setTargetTerminology(IndexedCorpus targetCorpus) {
		checkCorpus(targetCorpus);
		checkLang();
		this.targetCorpus = Optional.of(targetCorpus);
		return this;
	}
	
	public BilingualAligner setDicoPath(Path dicoPath) {
		Preconditions.checkArgument(dicoPath.toFile().exists(), "File %s does not exist", dicoPath);
		Preconditions.checkArgument(dicoPath.toFile().isFile(), "Not a file: %s", dicoPath);
		try {
			this.dico = Optional.of(BilingualDictionary.load(dicoPath.toString()));
		} catch (IOException e) {
			throw new TermSuiteException(e);
		}
		return this;
	}
	
	/**
	 * Creates the bilingual single-word aligner.
	 * 
	 * @return the aligner object
	 */
	public BilingualAlignmentService create() {
		Preconditions.checkArgument(sourceCorpus.isPresent(), "No source indexed corpus given");
		Preconditions.checkArgument(dico.isPresent(), "No bilingual dictionary given");
		Preconditions.checkArgument(targetCorpus.isPresent(), "No target indexed corpus given");
		
		Injector sourceInjector = TermSuiteFactory.createExtractorInjector(sourceCorpus.get());
		Injector targetInjector = TermSuiteFactory.createExtractorInjector(targetCorpus.get());
		
		AlignerModule alignerModule = new AlignerModule(
				sourceInjector, 
				targetInjector, 
				dico.get(), 
				distance
			);
		Injector alignerInjector = Guice.createInjector(alignerModule);
		BilingualAlignmentService alignService = alignerInjector.getInstance(BilingualAlignmentService.class);
		return alignService;
	}
	
	public BilingualAligner setDistanceCosine() {
		return setDistance(Cosine.class);
	}
	
	public BilingualAligner setDistanceJaccard() {
		return setDistance(Jaccard.class);
	}
}
