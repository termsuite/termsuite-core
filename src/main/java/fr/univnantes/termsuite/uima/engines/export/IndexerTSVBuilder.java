
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
package fr.univnantes.termsuite.uima.engines.export;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import fr.univnantes.termsuite.model.Property;
import fr.univnantes.termsuite.model.RelationProperty;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.TermProperty;
import fr.univnantes.termsuite.model.TermRelation;
import fr.univnantes.termsuite.model.Terminology;

/**
 * Incrementally creates an indexer output TSV file.
 * 
 * @author Sebastián Peña Saldarriaga
 */
public class IndexerTSVBuilder extends AbstractTSVBuilder {

	private List<Property<?>> properties;
	
	/**
	 * Initializes a new instance using the specified output
	 * 
	 * @param out
	 *            The output writer.
	 */
	public IndexerTSVBuilder(Writer out, List<Property<?>> properties) {
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
	public void addTerm(Terminology termino, Term term) throws IOException {
		startTerm(termino, term);
	}

	private static final String SPEC_FORMAT = "%.2f";
	private static final String T_FORMAT = "T";
	private static final String V_FORMAT = "V[%s]";
	
	private Term currentTerm = null;
	
	public void startTerm(Terminology termino, Term term) throws IOException {
		this.currentTerm = term;
		appendTerm(
				termino, 
				term, 
				String.format(T_FORMAT));
	}

	public void addVariant(Terminology termino, TermRelation variation, boolean addVariantTag) throws IOException {
		List<String> line = Lists.newArrayList();
		String typeCol = String.format(V_FORMAT, variation.getType().getLetter());
		if(addVariantTag)
			typeCol = typeCol + "[+]";
		line.add(typeCol);
		for(Property<?> p:properties) {
			if(p instanceof RelationProperty) {
				Comparable<?> value = variation.getPropertyValueUnchecked((RelationProperty)p);
				line.add(getPropertyValue(value));
			} else if(p instanceof TermProperty) {
				Comparable<?> value = variation.getTo().getPropertyValueUnchecked((TermProperty)p);
				line.add(getPropertyValue(value));
			} else
				line.add("");
		}
		append(
				currentTerm.getRank() == null ? "-" : Integer.toString(currentTerm.getRank()), 
				line.toArray(new String[line.size()]));
	}

	private void appendTerm(Terminology termino, Term t, String termType) throws IOException {
		List<String> line = Lists.newArrayList();
		line.add(termType);
		for(Property<?> p:properties) {
			if(p instanceof TermProperty) {
				Comparable<?> value = t.getPropertyValueUnchecked((TermProperty)p);
				line.add(getPropertyValue(value));
			} else
				line.add("");
		}
		append(
				currentTerm.getRank() == null ? "-" : Integer.toString(currentTerm.getRank()), 
				line.toArray(new String[line.size()]));
	}

	private String getPropertyValue(Comparable<?> value) {
		if (value instanceof Integer || value instanceof Long)
			return value.toString();
		else if(value instanceof Boolean) 
			return ((Boolean)value) ? "1" : "0";
		else if(value instanceof Double || value instanceof Float) {
			return String.format(SPEC_FORMAT, value);
		} else if(value == null)
			return "";
		else
			return value.toString();
	}


	public void writeHeaders() throws IOException {
		List<String> headers = Lists.newArrayList();
		headers.add("#");
		headers.add("type");
		for(Property<?> p:properties)
			headers.add(p.getShortName());
		write(Joiner.on("\t").join(headers));
		write("\n");
	}


}
