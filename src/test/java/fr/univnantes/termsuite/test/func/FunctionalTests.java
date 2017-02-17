
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

package fr.univnantes.termsuite.test.func;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import fr.univnantes.termsuite.index.Terminology;
import fr.univnantes.termsuite.model.Document;
import fr.univnantes.termsuite.model.Lang;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.TermProperty;
import fr.univnantes.termsuite.model.TextCorpus;
import fr.univnantes.termsuite.test.func.api.BilingualAlignerDeEnSpec;
import fr.univnantes.termsuite.test.func.api.BilingualAlignerFrEnSpec;
import fr.univnantes.termsuite.test.func.api.EnglishWindEnergySpec;
import fr.univnantes.termsuite.test.func.api.FrenchWindEnergyProjectorSpec;
import fr.univnantes.termsuite.test.func.api.FrenchWindEnergySpec;
import fr.univnantes.termsuite.test.func.api.GermanWindEnergySpec;
import fr.univnantes.termsuite.test.func.api.SemanticGathererSpec;
import fr.univnantes.termsuite.test.func.api.TerminoFiltererSpec;
import fr.univnantes.termsuite.test.func.io.JsonIOReturnSpec;
import fr.univnantes.termsuite.test.func.tools.AlignerCLISpec;
import fr.univnantes.termsuite.test.func.tools.PreprocessorCLISpec;
import fr.univnantes.termsuite.test.func.tools.TerminologyExtractorCLISpec;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	/*
	 * IO
	 */
	JsonIOReturnSpec.class,
	
	/*
	 * API
	 */
	// extraction engines
	FrenchWindEnergySpec.class,
	EnglishWindEnergySpec.class,
	GermanWindEnergySpec.class,
	SemanticGathererSpec.class,
	// projector
	FrenchWindEnergyProjectorSpec.class,
	// alignement
	BilingualAlignerFrEnSpec.class,
	BilingualAlignerDeEnSpec.class,
	// filterer
	TerminoFiltererSpec.class,
	
	/*
	 * CLI
	 */
	PreprocessorCLISpec.class,
	TerminologyExtractorCLISpec.class,
	AlignerCLISpec.class,
})
public class FunctionalTests {
	

	public static final Path TEST_RESOURCES = Paths.get("src", "test", "resources");
	public static final Path PACKAGE_TEST = TEST_RESOURCES.resolve(Paths.get( "fr", "univnantes", "termsuite", "test"));
	public static final Path PACKAGE_JSON = PACKAGE_TEST.resolve(Paths.get( "json"));
	public static final Path PACKAGE_CORPUS = PACKAGE_TEST.resolve(Paths.get( "corpus"));
	public static final Path PACKAGE_TERMINO = PACKAGE_TEST.resolve(Paths.get( "termino"));

	public static final Path EXTRACTOR_CONFIG_1=PACKAGE_JSON.resolve(Paths.get("extractor-config1.json"));
	public static final Path TERMINOLOGY_1=PACKAGE_JSON.resolve(Paths.get("termino1.json"));
	public static final Path CORPUS1_PATH=PACKAGE_CORPUS.resolve(Paths.get("corpus1"));
	public static final Path CORPUS2_PATH=PACKAGE_CORPUS.resolve(Paths.get("corpus2"));
	public static final Path TERMINO_WESHORT_PATH=PACKAGE_TEST.resolve(Paths.get("termino"));
	public static final Path DICO_PATH=PACKAGE_TEST.resolve(Paths.get("dico"));

			
	public static final Path CORPUS_WESHORT_PATH=PACKAGE_TEST.resolve("corpus").resolve("weshort");
	public static final Path CORPUS_WE_PATH=PACKAGE_TEST.resolve("corpus").resolve("we");
	public static final Path CORPUS_MOBILE_PATH=PACKAGE_TEST.resolve("corpus").resolve("mobile");
	
	private static final String FUNCTION_TESTS_CONFIG = "termsuite-test.properties";
	private static final String PROP_TREETAGGER_HOME_PATH = "treetagger.home.path";
	public static final TextCorpus CORPUS1 = new TextCorpus(Lang.FR, CORPUS1_PATH);

	private static Object getConfigProperty( String propName) {
		InputStream is = FunctionalTests.class.getClassLoader().getResourceAsStream(FUNCTION_TESTS_CONFIG);
		Properties properties = new Properties();
		try {
			properties.load(is);
			is.close();
			Preconditions.checkArgument(!properties.contains(propName), "No such property in config file %s: %s", FUNCTION_TESTS_CONFIG, propName);
			return properties.get(propName);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static TextCorpus getCorpusWEShort(Lang lang) {
		return new TextCorpus(lang, getCorpusWEShortPath(lang));
	}

	public static TextCorpus getCorpusWE(Lang lang) {
		return new TextCorpus(lang, getCorpusWEPath(lang));
	}
	
	public static Path getCorpusWEShortPath(Lang lang) {
		return CORPUS_WESHORT_PATH.resolve(lang.getName().toLowerCase()).resolve("txt");
	}

	public static Path getTsvPreprocessedCorpusWEShortPath(Lang lang) {
		return CORPUS_WESHORT_PATH.resolve(lang.getName().toLowerCase()).resolve("tsv");
	}

	public static Path getPreprocessedCorpusWEShortPathAsTermino(Lang lang) {
		return CORPUS_WESHORT_PATH.resolve(lang.getName().toLowerCase()).resolve("termino-imported.json");
	}

	public static Path getXmiPreprocessedCorpusWEShortPath(Lang lang) {
		return CORPUS_WESHORT_PATH.resolve(lang.getName().toLowerCase()).resolve("xmi");
	}

	public static Path getJsonPreprocessedCorpusWEShortPath(Lang lang) {
		return CORPUS_WESHORT_PATH.resolve(lang.getName().toLowerCase()).resolve("json");
	}

	public static Path getCorpusMobilePath(Lang lang) {
		return CORPUS_MOBILE_PATH.resolve(lang.getName().toLowerCase()).resolve( "txt");
	}
	
	public static Path getCorpusWEPath(Lang lang) {
		return CORPUS_WE_PATH.resolve(lang.getName().toLowerCase()).resolve( "txt");
	}

	public static Path getTerminoWEShortPath(Lang lang) {
		return TERMINO_WESHORT_PATH.resolve("we-short-" +  lang.getCode() + ".json");
	}

	public static Path getDicoPath(Lang source, Lang target) {
		String dicoFileName = String.format("%s-%s.txt", source.getCode(), target.getCode());
		return DICO_PATH.resolve(dicoFileName);
	}

	public static Path getTaggerPath() {
		return Paths.get((String)getConfigProperty(PROP_TREETAGGER_HOME_PATH));
	}
	
	public static List<Term> termsByProperty(Terminology termino, TermProperty termProperty, boolean desc) {
		List<Term> terms = Lists.newArrayList(termino.getTerms().values());
		Collections.sort(terms, termProperty.getComparator(desc));
		return terms;
	}

	public static Path getTestsOutputFile(String relativePath) {
		return getFunctionalTestsOutputDir().resolve(relativePath);
	}
	
	public static Path getFunctionalTestsOutputDir() {
		return Paths.get(getConfigProperty("tests.output").toString());
	}

	public static Path getFunctionalTestsControlDir() {
		return getFunctionalTestsOutputDir().resolve("control");
	}

	public static Path getTestTmpDir() {
		Path path = Paths.get(System.getProperty("java.io.tmpdir"), "termsuite-tests");
		return createIfNotExist(path);
	}
		
	private static Path createIfNotExist(Path path) {
		if(!path.toFile().exists())
			path.toFile().mkdirs();
		return path;
	}

	public static Path getCachedWindEnergyPreprocessedCorpusFile(Lang lang) {
		return getTestTmpDir().resolve("we-" + lang.getCode() + ".json");
	}

	public static Document getMobileTechnologyDocument(Lang lang, String filename) {
		return new Document(lang, getCorpusMobilePath(lang).resolve(filename).toUri().getPath());
	}
	public static Document getWindEnergyDocument(Lang lang, String filename) {
		return new Document(lang, getCorpusWEPath(lang).resolve(filename).toUri().getPath());
	}

}
