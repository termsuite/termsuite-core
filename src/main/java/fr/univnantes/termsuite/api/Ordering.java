package fr.univnantes.termsuite.api;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.ComparisonChain;

import fr.univnantes.termsuite.model.Property;
import fr.univnantes.termsuite.model.PropertyHolder;
import fr.univnantes.termsuite.model.PropertyHolderBase;

public abstract class Ordering<T extends Enum<T> & Property<?>, U extends PropertyHolder<T>, Z> {
	
	protected class Direction {
		private T property;
		private boolean ascending;
		private Direction(T property, boolean ascending) {
			super();
			Preconditions.checkNotNull(property);
			this.property = property;
			this.ascending = ascending;
		}
		protected T getProperty() {
			return property;
		}
		protected boolean isAscending() {
			return ascending;
		}
		@Override
		public String toString() {
			return String.format("%s %s", property.getShortName(), ascending ? "asc" : "desc");
		}
	}
	
	protected List<Direction> directions = new ArrayList<>();
	
	@SuppressWarnings("unchecked")
	public Z asc(T property) {
		directions.add(new Direction(property, true));
		return (Z)this;
	}

	@SuppressWarnings("unchecked")
	public Z desc(T property) {
		directions.add(new Direction(property, false));
		return (Z)this;
	}

	public Comparator<U> toComparator() {
		for(Direction d:directions) 
			PropertyHolderBase.checkComparable(d.getProperty().getRange(), d.getProperty());
			
		return new Comparator<U>() {
			@Override
			public int compare(U o1, U o2) {
				ComparisonChain chain = ComparisonChain.start();
				Comparable<?> v1, v2;
				for(Direction d:directions) {
					v1 = (Comparable<?>)o1.getPropertyValueUnchecked(d.getProperty());
					v2 = (Comparable<?>)o2.getPropertyValueUnchecked(d.getProperty());
					if(d.isAscending()) {
						if(v1 != null && v2 != null)
							chain = chain.compare(v1,v2);
						else if(v1 == null && v2 != null)
							return -1;
						else if(v1 != null && v2 == null)
							return 1;
						
					} else {
						
						if(v1 != null && v2 != null)
							chain = chain.compare(v2,v1);
						else if(v1 == null && v2 != null)
							return 1;
						else if(v1 != null && v2 == null)
							return -1;

					}
				}
				int result = chain.result();
				return result;
			}
		};
	}

}
