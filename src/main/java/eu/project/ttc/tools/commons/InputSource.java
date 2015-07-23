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

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import eu.project.ttc.tools.utils.FileComparator;
import eu.project.ttc.tools.utils.InputSourceFilter;

/**
 * Class representing an analysis engine input source, that is where the data
 * to be processed is located and what it looks like.
 */
public class InputSource {

    /**
     * Input file types for the {@link eu.project.ttc.tools.XMLTermSuiteRunner} class.
     *
     * @author Sebastián Peña Saldarriaga
     */
    public static enum InputSourceTypes {

        /**
         * Plain text input
         */
        TXT,
        /**
         * Input from URI
         */
        URI,
        /**
         * XMI input
         */
        XMI;

    }

    // directory where the files to process are located
    final File directory;
    // type of files to be processed
    final InputSourceTypes type;

    /**
     * Constructor.
     * Build a new InputSource describing how to access the data files
     * to be processed.
     *
     * @param directory
     *      path to the directory where to find the data files or eventually
     *      even a single file
     * @param type
     *      the type of file to consider for processing
     */
    public InputSource(String directory, InputSourceTypes type) {
        this.directory = new File(directory);
        this.type = type;
    }

    /**
     * Access to the kind of files to be processed.
     */
    public InputSourceTypes getType() {
        return this.type;
    }

    
    public File getDirectory() {
		return directory;
	}
    
    /**
     * Compute the list of the files to be processed.
     */
    public Collection<File> getInputFiles() throws FileNotFoundException {
        ArrayList<File> inFiles = new ArrayList<File>();
        InputSourceFilter filter = new InputSourceFilter(type);

        if (directory.isDirectory()) {
            inFiles.addAll( Arrays.asList(directory.listFiles(filter)) );
        } else if (directory.isFile()
                && filter.accept(directory.getParentFile(), directory.getName())) {
            inFiles.add(directory);
        } else {
            throw new FileNotFoundException(
                    directory.getAbsolutePath()
                    + " is not a directory, or it cannot be found.");
        }
        Collections.sort(inFiles, new FileComparator());
        return inFiles;
    }
}