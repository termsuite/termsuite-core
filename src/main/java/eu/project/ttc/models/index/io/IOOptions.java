package eu.project.ttc.models.index.io;

public class IOOptions {

	private boolean withOccurrences = true;
	protected boolean embedOccurrences = true;
	private boolean withContexts = true;
	
	public IOOptions withOccurrences(boolean withOccurrences) {
		this.withOccurrences = withOccurrences;
		return this;
	}
	public IOOptions embedOccurrences(boolean embedOccurrences) {
		this.embedOccurrences = embedOccurrences;
		return this;
	}
	public IOOptions withContexts(boolean withContexts) {
		this.withContexts = withContexts;
		return this;
	}
	
	
	public boolean withOccurrences() {
		return withOccurrences;
	}
	public boolean occurrencesEmbedded() {
		return embedOccurrences;
	}
	public boolean withContexts() {
		return withContexts;
	}

}
