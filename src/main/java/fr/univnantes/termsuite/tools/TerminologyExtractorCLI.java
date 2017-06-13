package fr.univnantes.termsuite.tools;

import java.nio.file.Path;

import org.slf4j.LoggerFactory;

import fr.univnantes.termsuite.api.IndexedCorpusIO;
import fr.univnantes.termsuite.api.Preprocessor;
import fr.univnantes.termsuite.api.TXTCorpus;
import fr.univnantes.termsuite.api.TermSuite;
import fr.univnantes.termsuite.api.TerminoExtractor;
import fr.univnantes.termsuite.framework.TermSuiteFactory;
import fr.univnantes.termsuite.model.IndexedCorpus;
import fr.univnantes.termsuite.model.Lang;
import fr.univnantes.termsuite.model.Tagger;
import fr.univnantes.termsuite.tools.opt.TermSuiteCliOption;

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

/**
 * 
 * Command line interface for the Corpus Preprocessing + Terminology extraction (Indexer) engines.
 * 
 * @author Damien Cram
 * 
 * @see Preprocessor
 * @see TerminoExtractor
 */
public class TerminologyExtractorCLI extends CommandLineClient {// NO_UCD (public entry point)

	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(TerminologyExtractorCLI.class);
	
	public TerminologyExtractorCLI() {
		super("Extracts terminology from a domain-specific textual corpus (or preprocessed corpus).");
	}
	
	public TerminologyExtractorCLI(String description) {
		super(description);
	}

	@Override
	public void configureOpts() {
		
		// Either from indexed corpus or prepared corpus
		declareExactlyOneOf(
				TermSuiteCliOption.FROM_TXT_CORPUS_PATH, 
				TermSuiteCliOption.FROM_PREPARED_CORPUS_PATH);
		
		declareFacultative(TermSuiteCliOption.LANGUAGE);

		// at least one output necessary
		declareAtLeastOneOf(
				TermSuiteCliOption.JSON, 
				TermSuiteCliOption.TSV, 
				TermSuiteCliOption.TBX);

		clientHelper.declareResourceOpts();
		clientHelper.declareHistory();
		
		/*
		 * Preprocessor options
		 */
		declareFacultative(TermSuiteCliOption.TAGGER);
		declareConditional(
				TermSuiteCliOption.FROM_TXT_CORPUS_PATH, 
				TermSuiteCliOption.TAGGER_PATH);
		declareFacultative(TermSuiteCliOption.ENCODING);
		
		/*
		 * Big corpus options
		 */
		clientHelper.declareBigCorpusOptions();

		/*
		 * Extractor options
		 */
		clientHelper.declareExtractorOptions();
		clientHelper.declareTsvOptions();
	}

	@Override
	protected void run() throws Exception {
		/*
		 * Fails fast on output options (before pipeline execution)
		 */
		checkOutputOptions();

		TerminoExtractor extractor = TermSuite.terminoExtractor();
		
		extractor.setOptions(clientHelper.getExtractorOptions(getLang()));
		
		extractor.setResourceConfig(clientHelper.getResourceConfig());

		if(clientHelper.getHistory().isPresent())
			extractor.setHistory(clientHelper.getHistory().get());
		
		IndexedCorpus corpus = getIndexedCorpus();
		extractor.execute(corpus);
		
		if(extractor.getStats().isPresent()) 
			logger.debug("Engine times: {}", extractor.getStats().get().getEngineStats());
		
		if(isSet(TermSuiteCliOption.JSON))
			TermSuiteFactory.createJsonExporter().export(corpus, asPath(TermSuiteCliOption.JSON));
		if(isSet(TermSuiteCliOption.TSV))
			TermSuiteFactory.createTsvExporter(clientHelper.getTsvOptions()).export(corpus, asPath(TermSuiteCliOption.TSV));
		if(isSet(TermSuiteCliOption.TBX))
			TermSuiteFactory.createTbxExporter().export(corpus, asPath(TermSuiteCliOption.TBX));
	}

	private void checkOutputOptions() {
		if(isSet(TermSuiteCliOption.JSON))
			asPath(TermSuiteCliOption.JSON);
		if(isSet(TermSuiteCliOption.TSV)) {
			clientHelper.getTsvOptions();
			asPath(TermSuiteCliOption.TSV);
		}
		if(isSet(TermSuiteCliOption.TBX))
			asPath(TermSuiteCliOption.TBX);
	} 

	public Lang getLang() {
		if(isSet(TermSuiteCliOption.FROM_TXT_CORPUS_PATH) || isSet(TermSuiteCliOption.FROM_PREPARED_CORPUS_PATH))
			return super.getLang();
		else 
			return getIndexedCorpus().getTerminology().getLang();
	}
	
	protected IndexedCorpus getIndexedCorpus() {
		if(isSet(TermSuiteCliOption.FROM_PREPARED_CORPUS_PATH)) {
			Path path = asPath(TermSuiteCliOption.FROM_PREPARED_CORPUS_PATH);
			if(path.toFile().isDirectory()) {
				return TermSuite.toIndexedCorpus(
						TermSuiteFactory.createPreprocessedCorpus(getLang(), path), 
						clientHelper.getCappedSize());
			} else {
				return IndexedCorpusIO.fromJson(path);
			}
		} else if(isSet(TermSuiteCliOption.FROM_TXT_CORPUS_PATH)) {
			Preprocessor preprocessor = TermSuite.preprocessor()
						.setTaggerPath(asPath(TermSuiteCliOption.TAGGER_PATH));
			
			if(isSet(TermSuiteCliOption.TAGGER))
				preprocessor.setTagger(Tagger.forName(asString(TermSuiteCliOption.TAGGER)));
			
			if(clientHelper.getHistory().isPresent())
				preprocessor.setHistory(clientHelper.getHistory().get());
			
			preprocessor.setResourceOptions(clientHelper.getResourceConfig());
			
			
			TXTCorpus txtCorpus = clientHelper.getTxtCorpus();
			IndexedCorpus indexedCorpus = preprocessor.toIndexedCorpus(
					txtCorpus, 
					clientHelper.getCappedSize(),
					clientHelper.getOccurrenceStore(txtCorpus.getLang()));
			return indexedCorpus;
		} else
			throw new IllegalStateException("Unexpected terminology extractor input state.");
	}


	public static void main(String[] args) {
		new TerminologyExtractorCLI().launch(args);
	}
}
