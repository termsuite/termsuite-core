package eu.project.ttc.stream;

import java.util.Map;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

import eu.project.ttc.readers.StreamingCollectionReader;

/**
 * 
 * A registry for {@link CasConsumer}s.
 * 
 * @author Damien Cram
 * @see CasConsumer
 * @see StreamingCollectionReader
 *
 */
public class ConsumerRegistry {

	/*
	 * SINGLETON
	 */
	private static ConsumerRegistry instance = null;

	private ConsumerRegistry() {
		super();
	}
	
	/**
	 * Returns the registry singleton.
	 * 
	 * @return the registry
	 */
	public static ConsumerRegistry getInstance() {
		if(instance == null)
			instance = new ConsumerRegistry();
		return instance;
	}
	
	private Map<String, CasConsumer> registry = Maps.newConcurrentMap();
	
	public CasConsumer getConsumer(String consumerName) {
		Preconditions.checkArgument(registry.containsKey(consumerName), "Queue %s does not exist", consumerName);
		return registry.get(consumerName);
	}

	
	public void registerConsumer(String consumerName, CasConsumer consumer) {
		Preconditions.checkNotNull(consumer);
		Preconditions.checkArgument(!registry.containsKey(consumerName), "Consumer %s already exists", consumerName);
		registry.put(consumerName, consumer);
	}

}
