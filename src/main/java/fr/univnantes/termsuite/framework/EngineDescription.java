package fr.univnantes.termsuite.framework;

import static java.util.stream.Collectors.joining;

import java.util.Arrays;

public class EngineDescription {
	private Class<? extends TerminologyEngine> engineClass;
	private Object[] parameters;
	public EngineDescription(Class<? extends TerminologyEngine> engineClass, Object... parameters) {
		super();
		this.engineClass = engineClass;
		this.parameters = parameters;
	}
	
	public Object[] getParameters() {
		return parameters;
	}
	
	public Class<? extends TerminologyEngine> getEngineClass() {
		return engineClass;
	}
	
	@Override
	public String toString() {
		return String.format("EngineDescription[%s]{%s}", 
				engineClass.getSimpleName(), 
				Arrays.asList(parameters).stream()
					.map(o-> String.format("%s=%s", o.getClass().getSimpleName(), o)
							).collect(joining(", ")));
	}
	
	public <T> EngineDescription bind(Class<T> cls, T instance) {
		return this;
	}

	public EngineDescription bindNamed(String name, Object instance) {
		return this;
	}

}

