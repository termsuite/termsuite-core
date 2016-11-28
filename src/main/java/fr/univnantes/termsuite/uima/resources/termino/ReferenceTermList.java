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
package fr.univnantes.termsuite.uima.resources.termino;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;

import org.apache.commons.io.IOUtils;
import org.apache.uima.resource.DataResource;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.SharedResourceObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Objects;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import fr.univnantes.termsuite.utils.TermSuiteConstants;

public class ReferenceTermList implements SharedResourceObject {
	private static final Logger LOGGER = LoggerFactory
			.getLogger(ReferenceTermList.class);

	private String path;
	
	public static class RTLTerm {
		private int id;
		private int line;
		private String string;
		private boolean variant;

		private Optional<RTLTerm> baseTerm = Optional.empty();
		private List<RTLTerm> variants = Lists.newArrayList();

		public RTLTerm(int line, int id, String string, boolean variant) {
			super();
			this.line = line;
			this.id = id;
			this.string = string;
			this.variant = variant;
		}

		void addVariant(RTLTerm variant) {
			this.variants.add(variant);
			if (variant.baseTerm.isPresent() && variant.baseTerm.get() != this)
				LOGGER.warn(
						"Cannot set base term {}:{} to variant term {}:{} (already has another base term {}:{})",
						id, string, variant.id, variant.string,
						variant.baseTerm.get().id,
						variant.baseTerm.get().string);
			else
				variant.baseTerm = Optional.of(this);
		}

		public RTLTerm getBaseTerm() {
			return baseTerm.get();
		}

		public boolean hashBaseTerm() {
			return baseTerm.isPresent();
		}

		public int getId() {
			return id;
		}

		public String getString() {
			return string;
		}

		public boolean isVariant() {
			return variant;
		}

		public List<RTLTerm> getVariants() {
			return ImmutableList.copyOf(variants);
		}
		
		@Override
		public String toString() {
			return String.format("%s%s:%s (l: %d)", isVariant() ? "V" : "T", id, string, line);
		}

		public String toTSVString() {
			return String.format("%s\t%s\t%s", id, isVariant() ? "V" : "T", string);
		}

		@Override
		public int hashCode() {
			return Objects.hashCode(string);
		}
		
		@Override
		public boolean equals(Object obj) {
			if (obj instanceof RTLTerm)
				return Objects.equal(string, ((RTLTerm)obj).string);
			return false;
		}

		public String toStringWithVar() {
			return String.format("%s (%s)", string, isVariant() ? "V" : "T");
		}

	}

	private Map<Integer, RTLTerm> bases = Maps.newHashMap();

	private int lineNb = 0;

	@Override
	public void load(DataResource data) throws ResourceInitializationException {
		InputStream inputStream = null;
		try {
			this.path = data.getUri().toString();
			LOGGER.debug("Loading reference term list at {}", this.path);
			inputStream = data.getInputStream();
			Scanner scanner = null;
			try {
				scanner = new Scanner(inputStream);
				scanner.useDelimiter(TermSuiteConstants.LINE_BREAK);
				while (scanner.hasNext()) {
					lineNb++;
					String line = scanner.next()
							.split(TermSuiteConstants.DIESE)[0].trim();
					List<String> els = Splitter.on(TermSuiteConstants.TAB)
							.splitToList(line.trim().toLowerCase());
					if (!els.isEmpty()) {
						if (els.size() != 3)
							LOGGER.warn(
									"Ignoring line {} : should have exactly 3 elements ({})",
									lineNb, line);
						else {
							int id = Integer.parseInt(els.get(0));
							RTLTerm refTerm = new RTLTerm(lineNb, id, els.get(2),
									els.get(1).toLowerCase().equals("v"));
							if(refTerm.isVariant()) {
								if(!bases.containsKey(id)) {
									LOGGER.warn("No such base term id {} for variant term {}", id, refTerm);
									continue;
								} else
									bases.get(id).addVariant(refTerm);
							} else {
								bases.put(id, refTerm);
							}
						}
					}
				}
				this.bases = ImmutableMap.copyOf(this.bases);
				int total = 0;
				for(RTLTerm ref:bases.values())
					total += 1 + ref.getVariants().size();
				LOGGER.debug("Reference term list loaded (nb terms: {}, nb terms and variants: {})", 
						this.bases.keySet().size(),
						total
						);
			} catch (Exception e) {
				e.printStackTrace();
				throw new ResourceInitializationException(e);
			} finally {
				IOUtils.closeQuietly(scanner);
			}
		} catch (IOException e) {
			LOGGER.error("Could not load file {}", data.getUrl());
			throw new ResourceInitializationException(e);
		} finally {
			IOUtils.closeQuietly(inputStream);
		}
	}
	
	public Collection<RTLTerm> getTerms() {
		return bases.values();
	}


	public String getPath() {
		return path;
	}

	public List<RTLTerm> asList() {
		return ImmutableList.copyOf(bases.values());
	}
}
