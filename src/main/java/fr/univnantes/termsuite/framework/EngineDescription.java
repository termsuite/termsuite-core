package fr.univnantes.termsuite.framework;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.google.common.base.Preconditions;

public class EngineDescription {
	private String engineName;
	private Class<? extends TerminologyEngine> engineClass;
	private Map<Class<?>, Object> parameters;
	
	public EngineDescription(String name, Class<? extends TerminologyEngine> engineClass, Object... parameters) {
		super();
		checkEngineParameters(parameters);
		this.engineName = name;
		this.engineClass = engineClass;
		this.parameters = new HashMap<>();
		for(Object o:parameters)
			this.parameters.put(o.getClass(), o);
	}

	
	
	public void checkEngineParameters(Object... parameters) {
		List<Class<?>> classesList = Arrays.stream(parameters).map(Object::getClass).collect(toList());
		Preconditions.checkArgument(
				classesList.size() == new HashSet<>(classesList).size(),
				"All engine parameters must be of the same type. Got: %s", classesList);
	}
	
	public Map<Class<?>, Object> getParameters() {
		return parameters;
	}
	
	public Class<? extends TerminologyEngine> getEngineClass() {
		return engineClass;
	}
	
	public String getEngineName() {
		return engineName;
	}
	
	@Override
	public String toString() {
		return String.format("EngineDescription[%s]{%s}", 
				engineClass.getSimpleName(), 
				Arrays.asList(parameters).stream()
					.map(o-> String.format("%s=%s", o.getClass().getSimpleName(), o)
							).collect(joining(", ")));
	}
}

