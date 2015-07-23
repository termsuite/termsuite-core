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
package eu.project.ttc.tools.commons;


public interface TermSuiteEngine {

    // FIXME
//    /**
//     * Compute the analysis engine descriptor to use.
//     *
//     * @return
//     *      absolute path to the resource corresponding to the description
//     *      of the corresponding analysis engine
//     * @throws Exception
//     */
//	public String getEngineDescriptor() throws Exception;
//
//    /**
//     * Compute the analysis engine configuration parameter settings to be used,
//     * that is to be directly passed to the corresponding UIMA engine.
//     *
//     * @return
//     *      instance of ConfigurationParameterSettings to be directly passed to
//     *      the analysis engine, that is to the descriptor running the engine.
//     * @throws Exception
//     */
//	public ConfigurationParameterSettings getAESettings() throws Exception;
//
//    /**
//     * Compute the description of where and what the files to be processed are.
//     */
//	public InputSource getInputSource();
	
//	public String language();
//
//	public String encoding();
//
//	public void callBack(CAS cas) throws Exception;

	/** Default encoding for TermSuite engines */
	public static final String DEFAULT_ENCODING = "utf-8";

}