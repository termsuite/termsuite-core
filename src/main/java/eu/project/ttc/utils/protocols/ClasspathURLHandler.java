package eu.project.ttc.utils.protocols;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

public class ClasspathURLHandler extends URLStreamHandler {
    /** The classloader to find resources from. */
    private final ClassLoader classLoader;

    public ClasspathURLHandler() {
        this.classLoader = getClass().getClassLoader();
    }

    public ClasspathURLHandler(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @Override
    protected URLConnection openConnection(URL u) throws IOException {
        final URL resourceUrl = classLoader.getResource(u.getPath());
        if(resourceUrl == null)
        	throw new IOException("Could not get resource path " + u.getPath() + " from classpath");
        return resourceUrl.openConnection();
    }
}