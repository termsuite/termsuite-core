/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright 2, 2013nership.  The ASF licenses this file
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

import java.util.Iterator;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Iterates over a list of {@link Node}s, avoiding {@link NodeList#getLength()}
 * overhead, and using the greedy {@link NodeList#item(int)} method.
 * 
 * @author Sebastián Peña Saldarriaga
 */
public abstract class IterableNodeList implements Iterable<Node> {

	/** No constructor */
	private IterableNodeList() {
	}

	/**
	 * Returns an iterable instance over the specified <code>list</code>
	 * 
	 * @param list
	 *            The list to iterate over.
	 * @return An iterable instance.
	 */
	public static final Iterable<Node> fromNodeList(final NodeList list) {
		return new IterableNodeList() {

			@Override
			public Iterator<Node> iterator() {
				return iteratorFromNodeList(list);
			}
		};
	}

	/**
	 * Returns an iterator over the specified <code>list</code>
	 * 
	 * @param list
	 *            The list to iterate over.
	 * @return An iterable instance.
	 */
	public static final Iterator<Node> iteratorFromNodeList(final NodeList list) {
		return new Iterator<Node>() {
			private Node current;
			private int idx = 0;

			@Override
			public boolean hasNext() {
				current = list.item(idx);
				idx++;
				return current != null;
			}

			@Override
			public Node next() {
				return current;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException(
						"Cannot remove Nodes from list");
			}
		};
	}
}
