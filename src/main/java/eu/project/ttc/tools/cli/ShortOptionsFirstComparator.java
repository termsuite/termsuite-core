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
package eu.project.ttc.tools.cli;

import java.util.Comparator;

import org.apache.commons.cli.Option;

/**
 * Compares {@link Option}s putting, long options to the end
 * 
 * @author Sebastián Peña Saldarriaga
 */
public class ShortOptionsFirstComparator implements Comparator<Option> {

	@Override
	public int compare(Option opt1, Option opt2) {
		boolean opt1Null = isNullOrEmpty(opt1.getOpt());
		boolean opt2Null = isNullOrEmpty(opt2.getOpt());
		
		if (opt1Null)
			return opt2Null ? opt1.getLongOpt().compareTo(opt2.getLongOpt()) : 1;
		if (opt2Null)
			return -1;
		return opt1.getOpt().compareTo(opt2.getOpt());
	}

	/**
	 * Determines whether <code>opt</code> is <code>null</code> or empty.
	 * 
	 * @param opt
	 *            The option to test
	 * @return <code>true</code> if the specified options is empty or
	 *         <code>null</code>.
	 */
	private boolean isNullOrEmpty(String opt) {
		return opt == null || opt.isEmpty();
	}

}
