package fr.univnantes.termsuite.framework;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;

import org.apache.uima.UimaContext;
import org.apache.uima.UimaContextAdmin;
import org.apache.uima.resource.DataResource;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceManager;
import org.apache.uima.resource.ResourceSpecifier;
import org.apache.uima.resource.metadata.ResourceMetaData;
import org.apache.uima.util.Logger;

import fr.univnantes.termsuite.api.TermSuiteException;

public class UIMATermSuiteResourceWrapper implements DataResource {
	
	private URL resourceUrl;
	
	public UIMATermSuiteResourceWrapper(URL resourceUrl) {
		super();
		this.resourceUrl = resourceUrl;
	}

	@Override
	public boolean initialize(ResourceSpecifier aSpecifier, Map<String, Object> aAdditionalParams)
			throws ResourceInitializationException {
		throw new UnsupportedOperationException();
	}

	@Override
	public ResourceMetaData getMetaData() {
		throw new UnsupportedOperationException();
	}

	@Override
	public ResourceManager getResourceManager() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Logger getLogger() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setLogger(Logger aLogger) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void destroy() {
		throw new UnsupportedOperationException();
	}

	@Override
	public UimaContext getUimaContext() {
		throw new UnsupportedOperationException();
	}

	@Override
	public UimaContextAdmin getUimaContextAdmin() {
		throw new UnsupportedOperationException();
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return getUrl().openStream();
	}

	@Override
	public URI getUri() {
		try {
			return getUrl().toURI();
		} catch (URISyntaxException e) {
			throw new TermSuiteException(e);
		}
	}

	@Override
	public URL getUrl() {
		return resourceUrl;
	}
}
