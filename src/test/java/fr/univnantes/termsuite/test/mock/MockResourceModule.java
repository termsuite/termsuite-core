package fr.univnantes.termsuite.test.mock;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import com.google.common.base.Preconditions;
import com.google.inject.AbstractModule;

import fr.univnantes.termsuite.framework.service.TermSuiteResourceManager;
import fr.univnantes.termsuite.uima.ResourceType;

public class MockResourceModule extends AbstractModule {
	private Map<ResourceType, Object> resources = new HashMap<>();
	
	public MockResourceModule bind(ResourceType type, Object resource) {
		Preconditions.checkArgument(!resources.containsKey(type), "Resource already exists: %s", type);
		resources.put(type, resource);
		return this;
	}
	
	@Override
	protected void configure() {
		bind(TermSuiteResourceManager.class).toInstance(new TermSuiteResourceManager() {
			@Override
			public Object get(ResourceType resourceType) {
				Preconditions.checkArgument(resources.containsKey(resourceType));
				return resources.get(resourceType);
			}
			@Override
			public URL getResourceURL(ResourceType resourceType) {
				throw new UnsupportedOperationException("Operation not supported for " + MockResourceModule.class.getSimpleName());
			}
		});
	}
}
