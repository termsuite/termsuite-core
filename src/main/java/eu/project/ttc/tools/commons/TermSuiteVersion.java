/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright 2, 2014nership.  The ASF licenses this file
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

import java.io.File;

/**
 * Defines the version information (and a bit more) of
 *
 * @author Fabien Poulard <fpoulard@dictanova.com>
 * @date 16/08/13
 */
public class TermSuiteVersion {

    /** Current version of the program */
    public static final String VERSION = "1.5";
    /** Title */
    public static final String TITLE = "Term Suite";
    /** Summary description of TermSuite */
    public static final String SUMMARY =
            "This tool provides 3 tools for processing terminology extraction "
            + "and terminology alignment from comparable corpora.\n"
            + "It deals with single-word terms (SWT) and multi-word terms (SWT)\n"
            + "It has been developed within the European TTC project (see http://www.ttc-project.eu/).";

    /** License */
    public static final String LICENSE =
            "This software is distributed under the Apache Licence version 2.\n"
            + "Icons by Iconic CC-by-sa 3.0 (http://somerandomdude.com/work/iconic/).";

    /** Name of the directory where the config is persisted */
    public static final String CFG_ROOT = System.getProperty("user.home")
                    + File.separator + ".term-suite" + File.separator + VERSION;
    /** Name of the file where the SpotterController config is persisted */
    public static final String CFG_SPOTTER = CFG_ROOT + File.separator + "spotter.settings";
    /** Name of the file where the Indexer config is persisted */
    public static final String CFG_INDEXER = CFG_ROOT + File.separator + "indexer.settings";
    /** Name of the file where the Aligner config is persisted */
    public static final String CFG_ALIGNER = CFG_ROOT + File.separator + "aligner.settings";

}
