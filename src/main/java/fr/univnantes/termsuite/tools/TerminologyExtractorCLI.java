package fr.univnantes.termsuite.tools;

import java.nio.file.Path;

import fr.univnantes.termsuite.api.IndexedCorpusIO;
import fr.univnantes.termsuite.api.Preprocessor;
import fr.univnantes.termsuite.api.TermSuite;
import fr.univnantes.termsuite.api.TerminoExtractor;
import fr.univnantes.termsuite.framework.TermSuiteFactory;
import fr.univnantes.termsuite.model.IndexedCorpus;
import fr.univnantes.termsuite.model.Lang;
import fr.univnantes.termsuite.model.Tagger;
import fr.univnantes.termsuite.tools.opt.CliOption;

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

	@Override
	protected void configureOpts() {
		declareExactlyOneOf(
				CliOption.FROM_TXT_CORPUS_PATH, 
				CliOption.FROM_PREPARED_CORPUS_PATH);
		
		declareAtLeastOneOf(
				CliOption.JSON, 
				CliOption.TSV, 
				CliOption.TBX);

		clientHelper.declareResourceOpts();
		clientHelper.declareHistory();
		
		/*
		 * Preprocessor options
		 */
		declareFacultative(CliOption.TAGGER);
		declareConditional(
				CliOption.FROM_TXT_CORPUS_PATH, 
				CliOption.LANGUAGE,
				CliOption.TAGGER_PATH,
				CliOption.LANGUAGE);
		declareFacultative(CliOption.ENCODING);
		
		/*
		 * Extractor options
		 */
		clientHelper.declareExtractorOptions();
		clientHelper.declareTsvOptions();
	}

	@Override
	protected void run() throws Exception {
		TerminoExtractor extractor = TermSuite.terminoExtractor();
		
		extractor.setOptions(clientHelper.getExtractorOptions(getLang()));
		
		extractor.setResourceConfig(clientHelper.getResourceConfig());

		if(clientHelper.getHistory().isPresent())
			extractor.setHistory(clientHelper.getHistory().get());
		
		IndexedCorpus corpus = getIndexedCorpus();
		extractor.execute(corpus);
		
		if(isSet(CliOption.JSON))
			TermSuiteFactory.createJsonExporter().export(corpus, asPath(CliOption.JSON));
		if(isSet(CliOption.TSV))
			TermSuiteFactory.createTsvExporter(clientHelper.getTsvOptions()).export(corpus, asPath(CliOption.TSV));
		if(isSet(CliOption.TBX))
			TermSuiteFactory.createTbxExporter().export(corpus, asPath(CliOption.TBX));
	} 

	public Lang getLang() {
		if(isSet(CliOption.FROM_TXT_CORPUS_PATH))
			return super.getLang();
		else 
			return getIndexedCorpus().getTerminology().getLang();
	}
	
	private IndexedCorpus getIndexedCorpus() {
		if(isSet(CliOption.FROM_PREPARED_CORPUS_PATH)) {
			Path path = asPath(CliOption.FROM_PREPARED_CORPUS_PATH);
			if(path.toFile().isDirectory()) {
				CliUtil.throwException("Extracting terminology from an XMI corpus not yet supported: %s", path);
				return null;
			} else {
				return IndexedCorpusIO.fromJson(path);
			}
		} else if(isSet(CliOption.FROM_TXT_CORPUS_PATH)) {
			Preprocessor preprocessor = TermSuite.preprocessor()
						.setTaggerPath(asPath(CliOption.TAGGER_PATH));
			
			if(isSet(CliOption.TAGGER))
				preprocessor.setTagger(Tagger.forName(asString(CliOption.TAGGER)));
			
			if(clientHelper.getHistory().isPresent())
				preprocessor.setHistory(clientHelper.getHistory().get());
			
			preprocessor.setResourceOptions(clientHelper.getResourceConfig());
			
			IndexedCorpus indexedCorpus = preprocessor.toIndexedCorpus(clientHelper.getTxtCorpus(), 500000);
			return indexedCorpus;
		} else
			throw new IllegalStateException("Unexpected terminology extractor input state.");
	}

	public static void main(String[] args) {
		new TerminologyExtractorCLI().launch(args);
	}
}
