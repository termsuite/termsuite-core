package fr.univnantes.termsuite.framework.injector;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Named;

import fr.univnantes.termsuite.api.TermSuiteException;
import fr.univnantes.termsuite.engines.SimpleEngine;
import fr.univnantes.termsuite.framework.Engine;
import fr.univnantes.termsuite.framework.Index;
import fr.univnantes.termsuite.framework.Parameter;
import fr.univnantes.termsuite.framework.Resource;
import fr.univnantes.termsuite.framework.service.IndexService;
import fr.univnantes.termsuite.framework.service.TermSuiteResourceManager;


/**
 * 
 * A injector dedicated for instance of class {@link Engine}.
 * 
 * @author Damien Cram
 *
 */
public class EngineInjector extends TermSuiteInjector {

	
	private ResourceInjector resourceInjector;
	public void injectResources(Object instance) {
		resourceInjector.injectResources(instance);
	}

	public void injectIndexes(Object object) {
		indexInjector.injectIndexes(object);
	}

	private IndexInjector indexInjector;
	
	public EngineInjector(TermSuiteResourceManager resourceMgr, IndexService indexService) {
		super();
		this.resourceInjector = new ResourceInjector(resourceMgr);
		this.indexInjector = new IndexInjector(indexService);
	}

	public void injectName(Engine engine, String engineName) {
		Field field = getNameField();
		field.setAccessible(true);
		try {
			field.set(engine, engineName);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			throw new TermSuiteException(e);
		}
	}
	
	private static Field nameField = null;
	private static Field getNameField() {
		if(nameField == null) {
			for(Field field:Engine.class.getDeclaredFields()) {
				if(field.isAnnotationPresent(Named.class) 
						&& field.getAnnotation(Named.class).value().equals("engineName")) {
					return field;
				}
			}
		}
		throw new IllegalStateException("Should have found field engineName in " + Engine.class );
	}
	
	public void injectParameters(Engine childEngine, Map<Class<?>, Object> params) {
		Map<Class<?>, Object> parameters = new HashMap<>(params);
		for(Field field:getAnnotatedFields(childEngine, Parameter.class)) {
			if(parameters.containsKey(field.getType())) {
				injectField(field, childEngine, parameters.remove(field.getType()));
			} else 
				if(!field.getAnnotation(Parameter.class).optional())
					throw new TermSuiteException(String.format(
						"Could not inject @parameter for field %s.%s. No parameter of type %s available. ", 
						childEngine.getClass().getSimpleName(),
						field.getName(),
						field.getType()));
		}
	}

	public void injectNullIndexes(SimpleEngine engine) {
		for(Field field:getAnnotatedFields(engine, Index.class))
			injectField(field, engine, null);
	}

	public void injectNullResources(SimpleEngine engine) {
		for(Field field:getAnnotatedFields(engine, Resource.class))
			injectField(field, engine, null);
	}

}
