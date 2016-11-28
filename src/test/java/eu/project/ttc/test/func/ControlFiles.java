
/*******************************************************************************
 * Copyright 2015-2016 - CNRS (Centre National de Recherche Scientifique)
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *******************************************************************************/

package eu.project.ttc.test.func;

import static org.assertj.core.api.Assertions.tuple;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;

import org.assertj.core.groups.Tuple;
import org.assertj.core.util.Lists;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;

import fr.univnantes.termsuite.model.CompoundType;
import fr.univnantes.termsuite.model.Lang;
import fr.univnantes.termsuite.model.RelationType;
import fr.univnantes.termsuite.tools.ControlFilesGenerator;

public class ControlFiles {
	
	
	private static final Path CONTROL_DIRETCORY_PATH = Paths.get("src", "test", "resources", "eu", "project", "ttc", "test", "corpus");


	public static Path getControlDirectory(Lang lang, String corpus) {
		Path controlDirectory = CONTROL_DIRETCORY_PATH.resolve(corpus).resolve(lang.getName()).resolve("control");
		Preconditions.checkState(controlDirectory.toFile().exists());
		Preconditions.checkState(controlDirectory.toFile().isDirectory());
		return controlDirectory;
		
	}
		
	/**
	 * 
	 * tuple("source gKey", "target gKey")
	 * 
	 * @param lang
	 * 			the language
	 * @param corpus
	 * 			the test corpus
	 * @return
	 * 			the list of tuples parsed from control file
	 */
	public static Tuple[] prefixVariationTuples(Lang lang, String corpus) {
		Path path = getControlDirectory(lang, corpus).resolve(ControlFilesGenerator.getPrefixFileName());
		return array(getPrefixVariationTuples(path.toFile()));
	}
	
	/**
	 * 
	 * tuple("Deriv pattern", "source gKey", "target gKey")
	 * 
	 * e.g. tuple("N A", "n: éolienne", "a: éolien")
	 * 
	 * @param lang
	 * 			the language
	 * @param corpus
	 * 			the test corpus
	 * @return
	 * 			the list of tuples parsed from control file
	 */
	public static Tuple[] derivateVariationTuples(Lang lang, String corpus) {
		Path path = getControlDirectory(lang, corpus).resolve(ControlFilesGenerator.getDerivatesFileName());
		return array(getDerivateVariationTuples(path.toFile()));
	}


	/**
	 * 
	 * 
	 * tuple("source gKey", "target gKey", VariationType)
	 * 
	 * 
	 * @param lang
	 * 			the language
	 * @param corpus
	 * 			the test corpus
	 * @param ruleName
	 * 			the name of the syntactic rule to control
	 * @return
	 * 			the list of tuples parsed from control file
	 */
	public static Iterable<Tuple> syntacticVariationTuples(Lang lang, String corpus, String ruleName) {
		Path path = syntacticVariationControlFilePath(lang, corpus, ruleName);
		return getSyntacticVariationTuples(path.toFile(), ruleName);
	}
	
	/**
	 * Returns a list of tuples having the following format :
	 * 
	 * tuple("Compound type short name", "term gKey", "compound string")
	 * 
	 * e.g. tuple("nat", "n: interconnexion", "inter:inter|connexion:connexion")
	 * 
	 * @see CompoundType#getShortName()
	 * @see ControlFilesGenerator#toCompoundString(eu.project.ttc.model.Term)
	 * @param lang
	 * 			the language
	 * @param corpus
	 * 			the test corpus
	 * @return
	 * 			the list of tuples parsed from control file
	 */
	public static Tuple[] compoundTuples(Lang lang, String corpus) {
		Path path = getControlDirectory(lang, corpus).resolve(ControlFilesGenerator.getCompoundsFileName());
		return array(getCompoundTuples(path.toFile()));
	}

	
	public static List<String> getLines(File file) {
		Preconditions.checkArgument(file.exists());
		List<String> lines = Lists.newArrayList();
		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
		    String line;
		    while ((line = br.readLine()) != null) {
		    	String trim = line.trim();
				if(trim.startsWith("#") || trim.isEmpty())
		    		continue;
				else
					lines.add(trim);
		    }
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
		return lines;
	}
	
	public static List<String[]> getRows(File file, int nbColumns, String sep) {
		List<String[]> rows = Lists.newArrayList();
		for(String line:getLines(file)) {
			List<String> valuesAsList = Splitter.on(sep).splitToList(line);
			Preconditions.checkArgument(valuesAsList.size() == nbColumns,
					"Bad row format for line: \"%s\". Expected %s columns, got %s", line, nbColumns, valuesAsList.size());
			rows.add(valuesAsList.toArray(new String[nbColumns]));
		}
		return rows;
	}
	
	public static List<Tuple> getSyntacticVariationTuples(File file, String ruleName) {
		List<Tuple> tuples = Lists.newArrayList();
		for(String[] row:getRows(file, 4, "\t")) {
			Preconditions.checkState(row[3].equals(ruleName));
			tuples.add(tuple(row[0], row[1], RelationType.valueOf(row[2])));
		}
		return tuples;
	}

	private static List<Tuple> getPrefixVariationTuples(File file) {
		List<Tuple> tuples = Lists.newArrayList();
		for(String[] row:getRows(file, 3, "\t")) {
			Preconditions.checkState(row[2].equals(RelationType.IS_PREFIX_OF.toString()));
			tuples.add(tuple(row[0], row[1]));
		}
		return tuples;
	}
	
	private static Tuple[] array(Collection<Tuple> tuples) {
		return tuples.toArray(new Tuple[tuples.size()]);
	}
	
	
	private static List<Tuple> getDerivateVariationTuples(File file) {
		List<Tuple> tuples = Lists.newArrayList();
		for(String[] row:getRows(file, 4, "\t")) {
			Preconditions.checkState(row[2].equals(RelationType.DERIVES_INTO.toString()));
			tuples.add(tuple(row[3], row[0], row[1]));
		}
		return tuples;
	}
	
	private static List<Tuple> getCompoundTuples(File file) {
		List<Tuple> tuples = Lists.newArrayList();
		for(String[] row:getRows(file, 3, "\t")) {
			tuples.add(tuple(row[1], row[0], row[2]));
		}
		return tuples;

	}

	public static Path syntacticVariationControlFilePath(Lang lang, String corpus, String ruleName) {
		return getControlDirectory(lang, corpus).resolve(ControlFilesGenerator.getSyntacticRuleFileName(ruleName));
	}

}
