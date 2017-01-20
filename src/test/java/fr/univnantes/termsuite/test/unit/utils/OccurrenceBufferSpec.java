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
package fr.univnantes.termsuite.test.unit.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;
import java.util.Iterator;

import org.junit.Test;

import fr.univnantes.lina.uima.tkregex.RegexOccurrence;
import fr.univnantes.termsuite.test.unit.TestUtil;
import fr.univnantes.termsuite.utils.OccurrenceBuffer;

public class OccurrenceBufferSpec {

	OccurrenceBuffer buffer;

	private void initBuffer() {
		buffer = new OccurrenceBuffer();
		buffer.bufferize(TestUtil.createOccurrence(0, 10, "A"));
		buffer.bufferize(TestUtil.createOccurrence(0, 10, "B"));
		buffer.bufferize(TestUtil.createOccurrence(4, 10, "C"));
		buffer.bufferize(TestUtil.createOccurrence(2, 8, "D"));
		buffer.bufferize(TestUtil.createOccurrence(2, 8, "E"));
		buffer.bufferize(TestUtil.createOccurrence(2, 8, "F"));

	}
	
	@Test
	public void testFindDuplicates() {
		initBuffer();
		assertThat(buffer.findDuplicates()).hasSize(2);
		Iterator<Collection<RegexOccurrence>> iterator = buffer.findDuplicates().iterator();
		assertThat(iterator.next()).hasSize(2).extracting("category").contains("A", "B");
		assertThat(iterator.next()).hasSize(3).extracting("category").contains("D", "E", "F");
	}
}
