package eu.project.ttc.models;

import com.google.common.base.Objects;

public class TermVariation {
	private VariationType variationType;
	private Term base;
	private Term variant;
	private Object info;
	private String _label;
	
	public TermVariation(VariationType variationType, Term base, Term variant, Object info) {
		super();
		this.variationType = variationType;
		this.base = base;
		this.variant = variant;
		this.info = info;
	}
	
	public VariationType getVariationType() {
		return variationType;
	}
	
	public Object getInfo() {
		return info;
	}
	
	public Term getVariant() {
		return variant;
	}
	
	public Term getBase() {
		return base;
	}
	
	@Override
	public String toString() {
		return String.format("%s --- %s --> %s", base.getGroupingKey(), this.info, variant.getGroupingKey());
	}
	
	@Override
	public int hashCode() {
		return Objects.hashCode(this.base, this.variant, this.variationType, this.info);
	}
	
	public String getLabel() {
		if(this._label == null) 
			this._label = this.variationType.getShortName() + ":" + this.info; 
		return this._label;
	}
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof TermVariation) {
			TermVariation v = (TermVariation) obj;
			return Objects.equal(this.base, v.base)
					&& Objects.equal(this.variant, v.variant)
					&& Objects.equal(this.info, v.info);
		} else 
			return false;
	}
	
	
}
