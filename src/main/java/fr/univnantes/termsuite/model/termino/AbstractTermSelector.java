
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

package fr.univnantes.termsuite.model.termino;

import com.google.common.base.Preconditions;

import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.Terminology;

public abstract class AbstractTermSelector implements TermSelector {

	private boolean requiredTermino;
	
	public AbstractTermSelector(boolean terminoRequired) {
		super();
		this.requiredTermino = terminoRequired;
	}

	@Override
	public boolean select(Term t) {
		String ERR_TERMINOLOGY_REQUIRED = "Terminology is required for selector %s. You must invoke the #select(Terminology, Term).";
		Preconditions.checkState(
				!this.requiredTermino,
				ERR_TERMINOLOGY_REQUIRED,
				this.getClass().getName());
		return select(null, t);
	}

	public abstract boolean select(Terminology termino, Term t);
}
