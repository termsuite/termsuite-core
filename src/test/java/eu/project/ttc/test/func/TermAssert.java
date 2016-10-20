
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

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.util.Objects;

import eu.project.ttc.engines.cleaner.TermProperty;
import eu.project.ttc.models.Term;

public class TermAssert extends AbstractAssert<TermAssert, Term> {

	public TermAssert(Term actual) {
		super(actual, TermAssert.class);
	}

	public TermAssert hasGroupingKey(String gKey) {
		isNotNull();
		if (!Objects.areEqual(actual.getGroupingKey(), gKey))
			failWithMessage("Expected term's grouping key to be <%s> but was <%s>", gKey, actual.getGroupingKey());
		return this;
	}

	public TermAssert hasPropertyValue(TermProperty p, Object value) {
		isNotNull();
		if (!Objects.areEqual(p.getValue(actual), value))
			failWithMessage("Expected term's <%s> value to be <%s> but was <%s>", p, value, p.getValue(actual));
		return this;

	}

	public TermAssert hasFrequency(int f) {
		isNotNull();
		if (!Objects.areEqual(actual.getFrequency(), f))
			failWithMessage("Expected term's frequency key to be <%s> but was <%s>", f, actual.getFrequency());
		return this;
	}
}
