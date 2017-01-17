package fr.univnantes.termsuite.framework;

import java.io.InputStreamReader;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

import org.apache.uima.resource.SharedResourceObject;

import com.google.common.base.Preconditions;

import fr.univnantes.termsuite.api.ResourceConfig;
import fr.univnantes.termsuite.api.TermSuiteException;
import fr.univnantes.termsuite.model.Lang;
import fr.univnantes.termsuite.uima.ResourceType;

public class TermSuiteResourceManager {
	
	private Lang lang;
	private ResourceConfig config;
	private Map<ResourceType, Object> loadedResources = new ConcurrentHashMap<>();
	private Semaphore resourceMutex = new Semaphore(1);
	
	public TermSuiteResourceManager(Lang lang, ResourceConfig config) {
		super();
		this.config = config;
		this.lang = lang;
	}

	public Object loadResource(ResourceType resourceType) {
			
		URL resourceURL = getResourceURL(resourceType);
		try {
			if(SharedResourceObject.class.isAssignableFrom(resourceType.getResourceClass())) {
				SharedResourceObject sro = (SharedResourceObject)resourceType.getResourceClass().newInstance();
					sro.load(new UIMATermSuiteResourceWrapper(resourceURL));
				return sro;
			} else if(TermSuiteResource.class.isAssignableFrom(resourceType.getResourceClass())) {
				try(InputStreamReader reader = new InputStreamReader(resourceURL.openStream())) {
					TermSuiteResource tr = (TermSuiteResource)resourceType.getResourceClass().newInstance();
					tr.load(reader);
					return tr;
				}
			} else 
				throw new IllegalStateException(String.format("resource implementing classes must be a subtype of %s or %s", 
						SharedResourceObject.class.getSimpleName(), 
						TermSuiteResource.class.getSimpleName()));
		} catch (Exception e) {
			throw new TermSuiteException("Resource initialization error", e);
		}
	}

	public Object get(ResourceType resourceType) {
		
		try {
			resourceMutex.acquire();
			if(!loadedResources.containsKey(resourceType)) {
				loadedResources.put(resourceType, loadResource(resourceType));
			}
			Object resource = loadedResources.get(resourceType);
			resourceMutex.release();
				
			return resource;
		} catch (InterruptedException e) {
			throw new TermSuiteException(e);
		}
	}
		
	public <T> T get(Class<T> cls, ResourceType resourceType) {
		Preconditions.checkArgument(cls.isAssignableFrom(resourceType.getResourceClass()),
				"Resource class of %s is %s, Expected any sub type of %s", 
				resourceType, 
				resourceType.getClass(), 
				cls);
		return cls.cast(get(resourceType));
	}

	public URL getResourceURL(ResourceType resourceType) {
		return resourceType.fromClasspath(lang);
	}
}
