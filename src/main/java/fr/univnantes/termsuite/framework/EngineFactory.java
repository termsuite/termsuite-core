package fr.univnantes.termsuite.framework;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Named;

import org.apache.uima.resource.SharedResourceObject;

import com.google.common.base.Preconditions;
import com.google.inject.Injector;

import fr.univnantes.termsuite.api.TermSuite;
import fr.univnantes.termsuite.api.TermSuiteException;
import fr.univnantes.termsuite.framework.service.TermSuiteResourceManager;
import fr.univnantes.termsuite.uima.ResourceType;

public class EngineFactory {
	
	private Injector injector;
	
	private TermSuiteResourceManager resourceMgr;
	
	public EngineFactory(Injector injector) {
		super();
		this.injector = injector;
		this.resourceMgr = injector.getInstance(TermSuiteResourceManager.class);
	}

	public <T extends TerminologyEngine> T create(Class<T> engineClass, Object... parameters) {
		return engineClass.cast(create(TermSuite.createEngineDescription(engineClass, parameters)));
	}

	public TerminologyEngine create(EngineDescription desc) {
		TerminologyEngine engine = injector.getInstance(desc.getEngineClass());
		injectName(engine, desc.getEngineName());
		injectParameters(engine, desc.getParameters());
		injectResources(engine);
		if(engine instanceof AggregateTerminologyEngine) {
			AggregateTerminologyEngine aggr = (AggregateTerminologyEngine)engine;
			aggr.configure();
			List<TerminologyEngine> children = new ArrayList<>();
			for(EngineDescription childDesc:aggr.getEngineDescriptions())
				children.add(create(childDesc));
			aggr.setEngines(children);
		}
		return engine;
	}

	private void injectName(TerminologyEngine engine, String engineName) {
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
			for(Field field:TerminologyEngine.class.getDeclaredFields()) {
				if(field.isAnnotationPresent(Named.class) 
						&& field.getAnnotation(Named.class).value().equals("engineName")) {
					return field;
				}
			}
		}
		throw new IllegalStateException("Should have found field engineName in " + TerminologyEngine.class );
	}
	
	
	private void injectResources(TerminologyEngine engine) {
		Class<?> cls = engine.getClass();
		while (cls != null) {
			for (Field field : cls.getDeclaredFields()) {
				if(field.isAnnotationPresent(Resource.class)) {
					Resource annotation = field.getAnnotation(Resource.class);
					ResourceType resourceType = annotation.type();
					
					Class<?> fieldClass = field.getType();
					Preconditions.checkState(
							SharedResourceObject.class.isAssignableFrom(fieldClass)
							|| TermSuiteResource.class.isAssignableFrom(fieldClass) , 
							"Can only inject resource of type %s or %s. Got: %s", 
							SharedResourceObject.class, 
							fieldClass);
					try {
						field.setAccessible(true);
						field.set(engine, resourceMgr.get(resourceType));
					} catch (IllegalAccessException e) {
						throw new TermSuiteException("An error occurred during injection of resource", e);
					}
					
				}
			}
			cls = cls.getSuperclass();
		}

	}

	private void injectParameters(TerminologyEngine childEngine, Map<Class<?>, Object> params) {
		Map<Class<?>, Object> parameters = new HashMap<>(params);
		Class<?> cls = childEngine.getClass();
		while(cls != null) {
			for(Field field:cls.getDeclaredFields()) {
				if(field.isAnnotationPresent(Parameter.class)) {
					if(parameters.containsKey(field.getType())) {
						field.setAccessible(true);
						try {
							Object value = parameters.remove(field.getType());
							field.set(childEngine, value);
						} catch (IllegalArgumentException | IllegalAccessException e) {
							throw new TermSuiteException(e);
						}
					} else 
						if(!field.getAnnotation(Parameter.class).optional())
							throw new TermSuiteException(String.format(
								"Could not inject @parameter for field %s.%s. No parameter of type %s available. ", 
								childEngine.getClass().getSimpleName(),
								field.getName(),
								field.getType()));
				}
			}
			cls = cls.getSuperclass();
		}
	}


}
