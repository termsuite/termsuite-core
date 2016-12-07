
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

public class Levenshtein extends AbstractEditDistance {

	private double failThreshold = -1;

	@Override
	public int compute(String s1, String s2, int maxDistance) {
		int l = Math.max(s1.length(), s2.length());

		int[][] dp = new int[s1.length() + 1][s2.length() + 1];
		for (int i = 0; i < dp.length; i++) {
			int bestPossibleEditDistance = dp.length;
			for (int j = 0; j < dp[i].length; j++) {
				dp[i][j] = i == 0 ? j : j == 0 ? i : 0;
				if (i > 0 && j > 0) {
					if (s1.charAt(i - 1) == s2.charAt(j - 1)) {
						dp[i][j] = dp[i - 1][j - 1];
					} else {
						dp[i][j] = Math.min(dp[i][j - 1] + 1, Math.min(
								dp[i - 1][j - 1] + 1, dp[i - 1][j] + 1));
					}
					bestPossibleEditDistance = Math.min(
							bestPossibleEditDistance, dp[i][j]);
				}
			}

			// After calculating row i, look for the smallest value in a given
			// column. Abort is maxDistance is strictly exceeded
			if (i > maxDistance && bestPossibleEditDistance > maxDistance) {
				return l;
			}
		}
		return dp[s1.length()][s2.length()];
	}

	@Override
	public double normalize(int distance, String source, String target) {
		int length = source.length() < target.length() ? target.length()
				: source.length();
		return 1.0 - ((double) distance / (double) length);
	}

	@Override
	public boolean isFailFast() {
		return true;
	}

	@Override
	public void setFailThreshold(double threshold) {
		failThreshold = threshold;
	}

	@Override
	public int compute(String s1, String s2) {
		int l = Math.max(s1.length(), s2.length());
		return compute(s1, s2, failThreshold == -1 ? Math.min(s1.length(),
				s2.length()) : (int) Math.round((1 - failThreshold) * l));
	}

}