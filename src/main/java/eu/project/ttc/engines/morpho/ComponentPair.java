package eu.project.ttc.engines.morpho;

import com.google.common.base.Objects;

import eu.project.ttc.models.Component;

public class ComponentPair {
	
	private Component component1;
	private Component component2;
	
	public ComponentPair(Component component1, Component component2) {
		super();
		if(component1.compareTo(component2) > 0) {
			this.component2 = component1;
			this.component1 = component2;			
		} else {
			this.component1 = component1;
			this.component2 = component2;
		}
	}
	
	public Component getComponent1() {
		return component1;
	}
	
	public Component getComponent2() {
		return component2;
	}
	@Override
	public int hashCode() {
		return Objects.hashCode(component1, component2);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ComponentPair) {
			ComponentPair o = (ComponentPair)obj;
			return Objects.equal(component1, o.component1)
					&& Objects.equal(component2, o.component2);
		} else return false;
	}
	
	public boolean isOrdered() {
		return component1.compareTo(component2) <= 0;
	}
	
	public boolean overlaps() {
		return this.component2.getBegin() < this.component1.getEnd();
	}
}
