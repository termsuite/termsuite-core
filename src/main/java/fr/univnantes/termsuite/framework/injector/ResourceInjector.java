package fr.univnantes.termsuite.framework.injector;

import java.lang.reflect.Field;

import org.apache.uima.resource.SharedResourceObject;

import fr.univnantes.termsuite.framework.Resource;
import fr.univnantes.termsuite.framework.TermSuiteResource;
import fr.univnantes.termsuite.framework.service.TermSuiteResourceManager;
import fr.univnantes.termsuite.uima.ResourceType;

public class ResourceInjector extends TermSuiteInjector {

	private TermSuiteResourceManager mgr;
	
	public ResourceInjector(TermSuiteResourceManager mgr) {
		super();
		this.mgr = mgr;
	}

	public  void injectResources(Object instance) {
		for(Field field:getAnnotatedFields(instance, Resource.class, SharedResourceObject.class, TermSuiteResource.class)) {
			Resource annotation = field.getAnnotation(Resource.class);
			ResourceType resourceType = annotation.type();
			injectField(field, instance, mgr.get(resourceType));
		}
	}

	
}
