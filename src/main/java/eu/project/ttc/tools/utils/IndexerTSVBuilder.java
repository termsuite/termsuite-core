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

import java.io.IOException;
import java.io.Writer;
import java.util.List;

import com.google.common.collect.Lists;

import eu.project.ttc.engines.cleaner.TermProperty;
import eu.project.ttc.models.Term;

/**
 * Incrementally creates an indexer output TSV file.
 * 
 * @author Sebastián Peña Saldarriaga
 */
public class IndexerTSVBuilder extends AbstractTSVBuilder {

	/** Number of terms */
	private int termCount = 1;

	private List<TermProperty> properties;
	
	/**
	 * Initializes a new instance using the specified output
	 * 
	 * @param out
	 *            The output writer.
	 */
	public IndexerTSVBuilder(Writer out, List<TermProperty> properties) {
		super(out);
		this.properties = properties;
	}

	
	/**
	 * Adds a term, increment id and return it
	 * 
	 * @param term
	 * @return The new id
	 * @throws IOException
	 */
	public Integer addTerm(Term term) throws IOException {
		startTerm(term);
		endTerm();
		return termCount;
	}

	private static final String SPEC_FORMAT = "%.2f";
	public void startTerm(Term term) throws IOException {
		appendTerm(term, "T");
	}

	public void addVariant(Term variant, String label) throws IOException {
		appendTerm(variant, String.format("V[%s]", label));
	}

	public void endTerm() {
		termCount++;
	}
	
	private void appendTerm(Term t, String termType) throws IOException {
		List<String> line = Lists.newArrayList();
		line.add(termType);
		for(TermProperty p:properties) {
			Comparable<?> value = p.getValue(t);
			if (value instanceof Integer || value instanceof Long)
				line.add(value.toString());
			else if(value instanceof Double || value instanceof Float) {
				line.add(String.format(SPEC_FORMAT, value));
			} else
				line.add(value.toString());
		}
		append(Integer.toString(termCount), line.toArray(new String[line.size()]));
	}
}
