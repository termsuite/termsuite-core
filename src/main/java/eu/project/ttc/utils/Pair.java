
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

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

public class Pair<T extends Comparable<? super T>> {
	
	private T element1;
	private T element2;
	
	public Pair(T element1, T element2) {
		super();
		Preconditions.checkNotNull(element1);
		Preconditions.checkNotNull(element2);
		this.element1 = element1;
		this.element2 = element2;
		if(this.element1.compareTo(element2) > 0) {
			this.element1 = element2;
			this.element2 = element1;
		}
	}

	public T getElement1() {
		return element1;
	}

	public T getElement2() {
		return element2;
	}
	
	@Override
	public int hashCode() {
		return Objects.hashCode(element1, element2);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Pair<?>) {
			Pair<?> o = (Pair<?>) obj;
			return Objects.equal(element1, o.element1) 
					&& Objects.equal(element2, o.element2);
		} else
			return false;
	}
	
	@Override
	public String toString() {
		return String.format("(%s,%s)", element1, element2);
	}
}
