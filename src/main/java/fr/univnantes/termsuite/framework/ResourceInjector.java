package fr.univnantes.termsuite.framework;

import java.lang.reflect.Field;

import org.apache.uima.resource.SharedResourceObject;

import com.google.common.base.Preconditions;
import com.google.inject.MembersInjector;

import fr.univnantes.termsuite.api.TermSuiteException;
import fr.univnantes.termsuite.uima.ResourceType;

public class ResourceInjector<T> implements MembersInjector<T> {

	private Field field;
	private ResourceType resourceType;
	private TermSuiteResourceManager resourceMgr;
	
	public ResourceInjector(Field field, ResourceType resourceType, TermSuiteResourceManager resourceMgr) {
		super();
		this.field = field;
		this.resourceType = resourceType;
		this.resourceMgr = resourceMgr;
		field.setAccessible(true);
	}

	@Override
	public void injectMembers(T instance) {
		Class<?> fieldClass = field.getType();
		Preconditions.checkState(
				SharedResourceObject.class.isAssignableFrom(fieldClass)
				|| TermSuiteResource.class.isAssignableFrom(fieldClass) , 
				"Can only inject resource of type %s or %s. Got: %s", 
				SharedResourceObject.class, 
				fieldClass);
		try {
			field.set(instance, resourceMgr.get(resourceType));
		} catch (IllegalAccessException e) {
			throw new TermSuiteException("An error occurred during injection of resource", e);
		}
	}
	
	
	
	
}
