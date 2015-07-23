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
package eu.project.ttc.metrics;

/**
 * Longest Common Subsequence.<br />
 * <pre>
 *           | ls(i-1,j-1) + 1,             if a[i]=b[j]
 * ls(i,j) = |
 *           | max(ls(i-1,j), ls(i,j-1)),   else
 * Boundary conditions: let think that   ls[i][-1] = 0,   ls[-1][j] = 0
 * </pre>
 */
public class LongestCommonSubsequence extends AbstractEditDistance {
	
	@Override
    public int compute(String a, String b) {
        this.a = a;
        this.b = b;
        this.set();
		return this.get();
    }
   
    // strings a, b
    private String a;
    private String b;
   
    // ls(i,j) - maximum length of common subsequence that end at a[i] and b[j].
    private int ls[][];
   
    // specifies which neighboring cell ls(i,j) it got its value
    Direction direction[][];
   
    private void set() {
        // init with 0 by default
        ls = new int[a.length() + 1][b.length() + 1];
   
        // so this could be skipped
        for (int i = 0; i <= a.length(); i++) {
        	ls[i][0] = 0;
        }
        for (int j = 0; j <= b.length(); j++) {
        	ls[0][j] = 0;
        }
   
        direction = new Direction[a.length() + 1][b.length() + 1];
   
        for (int i = 1; i <= a.length(); i++) {
            for (int j = 1; j <= b.length(); j++) {
                if (a.charAt(i - 1) == b.charAt(j - 1)) {
                    ls[i][j] = ls[i - 1][j - 1] + 1;
                    direction[i][j] = Direction.DIAG;
                } else if (ls[i - 1][j] >= ls[i][j - 1]) {
                    ls[i][j] = ls[i - 1][j];
                    direction[i][j] = Direction.UP;
                } else {
                    ls[i][j] = ls[i][j - 1];
                    direction[i][j] = Direction.LEFT;
                }
            }
        }
    }
   
    private int get() {
        StringBuilder sb = new StringBuilder();
        int i = a.length();
        int j = b.length();
        while (i > 0 && j > 0) {
            if (direction[i][j] == Direction.DIAG) {
                sb.append(a.charAt(i - 1));
                i--;
                j--;
            } else if (direction[i][j] == Direction.UP)
                i--;
            else
                j--;
        }
        return sb.reverse().toString().length();
    }
   
    private enum Direction {
        LEFT, UP, DIAG;
   
        @Override
        public String toString() {
            switch (this) {
                case LEFT:
                    return "<";
                case UP:
                    return "^";
                case DIAG:
                    return "\\";
            }
            throw new IllegalStateException("wrong direction");
        }
    }

	@Override
	public double normalize(int distance, String source, String target) {
		return (double) (2 * distance) / (double) (source.length() + target.length());
	}

	@Override
	public boolean isFailFast() {
		return false;
	}

	@Override
	public void setFailThreshold(double threshold) {
		// Nothing
	}
    
}
