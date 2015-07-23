/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright 2, 2015nership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package eu.project.ttc.tools.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.uima.jcas.tcas.Annotation;


/**
 * This class is intended to be used by the {@link TermAligner} class to
 * generate permutations.
 * 
 * @author Sebastián Peña Saldarriaga
 * 
 * @param <T>
 *            The annotation type.
 */
public class PermutationTree<T extends Annotation> {

	private T node;

	public T node() {
		return this.node;
	}

	private Set<PermutationTree<T>> children;

	public PermutationTree(T node) {
		this.children = new HashSet<>();
		this.node = node;
	}

	public Set<PermutationTree<T>> children() {
		return this.children;
	}

	public void addChild(PermutationTree<T> child) {
		children.add(child);
	}

	public List<List<String>> strings() {
		List<List<String>> results = new ArrayList<List<String>>();
		for (PermutationTree<T> t : this.children()) {
			List<String> list = new ArrayList<String>();
			String string = t.node().getCoveredText();
			list.add(string);
			results.addAll(t.strings(list));
		}
		return results;
	}

	public List<List<String>> strings(List<String> lists) {
		if (this.children().isEmpty()) {
			List<List<String>> results = new ArrayList<List<String>>();
			results.add(lists);
			return results;
		} else {
			List<List<String>> results = new ArrayList<List<String>>();
			for (PermutationTree<T> t : this.children()) {
				List<String> list = new ArrayList<String>(lists);
				String string = t.node().getCoveredText();
				list.add(string);
				results.addAll(t.strings(list));
			}
			return results;
		}
	}
}