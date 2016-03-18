package eu.project.ttc.models.index.io;

public class LoadOptions  {
	private boolean metadataOnly = false;
	public LoadOptions metadataOnly(boolean metadataOnly) {
		this.metadataOnly = metadataOnly;
		return this;
	}
	public boolean metadataOnly() {
		return metadataOnly;
	}
	
	
	private IOOptions ioOptionsDelegate = new IOOptions();
	public LoadOptions withOccurrences(boolean withOccurrences) {
		ioOptionsDelegate.withOccurrences(withOccurrences);
		return this;
	}
	public LoadOptions embedOccurrences(boolean embedOccurrences) {
		ioOptionsDelegate.embedOccurrences(embedOccurrences);
		return this;
	}
	public LoadOptions withContexts(boolean withContexts) {
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
