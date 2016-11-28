package fr.univnantes.termsuite.alignment;

public enum AlignmentMethod {
	DICTIONARY("dico", "dictionary"),
	DISTRIBUTIONAL("dist", "distributional"),
	COMPOSITIONAL("comp", "compositional"),
	NEOCLASSICAL("neo", "neoclassical"), 
	GRAPHICAL("graph", "graphical"),
	SEMI_DISTRIBUTIONAL("s-dist", "semi-distributional"),
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