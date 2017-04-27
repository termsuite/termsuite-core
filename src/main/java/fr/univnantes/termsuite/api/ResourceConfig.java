package fr.univnantes.termsuite.api;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.google.common.base.Preconditions;

import fr.univnantes.termsuite.uima.ResourceType;
import fr.univnantes.termsuite.utils.FileUtils;

/**
 * 
 * A Java-bean for the configuration of TermSuite resource access.
 * 
 * @author Damien Cram
 *
 */
public class ResourceConfig {

	private LinkedList<URL> urlPrefixes = new LinkedList<>();
	
	private Map<ResourceType, Path> customResourcePathes = new HashMap<>();
	
	public ResourceConfig addResourcePrefix(URL resourcePrefix) {
		try {
			urlPrefixes.add(new URL(withEndingSlash(resourcePrefix.toString())));
		} catch (MalformedURLException e) {
			throw new TermSuiteException(e);
		}
		return this;
	}

	public ResourceConfig addCustomResourcePath(ResourceType resource, Path path) {
		Preconditions.checkArgument(
				!customResourcePathes.containsKey(resource), 
				"A custom path is already set for resource %s", 
				resource);
		
		customResourcePathes.put(resource, path);
		return this;
	}
	
	public ResourceConfig addDirectory(Path path) {
		Preconditions.checkArgument(
				path.toFile().exists(),
				"Directory does not exist: %s", path);
		Preconditions.checkArgument(
				path.toFile().isDirectory(),
				"Not a directory: %s", path);
		try {
			URL urlPrefix = new URL("file:" + withEndingSlash(path.toString()));
			urlPrefixes.add(urlPrefix);
		} catch (MalformedURLException e) {
			throw new TermSuiteException(e);
		}
		return this;
	}

	public String withEndingSlash(String path) {
		return path.toString() + (path.endsWith(File.separator) ? "" : File.separator);
	}
	
	
	public ResourceConfig addJar(Path path) {
		Preconditions.checkArgument(
				FileUtils.isJar(path.toString()),
				"Not a jar: %s (No META-INF/MANIFEST.MF found)", path);
		try {
			URL urlPrefix = new URL(String.format("jar:file:%s!/", path));
			urlPrefixes.add(urlPrefix);
		} catch(MalformedURLException e) {
			throw new TermSuiteException(e);
		}
		return this;
	}
	
	public List<URL> getURLPrefixes() {
		return Collections.unmodifiableList(urlPrefixes);
	}
	
	public Map<ResourceType, Path> getCustomResourcePathes() {
		return customResourcePathes;
	}
}
