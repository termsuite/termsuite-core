/*******************************************************************************
 * Copyright 2015 - CNRS (Centre National de Recherche Scientifique)
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

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;

public class GroovyWord {
	public boolean neoclassical;
	public boolean compound;
	public String lemma;
	public String stem;
	public String syntacticLabel;
	public List<GroovyComponent> components;
	
	public GroovyWord(TermWord w) {
		this.compound = w.getWord().isCompound();
		this.neoclassical = w.getWord().isCompound() && w.getWord().getCompoundType() == CompoundType.NEOCLASSICAL;
		this.lemma = w.getWord().getLemma();
		this.stem = w.getWord().getStem();
		this.syntacticLabel = w.getSyntacticLabel();
		List<GroovyComponent> aux= new ArrayList<GroovyComponent>(w.getWord().getComponents().size());
		for(Component c:w.getWord().getComponents()) 
			aux.add(c.asGroovyComponent());
		this.components = ImmutableList.copyOf(aux);
	}

	public GroovyComponent getAt(int index) {
		return this.components.get(index);
	}
	
	@Override
	public int hashCode() {
		return Objects.hashCode(this.lemma);
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof GroovyWord)
			return this.lemma.equals(((GroovyWord)obj).lemma);
		else if(obj instanceof GroovyComponent)
			return this.lemma.equals(((GroovyComponent)obj).lemma);
		else if(obj instanceof CharSequence)
			return this.lemma.equals(obj);
		else return false;
	}
	
	@Override
	public String toString() {
		return Objects.toStringHelper(this)
				.add("lemma", this.lemma)
				.toString()
				;
	}
}