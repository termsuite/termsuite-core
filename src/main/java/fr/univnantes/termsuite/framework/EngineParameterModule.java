package fr.univnantes.termsuite.framework;

import static java.util.stream.Collectors.toList;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Preconditions;
import com.google.inject.AbstractModule;

import jetbrains.exodus.core.dataStructures.hash.HashSet;

/**
 * An module for injecting parameters of a TerminologyEngine.
 * 
 * @see AggregateTerminologyEngine#configure()
 * 
 * @author Damien Cram
 *
 */
public class EngineParameterModule extends AbstractModule {
	
	private Class<?> cls;
	private Map<Class<?>, Object> params = new HashMap<>();

	public EngineParameterModule(EngineDescription engineDesc) {
		super();
		List<Class<?>> classesList = Arrays.stream(engineDesc.getParameters()).map(Object::getClass).collect(toList());
		Preconditions.checkArgument(
				classesList.size() == new HashSet<>(classesList).size(),
				"All engine parameters must be of the same type. Got: %s", classesList);
		for(Object param:engineDesc.getParameters())
			params.put(param.getClass(), param);
		this.cls = engineDesc.getEngineClass();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected void configure() {
		bind(cls);
		
		for(Class<?> cls:params.keySet()) {
			bind((Class<Object>)cls).toInstance(params.get(cls));
		}
//		bindListener(Matchers.any(), new TypeListener() {
//			@Override
//			public <I> void hear(TypeLiteral<I> type, TypeEncounter<I> encounter) {
//				Class<? super I> cls = type.getRawType();
//				if(params.containsKey(cls)) {
//					encounter.register(new EngineParameterInjector<>(field, params.get(field.getType())));
//
//				}
//				if(TerminologyEngine.class.isAssignableFrom(cls)) {
//					while (cls != null) {
//						for (Field field : cls.getDeclaredFields()) {
//							boolean hasInjectAnno = field.isAnnotationPresent(Inject.class) || field.isAnnotationPresent(javax.inject.Inject.class);
//							if (hasInjectAnno && params.containsKey(field.getType()))
//								encounter.register(new EngineParameterInjector<>(field, params.get(field.getType())));
//						}
//						cls = cls.getSuperclass();
//					}
//				}
//			}
//		});
	}

}
