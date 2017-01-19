package fr.univnantes.termsuite.api;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.google.common.base.Preconditions;

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
	
	
	public ResourceConfig addResourcePrefix(URL resourcePrefix) {
		urlPrefixes.add(resourcePrefix);
		return this;
	}

	public ResourceConfig addDirectory(Path path) {
		Preconditions.checkArgument(
				path.toFile().exists(),
				"Directory does not exist: %s", path);
		Preconditions.checkArgument(
				path.toFile().isDirectory(),
				"Not a directory: %s", path);
		String pathStr = path.endsWith(File.separator) ? "" : File.separator;
		try {
			URL urlPrefix = new URL("file:" + pathStr);
			urlPrefixes.add(urlPrefix);
		} catch (MalformedURLException e) {
			throw new TermSuiteException(e);
		}
		return this;
	}
	
	
	public ResourceConfig addJar(Path path) {
		Preconditions.checkArgument(
				FileUtils.isJar(path.toString()),
				"Not a jar: %s", path);
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
}
