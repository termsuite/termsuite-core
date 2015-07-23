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
package eu.project.ttc.tools.spotter;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.uima.UIMAFramework;
import org.apache.uima.resource.ResourceConfigurationException;
import org.apache.uima.resource.metadata.ConfigurationParameter;
import org.apache.uima.resource.metadata.ConfigurationParameterDeclarations;
import org.apache.uima.resource.metadata.ConfigurationParameterSettings;
import org.apache.uima.resource.metadata.NameValuePair;
import org.apache.uima.resource.metadata.ResourceMetaData;
import org.apache.uima.util.InvalidXMLException;
import org.apache.uima.util.Level;
import org.apache.uima.util.XMLInputSource;
import org.xml.sax.SAXException;

import eu.project.ttc.tools.commons.InvalidTermSuiteConfiguration;
import eu.project.ttc.tools.commons.ToolModel;

/**
 * Model of the Spotter tool.
 *
 * The spotter is responsible for processing documents and extract term candidates from it.
 * This model maintains the values of the parameters used by the dedicated engine.
 */
public class SpotterModel extends ToolModel implements SpotterBinding {

    public static final String P_LANGUAGE = "Language";
    public static final String P_INPUT_DIRECTORY = "InputDirectory";
    public static final String P_OUTPUT_DIRECTORY = "OutputDirectory";
    public static final String P_TREETAGGER_HOME_DIRECTORY = "TreeTaggerHomeDirectory";
    public static final String P_ENABLE_TSV_OUTPUT = "EnableTSVOutput";

    /** Language configuration parameter */
    private ConfigurationParameter pLang;
    /** Input directory where the resources to be processed will be found */
    private ConfigurationParameter pIDir;
    /** Output directory where the results of processing will be stored */
    private ConfigurationParameter pODir;
    /** Home of TreeTagger */
    private ConfigurationParameter pTtg;
    /** Enable tsv output */
    private ConfigurationParameter pTsv;
    
    // Where the parameter value are stored
    ConfigurationParameterSettings pSettings;

    /**
     * Constructor.
     *
     * @param spotterCfg
     *      configuration file where the model is persisted
     */
    public SpotterModel(File spotterCfg) {
        super(spotterCfg);
        declareParameters();
	}

    /**
     * Declare the parameters that are used by the Spotter tool.
     */
    private void declareParameters() {
        // Language
        pLang = UIMAFramework
                .getResourceSpecifierFactory().createConfigurationParameter();
        pLang.setName(P_LANGUAGE);
        pLang.setType(ConfigurationParameter.TYPE_STRING);
        pLang.setMultiValued(false);
        pLang.setMandatory(true);
        pLang.setDescription("values:en|fr|de|es|ru|da|lv|zh");

        // Input directory
        pIDir = UIMAFramework
                .getResourceSpecifierFactory().createConfigurationParameter();
        pIDir.setName(P_INPUT_DIRECTORY);
        pIDir.setType(ConfigurationParameter.TYPE_STRING);
        pIDir.setMultiValued(false);
        pIDir.setMandatory(true);

        // Output directory
        pODir = UIMAFramework
                .getResourceSpecifierFactory().createConfigurationParameter();
        pODir.setName(P_OUTPUT_DIRECTORY);
        pODir.setType(ConfigurationParameter.TYPE_STRING);
        pODir.setMultiValued(false);
        pODir.setMandatory(true);

        // TreeTagger Home
        pTtg = UIMAFramework
                .getResourceSpecifierFactory().createConfigurationParameter();
        pTtg.setName(P_TREETAGGER_HOME_DIRECTORY);
        pTtg.setType(ConfigurationParameter.TYPE_STRING);
        pTtg.setMultiValued(false);
        pTtg.setMandatory(true);

        // TSV output
        pTsv = UIMAFramework
                .getResourceSpecifierFactory().createConfigurationParameter();
        pTsv.setName(P_ENABLE_TSV_OUTPUT);
        pTsv.setType(ConfigurationParameter.TYPE_BOOLEAN);
        pTsv.setMultiValued(false);
        pTsv.setMandatory(false);
        
        // Bundle everything in a ConfigurationParameterSettings to add values
        pSettings = UIMAFramework.getResourceSpecifierFactory()
                        .createConfigurationParameterSettings();
    }

    /**
     * Load persisted parameters values from the configuration file.
     * We use UIMA metadata resource format to persist the configuration.
     *
     * @see eu.project.ttc.tools.commons.ToolModel#load()
     */
    @Override
    public void load() throws IOException, InvalidTermSuiteConfiguration {
        // Check if the file exist, not an error if it is empty
        if (!getConfigurationFile().exists())
            return;

        // Load data from the persisted file as a UIMA metadata resource
        ResourceMetaData uimaMetadata;
        XMLInputSource input = new XMLInputSource(getConfigurationFile());
        try {
            uimaMetadata = UIMAFramework.getXMLParser().parseResourceMetaData(input);
        } catch (InvalidXMLException e) {
            UIMAFramework.getLogger().log(Level.WARNING, e.getMessage());
            throw new IOException(e);
        }

        // Set the model properties accordingly
        ConfigurationParameterSettings settings =
                uimaMetadata.getConfigurationParameterSettings();
        for(NameValuePair nvp: settings.getParameterSettings()) {
            try {
                if ( pLang.getName().equals(nvp.getName()) ) {
                    setLanguage((String) nvp.getValue());
                } else if ( pIDir.getName().equals(nvp.getName()) ) {
                    setInputDirectory((String) nvp.getValue());
                } else if ( pODir.getName().equals(nvp.getName()) ) {
                    setOutputDirectory((String) nvp.getValue());
                } else if ( pTtg.getName().equals(nvp.getName()) ) {
                    setTreetaggerHome((String) nvp.getValue());
                }else if ( pTsv.getName().equals(nvp.getName()) ) {
                    setEnableTsvOutput((Boolean) nvp.getValue());
                } else {
                    UIMAFramework.getLogger().log(Level.WARNING,
                            "Ignoring parameter {} as it is not supported by the model.",
                            new String[]{nvp.getName()});
                }
            } catch (IllegalArgumentException e) {
                String msg = "Unable to correctly load the configuration persisted in file '"
                        + getConfigurationFile().getAbsolutePath() + "' as it contains invalid values.";
                UIMAFramework.getLogger().log(Level.SEVERE, msg);
                throw new InvalidTermSuiteConfiguration(msg, e);
            }
        }
    }

    /**
     * Persist the model as it is to a configuration file.
     * We use UIMA metadata resource format to persist the configuration.
     *
     * @see eu.project.ttc.tools.commons.ToolModel#save()
     */
    @Override
    public void save() throws IOException {
        // Create the UIMA declaration out of the properties
        ConfigurationParameterDeclarations uimaParamDeclarations = UIMAFramework
                .getResourceSpecifierFactory()
                .createConfigurationParameterDeclarations();
        uimaParamDeclarations.addConfigurationParameter(pLang);
        uimaParamDeclarations.addConfigurationParameter(pIDir);
        uimaParamDeclarations.addConfigurationParameter(pODir);
        uimaParamDeclarations.addConfigurationParameter(pTtg);
        uimaParamDeclarations.addConfigurationParameter(pTsv);

        // Create and populate the metadata
        ResourceMetaData uimaMetadata = UIMAFramework
                .getResourceSpecifierFactory().createResourceMetaData();
        uimaMetadata.setConfigurationParameterDeclarations(uimaParamDeclarations);
        uimaMetadata.setConfigurationParameterSettings(pSettings);

        // Persist everything
        OutputStream out = new FileOutputStream(getConfigurationFile());
        try {
            uimaMetadata.toXML(out);
        } catch (SAXException e) {
            throw new IOException(e);
        } finally {
            out.close();
        }
    }

    @Override
    public void validate() throws ResourceConfigurationException {
        // FIXME code is redundant with save()

        // Create the UIMA declaration out of the properties
        ConfigurationParameterDeclarations uimaParamDeclarations = UIMAFramework
                .getResourceSpecifierFactory()
                .createConfigurationParameterDeclarations();
        uimaParamDeclarations.addConfigurationParameter(pLang);
        uimaParamDeclarations.addConfigurationParameter(pIDir);
        uimaParamDeclarations.addConfigurationParameter(pODir);
        uimaParamDeclarations.addConfigurationParameter(pTtg);
        uimaParamDeclarations.addConfigurationParameter(pTsv);

        // Create and populate the metadata
        ResourceMetaData uimaMetadata = UIMAFramework
                .getResourceSpecifierFactory().createResourceMetaData();
        uimaMetadata.setConfigurationParameterDeclarations(uimaParamDeclarations);
        uimaMetadata.setConfigurationParameterSettings(pSettings);

        uimaMetadata.validateConfigurationParameterSettings();
    }

    //////////////////////////////////////////////////////////////////// ACCESSORS

    @Override
    public void addLanguageChangeListener(PropertyChangeListener listener) {
        addPropertyChangeListener(PRM.LANGUAGE.getProperty(), listener);
    }

    @Override
    public void addInputDirectoryChangeListener(PropertyChangeListener listener) {
        addPropertyChangeListener(PRM.INPUT.getProperty(), listener);
    }

    @Override
    public void addOutputDirectoryChangeListener(PropertyChangeListener listener) {
        addPropertyChangeListener(PRM.OUTPUT.getProperty(), listener);
    }

    @Override
    public void addTtgDirectoryChangeListener(PropertyChangeListener listener) {
        addPropertyChangeListener(PRM.TTGHOME.getProperty(), listener);
    }

    @Override
    public void addEnableTsvOutputChangeListener(PropertyChangeListener listener) {
        addPropertyChangeListener(PRM.ENABLETSV.getProperty(), listener);
    }
    
    /**
     * Setter for language parameter value.
     * If the value is valid, then the parameter value is changed in the
     * model and an event is fired indicating that the property has
     * changed in the model.
     */
    public void setLanguage(String language) {
        if ( language.matches("en|fr|de|es|ru|da|lv|zh") ) {
            String oldValue = (String) pSettings.getParameterValue(pLang.getName());
            pSettings.setParameterValue(pLang.getName(), language);
            firePropertyChange(PRM.LANGUAGE.getProperty(), oldValue, language);
        } else {
            String msg = "Language parameter value '" + language + "' is invalid.";
            UIMAFramework.getLogger().log(Level.SEVERE, msg);
            throw new IllegalArgumentException(msg);
        }
    }
    /** Getter for language property */
    public String getLanguage() {
        return (String) pSettings.getParameterValue(pLang.getName());
    }

    /**
     * Setter for input directory parameter value.
     * If the value is valid, then the parameter value is changed in the
     * model and an event is fired indicating that the property has
     * changed in the model.
     */
    public void setInputDirectory(String inputDirectory) {
        File input = new File(inputDirectory);
        if ( input.exists() && input.isDirectory() ) {
            String oldValue = (String) pSettings.getParameterValue(pIDir.getName());
            pSettings.setParameterValue(pIDir.getName(), inputDirectory);
            firePropertyChange(PRM.INPUT.getProperty(), oldValue, inputDirectory);
        } else {
            String msg = "Input directory parameter value '" + inputDirectory + "' is invalid.";
//            UIMAFramework.getLogger().log(Level.SEVERE, msg);
            throw new IllegalArgumentException(msg);
        }
    }
    /** Getter for input directory property */
    public String getInputDirectory() {
        return (String) pSettings.getParameterValue(pIDir.getName());
    }

    /**
     * Setter for output directory parameter value.
     * If the value is valid, then the parameter value is changed in the
     * model and an event is fired indicating that the property has
     * changed in the model.
     */
    public void setOutputDirectory(String outputDirectory) {
        File output = new File(outputDirectory);
        if ( ! output.exists() )
            output.mkdirs(); // make sure output directory exists
        if ( output.exists() && output.isDirectory() ) {
            String oldValue = (String) pSettings.getParameterValue(pODir.getName());
            pSettings.setParameterValue(pODir.getName(), outputDirectory);
            firePropertyChange(PRM.OUTPUT.getProperty(), oldValue, outputDirectory);
        } else {
            String msg = "Output directory parameter value '" + outputDirectory + "' is invalid.";
//            UIMAFramework.getLogger().log(Level.SEVERE, msg);
            throw new IllegalArgumentException(msg);
        }
    }
    /** Getter for output directory property */
    public String getOutputDirectory() {
        return (String) pSettings.getParameterValue(pODir.getName());
    }

    /**
     * Setter for treetagger directory parameter value.
     * If the value is valid, then the parameter value is changed in the
     * model and an event is fired indicating that the property has
     * changed in the model.
     */
    public void setTreetaggerHome(String treetaggerHome) {
        File output = new File(treetaggerHome);
        if ( output.exists() && output.isDirectory() ) {
            String oldValue = (String) pSettings.getParameterValue(pTtg.getName());
            pSettings.setParameterValue(pTtg.getName(), treetaggerHome);
            firePropertyChange(PRM.TTGHOME.getProperty(), oldValue, treetaggerHome);
        } else {
            String msg = "TreeTagger home parameter value '" + treetaggerHome + "' is invalid.";
//            UIMAFramework.getLogger().log(Level.SEVERE, msg);
            throw new IllegalArgumentException(msg);
        }
    }
    /** Getter for tree tagger home property. */
    public String getTreetaggerHome() {
        return (String) pSettings.getParameterValue(pTtg.getName());
    }

    /**
     * Enables/disables tsv output property
     * @param enableTsv
     */
    public void setEnableTsvOutput(boolean enableTsv) {
        Boolean oldValue = isEnableTsvOutput();
        Boolean newValue = Boolean.valueOf(enableTsv);
        pSettings.setParameterValue(pTsv.getName(), Boolean.valueOf(enableTsv));
        firePropertyChange(PRM.ENABLETSV.getProperty(), oldValue, newValue);
    }
    
    /** Getter for tsv output property. */
    public Boolean isEnableTsvOutput() {
        return Boolean.TRUE.equals(pSettings.getParameterValue(pTsv.getName()));
    }
}
