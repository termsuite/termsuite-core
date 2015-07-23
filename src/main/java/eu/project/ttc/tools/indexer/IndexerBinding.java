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
package eu.project.ttc.tools.indexer;

import java.beans.PropertyChangeListener;

/**
 * This interface declares the parameters as well as the associated methods
 * (accessor and listeners) that should be available by any view or model
 * related to the Indexer tool.
 *
 * @author Fabien Poulard <fpoulard@dictanova.com>
 * @date 15/08/13
 */
public interface IndexerBinding {

    /** Prefix of the Indexer properties */
    public final static String EVT_PREFIX = "indexer.";

    /** Enum of all the properties handled by the Indexer models and views */
    public static enum PRM {
        /** Language parameter */
        LANGUAGE            ("Language", "indexer.language", "en"),
        /** Input directory parameter */
        INPUT               ("InputDirectory", "indexer.inputdirectory", null),
        /** Output directory parameter */
        OUTPUT              ("Directory", "indexer.outputdirectory", null),
        /** Ignore diacritics parameter */
        IGNOREDIACRITICS    ("IgnoreDiacriticsInMultiwordTerms", "indexer.ignorediacritics", false),
        /** Variant detection parameter */
        SYNTVARIANTDETECTION("EnableSyntacticVariantDetection", "indexer.syntacticvariant", true),
        /** Variant detection parameter */
        GRPHVARIANTDETECTION("EnableGraphicalVariantDetection", "indexer.graphicalvariant", false),
        /** Edit distance class parameter */
        EDITDISTANCECLS     ("EditDistanceClassName", "indexer.editdistanceclass", "eu.project.ttc.metrics.Levenshtein"),
        /** Edit distance threshold parameter */
        EDITDISTANCETLD     ("EditDistanceThreshold", "indexer.editdistancethreshold", 0.9f),
        /** Edit distance ngrams parameter */
        EDITDISTANCENGRAMS  ("EditDistanceNgrams", "indexer.editdistancengrams", 1),
        /** Occurrence threshold parameter */
        OCCURRENCETLD       ("OccurrenceThreshold", "indexer.occurrencethreshold", 2),
        /** Association measure parameter */
        ASSOCIATIONMEASURE  ("AssociationRateClassName", "indexer.associationmeasure", "eu.project.ttc.metrics.LogLikelihood"),
        /** Filtering threshold parameter */
        FILTERINGTLD        ("FilterRuleThreshold", "indexer.filteringthreshold", 1),
        /** Filter rule parameter -> actually used for sorting ? */
        FILTERRULE          ("FilterRule", "indexer.filterrule", "None"),
        /** Keep verbs parameter */
        KEEPVERBS           ("KeepVerbsAndOthers", "indexer.keepverbs", false),
        /** TSV export parameter */
        TSV                 ("EnableTsvOutput", "indexer.tsv", false), 
        EXPORT_EVALUATION_FILES				("EvaluationFileOutput", "spec.txt", false);

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
            throw new IllegalArgumentException("Unable to identify the parameter serialized as "+ parameter);
        }
    }

    /**
     * Available filter rules for the indexer.
     */
    public static enum FilterRules {
        None, OccurrenceThreshold, SpecificityThreshold,
        TopNByOccurrence, TopNBySpecificity
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

    /**
     * Mark the parameter as in error.
     */
    void setLanguageError(Throwable e);

    /**
     * Unmark the parameter as in error.
     */
    void unsetLanguageError();

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

    /**
     * Mark the parameter as in error.
     */
    void setInputDirectoryError(Throwable e);

    /**
     * Unmark the parameter as in error.
     */
    void unsetInputDirectoryError();

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

    /**
     * Mark the parameter as in error.
     */
    void setOutputDirectoryError(Throwable e);

    /**
     * Unmark the parameter as in error.
     */
    void unsetOutputDirectoryError();

    ///////////////////////////////////////////////////////////// IGNORE DIACRITICS

    /**
     * Setter for the ignore diacritics parameter value.
     * @param ignoreDiacritics  flag for ignoring diacritics or not
     */
    void setIgnoreDiacritics(boolean ignoreDiacritics);

    /**
     * Accessor to the ignore diacritics parameter value.
     */
    Boolean isIgnoreDiacritics();

    /**
     * Listener to a change regarding the ignore diacritics parameter.
     */
    void addIgnoreDiacriticsChangeListener(PropertyChangeListener listener);

    /**
     * Mark the parameter as in error.
     */
    void setIgnoreDiacriticsError(Throwable e);

    /**
     * Unmark the parameter as in error.
     */
    void unsetIgnoreDiacriticsError();

    ///////////////////////////////////////////////////////////// GRAPHICAL VARIANT DETECTIONS

    /**
     * Setter for the graphical variant detection parameter value.
     * @param variantDetection  flag for variant detection or not
     */
    void setGraphicalVariantDetection(boolean variantDetection);

    /**
     * Accessor to the graphical variant detection parameter value.
     */
    Boolean isGraphicalVariantDetection();

    /**
     * Listener to a change regarding the graphical variant detection parameter.
     */
    void addGraphicalVariantDetectionChangeListener(PropertyChangeListener listener);

    /**
     * Mark the parameter as in error.
     */
    void setGraphicalVariantDetectionError(Throwable e);

    /**
     * Unmark the parameter as in error.
     */
    void unsetGraphicalVariantDetectionError();

    ///////////////////////////////////////////////////////////// SYNTACTIC VARIANT DETECTIONS

    /**
     * Setter for the Syntactic variant detection parameter value.
     * @param variantDetection  flag for variant detection or not
     */
    void setSyntacticVariantDetection(boolean variantDetection);

    /**
     * Accessor to the Syntactic variant detection parameter value.
     */
    Boolean isSyntacticVariantDetection();

    /**
     * Listener to a change regarding the Syntactic variant detection parameter.
     */
    void addSyntacticVariantDetectionChangeListener(PropertyChangeListener listener);

    /**
     * Mark the parameter as in error.
     */
    void setSyntacticVariantDetectionError(Throwable e);

    /**
     * Unmark the parameter as in error.
     */
    void unsetSyntacticVariantDetectionError();
    
    ///////////////////////////////////////////////////////////// EDIT DISTANCE CLASS

    /**
     * Setter for the edit distance class parameter value.
     * @param editDistanceClass  name of the class for computing edit distance
     */
    void setEditDistanceClass(String editDistanceClass);

    /**
     * Accessor to the edit distance class parameter value.
     */
    String getEditDistanceClass();

    /**
     * Listener to a change regarding the edit distance class parameter.
     */
    void addEditDistanceClassChangeListener(PropertyChangeListener listener);

    /**
     * Mark the parameter as in error.
     */
    void setEditDistanceClassError(Throwable e);

    /**
     * Unmark the parameter as in error.
     */
    void unsetEditDistanceClassError();

    ///////////////////////////////////////////////////////////// EDIT DISTANCE THRESHOLD

    /**
     * Setter for the edit distance threshold parameter value.
     * @param editDistanceThreshold threshold value for the edit distance
     */
    void setEditDistanceThreshold(Float editDistanceThreshold);

    /**
     * Accessor to the edit distance threshold parameter value.
     */
    Float getEditDistanceThreshold();

    /**
     * Listener to a change regarding the edit distance threshold parameter.
     */
    void addEditDistanceThresholdChangeListener(PropertyChangeListener listener);

    /**
     * Mark the parameter as in error.
     */
    void setEditDistanceThresholdError(Throwable e);

    /**
     * Unmark the parameter as in error.
     */
    void unsetEditDistanceThresholdError();

    ///////////////////////////////////////////////////////////// EDIT DISTANCE NGRAMS

    /**
     * Setter for the edit distance ngrams parameter value.
     * @param editDistanceNgrams    number of ngrams for the edit distance
     */
    void setEditDistanceNgrams(Integer editDistanceNgrams);

    /**
     * Accessor to the edit distance ngrams parameter value.
     */
    Integer getEditDistanceNgrams();

    /**
     * Listener to a change regarding the edit distance ngrams parameter.
     */
    void addEditDistanceNgramsChangeListener(PropertyChangeListener listener);

    /**
     * Mark the parameter as in error.
     */
    void setEditDistanceNgramsError(Throwable e);

    /**
     * Unmark the parameter as in error.
     */
    void unsetEditDistanceNgramsError();

    ///////////////////////////////////////////////////////////// FREQUENCY THRESHOLD

    /**
     * Setter for the frequency threshold parameter value.
     * @param occurrenceThreshold    threshold for the frequency filtering
     */
    void setOccurrenceThreshold(Integer occurrenceThreshold);

    /**
     * Accessor to the frequency threshold parameter value.
     */
    Integer getOccurrenceThreshold();

    /**
     * Listener to a change regarding the frequency threshold parameter.
     */
    void addOccurrenceThresholdChangeListener(PropertyChangeListener listener);

    /**
     * Mark the parameter as in error.
     */
    void setOccurrenceThresholdError(Throwable e);

    /**
     * Unmark the parameter as in error.
     */
    void unsetOccurrenceThresholdError();

    ///////////////////////////////////////////////////////////// ASSOCIATION MEASURE

    /**
     * Setter for the association measure parameter value.
     * @param associationMeasure    association measure to use
     */
    void setAssociationMeasure(String associationMeasure);

    /**
     * Accessor to the association measure parameter value.
     */
    String getAssociationMeasure();

    /**
     * Listener to a change regarding the association measure parameter.
     */
    void addAssociationMeasureChangeListener(PropertyChangeListener listener);

    /**
     * Mark the parameter as in error.
     */
    void setAssociationMeasureError(Throwable e);

    /**
     * Unmark the parameter as in error.
     */
    void unsetAssociationMeasureError();

    ///////////////////////////////////////////////////////////// FILTERING THRESHOLD

    /**
     * Setter for the filtering threshold parameter value.
     * @param filteringThreshold    threshold for the filtering
     */
    void setFilteringThreshold(Float filteringThreshold);

    /**
     * Accessor to the filtering threshold parameter value.
     */
    Float getFilteringThreshold();

    /**
     * Listener to a change regarding the filtering threshold parameter.
     */
    void addFilteringThresholdChangeListener(PropertyChangeListener listener);

    /**
     * Mark the parameter as in error.
     */
    void setFilteringThresholdError(Throwable e);

    /**
     * Unmark the parameter as in error.
     */
    void unsetFilteringThresholdError();

    ///////////////////////////////////////////////////////////// FILTERING RULE

    /**
     * Setter for the filter rule parameter value.
     * @param filterRule    the filter rule
     */
    void setFilterRule(String filterRule);

    /**
     * Accessor to the filter rule parameter value.
     */
    String getFilterRule();

    /**
     * Listener to a change regarding the filter rule parameter.
     */
    void addFilterRuleChangeListener(PropertyChangeListener listener);

    /**
     * Mark the parameter as in error.
     */
    void setFilterRuleError(Throwable e);

    /**
     * Unmark the parameter as in error.
     */
    void unsetFilterRuleError();

    ///////////////////////////////////////////////////////////// KEEP VERBS

    /**
     * Setter for the keep verbs parameter value.
     * @param keepVerbs    the keep verbs flag
     */
    void setKeepVerbs(Boolean keepVerbs);

    /**
     * Accessor to the keep verbs parameter value.
     */
    Boolean isKeepVerbs();

    /**
     * Listener to a change regarding the keep verbs parameter.
     */
    void addKeepVerbsChangeListener(PropertyChangeListener listener);

    /**
     * Mark the parameter as in error.
     */
    void setKeepVerbsError(Throwable e);

    /**
     * Unmark the parameter as in error.
     */
    void unsetKeepVerbsError();

    ///////////////////////////////////////////////////////////// TSV

    /**
     * Setter for the TSV export parameter value.
     * @param tsvExport the TSV export flag
     */
    void setTSVExport(Boolean tsvExport);

    /**
     * Accessor to the TSV export parameter value.
     */
    Boolean isTSVExport();

    /**
     * Listener to a change regarding the TSV export parameter.
     */
    void addTSVExportChangeListener(PropertyChangeListener listener);

    /**
     * Mark the parameter as in error.
     */
    void setTSVExportError(Throwable e);

    /**
     * Unmark the parameter as in error.
     */
    void unsetTSVExportError();

}
