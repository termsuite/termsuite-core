package eu.project.ttc.utils;

import java.net.MalformedURLException;
import java.net.URL;

import eu.project.ttc.utils.protocols.ClasspathURLHandler;

public class URLUtils {
	
	private static ClasspathURLHandler classpathURLHandler;
	
	public static ClasspathURLHandler classpathURLHandler() {
		if( classpathURLHandler == null)
			classpathURLHandler = new ClasspathURLHandler();
		return classpathURLHandler;
	}

	
	public static URL join(URL prefix, String path) throws MalformedURLException {
		if(prefix.getProtocol().equals("classpath"))
			return new URL(prefix, path, classpathURLHandler());
		else
			return new URL(prefix, path);
	}
}
