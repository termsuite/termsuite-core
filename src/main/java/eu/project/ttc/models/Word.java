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
package eu.project.ttc.models;

import java.util.List;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import eu.project.ttc.utils.StringUtils;

public class Word extends LemmaStemHolder {
	
	private CompoundType compoundType;
	
	private String stem;

	private List<Component> components;

	private static final String MSG_NOT_COMPOUND = "Word <%s> is not a compound";
	private static final String MSG_NOT_NEOCLASSICAL = "Word <%s> is not a neoclassical compound.";
	private static final String MSG_NO_NEOCLASSICAL_COMPOUND = "No neoclassical compound found for word <%s>";
	
	public Word(String lemma, String stem) {
		super(lemma);
		this.stem = stem;
		resetComposition();
	}

	public void resetComposition() {
		components = ImmutableList.of();
		compoundType = CompoundType.UNSET;
	}

	public boolean isCompound() {
		return compoundType != CompoundType.UNSET && this.components.size() > 1;
	}

	public String getStem() {
		return stem;
	}

	public void setStem(String stem) {
		this.stem = stem;
	}

	public void setComposition(CompoundType type, List<Component> components) {
		this.compoundType = type;
		this.components = ImmutableList.copyOf(components);
	}
	
	public List<Component> getComponents() {
		return components;
	}
	
	
	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("lemma", this.lemma)
				.add("isCompound", this.isCompound())
				.toString();
	}
	
	public CompoundType getCompoundType() {
		return compoundType;
	}

	void setCompoundType(CompoundType compoundType) {
		this.compoundType = compoundType;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Word) {
			Word o = (Word) obj;
			return this.lemma.equals(o.lemma);
		} else
			return false;
	}
	
	@Override
	public int hashCode() {
		return this.lemma.hashCode();
	}

	private String normalizedStem;
	public String getNormalizedStem() {
		if(normalizedStem == null)
			this.normalizedStem = StringUtils.replaceAccents(stem).toLowerCase();
		return this.normalizedStem;
	}

	private String normalizedLemma;
	public String getNormalizedLemma() {
		if(normalizedLemma == null)
			this.normalizedLemma = StringUtils.replaceAccents(this.lemma).toLowerCase();
		return this.normalizedLemma;
	}

	/**
	 * Returns the {@link Component} object if
	 * 
	 * @return
	 * 		The neoclassical component affix
	 * 
	 * @throws IllegalStateException when this word is not a neoclassical compound
	 */
	public Component getNeoclassicalAffix() {
		Preconditions.checkState(isCompound(), MSG_NOT_COMPOUND, this);
		Preconditions.checkState(getCompoundType() == CompoundType.NEOCLASSICAL, MSG_NOT_NEOCLASSICAL, this);
		for(Component c:components)
			if(c.isNeoclassicalAffix())
				return c;
		throw new IllegalArgumentException(String.format(MSG_NO_NEOCLASSICAL_COMPOUND, this));
	}
}
