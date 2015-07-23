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
package eu.project.ttc.tools.spotter;

import java.beans.PropertyChangeListener;

/**
 * This interface declares the parameters as well as the associated methods
 * (accessor and listeners) that should be available by any view or model
 * related to the Spotter tool.
 *
 * @author Fabien Poulard <fpoulard@dictanova.com>
 * @date 15/08/13
 */
public interface SpotterBinding {

    /** Prefix of the Spotter properties */
    public final static String EVT_PREFIX = "spotter.";

    /** Enum of all the properties handled by the Spotter models and views */
    public static enum PRM {
        /** Language parameter */
        LANGUAGE            ("Language", "spotter.language", "en"),
        /** Input directory parameter */
        INPUT               ("InputDirectory", "spotter.inputdirectory", null),
        /** Output directory parameter */
        OUTPUT              ("Directory", "spotter.outputdirectory", null),
        /** TreeTagger directory parameter */
        TTGHOME             ("TreeTaggerHomeDirectory", "spotter.ttgdirectory", null),
        /** Enable TSV output parameter */
        ENABLETSV           ("EnableTSVOutput", "spotter.enabletsv", Boolean.FALSE);

        private final String property;
        private final String parameter;
        private final Object defaultValue;

        PRM(String uimaParameter, String property, Object defaultValue) {
            this.parameter = uimaParameter;
            this.property = property;
            this.defaultValue = defaultValue;
        }

        public String getProperty() {
            return this.property;
        }

        public String getParameter() {
            return this.parameter;
        }

        public Object getDefaultValue() {
            return defaultValue;
        }

        /**
         * Get a particular PRM from a parameter name.
         *
         * @param parameter
         *      name of the parameter we want the corresponding PRM of
         * @return
         *      either the PRM if found, null otherwise
         */
        public static PRM fromParameter(String parameter) {
            if (parameter != null) {
                for (PRM c : PRM.values()) {
                    if (parameter.equals(c.getParameter())) {
                        return c;
                    }
                }
            }
            return null;
        }
    }

    ///////////////////////////////////////////////////////////// LANGUAGE

    /**
     * Setter for the language parameter value.
     * @param language  two letters language code
     */
    void setLanguage(String language);

    /**
     * Accessor to the language parameter value.
     */
    String getLanguage();

    /**
     * Listener to a change regarding the language parameter.
     */
    void addLanguageChangeListener(PropertyChangeListener listener);

    ///////////////////////////////////////////////////////////// INPUT DIRECTORY

    /**
     * Setter for the input directory parameter value.
     * @param inputDirectory  path to the directory to use
     */
    void setInputDirectory(String inputDirectory);

    /**
     * Accessor to the input directory parameter value.
     */
    String getInputDirectory();

    /**
     * Listener to a change regarding the input directory parameter.
     */
    void addInputDirectoryChangeListener(PropertyChangeListener listener);

    ///////////////////////////////////////////////////////////// OUTPUT DIRECTORY

    /**
     * Setter for the output directory parameter value.
     * @param outputDirectory  path to the directory to use
     */
    void setOutputDirectory(String outputDirectory);

    /**
     * Accessor to the output directory parameter value.
     */
    String getOutputDirectory();

    /**
     * Listener to a change regarding the output directory parameter.
     */
    void addOutputDirectoryChangeListener(PropertyChangeListener listener);

    ///////////////////////////////////////////////////////////// TREETAGGER DIRECTORY

    /**
     * Setter for the treetagger directory parameter value.
     * @param treetaggerHome    path to the directory to use
     */
    void setTreetaggerHome(String treetaggerHome);

    /**
     * Accessor to the treetagger directory parameter value.
     */
    String getTreetaggerHome();

    /**
     * Listener to a change regarding the treetagger directory parameter.
     */
    void addTtgDirectoryChangeListener(PropertyChangeListener listener);

    ///////////////////////////////////////////////////////////// Enable TSV output

    /**
     * Enables/disables tsv output property
     * @param enableTsv if <code>true</code> tsv output is enabled, or disable otherwise
     */
    void setEnableTsvOutput(boolean enableTsv);

    /**
     * Returns whether tsv output is enabled or not.
     */
    Boolean isEnableTsvOutput();

    /**
     * Listener to a change regarding the enable tsv parameter.
     */
    void addEnableTsvOutputChangeListener(PropertyChangeListener listener);
}
