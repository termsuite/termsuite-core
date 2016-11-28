package fr.univnantes.termsuite.eval.model;

import fr.univnantes.termsuite.model.Lang;

public class LangPair {
	
	private Lang source;
	private Lang target;
	
	public LangPair(Lang source, Lang target) {
		super();
		this.source = source;
		this.target = target;
	}
	
	public Lang getSource() {
		return source;
	}
	
	public void setSource(Lang source) {
		this.source = source;
	}
	
	public Lang getTarget() {
		return target;
	}
	
	public void setTarget(Lang target) {
		this.target = target;
	}
	
	@Override
	public String toString() {
		return String.format("%s-%s", source.getCode(), target.getCode());
	}
}
