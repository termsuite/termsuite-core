
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

package eu.project.ttc.models.index.selectors;

import com.google.common.base.Preconditions;

import eu.project.ttc.models.Term;
import eu.project.ttc.models.TermIndex;

public abstract class AbstractTermSelector implements TermSelector {

	private boolean requiredTermIndex;
	
	public AbstractTermSelector(boolean requiredTermIndex) {
		super();
		this.requiredTermIndex = requiredTermIndex;
	}

	@Override
	public boolean select(Term t) {
		String ERR_TERM_INDEX_REQUIRED = "TermIndex is required for selector %s. You must invoke the #select(TermIndex, Term).";
		Preconditions.checkState(
				!this.requiredTermIndex,
				ERR_TERM_INDEX_REQUIRED,
				this.getClass().getName());
		return select(null, t);
	}

	public abstract boolean select(TermIndex termIndex, Term t);
}
