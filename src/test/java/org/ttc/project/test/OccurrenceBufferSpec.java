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
package org.ttc.project.test;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;
import java.util.Iterator;

import org.assertj.core.api.Condition;
import org.junit.Test;
import org.ttc.project.TestUtil;

import com.google.common.collect.Lists;

import eu.project.ttc.utils.OccurrenceBuffer;
import fr.univnantes.lina.uima.tkregex.RegexOccurrence;

public class OccurrenceBufferSpec {

	OccurrenceBuffer buffer1;
	OccurrenceBuffer buffer2;
	

	private void initBuffer(String strategy) {
		buffer1 = new OccurrenceBuffer(strategy);
		buffer1.bufferize(TestUtil.createOccurrence(0, 10, "A"));
		buffer1.bufferize(TestUtil.createOccurrence(0, 5, "B"));
		buffer1.bufferize(TestUtil.createOccurrence(4, 10, "C"));
		buffer1.bufferize(TestUtil.createOccurrence(2, 8, "D"));
		buffer1.bufferize(TestUtil.createOccurrence(7, 12, "E"));
		buffer1.bufferize(TestUtil.createOccurrence(9, 12, "F"));
		
		
		buffer2 = new OccurrenceBuffer(strategy);
		buffer2.bufferize(TestUtil.createOccurrence(0, 10, "A"));
		buffer2.bufferize(TestUtil.createOccurrence(0, 10, "B"));
		buffer2.bufferize(TestUtil.createOccurrence(4, 10, "C"));
		buffer2.bufferize(TestUtil.createOccurrence(2, 8, "D"));
		buffer2.bufferize(TestUtil.createOccurrence(2, 8, "E"));
		buffer2.bufferize(TestUtil.createOccurrence(2, 8, "F"));

	}
	
	@Test
	public void testCleaningWithoutStrategy() {
		initBuffer(OccurrenceBuffer.NO_CLEANING);
		buffer1.cleanBuffer();
		assertThat(Lists.newArrayList(buffer1)).hasSize(6)
			.areExactly(1, cat("A"))
			.areExactly(1, cat("B"))
			.areExactly(1, cat("C"))
			.areExactly(1, cat("D"))
			.areExactly(1, cat("E"))
			.areExactly(1, cat("F"))
			;
	}

	private Condition<RegexOccurrence> cat(final String category) {
		return new Condition<RegexOccurrence>() {
			@Override
			public boolean matches(RegexOccurrence occurrence) {
				return occurrence.getCategory().equals(category);
			}
		};
	}

	@Test
	public void testCleaningKeepPrefixes() {
		initBuffer(OccurrenceBuffer.KEEP_PREFIXES);
		buffer1.cleanBuffer();
		assertThat(Lists.newArrayList(buffer1)).hasSize(3)
			.areExactly(1, cat("A"))
			.areExactly(1, cat("B"))
			.areExactly(1, cat("E"))
			;
	}

	@Test
	public void testCleaningKeepSuffixes() {
		initBuffer(OccurrenceBuffer.KEEP_SUFFIXES);
		buffer1.cleanBuffer();
		assertThat(Lists.newArrayList(buffer1)).hasSize(4)
			.areExactly(1, cat("A"))
			.areExactly(1, cat("C"))
			.areExactly(1, cat("E"))
			.areExactly(1, cat("F"))
			;
	}

	
	@Test
	public void testFindDuplicates() {
		initBuffer(OccurrenceBuffer.NO_CLEANING);
		assertThat(buffer2.findDuplicates()).hasSize(2);
		Iterator<Collection<RegexOccurrence>> iterator = buffer2.findDuplicates().iterator();
		assertThat(iterator.next()).hasSize(2).extracting("category").contains("A", "B");
		assertThat(iterator.next()).hasSize(3).extracting("category").contains("D", "E", "F");
	}
}
