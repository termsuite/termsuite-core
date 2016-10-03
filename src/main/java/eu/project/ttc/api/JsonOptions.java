package eu.project.ttc.api;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

public class JsonOptions {

	private boolean metadataOnly = false;
	private boolean withOccurrences = true;
	private boolean withContexts = true;
	private boolean embeddedOccurrences = true;
	
	public JsonOptions metadataOnly(boolean metadataOnly) {
		this.metadataOnly = metadataOnly;
		return this;
	}
	
	public boolean isMetadataOnly() {
		return metadataOnly;
	}
	
	public JsonOptions withOccurrences(boolean withOccurrences) {
		this.withOccurrences = withOccurrences;
		return this;
	}
	public JsonOptions embedOccurrences(boolean embedOccurrences) {
		this.embeddedOccurrences = embedOccurrences;
		return this;
	}
	public JsonOptions withContexts(boolean withContexts) {
		this.withContexts = withContexts;
		return this;
	}
	
	public boolean isWithOccurrences() {
		return withOccurrences;
	}
	
	public boolean withOccurrences() {
		return withOccurrences;
	}
	
	public boolean isEmbeddedOccurrences() {
		return embeddedOccurrences;
	}
	
	public boolean isWithContexts() {
		return withContexts;
	}

	private Optional<String> mongoDBOccStore = Optional.absent();
	
	public JsonOptions mongoDBOccStoreURI(String mongoDBOccStoreURI) {
		Preconditions.checkNotNull(mongoDBOccStoreURI, "MongoDBUri must not be null");
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
