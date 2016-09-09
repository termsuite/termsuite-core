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

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {
	public static Comparator<String> alphanumComparator = new AlphanumComparator();
	public static Comparator<File> alphanumFileComparator = new AlphanumFileComparator();
	
	private static class AlphanumFileComparator implements Comparator<File> {
		@Override
		public int compare(File o1, File o2) {
			return alphanumComparator.compare(o1.getAbsolutePath(), o2.getAbsolutePath());
		}
	}
	
	
	private static class AlphanumComparator implements Comparator<String> {
		private final boolean isDigit(char ch) {
			return ch >= 48 && ch <= 57;
		}

		/**
		 * Length of string is passed in for improved efficiency (only need to
		 * calculate it once)
		 **/
		private final String getChunk(String s, int slength, int marker) {
			StringBuilder chunk = new StringBuilder();
			char c = s.charAt(marker);
			chunk.append(c);
			marker++;
			if (isDigit(c)) {
				while (marker < slength) {
					c = s.charAt(marker);
					if (!isDigit(c))
						break;
					chunk.append(c);
					marker++;
				}
			} else {
				while (marker < slength) {
					c = s.charAt(marker);
					if (isDigit(c))
						break;
					chunk.append(c);
					marker++;
				}
			}
			return chunk.toString();
		}

		public int compare(String s1, String s2) {
			int thisMarker = 0;
			int thatMarker = 0;
			int s1Length = s1.length();
			int s2Length = s2.length();

			while (thisMarker < s1Length && thatMarker < s2Length) {
				String thisChunk = getChunk(s1, s1Length, thisMarker);
				thisMarker += thisChunk.length();

				String thatChunk = getChunk(s2, s2Length, thatMarker);
				thatMarker += thatChunk.length();

				// If both chunks contain numeric characters, sort them
				// numerically
				int result = 0;
				if (isDigit(thisChunk.charAt(0))
						&& isDigit(thatChunk.charAt(0))) {
					// Simple chunk comparison by length.
					int thisChunkLength = thisChunk.length();
					result = thisChunkLength - thatChunk.length();
					// If equal, the first different number counts
					if (result == 0) {
						for (int i = 0; i < thisChunkLength; i++) {
							result = thisChunk.charAt(i) - thatChunk.charAt(i);
							if (result != 0) {
								return result;
							}
						}
					}
				} else {
					result = thisChunk.compareTo(thatChunk);
				}

				if (result != 0)
					return result;
			}

			return s1Length - s2Length;
		}
	}
	
	public static boolean containsWhiteSpace(final String testCode){
	    if(testCode != null){
	        for(int i = 0; i < testCode.length(); i++){
	            if(Character.isWhitespace(testCode.charAt(i))){
	                return true;
	            }
	        }
	    }
	    return false;
	}

	
	private static final String EMPTY_STRING = "";
	private static final String ASCII_REPLACEMENT = "[^\\p{ASCII}]";
	public static String replaceAccents(String string) {
		String withoutAccent = Normalizer.normalize(string, Form.NFD).replaceAll(ASCII_REPLACEMENT, EMPTY_STRING);

		//FIXME accent removal fails for russian. This is a quick fix
		if(withoutAccent.isEmpty() && !string.isEmpty()) 
			withoutAccent = string;
		
		return withoutAccent;
	}
	
	private static final String SPECIAL_CHARACTERS = "()[]{}\"'~:/*=+#±¶©·´`“”‘’«»•._";
	

	public static int nbSpecialCharacters(String string) {
		int nb = 0;
		for(char c:string.toCharArray())
			if(SPECIAL_CHARACTERS.indexOf(c) != -1)
				nb++;
		return nb;
		
	}
	
	
	/**
	 * Collapse all sequences of line breaks into one single line break.
	 * 
	 * This helper method is used to convert files into the format where 
	 * there is only one sentence per phrase.
	 * @param string
	 * @return
	 */
	public static String toOnelineSentences(String string) {
		return string
				.replaceAll("(\n\\s*){2,}", "\n");
		
	}

	
	public static boolean hasSpecialCharacters(String string) {
		for(char c:string.toCharArray())
			if(SPECIAL_CHARACTERS.indexOf(c) != -1)
				return true;
		return false;
	}

	
	/**
	 * Read a file to a string
	 * 
	 * @param path
	 * @param encoding
	 * @return
	 * @throws IOException
	 */
	public static String readFile(String path, Charset encoding) throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return new String(encoded, encoding);
	}
	
	public static boolean hasDigits(String string) {
		for(char c:string.toCharArray())
			if(Character.isDigit(c))
				return true;
		return false;
	}
	
	private static final Pattern DIGIT = Pattern.compile("(\\d+)");

	public static int nbDigitSequences(String string) {
		Matcher matcher = DIGIT.matcher(string);
		int count = 0;
		while (matcher.find())
		    count++;
		return count;
	}

	public static double getOrthographicScore(String str) {
		double score;
		switch (str.length()) {
		case 1:
			score = 0.15;
			break;
		case 2:
			score = 0.45;
			break;
		case 3:
			score = 0.70;
			break;
		case 4:
			score = 0.95;
			break;
		default:
			score = 1;
		}
		if(StringUtils.nbDigitSequences(str) == 1 
				&& StringUtils.nbDigits(str) == 1
				&& (Character.isDigit(str.charAt(0)) 
						|| Character.isDigit(str.charAt(str.length()-1)))) {
			// if starts with a digit or end with a digit, apply a small malus
			score = 0.85*score;
		} else
			// else, apply full digit malus
			score = score / (Math.pow(1.8, StringUtils.nbDigitSequences(str)));
		score = score / Math.pow(2, StringUtils.nbSpecialCharacters(str));
		return score;
	}

	public static int nbDigits(String str) {
		int cnt = 0;
		for(char c:str.toCharArray())
			if(Character.isDigit(c))
				cnt++;
		return cnt;
	}
	
	
	public static String replaceLast(String string, String toReplace, String replacement) {
	    int pos = string.lastIndexOf(toReplace);
	    if (pos > -1) {
	        return string.substring(0, pos)
	             + replacement
	             + string.substring(pos + toReplace.length(), string.length());
	    } else {
	        return string;
	    }
	}


}
