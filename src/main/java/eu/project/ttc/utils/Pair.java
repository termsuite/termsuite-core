package eu.project.ttc.utils;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

public class Pair<T extends Comparable<? super T>> {
	
	private T element1;
	private T element2;
	
	public Pair(T element1, T element2) {
		super();
		Preconditions.checkNotNull(element1);
		Preconditions.checkNotNull(element2);
		this.element1 = element1;
		this.element2 = element2;
		if(this.element1.compareTo(element2) > 0) {
			this.element1 = element2;
			this.element2 = element1;
		}
	}

	public T getElement1() {
		return element1;
	}

	public T getElement2() {
		return element2;
	}
	
	@Override
	public int hashCode() {
		return Objects.hashCode(element1, element2);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Pair<?>) {
			Pair<?> o = (Pair<?>) obj;
			return Objects.equal(element1, o.element1) 
					&& Objects.equal(element2, o.element2);
		} else
			return false;
	}
	
	@Override
	public String toString() {
		return String.format("(%s,%s)", element1, element2);
	}
}
