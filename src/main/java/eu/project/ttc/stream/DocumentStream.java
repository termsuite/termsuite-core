package eu.project.ttc.stream;

import eu.project.ttc.readers.CollectionDocument;

public class DocumentStream {
	private DocumentProvider provider;
	private Thread streamThread;
	private CasConsumer consumer;
	private String queueName;

	public DocumentStream(Thread streamThread, DocumentProvider provider, CasConsumer consumer, String queueName) {
		super();
		this.streamThread = streamThread;
		this.provider = provider;
		this.queueName = queueName;
		this.consumer = consumer;
	}

	public void addDocument(CollectionDocument doc) {
		provider.provide(doc);
	}

	public Thread getStreamThread() {
		return streamThread;
	}

	public void flush() {
		provider.provide(CollectionDocument.LAST_DOCUMENT);
		try {
			streamThread.join();
		} catch (InterruptedException e) {
			new RuntimeException(e);
		}
	}
	
	public CasConsumer getConsumer() {
		return consumer;
	}
	
	public String getQueueName() {
		return queueName;
	}

}
