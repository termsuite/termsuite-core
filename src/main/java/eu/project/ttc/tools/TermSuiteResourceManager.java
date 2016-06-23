package eu.project.ttc.tools;

import java.util.Map;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

public class TermSuiteResourceManager {
	private static TermSuiteResourceManager instance;
	
	private Map<String, Object> resources = Maps.newHashMap();
	
	private TermSuiteResourceManager() {
	}

	public static TermSuiteResourceManager getInstance() {
		if(instance == null)
			instance = new TermSuiteResourceManager();
		return instance;
	}
	
	public void register(String resourceName, Object resource) {
		Preconditions.checkArgument(
				!this.resources.containsKey(resourceName),
				"Resource already registered: %s",
				resourceName);
		this.resources.put(resourceName, resource);
	}
	
	public Object get(String resourceName) {
		Preconditions.checkArgument(
				this.resources.containsKey(resourceName),
				"No such resource: %s",
				resourceName);
		return this.resources.get(resourceName);
	}

	public boolean contains(String resourceName) {
		return this.resources.containsKey(resourceName);
	}

	public void clear() {
		this.resources.clear();
	}
}
