package fr.univnantes.termsuite.framework;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Named;

import org.apache.uima.resource.SharedResourceObject;

import com.google.inject.Injector;

import fr.univnantes.termsuite.api.TermSuiteException;
import fr.univnantes.termsuite.engines.SimpleEngine;
import fr.univnantes.termsuite.framework.service.IndexService;
import fr.univnantes.termsuite.framework.service.TermSuiteResourceManager;
import fr.univnantes.termsuite.index.TermIndex;
import fr.univnantes.termsuite.index.TermIndexType;
import fr.univnantes.termsuite.uima.ResourceType;

public class EngineInjector {

	private Class<?extends Engine> engineCls;
	
	private IndexService indexService;
	
	private TermSuiteResourceManager resourceMgr;

	public EngineInjector(Class<?extends Engine> engineCls, Injector guiceInjector) {
		super();
		this.engineCls = engineCls;
		indexService = guiceInjector.getInstance(IndexService.class);
		resourceMgr = guiceInjector.getInstance(TermSuiteResourceManager.class);
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
	
	
	
	public  void injectIndexes(Engine engine) {
		for(Field field:getAnnotatedFields(Index.class, TermIndex.class)) {
			Index annotation = field.getAnnotation(Index.class);
			TermIndexType indexType = annotation.type();
			injectField(field, engine, indexService.getIndex(indexType));
		}
	}

	
	public  void injectResources(Engine engine) {
		for(Field field:getAnnotatedFields(Resource.class, SharedResourceObject.class, TermSuiteResource.class)) {
			Resource annotation = field.getAnnotation(Resource.class);
			ResourceType resourceType = annotation.type();
			injectField(field, engine, resourceMgr.get(resourceType));
		}
	}

	public void injectParameters(Engine childEngine, Map<Class<?>, Object> params) {
		Map<Class<?>, Object> parameters = new HashMap<>(params);
		for(Field field:getAnnotatedFields(Parameter.class)) {
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

	
	public void injectField(Field field, Engine engine, Object termSuiteResource) {
		try {
			field.setAccessible(true);
			field.set(engine, termSuiteResource);
		} catch (IllegalAccessException e) {
			throw new TermSuiteException("An error occurred during injection of resource", e);
		}
	}

	
	public List<Field> getAnnotatedFields(Class<? extends Annotation> annotation, Class<?>... allowedRanges) {
		List<Field> fields = new ArrayList<>();
		Class<?> cls = engineCls;
		while(cls != null) {
			for(Field field:cls.getDeclaredFields()) {
				if(field.isAnnotationPresent(annotation)) {
					if(allowedRanges.length == 0)
						fields.add(field);
					else if(Arrays.stream(allowedRanges).anyMatch(range -> range.isAssignableFrom(field.getType())))
						fields.add(field);
					else 
						throw new IllegalStateException(String.format("Can only inject values of type %s for field [@%s] %s. Got field type: %s", 
								Arrays.asList(allowedRanges),
								annotation.getSimpleName(),
								field.getDeclaringClass().getSimpleName() + "#" + field.getType().getSimpleName() + " " + field.getName(),
								field.getType()
							));
				}
			}
			cls = cls.getSuperclass();
		}
		return fields;
	}

	public void injectNullIndexes(SimpleEngine engine) {
		for(Field field:getAnnotatedFields(Index.class))
			injectField(field, engine, null);
	}

	public void injectNullResources(SimpleEngine engine) {
		for(Field field:getAnnotatedFields(Resource.class))
			injectField(field, engine, null);
	}
}
