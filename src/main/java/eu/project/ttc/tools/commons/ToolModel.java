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
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;

import org.apache.uima.resource.ResourceConfigurationException;

/**
 * Model part of the MVC paradigm used for the application.
 * The TermSuite application is made of tools (Spotter, Indexer, Aligner) that are each designed
 * according to the MVC paradigm and so made of a view, a controller and a model. This class
 * is the abstract implementation of the model part of the tools.
 *
 * In our implementation, the model does not know about the controller or the views. It simply
 * informs all PropertyChangeListener of anything going on.
 * Each tool is responsible of its own configuration persistence. Therefore the ToolModel must
 * be able to persist any data related to the configuration. Most likely this data is persisted
 * as UIMA parameters.
 *
 * @author Fabien Poulard <fpoulard@dictanova.com>
 */
public abstract class ToolModel {

    // Watch property changes in the bean
    protected PropertyChangeSupport propertyChangeSupport;
    // File where the model is serialized (UIMA configuration file)
    private File persistedCfg;

    /**
     * Constructor.
     * Create a new ToolModel and initialize the support of property changes
     * as well as the saving the file
     */
    public ToolModel(File cfg) {
        propertyChangeSupport = new PropertyChangeSupport(this);
        persistedCfg = cfg;
    }

    /**
     * Access to the configuration file where the configuration is persisted.
     */
    public File getConfigurationFile() {
        return persistedCfg;
    }

    /**
     * Method called when a new run is about to start. If necessary some elements in the
     * model should be reset (stats...).
     */
    public void runStarts() {
        // Nothing to do
    }

    /**
     * Method called when the run has ended. If necessary some elements in the model
     * should be computed (stats...).
     */
    public void runEnds() {
        // Nothing to do
    }

    /**
     * Load the configuration persisted if any.
     */
    public abstract void load() throws IOException, InvalidTermSuiteConfiguration;

    /**
     * Validate the current configuration.
     */
    public abstract void validate() throws ResourceConfigurationException;

    /**
     * Persists the configuration.
     */
    public abstract void save() throws IOException;

    /**
     * For the controller to bind to property changes on the model side.
     *
     * @param property
     *      name of the property to look for
     * @param listener
     *      listener that will be called when the property is changed
     *
     * @see PropertyChangeSupport#addPropertyChangeListener(String, java.beans.PropertyChangeListener)
     */
    public void addPropertyChangeListener(String property, PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(property, listener);
    }

    /**
     * Informs the listeners that some specific property has changed.
     *
     * @param property
     *      name of the property that has changed
     * @param oldValue
     *      the previous value of the property
     * @param newValue
     *      the new value of the property
     *
     * @see PropertyChangeSupport#firePropertyChange(String, boolean, boolean)
     */
    protected void firePropertyChange(String property, Object oldValue, Object newValue) {
        propertyChangeSupport.firePropertyChange(property, oldValue, newValue);
    }
	
}