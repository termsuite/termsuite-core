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
package eu.project.ttc.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;

import org.apache.commons.io.FileUtils;
import org.apache.uima.UIMAFramework;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineManagement;
import org.apache.uima.analysis_engine.JCasIterator;
import org.apache.uima.analysis_engine.metadata.AnalysisEngineMetaData;
import org.apache.uima.cas.impl.XmiCasDeserializer;
import org.apache.uima.examples.SourceDocumentInformation;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.metadata.ConfigurationParameterSettings;
import org.apache.uima.resource.metadata.NameValuePair;
import org.apache.uima.util.JCasPool;
import org.apache.uima.util.Level;

import eu.project.ttc.tools.commons.InputSource.InputSourceTypes;
import eu.project.ttc.tools.utils.InputSourceFilter;
import eu.project.ttc.utils.StringUtils;

/**
 * A simple runner for CLI, does not include GUI dependences and should not rely
 * en X.
 * 
 * @author Sebastián Peña Saldarriaga <sebastian@dictanova.com>
 */
public class TermSuiteCLIRunner implements Runnable {

    /**
     * This attribute corresponds to the CAS pool build from the XMI file list
     * of the remote resources.
     */
    private JCasPool pool;

    private AnalysisEngine analysisEngine;

    private final ArrayList<File> data = new ArrayList<File>();

    private final InputSourceTypes inputMode;

    private final String language;

    private final String encoding;

    public TermSuiteCLIRunner(AnalysisEngineDescription description,
            String directory, InputSourceTypes sourceType, String language,
            String encoding) throws Exception {
        this.inputMode = sourceType;
        this.language = language;
        this.encoding = encoding;
        this.initializeEngine(description, description
                .getAnalysisEngineMetaData()
                .getConfigurationParameterSettings());
        this.initializePool();
        this.setData(directory, this.inputMode);
    }

    /**
     * Creates the internal {@link AnalysisEngine} from the specified
     * <code>description</code> and <code>settings</code>.
     * 
     * @param description
     *            The AE description
     * @param settings
     *            The AE parameter settings
     * @throws ResourceInitializationException
     */
    private void initializeEngine(AnalysisEngineDescription description,
            ConfigurationParameterSettings settings)
            throws ResourceInitializationException {
        UIMAFramework.getLogger().log(Level.INFO,
                "Initializing analysis engine.");
        description.getAnalysisEngineMetaData()
                .setConfigurationParameterSettings(settings);
        for (NameValuePair pair : settings.getParameterSettings()) {
            String name = pair.getName();
            Object value = pair.getValue();
            description.getAnalysisEngineMetaData()
                    .getConfigurationParameterSettings()
                    .setParameterValue(name, value);
        }
        analysisEngine = AnalysisEngineFactory.createEngine(description);
        UIMAFramework.getLogger()
                .log(Level.INFO, "Initialization of AEs done.");
    }

    /**
     * Initializes a pool of {@link JCas}s for internal processing
     * 
     * @throws ResourceInitializationException
     */
    private void initializePool() throws ResourceInitializationException {
        int threads = Runtime.getRuntime().availableProcessors();
        this.pool = new JCasPool(threads, this.analysisEngine);
    }

    /**
     * Loads list of files to process from the specified <code>path</code>
     * assuming the given input <code>mode</code>
     * 
     * @param path
     *            The path to files
     * @param mode
     *            The file input type
     * @throws Exception
     */
    private void setData(String path, InputSourceTypes mode) throws Exception {
        InputSourceFilter filter = new InputSourceFilter(mode);
        File file = new File(path);
        if (file.isDirectory()) {
            File[] files = file.listFiles(filter);
            for (File f : files) {
                this.data.add(f);
            }
        } else if (file.isFile()
                && filter.accept(file.getParentFile(), file.getName())) {
            this.data.add(file);
        } else {
            throw new FileNotFoundException(path
                    + " is not a directory, or it cannot be found.");
        }
        Collections.sort(this.data, StringUtils.alphanumFileComparator);
    }

    @Override
    public void run() {
        try {
            for (int index = 0; index < this.data.size(); index++) {
                File file = this.data.get(index);
                UIMAFramework.getLogger().log(Level.INFO,
                        "Processing file '" + file + "'.");
                boolean last = index == this.data.size() - 1;
                try {
                    this.process(file, this.encoding, this.language,
                            this.inputMode, last);
                } catch (Throwable e) {
                    UIMAFramework.getLogger().log(Level.SEVERE, e.getMessage());
                    e.printStackTrace();
                }
            }
            this.analysisEngine.collectionProcessComplete();
        } catch (Throwable e) {
            UIMAFramework.getLogger().log(
                    Level.SEVERE,
                    "An error occurred while running the analysis: "
                            + e.getMessage());
            e.printStackTrace();
        } finally {
            cleanUp();
        }
    }

    public void cleanUp() {
//        String message = display(
//                this.analysisEngine.getAnalysisEngineMetaData(),
//                this.analysisEngine.getManagementInterface(), 0);
//        UIMAFramework.getLogger().log(Level.INFO, message);
        UIMAFramework.getLogger().log(Level.INFO, "Cleanning up resources.");
        analysisEngine.destroy();
        UIMAFramework.getLogger().log(Level.INFO, "Cleanning done.");
    }

    private void process(File file, String encoding, String language,
            InputSourceTypes mode, boolean last) throws Exception {

        // FIXME quick hack to dispose language
        language = (String) this.analysisEngine.getAnalysisEngineMetaData()
                .getConfigurationParameterSettings()
                .getParameterValue("Language");

        JCas cas = this.pool.getJCas();
        try {
            String uri = file.toURI().toString();
            SourceDocumentInformation sdi = new SourceDocumentInformation(cas);
            sdi.setUri(uri);

            switch (mode) {
            case TXT:
                String text = FileUtils.readFileToString(file, encoding);
                cas.setDocumentLanguage(language);
                cas.setDocumentText(text);
                sdi.setBegin(0);
                sdi.setEnd(text.length());
                sdi.setOffsetInSource(0);
                break;
            case XMI:
                InputStream inputStream = new FileInputStream(file);
                try {
                    XmiCasDeserializer.deserialize(inputStream, cas.getCas(),
                            true);
                } finally {
                    inputStream.close();
                }
                break;
            case URI:
                String mime = file.toURI().toURL().openConnection()
                        .getContentType();
                cas.setSofaDataURI(uri, mime);
                break;
            }

            sdi.setLastSegment(last);
            sdi.addToIndexes();
            if (this.analysisEngine.getAnalysisEngineMetaData()
                    .getOperationalProperties().getOutputsNewCASes()) {
                JCasIterator iterator = this.analysisEngine
                        .processAndOutputNewCASes(cas);
                iterator.release();
            } else {
                this.analysisEngine.process(cas);
            }
        } finally {
            this.pool.releaseJCas(cas);
        }
    }

    /**
     * Creates a message for the end of the run
     * 
     * @param metadata
     * @param managment
     * @param level
     * @return
     */
    public static final String display(AnalysisEngineMetaData metadata,
            AnalysisEngineManagement managment, int level) {
        long time = managment.getAnalysisTime();
        String name = managment.getName();
        String perf = managment.getCASesPerSecond();
        long num = managment.getNumberOfCASesProcessed();
        StringBuilder display = new StringBuilder();
        if (level == 0) {
            display.append("\n\n");
        }
        for (int index = 0; index < level; index++) {
            display.append("    ");
        }
        display.append("Analysis Engine '" + name + "'\n");
        for (int index = 0; index < level; index++) {
            display.append("    ");
        }
        display.append("processes " + num + " documents in " + time
                + " milli-seconds (" + perf + " doc/s).\n\n");
        for (String key : managment.getComponents().keySet()) {
            AnalysisEngineManagement m = managment.getComponents().get(key);
            display.append(display(metadata, m, level + 1));
        }
        return display.toString();
    }
}
