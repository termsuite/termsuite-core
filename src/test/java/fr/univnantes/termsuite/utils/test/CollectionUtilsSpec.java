
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

package fr.univnantes.termsuite.utils.test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.util.List;
import java.util.Set;

import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import fr.univnantes.termsuite.utils.CollectionUtils;

public class CollectionUtilsSpec {

	@Test
	public void testCombineAndProduct() {
		List<? extends Set<String>> sets = ImmutableList.of(
				ImmutableSet.of("a", "b", "c"),
				ImmutableSet.of("d"),
				ImmutableSet.of("e", "f")
			);
		
		assertThat(CollectionUtils.combineAndProduct(sets))
			.hasSize(11)
			.extracting("element1", "element2")
			.contains(
					tuple("a", "d"),
					tuple("b", "d"),
					tuple("c", "d"),
					tuple("a", "e"),
					tuple("b", "e"),
					tuple("c", "e"),
					tuple("a", "f"),
					tuple("b", "f"),
					tuple("c", "f"),
					tuple("d", "e"),
					tuple("d", "f")
				);
	}
}
