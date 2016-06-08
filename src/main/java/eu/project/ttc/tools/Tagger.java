package eu.project.ttc.tools;

public enum Tagger {
	MATE("mate", "mate"),
	TREE_TAGGER("tree-tagger", "tt");
	
	private String shortName;
	private String name;

	private Tagger(String name, String shortName) {
		this.shortName = shortName;
		this.name = name;
	}
	
	public String getShortName() {
		return shortName;
	}
	
	public String getName() {
		return name;
	}
}
