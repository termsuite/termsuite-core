
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

package eu.project.ttc.utils;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;

public class CollectionUtils {
	
	/**
	 * 
	 * Transforms a list of sets into a set of element pairs,
	 * which is the union of all cartesian products of
	 * all pairs of sets.
	 * 
	 * Example: 
	 * 
	 * A = {a,b,c}
	 * B = {d}
	 * C = {e, f}
	 * 
	 * returns AxB U AxC U BxC = {
	 * 	{a,d}, {b,d}, {c,d}, 
	 *  {a,e}, {a,f}, {b,e}, {b,f}, {c,e}, {c,f},
	 *  {d,e}, {d,f}
	 * }
	 * 
	 * 
	 * @param iterable
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Comparable<? super T>> Set<Pair<T>> combineAndProduct(List<? extends Set<? extends T>> sets) {
		Set<Pair<T>> results = Sets.newHashSet();
		for(int i = 0; i< sets.size(); i++) {
			for(int j = i+1; j< sets.size(); j++)
				for(List<T> l:Sets.cartesianProduct(sets.get(i), sets.get(j)))
					results.add(new Pair<T>(l.get(0), l.get(1)));
		}
		return results;
	}

}
