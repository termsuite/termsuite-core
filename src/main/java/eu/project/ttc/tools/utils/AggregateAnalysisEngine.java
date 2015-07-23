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
package eu.project.ttc.tools.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.uima.ResourceSpecifierFactory;
import org.apache.uima.UIMAFramework;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.metadata.AnalysisEngineMetaData;
import org.apache.uima.analysis_engine.metadata.FixedFlow;
import org.apache.uima.collection.metadata.CpeDescriptorException;
import org.apache.uima.resource.ResourceConfigurationException;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.metadata.Capability;
import org.apache.uima.resource.metadata.ConfigurationParameter;
import org.apache.uima.resource.metadata.ConfigurationParameterDeclarations;
import org.apache.uima.resource.metadata.ConfigurationParameterSettings;
import org.apache.uima.resource.metadata.Import;
import org.apache.uima.resource.metadata.MetaDataObject;
import org.apache.uima.resource.metadata.NameValuePair;
import org.apache.uima.resource.metadata.OperationalProperties;
import org.apache.uima.util.InvalidXMLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import eu.project.ttc.engines.CompostAE;

public abstract class AggregateAnalysisEngine {
	private static final Logger LOGGER = LoggerFactory.getLogger(CompostAE.class);

	private String name;
	
	private void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return this.name;
	}
	
	private File file;
	
	private void setFile() throws IOException {
		String name = this.getName().replaceAll("\\s+","-");
		this.file = File.createTempFile(name.toLowerCase() + "-",".xml");
		this.file.deleteOnExit();
	}
	
	public File getFile() {
		return this.file;
	}
	
	private ResourceSpecifierFactory factory;
	
	private void setFactory() {
		this.factory = UIMAFramework.getResourceSpecifierFactory();
	}
	
	protected ResourceSpecifierFactory getFactory() {
		return this.factory;
	}
	
	private AnalysisEngineDescription description;
	
	private void setAnnalysisEngineDescription() throws CpeDescriptorException, IOException, InvalidXMLException, SAXException, ResourceInitializationException, ResourceConfigurationException {
		this.description = this.getFactory().createAnalysisEngineDescription();
		this.description.setPrimitive(false);
		this.setMetaData();
		this.setOperationalProperties();
		this.setParameters();
		this.setCapabilities();
		this.setFlowController();
		this.setFlowConstraints();
		this.setExternalResources();
		this.setConfigurationParameterDeclarations();
		this.setParameterSettings();
		this.description.validate();
		this.description.doFullValidation();
		this.doStore();
	}
	
	public AnalysisEngineDescription getAnalysisEngineDescription() {
		return this.description;
	}
	
	public AggregateAnalysisEngine(String name) throws IOException, InvalidXMLException, ResourceInitializationException, ResourceConfigurationException, CpeDescriptorException, SAXException {
		this.setName(name);
		this.setFile();
		this.setFactory();
		this.setAnnalysisEngineDescription();
	}
	
	private void setMetaData() {
		AnalysisEngineMetaData md = this.getFactory().createAnalysisEngineMetaData();
		md.setName(this.getName());
		this.getAnalysisEngineDescription().setMetaData(md);
	}
	
	private void setCapabilities() {
		Capability capability = this.getFactory().createCapability();
		Capability[] capabilities = new Capability[] { capability };
		this.getAnalysisEngineDescription().getAnalysisEngineMetaData().setCapabilities(capabilities);
	}

	private void setParameters() {
		ConfigurationParameterDeclarations parameters = this.getFactory().createConfigurationParameterDeclarations();
		this.getAnalysisEngineDescription().getAnalysisEngineMetaData().setConfigurationParameterDeclarations(parameters);
	}
	
	protected void setConfigurationParameterDeclarations() {
		
	}
	
	protected void setParameter(String name, String type) {
		
		ConfigurationParameter parameter = this.getFactory().createConfigurationParameter();
		parameter.setName(name);
		parameter.setMultiValued(false);
		parameter.setType(type);
		Map<String, MetaDataObject> analysisEngines = this.getAnalysisEngineDescription().getDelegateAnalysisEngineSpecifiersWithImports();
		Set<String> keys = analysisEngines.keySet();
		List<String> overrides = new ArrayList<String>();
		Iterator<String> iterator = keys.iterator();
		while (iterator.hasNext()) {
			String key = iterator.next();
			try {
				AnalysisEngineDescription ae = (AnalysisEngineDescription) this.getAnalysisEngineDescription().getDelegateAnalysisEngineSpecifiers().get(key);
				ConfigurationParameterDeclarations decl = ae.getAnalysisEngineMetaData().getConfigurationParameterDeclarations();
				if (decl.getConfigurationParameter(null, name) != null) {
					overrides.add( key + "/" + name);
				}
			} catch (InvalidXMLException e) {
				LOGGER.warn(e.getMessage());
			}
		}
		String[] overRides = new String[overrides.size()];
		overrides.toArray(overRides);
		parameter.setOverrides(overRides);
		ConfigurationParameterDeclarations declarations = this.getAnalysisEngineDescription().getAnalysisEngineMetaData().getConfigurationParameterDeclarations();
		declarations.addConfigurationParameter(parameter);
	}

	private void setOperationalProperties() {
		OperationalProperties opProp = this.getFactory().createOperationalProperties();
		opProp.setModifiesCas(true);
		opProp.setMultipleDeploymentAllowed(true);
		opProp.setOutputsNewCASes(false);
		this.getAnalysisEngineDescription().getAnalysisEngineMetaData().setOperationalProperties(opProp);
	}
	
	private void setParameterSettings() {
		ConfigurationParameterSettings settings = this.getFactory().createConfigurationParameterSettings();
		this.getAnalysisEngineDescription().getAnalysisEngineMetaData().setConfigurationParameterSettings(settings);
		settings.setParameterSettings(this.getNameValuePairs());
	}
	
	protected void setFlowController() {
		
	}
	
	private void setFlowConstraints() {
		String[] annotators = this.getFlow();
		String[] flows = new String[annotators.length]; 
		for (int index = 0; index < flows.length; index++) {
			this.setFlowConstraint(flows,annotators[index], index);
		}
		FixedFlow constraints = this.getFactory().createFixedFlow();
		constraints.setFixedFlow(flows);
		this.getAnalysisEngineDescription().getAnalysisEngineMetaData().setFlowConstraints(constraints);
	}
	
	private void setFlowConstraint(String[] flows,String name,int index) {
		Import aeImport = this.getFactory().createImport();
		aeImport.setName(name);
		String key = this.getKey(name);
		flows[index] = key;
		this.getAnalysisEngineDescription().getDelegateAnalysisEngineSpecifiersWithImports().put(key,aeImport);
	}

	private String getKey(String name) {
		int index = name.lastIndexOf('.');
		if (index == -1) {
			return name;
		} else {
			return name.substring(index + 1);
		}
	}
	
	private void doStore() throws FileNotFoundException, SAXException, IOException {
		FileOutputStream stream = new FileOutputStream(this.getFile());
		this.getAnalysisEngineDescription().toXML(stream,true);
		stream.close();
	}
	
	protected void setExternalResources() {
		
	}
	
	protected abstract NameValuePair[] getNameValuePairs();
	
	protected abstract String[] getFlow();
	
}
