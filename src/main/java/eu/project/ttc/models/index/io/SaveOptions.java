package eu.project.ttc.models.index.io;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

public class SaveOptions extends IOOptions {

	private Optional<String> mongoDBOccStore = Optional.absent();
	
	public SaveOptions mongoDBOccStoreURI(String mongoDBOccStoreURI) {
		Preconditions.checkNotNull(mongoDBOccStore, "MongoDBUri must nopt be null");
		this.mongoDBOccStore = Optional.of(mongoDBOccStoreURI);
		this.embedOccurrences = false;
		return this;
	}
	
	public boolean isMongoDBOccStore() {
		return this.mongoDBOccStore.isPresent();
	}
	
	public String getMongoDBOccStore() {
		return mongoDBOccStore.get();
	}
	
	
	private IOOptions ioOptionsDelegate = new IOOptions();
	public SaveOptions withOccurrences(boolean withOccurrences) {
		ioOptionsDelegate.withOccurrences(withOccurrences);
		return this;
	}
	public SaveOptions embedOccurrences(boolean embedOccurrences) {
		ioOptionsDelegate.embedOccurrences(embedOccurrences);
		return this;
	}
	public SaveOptions withContexts(boolean withContexts) {
		ioOptionsDelegate.withContexts(withContexts);
		return this;
	}
	public boolean withOccurrences() {
		return ioOptionsDelegate.withOccurrences();
	}
	public boolean occurrencesEmbedded() {
		return ioOptionsDelegate.occurrencesEmbedded();
	}
	public boolean withContexts() {
		return ioOptionsDelegate.withContexts();
	}
	
}
