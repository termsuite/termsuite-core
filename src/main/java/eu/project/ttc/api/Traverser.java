package eu.project.ttc.api;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Lists;

import eu.project.ttc.engines.cleaner.TermProperty;
import eu.project.ttc.models.Term;
import eu.project.ttc.models.TermIndex;

public class Traverser {
	public static enum Direction {
		ASC, DESC;
		
		public static Direction fromString(String string) {
			switch (string.toLowerCase()) {
			case "asc":
				return ASC;
			case "desc":
				return DESC;
			default:
				throw new IllegalArgumentException("Unknown direction: " + string);
			}
		}
		
		@Override
		public String toString() {
			return super.toString().toLowerCase();
		}
	}

	public static class Ordering {
		private TermProperty property;
		private Direction direction;
		public Ordering(TermProperty property, Direction direction) {
			super();
			this.property = property;
			this.direction = direction;
		}
		
		public static Ordering fromString(String string) {
			List<String> strings = Splitter.on(" ").splitToList(string.trim());
			Preconditions.checkArgument(strings.size() > 0, "Empty string not allowed");
			Preconditions.checkArgument(strings.size() <= 2, "Too many arguments: %s", string);
			TermProperty property = TermProperty.forName(strings.get(1));
			Direction direction = strings.size() == 2 ? Direction.fromString(strings.get(1)) : Direction.ASC;
			return new Ordering(property, direction);
		}
		
		@Override
		public String toString() {
			return String.format("%s %s", property.getPropertyName(), direction);
		}
	}
	
	private List<Ordering> orderings;
	
	
	public Traverser(List<Ordering> orderings) {
		super();
		this.orderings = orderings;
	}

	public static Traverser createDefault() {
		return new Traverser(Lists.newArrayList(
				new Ordering(TermProperty.SPECIFICITY, Direction.DESC),
				new Ordering(TermProperty.FREQUENCY, Direction.DESC)
			));
	}
	
	public static Traverser createFromString(String string) {
		return create(
				Splitter.on(",").splitToList(string).stream().map(str -> {
						return Ordering.fromString(str);
					}).iterator()
			);
	}

	public static Traverser create(Iterator<Ordering>  orderings) {
		return create(Lists.newArrayList(orderings));
	}

	public static Traverser create(List<Ordering> orderings) {
		return new Traverser(orderings);
	}

	public static Traverser create(Ordering... orderings) {
		return create(Lists.newArrayList(orderings));
	}

	
	public Iterator<Term> iterator(TermIndex termIndex) {
		List<Term> terms = toList(termIndex);
		return terms.iterator();
	}

	public Stream<Term> stream(TermIndex termIndex) {
		return toList(termIndex).stream();
	}

	public List<Term> toList(TermIndex termIndex) {
		List<Term> terms = Lists.newArrayList(termIndex.getTerms());
		Collections.sort(terms, toComparator(termIndex));
		return terms;
	}
	
	public Comparator<Term> toComparator(final TermIndex termIndex) {
		return new Comparator<Term>() {
			@Override
			public int compare(Term o1, Term o2) {
				ComparisonChain chain = ComparisonChain.start();
				for(Ordering ordering:orderings) {
					if(ordering.direction == Direction.ASC)
						chain.compare(ordering.property.getValue(termIndex, o1), ordering.property.getValue(termIndex, o2));
					else 
						chain.compare(ordering.property.getValue(termIndex, o2), ordering.property.getValue(termIndex, o1));
				}
				return chain.result();
			}
		};
	}
	
	@Override
	public String toString() {
		return Joiner.on(", ").join(orderings);
	}
	
}
