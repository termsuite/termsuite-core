package eu.project.ttc.api;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

public class JSONOptions {

	private boolean metadataOnly = false;
	private boolean withOccurrences = true;
	private boolean withContexts = true;
	private boolean embeddedOccurrences = true;
	
	public JSONOptions metadataOnly(boolean metadataOnly) {
		this.metadataOnly = metadataOnly;
		return this;
	}
	
	public boolean metadataOnly() {
		return metadataOnly;
	}
	
	public JSONOptions withOccurrences(boolean withOccurrences) {
		this.withOccurrences = withOccurrences;
		return this;
	}
	public JSONOptions embedOccurrences(boolean embedOccurrences) {
		this.embeddedOccurrences = embedOccurrences;
		return this;
	}
	public JSONOptions withContexts(boolean withContexts) {
		this.withContexts = withContexts;
		return this;
	}
	
	public boolean withOccurrences() {
		return withOccurrences;
	}
	
	public boolean embeddedOccurrences() {
		return embeddedOccurrences;
	}
	
	public boolean withContexts() {
		return withContexts;
	}

	private Optional<String> mongoDBOccStore = Optional.absent();
	
	public JSONOptions mongoDBOccStoreURI(String mongoDBOccStoreURI) {
		Preconditions.checkNotNull(mongoDBOccStore, "MongoDBUri must nopt be null");
		this.mongoDBOccStore = Optional.of(mongoDBOccStoreURI);
		this.embeddedOccurrences = false;
		return this;
	}
	
	
	public boolean isMongoDBOccStore() {
		return this.mongoDBOccStore.isPresent();
	}
	
	public String getMongoDBOccStore() {
		return mongoDBOccStore.get();
	}

}
