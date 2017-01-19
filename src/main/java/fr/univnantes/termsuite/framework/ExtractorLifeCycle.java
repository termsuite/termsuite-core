package fr.univnantes.termsuite.framework;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Named;

import com.google.inject.Injector;

import fr.univnantes.termsuite.api.TermSuiteException;

public class ExtractorLifeCycle {
	
	private Injector injector;
	
	public ExtractorLifeCycle(Injector injector) {
		super();
		this.injector = injector;
	}

	public TerminologyEngine create(EngineDescription desc) {
		TerminologyEngine engine = injector.getInstance(desc.getEngineClass());
		injectName(engine, desc.getEngineName());
		injectParameters(engine, desc.getParameters());
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
