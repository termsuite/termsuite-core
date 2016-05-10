package eu.project.ttc.tools;

public enum Tagger {
	MATE("mate"),
	TREE_TAGGER("tt");
	
	private String resourceShortName;

	private Tagger(String resourceShortName) {
		this.resourceShortName = resourceShortName;
	}
	
	public String getResourceShortName() {
		return resourceShortName;
	}
}
