package eu.project.ttc.models;

public enum VariationType {
	MORPHOLOGICAL(1, "morph", true),
	SYNTACTICAL(2, "syn", true),
	GRAPHICAL(3, "graph", false);
	
	private int order;
	private String shortName;
	private boolean directional;
	
	private VariationType(int order, String shortName, boolean directional) {
		this.order = order;
		this.directional = directional;
		this.shortName = shortName;
	}
	
	public int getOrder() {
		return order;
	}
	
	public boolean isDirectional() {
		return directional;
	}

	public String getShortName() {
		return shortName;
	}
	
	public boolean isSymetric() {
		return !directional;
	}
	
	public static VariationType fromShortName(String shortName) {
		for(VariationType vt:values())
			if(vt.getShortName().equals(shortName))
				return vt;
		throw new IllegalArgumentException("No such variation type with name: " + shortName);
	}
}
