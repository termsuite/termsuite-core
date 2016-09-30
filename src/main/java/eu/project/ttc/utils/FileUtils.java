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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class FileUtils {

	public static String getFileName(String path) {
		return Iterables.getLast(Splitter.on('/').split(path));
	}
	

	public static String humanReadableByteCount(long bytes, boolean si) {
	    int unit = si ? 1000 : 1024;
	    if (bytes < unit) return bytes + " B";
	    int exp = (int) (Math.log(bytes) / Math.log(unit));
	    String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
	    return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
	}
	
	public static boolean isJar(String file) {
		try {
			JarFile jar = new JarFile(file);
			JarEntry entry = jar.getJarEntry("META-INF/MANIFEST.MF");
			jar.close();
			return entry != null;
		} catch (IOException e) {
			return false;
		}
	}


	public static List<String> getUncommentedLines(File file, Charset forName) throws IOException {
		try {
			List<String> ids = Lists.newArrayList();
			try(BufferedReader br = new BufferedReader(new FileReader(file))) {
				for(String line; (line = br.readLine()) != null; ) {
					if(!line.trim().startsWith("#") && !line.trim().isEmpty())
						ids.add(line.trim());
				}
			}
			return ids;			
		} catch(FileNotFoundException e) {
			throw new IOException(e);
		}
	}

	public static String replaceRootDir(String fileUri, File oldRoot, File newRoot) throws IOException {
		Preconditions.checkArgument(oldRoot.isDirectory());
		String oldRootPath = oldRoot.getCanonicalPath();
		Preconditions.checkArgument(fileUri.startsWith(oldRootPath),
				"file path %s must start with the old root path %s", fileUri, oldRoot);
		String newRootPath = newRoot.getCanonicalPath();
		return replaceRootDir(fileUri, oldRootPath, newRootPath);
	}


	public static String replaceRootDir(String fileUri, String oldRootPath, String newRootPath) {
		Preconditions.checkArgument(fileUri.startsWith(oldRootPath),
				"file path %s must start with the old root path %s", fileUri, oldRootPath);

		return fileUri.replaceFirst(oldRootPath, newRootPath);
	}


	public static String readFile(String path, Charset encoding) throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return new String(encoded, encoding);
	}


	public static String replaceExtensionWith(String toFilePath, String newExtension) {
		String dirname = toFilePath.substring(0, toFilePath.lastIndexOf(File.separatorChar) + 1);
		String filename = toFilePath.substring(toFilePath.lastIndexOf(File.separatorChar) + 1, toFilePath.length());
		
		String basename = filename.contains(".") ? filename.substring(0, filename.lastIndexOf('.') + 1) : (filename + ".");
		
		return Paths.get(dirname, basename + newExtension).toString();
		
	}
}
