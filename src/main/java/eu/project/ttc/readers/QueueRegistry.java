package eu.project.ttc.readers;

import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;

/**
 * 
 * A registry for queues of {@link CollectionDocument}, used in {@link StreamingCollectionReader}.
 * 
 * @author Damien Cram
 * @see StreamingCollectionReader
 *
 */
public class QueueRegistry {

	/*
	 * SINGLETON
	 */
	private static QueueRegistry instance = null;

	private QueueRegistry() {
		super();
	}
	
	/**
	 * Returns the registry singleton.
	 * 
	 * @return the registry
	 */
	public static QueueRegistry getInstance() {
		if(instance == null)
			instance = new QueueRegistry();
		return instance;
	}
	
	private Map<String, BlockingQueue<CollectionDocument>> registry = Maps.newConcurrentMap();
	
	/**
	 * Retrieves and returns a registered queue by name.
	 * 
	 * @param queueName
	 * 			The name of the queue to retrieve
	 * @return
	 * 			The retrieved queue with name <code>queueName</code>
	 * 
	 * @throws IllegalArgumentException if there is queue with the name <code>queueName</code>
	 */
	public BlockingQueue<CollectionDocument> getQueue(String queueName) {
		Preconditions.checkArgument(registry.containsKey(queueName), "Queue %s does not exist", queueName);
		return registry.get(queueName);
	}

	
	/**
	 * Creates and registers a new {@link ArrayBlockingQueue} with given initial
	 * capacity.
	 *  
	 * @param queueName
	 * 			The name of the queue
	 * @param capacity
	 * 			The initial capacity of the queue
	 * @return
	 * 		the created and registered queue
	 * 
	 * @throws IllegalArgumentException if <code>queueName</code> is already registered.
	 */
	public BlockingQueue<CollectionDocument> registerQueue(String queueName, int capacity) {
		Preconditions.checkArgument(!registry.containsKey(queueName), "Queue %s already exists", queueName);
		ArrayBlockingQueue<CollectionDocument> q = Queues.newArrayBlockingQueue(capacity);
		registry.put(queueName, q);
		return q;
	}

}
