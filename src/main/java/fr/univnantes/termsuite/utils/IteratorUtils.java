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
package fr.univnantes.termsuite.utils;

import java.util.Iterator;

public class IteratorUtils {
	public static int count(Iterable<?> iterable) {
		return count(iterable.iterator());
	}
	public static int count(Iterator<?> it) {
		int cnt = 0;
		while (it.hasNext()) {
			cnt++;
			it.next();
		}
		return cnt;
	}

	public static <T> Iterable<T> toIterable(final Iterator<T> it) {
		return new Iterable<T>() {
			@Override
			public Iterator<T> iterator() {
				return it;
			}
		};
	}
	
}
