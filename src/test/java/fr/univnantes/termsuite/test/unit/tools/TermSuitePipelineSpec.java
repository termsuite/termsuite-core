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
package fr.univnantes.termsuite.test.unit.tools;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import fr.univnantes.termsuite.test.unit.io.JsonTermIndexIOSpec;
import fr.univnantes.termsuite.tools.TermSuiteCLIUtils;
import fr.univnantes.termsuite.uima.TermSuitePipeline;
import fr.univnantes.termsuite.uima.TermSuitePipelineException;
import fr.univnantes.termsuite.uima.TermSuiteResourceException;

public class TermSuitePipelineSpec {
	
	private static final String DIR_PATH = "./src/main/resources/fr/univnantes/termsuite/resources/";
	private static final String URL = "file:./src/main/resources/fr/univnantes/termsuite/resources/";
	
	@Before
	public void setup() {
		TermSuiteCLIUtils.disableLogging();
	}
	
	public void runPipeline(TermSuitePipeline pipeline) {
		pipeline.aeWordTokenizer().run();
	}
	
	public TermSuitePipeline startPipeline() {
		return TermSuitePipeline
			.create("en")
			.setInlineString("The texte to analyse.");
	}
	
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
	@Test
	public void testLoadResourcesFromClasspath() {
		TermSuitePipeline pipeline = startPipeline();
		runPipeline(pipeline);
		// should not raise exception
	}

	@Test
	public void testLoadResourcesFromDirectoryException1() {
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("Not a directory");

		TermSuitePipeline pipeline = startPipeline();
		pipeline.setResourceDir(DIR_PATH + "ebgeb");
		runPipeline(pipeline);
		// should not raise exception
		
	}

	@Test
	public void testLoadResourcesFromDirectory() {
		TermSuitePipeline pipeline = startPipeline();
		pipeline.setResourceDir(DIR_PATH);
		runPipeline(pipeline);
		// should not raise exception
	}
	
	@Test
	public void testLoadResourcesFromDirectoryWithMissingTrailingSlash() {
		TermSuitePipeline pipeline = startPipeline();
		pipeline.setResourceDir(DIR_PATH.substring(0, DIR_PATH.length() - 1));
		runPipeline(pipeline);
		// should not raise exception
		
	}


	@Test
	public void testLoadResourcesFromJarExceptionSimpleExistingFile() {
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("Not a jar");
		
		TermSuitePipeline pipeline = startPipeline();
		pipeline.setResourceJar(JsonTermIndexIOSpec.jsonFile1);
		runPipeline(pipeline);
	}

	
	@Test
	public void testLoadResourcesFromJarExceptionNotExisting() {
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("Not a jar");
		
		TermSuitePipeline pipeline = startPipeline();
		pipeline.setResourceJar("/ergve/erve/ergred");
		runPipeline(pipeline);
		
	}

	@Test
	public void testLoadResourcesFromJarExceptionDirectory() {
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("Not a jar");
		
		TermSuitePipeline pipeline = startPipeline();
		pipeline.setResourceJar(DIR_PATH);
		runPipeline(pipeline);

		
	}

	@Test
	public void testLoadResourcesFromEmbeddedResources() {
		TermSuitePipeline pipeline = startPipeline();
		runPipeline(pipeline);
		// should not raise exception
	}
	
	@Test
	public void testLoadResourcesFromUrlPrefix() {
		TermSuitePipeline pipeline = startPipeline();
		pipeline.setResourceUrlPrefix(URL);
		runPipeline(pipeline);
		// should not raise exception
		
	}


	@Test
	public void testLoadResourcesFromUrlPrefixBadUrl() {
		thrown.expect(TermSuitePipelineException.class);
		thrown.expectMessage("Bad url");
		TermSuitePipeline pipeline = startPipeline();
		pipeline.setResourceUrlPrefix("prototo:/the/path");
		runPipeline(pipeline);
		// should not raise exception
	}
	
	@Test
	public void testLoadResourcesFromUrlPrefixPointingToNowhere() {
		
		TermSuitePipeline pipeline = startPipeline();
		pipeline.setResourceUrlPrefix("file:/pointing/to/nowhere");
		try {
			runPipeline(pipeline);
			fail("Should raise exception");
		} catch(TermSuitePipelineException e) {
			assertThat(e.getCause())
				.isInstanceOf(TermSuiteResourceException.class)
				.hasMessageStartingWith("Cannot open stream");
		} catch( Exception e) {
			fail("Should raise TermSuitePipelineException");
		}
		// should not raise exception
	}
}
