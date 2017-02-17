package fr.univnantes.termsuite.framework.injector;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import fr.univnantes.termsuite.api.TermSuiteException;

/**
 * 
 * Abstract class for injecting fields.
 * 
 * @author Damien Cram
 *
 */
public class TermSuiteInjector {
	
	public void injectField(Field field, Object instance, Object fieldValue) {
		try {
			field.setAccessible(true);
			field.set(instance, fieldValue);
		} catch (IllegalAccessException e) {
			throw new TermSuiteException("An error occurred during injection of field " + field, e);
		}
	}
	
	public List<Field> getAnnotatedFields(Class<?> cls, Class<? extends Annotation> annotation, Class<?>... allowedRanges) {
		List<Field> fields = new ArrayList<>();
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

	public List<Field> getAnnotatedFields(Object instance, Class<? extends Annotation> annotation, Class<?>... allowedRanges) {
		return getAnnotatedFields(instance.getClass(), annotation, allowedRanges);
	}
}
