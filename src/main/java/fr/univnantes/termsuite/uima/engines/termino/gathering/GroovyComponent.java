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
package fr.univnantes.termsuite.uima.engines.termino.gathering;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import fr.univnantes.termsuite.model.Component;

public class GroovyComponent {
	public String lemma;
	public String substring;
	
	public GroovyComponent(Component a) {
		this.lemma = a.getLemma();
		this.substring = a.getSubstring();
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(lemma);
	}
	
	private boolean hasLemma() {
		return this.lemma != null;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(!hasLemma())
			return false;
		else if(obj instanceof GroovyWord)
			return this.lemma.equals(((GroovyWord)obj).lemma);
		else if(obj instanceof GroovyComponent)
			return this.lemma.equals(((GroovyComponent)obj).lemma);
		else if(obj instanceof CharSequence)
			return this.lemma.equals(obj);
		else return false;
	}
	
	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("lemma", this.lemma)
				.toString()
				;
	}
}