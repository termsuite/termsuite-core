package eu.project.ttc.align;

public enum AlignmentMethod {
	DICTIONARY("dico", "dictionary"),
	DISTRIBUTIONAL("dist", "distributional"),
	COMPOSITIONAL("comp", "compositional"),
	SEMI_DISTRIBUTIONAL("s-dist", "semi-distributional"),
	NEOCLASSICAL("neodico", "neoclassical-dico"), 
	GRAPHICAL("graph", "graphical"),
	;
	
	private String shortName;
	private String longName;
	private AlignmentMethod(String shortName, String longName) {
		this.shortName = shortName;
		this.longName = longName;
	}
	
	public String getShortName() {
		return shortName;
	}
	public String getLongName() {
		return longName;
	}
}