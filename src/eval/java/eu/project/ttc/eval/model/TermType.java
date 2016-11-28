package eu.project.ttc.eval.model;

import fr.univnantes.termsuite.model.Term;

public enum TermType {
	SWT("swt"),
	SWT_C("swt-c"),
	MWT("mwt"),
	MWT_C("mwt-c");

	private String name;
	
	private TermType(String name) {
		this.name = name;
	}

	public String getTermTypeName() {
		return name;
	}
	
	public static TermType ofTermTypeName(String name) {
		for(TermType t:values())
			if(t.getTermTypeName().equals(name))
				return t;
		
		return null;
	}
	public static TermType ofTerm(Term term) {
		if(term.isSingleWord())
			return term.isCompound() ? SWT_C : SWT;
		else 
			return term.getWords().stream().anyMatch(tw -> tw.getWord().isCompound()) ? MWT_C : MWT;
	}
}
