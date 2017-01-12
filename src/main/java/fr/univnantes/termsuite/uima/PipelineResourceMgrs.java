package fr.univnantes.termsuite.uima;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.google.common.base.Preconditions;

public class PipelineResourceMgrs {

	private static Map<String, PipelineResourceMgrs> managers = new ConcurrentHashMap<>();

	private String pipelineId;
	
	private PipelineResourceMgrs(String pipelineId) {
		super();
		this.pipelineId = pipelineId;
	}

	public static PipelineResourceMgrs getResourceMgr(String pipelineId) {
		if(!managers.containsKey(pipelineId))
			managers.put(pipelineId, new PipelineResourceMgrs(pipelineId));
		return managers.get(pipelineId);
	}

	public static void clearPipeline(String pipelineId) {
		managers.remove(pipelineId);
	}
	
	private Map<Class<?>, Object> resources = new ConcurrentHashMap<>();
	
	public <T> void register(Class<T> cls, T resource) {
		Preconditions.checkArgument(
				!resources.containsKey(cls), 
				"Resource manager %s already contains resource for key %s", this, cls.getSimpleName());
		resources.put(cls, resource);
	}
	
	public <T> T getResource(Class<T> cls) {
		Preconditions.checkArgument(resources.containsKey(cls), "Unknown resource for key %s in %s", cls.getSimpleName(), this);
		return cls.cast(resources.get(cls));
	}
	
	@Override
	public String toString() {
		return String.format("%s[pipelineId=%d]", this.getClass().getSimpleName(), this.pipelineId);
	}

	public <T> T getResourceOrNull(Class<T> cls) {
		if(!resources.containsKey(cls))
			return null;
		else
			return getResource(cls);
	}
}
