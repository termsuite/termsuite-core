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
package eu.project.ttc.tools.commons;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;

import org.apache.uima.cas.CAS;
import org.apache.uima.resource.ResourceConfigurationException;
import org.apache.uima.resource.metadata.ConfigurationParameterSettings;

/**
 * Controller part of the MVC paradigm used for the application.
 * The TermSuite application is made of tools (Spotter, Indexer, Aligner) that are each designed
 * according to the MVC paradigm and so made of a view, a controller and a model. This class
 * is the abstract implementation of the controller part of the tools.
 *
 * In our implementation, the controller is the bound between views and controllers. It is the
 * one responsible for binding them together and flowing the data from one another.
 * This controller implements the {@link PropertyChangeListener} interface so that it can listen
 * for changing properties both in the models and in the views so that it can reflect the changes.
 *
 * @author Fabien Poulard <fpoulard@dictanova.com>
 */
public abstract class ToolController {

    // Default encoding for TermSuite analysis engines
    public static final String DEFAULT_ENCODING = "utf-8";

    // All the registered views this controller controls
    private ToolView registeredView;
    // There is only one registered model
    private ToolModel registeredModel;

    /**
     * Constructor.
     * Register the model controlled by this controller as well as the view
     * exposing the model.
     */
    public ToolController(ToolModel model, ToolView view) {
        registeredView = view;
        registeredModel = model;
    }

    /**
     * (re)Load the persisted configuration for the model.
     */
    public void loadConfiguration() throws IOException, InvalidTermSuiteConfiguration {
        // Proxy for ToolModel#load()
        getToolModel().load();
    }

    /**
     * Persist the configuration for the model.
     */
    public void validateAndSaveConfiguration() throws IOException, InvalidTermSuiteConfiguration {
        try {
            getToolModel().validate();
            getToolModel().save();
        } catch (ResourceConfigurationException e) {
            throw new InvalidTermSuiteConfiguration("The configuration is invalid in UIMA terms.", e);
        }
    }

    /**
     * Build the analysis engine settings from the model so that the resulting settings
     * can be directly passed to the corresponding engine.
     */
    public abstract ConfigurationParameterSettings getAESettings() throws ResourceConfigurationException;

    /**
     * Compute the name of the resource corresponding to the descriptor to use
     * to run the corresponding engine.
     */
    public abstract String getAEDescriptor();

    /**
     * Compute the description of where and what the files to be processed are.
     */
    public abstract InputSource getInputSource();

    /**
     * Method called once a CAS has been processed so that the result can be
     * shared with the model and the views.
     */
    public abstract void processingCallback(CAS cas) throws Exception;

    /**
     * Method called when a new run is about to start. If necessary some elements in the
     * tool should be reset (some parts of the views like results, some stats too).
     */
    public void runStarts() {
        getToolView().runStarts();
        getToolModel().runStarts();
    }

    /**
     * Method called when the run has ended. If necessary some elements in the tool
     * should be displayed or computed (parts of the views like results, stats...).
     */
    public void runEnds() {
        getToolView().runEnds();
        getToolModel().runEnds();
    }

    /**
     * Force the synchronization between view and model so that the values displayed
     * in the view are applied to the model.
     */
    public abstract void synchronizeViewToModel();

    /** Getter for the language parameter */
    public abstract String getLanguage();

    /** Getter for the encoding parameter */
    public String getCorpusEncoding() {
        return DEFAULT_ENCODING;
    }

    /**
     * Getter to the model
     */
    protected ToolModel getToolModel() {
        return registeredModel;
    }

    /**
     * Getter to the view
     */
    protected ToolView getToolView() {
        return registeredView;
    }

    /**
     * Accessor to the tool configuration file where its model is persisted.
     */
    public File getConfigurationFile() {
        return registeredModel.getConfigurationFile();
    }

}