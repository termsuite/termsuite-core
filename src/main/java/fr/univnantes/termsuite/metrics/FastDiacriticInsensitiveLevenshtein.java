
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
package fr.univnantes.termsuite.metrics;

import fr.univnantes.termsuite.utils.StringUtils;

/**
 * 
 * Diacritic-insensitive Levenshtein-like distance by simply 
 * invoking {@link Levenshtein} on strings without their accents.
 * 
 * @author Damien Cram
 *
 */
public class FastDiacriticInsensitiveLevenshtein extends Levenshtein {

	private boolean caseSensitive = true;
	
	public FastDiacriticInsensitiveLevenshtein(boolean caseSensitive) {
		super();
		this.caseSensitive = caseSensitive;
	}

	@Override
	public int compute(String str, String rst, int maxDistance) {
		return super.compute(
				caseSensitive ? StringUtils.replaceAccents(str) : StringUtils.replaceAccents(str).toLowerCase(), 
				caseSensitive ? StringUtils.replaceAccents(rst) : StringUtils.replaceAccents(rst).toLowerCase(),
				maxDistance);
	}
}
