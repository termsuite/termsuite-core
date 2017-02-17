package fr.univnantes.termsuite.framework;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.google.common.base.Preconditions;

import fr.univnantes.termsuite.index.TermIndexType;
import fr.univnantes.termsuite.uima.ResourceType;

public class EngineDescription {
	private Optional<EngineDescription> parent = Optional.empty();
	private String engineName;
	private Class<? extends Engine> engineClass;
	private Map<Class<?>, Object> parameters;
	private Set<ResourceType> requiredResources;
	private Map<TermIndexType, Boolean> requiredIndexes;
	
	public EngineDescription(String name, Class<? extends Engine> engineClass, Object... parameters) {
		super();
		checkEngineParameters(parameters);
		this.engineName = name;
		this.engineClass = engineClass;
		this.parameters = new HashMap<>();
		for(Object o:parameters)
			this.parameters.put(o.getClass(), o);
		this.requiredIndexes = new HashMap<>();
		this.requiredResources = new HashSet<>();
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
	
	public Class<? extends Engine> getEngineClass() {
		return engineClass;
	}
	
	public Optional<EngineDescription> getParent() {
		return parent;
	}
	
	public boolean isAggregated() {
		return AggregateEngine.class.isAssignableFrom(engineClass);
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
	
	public Map<TermIndexType, Boolean> getRequiredIndexes() {
		return requiredIndexes;
	}
	
	public Set<ResourceType> getRequiredResources() {
		return requiredResources;
	}
}

