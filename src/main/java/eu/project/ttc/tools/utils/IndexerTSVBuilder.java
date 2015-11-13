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

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import eu.project.ttc.engines.cleaner.TermProperty;
import eu.project.ttc.models.Term;
import eu.project.ttc.models.TermIndex;

/**
 * Incrementally creates an indexer output TSV file.
 * 
 * @author Sebastián Peña Saldarriaga
 */
public class IndexerTSVBuilder extends AbstractTSVBuilder {

	/** Number of terms */
	private int termCount = 1;

	private List<TermProperty> properties;
	
	private boolean showScores;
	/**
	 * Initializes a new instance using the specified output
	 * 
	 * @param out
	 *            The output writer.
	 */
	public IndexerTSVBuilder(Writer out, List<TermProperty> properties, boolean showScores) {
		super(out);
		this.properties = properties;
		this.showScores = showScores;
	}

	
	/**
	 * Adds a term, increment id and return it
	 * 
	 * @param term
	 * @return The new id
	 * @throws IOException
	 */
	public Integer addTerm(TermIndex termIndex, Term term, String label) throws IOException {
		startTerm(termIndex, term, label);
		endTerm();
		return termCount;
	}

	private static final String SPEC_FORMAT = "%.2f";
	private static final String T_LABEL_FORMAT = "T[%s]";
	private static final String T_LABEL = "T";
	private static final String V_LABEL_FORMAT = "V[%s]";
	private static final String V_LABEL = "V";
	
	public void startTerm(TermIndex termIndex, Term term, String label) throws IOException {
		if(showScores) {
			appendTerm(termIndex, term, String.format(T_LABEL_FORMAT,label));
		} else {
			appendTerm(termIndex, term, T_LABEL);
		}
	}

	public void addVariant(TermIndex termIndex,Term variant, String label) throws IOException {
		if(showScores) {
			appendTerm(termIndex, variant, String.format(V_LABEL_FORMAT, label));
		} else {
			appendTerm(termIndex, variant, V_LABEL);
		}
	}

	public void endTerm() {
		termCount++;
	}
	
	private void appendTerm(TermIndex termIndex, Term t, String termType) throws IOException {
		List<String> line = Lists.newArrayList();
		line.add(termType);
		for(TermProperty p:properties) {
			Comparable<?> value = p.getValue(termIndex, t);
			if (value instanceof Integer || value instanceof Long)
				line.add(value.toString());
			else if(value instanceof Double || value instanceof Float) {
				line.add(String.format(SPEC_FORMAT, value));
			} else
				line.add(value.toString());
		}
		append(Integer.toString(termCount), line.toArray(new String[line.size()]));
	}


	public void writeHeaders() throws IOException {
		List<String> headers = Lists.newArrayList();
		headers.add("baseId");
		headers.add("type");
		for(TermProperty p:properties)
			headers.add(p.getShortName());
		write(Joiner.on("\t").join(headers));
		write("\n");
	}


}
