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
package fr.univnantes.termsuite.test.func.framework;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

import com.google.inject.Guice;
import com.google.inject.Injector;

import fr.univnantes.julestar.uima.resources.MultimapFlatResource;
import fr.univnantes.termsuite.api.ResourceConfig;
import fr.univnantes.termsuite.engines.gatherer.YamlRuleSet;
import fr.univnantes.termsuite.framework.modules.LangModule;
import fr.univnantes.termsuite.framework.modules.ResourceModule;
import fr.univnantes.termsuite.framework.service.TermSuiteResourceManager;
import fr.univnantes.termsuite.model.Lang;
import fr.univnantes.termsuite.uima.ResourceType;
import fr.univnantes.termsuite.uima.resources.preproc.SimpleWordSet;

public class TermSuiteResourceManagerSpec {
	
	private static final Path CUSTOM_JAR_PATH = Paths.get("./src/test/resources/fr/univnantes/termsuite/test/resources/custom-res.jar");
	private static final Path CUSTOM_DIR_PATH = Paths.get("./src/test/resources/fr/univnantes/termsuite/test/resources/custompath");
	
	private ResourceConfig resourceConfig;
	
	@Before
	public void setup() {
		resourceConfig = new ResourceConfig();
	}

	public Injector injector() {
		return Guice.createInjector(new LangModule(Lang.FR), new ResourceModule(resourceConfig));
	}
	
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
	@Test
	public void testLoadResourcesFromClasspath() {
		resourceMgr().loadResource(ResourceType.ALLOWED_CHARS);
		// should not raise exception
	}

	public TermSuiteResourceManager resourceMgr() {
		return injector().getInstance(TermSuiteResourceManager.class);
	}

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	
	@Test
	public void testLoadCustomResourcesPath() throws IOException {
		Path dicoPath = Paths.get(folder.getRoot().getAbsolutePath(), "custom-dico");
		resourceConfig.addCustomResourcePath(ResourceType.SYNONYMS, dicoPath);
		
		try(Writer writer = new FileWriter(dicoPath.toFile())) {
			writer.write("tata\ttiti\n");
			writer.write("tata\ttoto\n");
			writer.write("tutu\ttoto\n");
		}
		
		MultimapFlatResource dico = resourceMgr().get(
				MultimapFlatResource.class, 
				ResourceType.SYNONYMS);
		
		assertThat(dico.getValues("tata"))
			.hasSize(2)
			.contains("titi", "toto");
		assertThat(dico.getValues("tutu"))
			.hasSize(1)
			.contains("toto");
	}

	@Test
	public void testLoadResourcesFromCustomDirectory() {
		SimpleWordSet dico = resourceMgr().get(
				SimpleWordSet.class, 
				ResourceType.DICO);
		assertThat(dico.getElements())
			.contains("étoile")
			.hasSize(58179);
		
		resourceConfig.addDirectory(CUSTOM_DIR_PATH);
		
		SimpleWordSet customDico = resourceMgr().get(
				SimpleWordSet.class, 
				ResourceType.DICO);
		
		assertThat(customDico.getElements())
			.contains("toto")
			.contains("tata")
			.hasSize(2);
	}
	
	@Test
	public void testLoadFallbackClasspathResourcesIfNotFoundFoundCustom() {
		YamlRuleSet rules = resourceMgr().get(
				YamlRuleSet.class, 
				ResourceType.VARIANTS);
		assertThat(rules.getVariantRules())
			.hasSize(49);
		
		resourceConfig.addDirectory(CUSTOM_DIR_PATH);
		
		YamlRuleSet customRules = resourceMgr().get(
				YamlRuleSet.class, 
				ResourceType.VARIANTS);
		assertThat(customRules.getVariantRules())
			.hasSize(49);

	}

	
	@Test
	public void testLoadResourcesFromCustomJar() {
		SimpleWordSet dico = resourceMgr().get(
				SimpleWordSet.class, 
				ResourceType.DICO);
		assertThat(dico.getElements())
			.contains("étoile")
			.hasSize(58179);
		
		resourceConfig.addJar(CUSTOM_JAR_PATH);
		
		SimpleWordSet customDico = resourceMgr().get(
				SimpleWordSet.class, 
				ResourceType.DICO);
		
		assertThat(customDico.getElements())
			.contains("tonton")
			.hasSize(1);
	}


	@Test
	public void testLoadResourcesFromCustomPrefix() throws MalformedURLException {
		SimpleWordSet dico = resourceMgr().get(
				SimpleWordSet.class, 
				ResourceType.DICO);
		assertThat(dico.getElements())
			.contains("étoile")
			.hasSize(58179);
		
		String url = "file:" + CUSTOM_DIR_PATH.toString();
		resourceConfig.addResourcePrefix(new java.net.URL(url));
		
		SimpleWordSet customDico = resourceMgr().get(
				SimpleWordSet.class, 
				ResourceType.DICO);
		
		assertThat(customDico.getElements())
			.contains("toto")
			.contains("tata")
			.hasSize(2);
		
	}
}
