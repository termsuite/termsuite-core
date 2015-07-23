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
package eu.project.ttc.tools.aligner;

import java.beans.PropertyChangeListener;

/**
 * This interface declares the parameters as well as the associated methods
 * (accessor and listeners) that should be available by any view or model
 * related to the Aligner tool.
 *
 * @author Fabien Poulard <fpoulard@dictanova.com>
 * @date 19/08/13
 */
public interface AlignerBinding {

    /** Prefix of the Aligner properties */
    public final static String EVT_PREFIX = "aligner.";

    /** Enum of all the properties handled by the Aligner models and views */
    public static enum Params {
        /** Source language parameter */
        SOURCE_LANGUAGE            ("SourceLanguage", "aligner.srclanguage", "en"),
        /** Target language parameter */
        TARGET_LANGUAGE             ("TargetLanguage", "aligner.trglanguage", "en"),
        /** Output directory parameter */
        ALIGNER_OUTPUT_DIRECTORY                  ("AlignerOutputDirectory", "aligner.output", null),
        /** Source terminology XMI file parameter */
        SOURCE_TERMINOLOGY_FILE          ("SourceTerminologyFile", "aligner.srcterminology", null),
        /** Target terminology XMI file parameter */
        TARGET_TERMINOLOGY_FILE          ("TargetTerminologyFile", "aligner.trgterminology", null),
        /** Bilingual dictionary parameter */
        DICTIONARY_FILE              ("DictionaryFile", "aligner.dictionary", null),
        /** Evaluation directory parameter */
        DIRECTORY                 ("Directory", "aligner.evaldir", null),
        /** Compositional method parameter */
        COMPOSITIONAL_METHOD           ("CompositionalMethod", "aligner.compositional", false),
        /** Distributional method */
        DISTRIBUTIONAL_METHOD          ("DistributionalMethod", "aligner.distributional", true),
        /** Distributional method */
        SEMI_DISTRIBUTIONAL_METHOD          ("SemiDistributionalMethod", "aligner.semidistributional", false),
        /** Similarity distance parameter */
        SIMILARITY_DISTANCE_CLASS_NAME              ("SimilarityDistanceClassName", "aligner.similarity", "eu.project.ttc.metrics.Jaccard"),
        /** Max candidates parameter */
        MAX_TRANSLATIONS_CANDIDATES           ("MaxTranslationCandidates", "aligner.maxcandidates", 100);

        private final String property;
        private final String parameter;
        private final Object defaultValue;

        Params(String uimaParameter, String property, Object defaultValue) {
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
        public static Params fromParameter(String parameter) {
            if (parameter != null) {
                for (Params c : Params.values()) {
                    if (parameter.equals(c.getParameter())) {
                        return c;
                    }
                }
            }
            return null;
        }
    }

    //////////////////////////////////////////////////////////// SRC LANGUAGE

    /**
     * Setter for the source language parameter value.
     * @param language  two letters language code
     */
    void setSourceLanguage(String language);

    /**
     * Accessor to the source language parameter value.
     */
    String getSourceLanguage();

    /**
     * Listener to a change regarding the source language parameter.
     */
    void addSourceLanguageChangeListener(PropertyChangeListener listener);

    /**
     * Mark the parameter as in error.
     */
    void setSourceLanguageError(Throwable e);

    /**
     * Unmark the parameter as in error.
     */
    void unsetSourceLanguageError();

    //////////////////////////////////////////////////////////// TARGET LANGUAGE

    /**
     * Setter for the target language parameter value.
     * @param language  two letters language code
     */
    void setTargetLanguage(String language);

    /**
     * Accessor to the target language parameter value.
     */
    String getTargetLanguage();

    /**
     * Listener to a change regarding the target language parameter.
     */
    void addTargetLanguageChangeListener(PropertyChangeListener listener);

    /**
     * Mark the parameter as in error.
     */
    void setTargetLanguageError(Throwable e);

    /**
     * Unmark the parameter as in error.
     */
    void unsetTargetLanguageError();

    //////////////////////////////////////////////////////////// OUTPUT

    /**
     * Setter for the output directory parameter value.
     * @param outputDirectory  directory where to output files
     */
    void setOutputDirectory(String outputDirectory);

    /**
     * Accessor to the output directory parameter value.
     */
    String getOutputDirectory();

    /**
     * Listener to a change regarding the target language parameter.
     */
    void addOutputDirectoryChangeListener(PropertyChangeListener listener);

    /**
     * Mark the parameter as in error.
     */
    void setOutputDirectoryError(Throwable e);

    /**
     * Unmark the parameter as in error.
     */
    void unsetOutputDirectoryError();

    //////////////////////////////////////////////////////////// SRC TERMINOLOGY

    /**
     * Setter for the source terminology parameter value.
     * @param sourceTerminology  file to use as source terminology
     */
    void setSourceTerminology(String sourceTerminology);

    /**
     * Accessor to the source terminology parameter value.
     */
    String getSourceTerminology();

    /**
     * Listener to a change regarding the source terminology parameter.
     */
    void addSourceTerminologyChangeListener(PropertyChangeListener listener);

    /**
     * Mark the parameter as in error.
     */
    void setSourceTerminologyError(Throwable e);

    /**
     * Unmark the parameter as in error.
     */
    void unsetSourceTerminologyError();

    //////////////////////////////////////////////////////////// TARGET TERMINOLOGY

    /**
     * Setter for the target terminology parameter value.
     * @param targetTerminology  file to use as target terminology
     */
    void setTargetTerminology(String targetTerminology);

    /**
     * Accessor to the target terminology parameter value.
     */
    String getTargetTerminology();

    /**
     * Listener to a change regarding the target terminology parameter.
     */
    void addTargetTerminologyChangeListener(PropertyChangeListener listener);

    /**
     * Mark the parameter as in error.
     */
    void setTargetTerminologyError(Throwable e);

    /**
     * Unmark the parameter as in error.
     */
    void unsetTargetTerminologyError();

    //////////////////////////////////////////////////////////// DICTIONARY

    /**
     * Setter for the bilingual dictionary parameter value.
     * @param bilingualDictionary  file to use as bilingual dictionary
     */
    void setBilingualDictionary(String bilingualDictionary);

    /**
     * Accessor to the bilingual dictionary parameter value.
     */
    String getBilingualDictionary();

    /**
     * Listener to a change regarding the bilingual dictionary parameter.
     */
    void addBilingualDictionaryChangeListener(PropertyChangeListener listener);

    /**
     * Mark the parameter as in error.
     */
    void setBilingualDictionaryError(Throwable e);

    /**
     * Unmark the parameter as in error.
     */
    void unsetBilingualDictionaryError();

    //////////////////////////////////////////////////////////// EVALUATION DIR

    /**
     * Setter for the evaluation directory parameter value.
     * @param evaluationDirectory  directory where to output evaluation files
     */
    void setEvaluationDirectory(String evaluationDirectory);

    /**
     * Accessor to the evaluation directory parameter value.
     */
    String getEvaluationDirectory();

    /**
     * Listener to a change regarding the evaluation directory parameter.
     */
    void addEvaluationDirectoryChangeListener(PropertyChangeListener listener);

    /**
     * Mark the parameter as in error.
     */
    void setEvaluationDirectoryError(Throwable e);

    /**
     * Unmark the parameter as in error.
     */
    void unsetEvaluationDirectoryError();

    //////////////////////////////////////////////////////////// COMPOSITIONAL METHOD

    /**
     * Setter for the compositional method parameter value.
     * @param isCompositionalMethod  flag for enabling compositional method
     */
    void setCompositionalMethod(boolean isCompositionalMethod);

    /**
     * Accessor to the compositional method parameter value.
     */
    Boolean isCompositionalMethod();

    /**
     * Listener to a change regarding the compositional method parameter.
     */
    void addCompositionalMethodChangeListener(PropertyChangeListener listener);

    /**
     * Mark the parameter as in error.
     */
    void setCompositionalMethodError(Throwable e);

    /**
     * Unmark the parameter as in error.
     */
    void unsetCompositionalMethodError();

    //////////////////////////////////////////////////////////// DISTRIBUTIONAL METHOD

    /**
     * Setter for the distributional method parameter value.
     * @param isDistributionalMethod  flag for enabling distributional method
     */
    void setDistributionalMethod(boolean isDistributionalMethod);

    /**
     * Accessor to the distributional method parameter value.
     */
    Boolean isDistributionalMethod();

    /**
     * Listener to a change regarding the distributional method parameter.
     */
    void addDistributionalMethodChangeListener(PropertyChangeListener listener);

    /**
     * Mark the parameter as in error.
     */
    void setDistributionalMethodError(Throwable e);

    /**
     * Unmark the parameter as in error.
     */
    void unsetDistributionalMethodError();

    //////////////////////////////////////////////////////////// SEMIDISTRIBUTIONAL METHOD

    /**
     * Setter for the semi distributional method parameter value.
     * @param isDistributionalMethod  flag for enabling semidistributional method
     */
    void setSemidistributionalMethod(boolean isSemidistributionalMethod);

    /**
     * Accessor to the distributional method parameter value.
     */
    Boolean isSemidistributionalMethod();

    /**
     * Listener to a change regarding the distributional method parameter.
     */
    void addSemidistributionalMethodChangeListener(PropertyChangeListener listener);

    /**
     * Mark the parameter as in error.
     */
    void setSemidistributionalMethodError(Throwable e);

    /**
     * Unmark the parameter as in error.
     */
    void unsetSemidistributionalMethodError();
    
    //////////////////////////////////////////////////////////// SIMILARITY DISTANCE

    /**
     * Setter for the similarity distance class parameter value.
     * @param similarityDistanceClass  name of the class for computing similarity distance
     */
    void setSimilarityDistanceClass(String similarityDistanceClass);

    /**
     * Accessor to the similarity distance class parameter value.
     */
    String getSimilarityDistanceClass();

    /**
     * Listener to a change regarding the similarity distance class parameter.
     */
    void addSimilarityDistanceClassChangeListener(PropertyChangeListener listener);

    /**
     * Mark the parameter as in error.
     */
    void setSimilarityDistanceClassError(Throwable e);

    /**
     * Unmark the parameter as in error.
     */
    void unsetSimilarityDistanceClassError();

    //////////////////////////////////////////////////////////// MAX CANDIDATES

    /**
     * Setter for the max candidates parameter value.
     * @param maxCandidates    maximum number of translation candidates
     */
    void setMaxCandidates(Integer maxCandidates);

    /**
     * Accessor to the max candidates parameter value.
     */
    Integer getMaxCandidates();

    /**
     * Listener to a change regarding the max candidates parameter.
     */
    void addMaxCandidatesChangeListener(PropertyChangeListener listener);

    /**
     * Mark the parameter as in error.
     */
    void setMaxCandidatesError(Throwable e);

    /**
     * Unmark the parameter as in error.
     */
    void unsetMaxCandidatesError();

}
