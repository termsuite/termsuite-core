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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Locale;

import org.apache.commons.lang3.text.WordUtils;
import org.apache.uima.UIMAFramework;
import org.apache.uima.cas.CAS;
import org.apache.uima.resource.ResourceConfigurationException;
import org.apache.uima.resource.metadata.ConfigurationParameterSettings;

import eu.project.ttc.tools.commons.InputSource;
import eu.project.ttc.tools.commons.ToolController;

/**
 * Controller of the Spotter tool.
 *
 * The spotter tool is responsible for processing documents and extract
 * term candidates from it.
 *
 * The controller conciliates the model and the view as well as providing
 * higher level features as to build analysis engine description and
 * parameters settings.
 */
public class SpotterController extends ToolController {

    /**
     * Constructor.
     * Create a SpotterController that is connected to a view and a model.
     * Double binds the view and the model so that a change in the view is
     * reflected to the model, and a change in the model is reflected in
     * the view.
     */
    public SpotterController(SpotterModel model, SpotterView view) {
        super(model, view);
        bindViewToModel();
        bindModelToView();
    }

    /**
     * Bind view listeners to model changes so that any change to the view
     * is reflected to the model.
     */
    private void bindViewToModel() {
        // Language
        getView().addLanguageChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                boolean success = true;
                if ((getModel().getLanguage() == null) ||
                        !getModel().getLanguage().equals(evt.getNewValue())) {
                    try {
                        System.out.println("Reflecting language change from view->" + evt.getNewValue());
                        getModel().setLanguage((String) evt.getNewValue());
                    } catch (IllegalArgumentException e) {
                        success = false;
                        getView().setLanguageError(e);
                    }
                } // else, no need to reflect the change (and prevent looping)
                if (success) getView().unsetLanguageError();
            }
        });
        // Input directory
        getView().addInputDirectoryChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                boolean success = true;
                if ( (getModel().getInputDirectory()==null) ||
                        ! getModel().getInputDirectory().equals(evt.getNewValue()) ) {
                    try {
                        System.out.println("Reflecting input directory change from view->" + evt.getNewValue());
                        getModel().setInputDirectory((String) evt.getNewValue());
                    } catch (IllegalArgumentException e) {
                        success = false;
                        getView().setInputDirectoryError(e);
                    }
                } // else, no need to reflect the change (and prevent looping)
                if ( success ) getView().unsetInputDirectoryError();
            }
        });
        // Output directory
        getView().addOutputDirectoryChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                boolean success = true;
                if ( (getModel().getOutputDirectory()==null) ||
                        ! getModel().getOutputDirectory().equals(evt.getNewValue()) ) {
                    try {
                        System.out.println("Reflecting output directory change from view->" + evt.getNewValue());
                        getModel().setOutputDirectory((String) evt.getNewValue());
                    } catch (IllegalArgumentException e) {
                        success = false;
                        getView().setOutputDirectoryError(e);
                    }
                } // else, no need to reflect the change (and prevent looping)
                if ( success ) getView().unsetOutputDirectoryError();
            }
        });
        // TreeTagger directory
        getView().addTtgDirectoryChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                boolean success = true;
                if ((getModel().getTreetaggerHome() == null) ||
                        !getModel().getTreetaggerHome().equals(evt.getNewValue())) {
                    try {
                        System.out.println("Reflecting treetagger directory change from view->" + evt.getNewValue());
                        getModel().setTreetaggerHome((String) evt.getNewValue());
                    } catch (IllegalArgumentException e) {
                        success = false;
                        getView().setTreetaggerHomeError(e);
                    }
                } // else, no need to reflect the change (and prevent looping)
                if (success) getView().unsetTreetaggerHomeError();
            }
        });
        // TSV output
        getView().addEnableTsvOutputChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                boolean success = true;
                if (!getModel().isEnableTsvOutput().equals(evt.getNewValue())) {
                    try {
                        System.out.println("Reflecting tsv output change from view->" + evt.getNewValue());
                        getModel().setEnableTsvOutput((Boolean) evt.getNewValue());
                    } catch (Exception e) {
                        success = false;
                        getView().setEnableTsvOutputError(e);
                    }
                } // else, no need to reflect the change (and prevent looping)
                if (success) getView().unsetEnableTsvOutputError();
            }
        });
    }

    /**
     * Bind model listeners to view changes so that any change to the model
     * is reflected to the view.
     */
    private void bindModelToView() {
        // Language
        getModel().addLanguageChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if ( (getView().getLanguage()==null) ||
                        ! getView().getLanguage().equals(evt.getNewValue()) ) {
                    System.out.println("Reflecting language change from model->" + evt.getNewValue());
                    getView().setLanguage((String) evt.getNewValue());
                } // else, no need to reflect the change (and prevent looping)
            }
        });
        // Input directory
        getModel().addInputDirectoryChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if ( (getView().getInputDirectory()==null) ||
                        ! getView().getInputDirectory().equals(evt.getNewValue()) ) {
                    System.out.println("Reflecting input directory change from model->" + evt.getNewValue());
                    getView().setInputDirectory((String) evt.getNewValue());
                } // else, no need to reflect the change (and prevent looping)
            }
        });
        // Output directory
        getModel().addOutputDirectoryChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if ( (getView().getOutputDirectory()==null) ||
                        ! getView().getOutputDirectory().equals(evt.getNewValue()) ) {
                    System.out.println("Reflecting output directory change from model->" + evt.getNewValue());
                    getView().setOutputDirectory((String) evt.getNewValue());
                } // else, no need to reflect the change (and prevent looping)
            }
        });
        // TreeTagger directory
        getModel().addTtgDirectoryChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if ( (getView().getTreetaggerHome()==null) ||
                        ! getView().getTreetaggerHome().equals(evt.getNewValue()) ) {
                    System.out.println("Reflecting ttg directory change from model->" + evt.getNewValue());
                    getView().setTreetaggerHome((String) evt.getNewValue());
                } // else, no need to reflect the change (and prevent looping)
            }
        });
        
        // TSV output
        getModel().addEnableTsvOutputChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if (!getView().isEnableTsvOutput().equals(evt.getNewValue())) {
                        System.out.println("Reflecting tsv output change from model->" + evt.getNewValue());
                        getView().setEnableTsvOutput((Boolean) evt.getNewValue());
                } // else, no need to reflect the change (and prevent looping)
            }
        });
    }

    /**
     * @see eu.project.ttc.tools.commons.ToolController#synchronizeViewToModel()
     */
    @Override
    public void synchronizeViewToModel() {
        try { getModel().setInputDirectory( getView().getInputDirectory() ); }
        catch (IllegalArgumentException e) { getView().setInputDirectoryError(e); }

        try { getModel().setLanguage( getView().getLanguage() ); }
        catch (IllegalArgumentException e) { getView().setLanguageError(e); }

        try { getModel().setOutputDirectory( getView().getOutputDirectory() ); }
        catch (IllegalArgumentException e) { getView().setOutputDirectoryError(e); }

        try { getModel().setTreetaggerHome( getView().getTreetaggerHome() ); }
        catch (IllegalArgumentException e) { getView().setTreetaggerHomeError(e); }
        
        try { getModel().setEnableTsvOutput( getView().isEnableTsvOutput() ); }
        catch (IllegalArgumentException e) { getView().setEnableTsvOutputError(e); }
    }

    /** Getter to the model with appropriate casting */
    protected SpotterModel getModel() {
        return (SpotterModel) getToolModel();
    }

    /** Getter to the view with appropriate casting */
    protected SpotterView getView() {
        return (SpotterView) getToolView();
    }

    /**
     * @see eu.project.ttc.tools.commons.ToolController#getInputSource()
     *
     * For the spotter tool, the data files to processed are text files
     * and they are located in the input directory specified as parameter.
     */
    @Override
    public InputSource getInputSource() {
        return new InputSource(getModel().getInputDirectory(), InputSource.InputSourceTypes.TXT);
    }

    /**
     * @see ToolController#processingCallback(org.apache.uima.cas.CAS)
     */
    @Override
    public void processingCallback(CAS cas) throws Exception {
        ProcessingResult result = new ProcessingResult();
        result.setCas(cas);
        getView().addProcessingResult(result);
    }

    /**
     * @see eu.project.ttc.tools.commons.ToolController#getLanguage()
     */
    @Override
    public String getLanguage() {
        return getModel().getLanguage();
    }

    /**
     * Build a ConfigurationParameterSettings used to configure an instance of
     * the corresponding AE.
     * The returned ConfigurationParameterSettings is configured using the data
     * in the model.
     *
     * @see eu.project.ttc.tools.commons.ToolController#getAESettings()
     */
    @Override
    public ConfigurationParameterSettings getAESettings() throws ResourceConfigurationException {
        getModel().validate();

        // Prepare an empty ConfigurationParameterSetting
        ConfigurationParameterSettings settings = UIMAFramework
                .getResourceSpecifierFactory().createConfigurationParameterSettings();

        // Only set parameters needed by the Spotter AE
        settings.setParameterValue(SpotterBinding.PRM.OUTPUT.getParameter(),
                getModel().getOutputDirectory());
        settings.setParameterValue(SpotterBinding.PRM.TTGHOME.getParameter(),
                getModel().getTreetaggerHome());
        settings.setParameterValue(SpotterBinding.PRM.ENABLETSV.getParameter(),
                getModel().isEnableTsvOutput());

        return settings;
    }

    /**
     * @return  the name of the AE descriptor resource
     */
    @Override
    public String getAEDescriptor() {
        if ( getModel().getLanguage() != null) {
            String code = getModel().getLanguage();
            String language = new Locale(code)
                    .getDisplayLanguage(Locale.ENGLISH).toLowerCase();
            return String.format("eu.project.ttc.%s.engines.spotter.%sSpotter",
                    language.toLowerCase(), WordUtils.capitalizeFully(language));
        } else {
            throw new IllegalStateException("Unable to generate descriptor name for Spotter as no " +
                    "language have been specified in the model.");
        }
    }
		
}
