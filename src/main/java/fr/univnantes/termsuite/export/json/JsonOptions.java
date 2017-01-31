package fr.univnantes.termsuite.export.json;

import java.util.Optional;

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

	private Optional<String> persistentOccStorePath = Optional.empty();
	
	public JsonOptions persistentOccStorePath(String persistentOccStorePath) {
		Preconditions.checkNotNull(persistentOccStorePath, "persistentOccStorePath must not be null");
		this.persistentOccStorePath = Optional.of(persistentOccStorePath);
		this.embeddedOccurrences = false;
		return this;
	}
	
	
	public boolean isMongoDBOccStore() {
		return this.persistentOccStorePath.isPresent();
	}
	
	public String getMongoDBOccStore() {
		return persistentOccStorePath.get();
	}

}
