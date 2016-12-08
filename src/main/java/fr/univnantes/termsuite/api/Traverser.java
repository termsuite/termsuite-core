package fr.univnantes.termsuite.api;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.TermProperty;
import fr.univnantes.termsuite.model.Terminology;

public class Traverser {

	private static final String ERR_EMPTY_STRING_NOT_ALLOWED = "Empty string not allowed";
	private static final String ERR_TOO_MANY_ARGUMENTS = "Too many arguments: %s";
	private static final String ERR_UNKNOWN_DIRECTION = "Unknown direction: ";

	public static enum Direction {
		ASC, DESC;
		
		public static Direction fromString(String string) {
			switch (string.toLowerCase()) {
			case "asc":
				return ASC;
			case "desc":
				return DESC;
			default:
				throw new IllegalArgumentException(ERR_UNKNOWN_DIRECTION + string);
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
			Preconditions.checkArgument(strings.size() > 0, ERR_EMPTY_STRING_NOT_ALLOWED);
			Preconditions.checkArgument(strings.size() <= 2, ERR_TOO_MANY_ARGUMENTS, string);
			TermProperty property = TermProperty.forName(strings.get(0));
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

	private static final List<Ordering> DEFAULT_ORDERING = Lists.newArrayList(
			new Ordering(TermProperty.RANK, Direction.ASC),
			new Ordering(TermProperty.SPECIFICITY, Direction.DESC),
			new Ordering(TermProperty.FREQUENCY, Direction.DESC),
			new Ordering(TermProperty.GROUPING_KEY, Direction.ASC)
			
			);
	
	public static Traverser create() {
		return new Traverser(DEFAULT_ORDERING);
	}
	
	public static Traverser by(String string) {
		Preconditions.checkArgument(!string.trim().isEmpty(), ERR_EMPTY_STRING_NOT_ALLOWED);
		return by(
				Splitter.on(",").splitToList(string).stream()
				.filter(str -> {
					Preconditions.checkArgument(!str.trim().isEmpty(), ERR_EMPTY_STRING_NOT_ALLOWED);
					return true;
				})
				.map(str -> {
						return Ordering.fromString(str);
					}).collect(Collectors.toList())
			);
	}

	public static Traverser by(Iterator<Ordering> orderings) {
		return by(Lists.newArrayList(orderings));
	}

	public static Traverser by(List<Ordering> orderings) {
		orderings.addAll(DEFAULT_ORDERING);
		return new Traverser(orderings);
	}

	public static Traverser by(Ordering... orderings) {
		return by(Lists.newArrayList(orderings));
	}

	
	public Iterator<Term> iterator(Terminology termino) {
		List<Term> terms = toList(termino);
		return terms.iterator();
	}

	public Stream<Term> stream(Terminology termino) {
		return toList(termino).stream();
	}

	public List<Term> toList(Terminology termino) {
		List<Term> terms = Lists.newArrayList(termino.getTerms());
		Collections.sort(terms, toComparator());
		return terms;
	}
	
	public Comparator<Term> toComparator() {
		return new Comparator<Term>() {
			@Override
			public int compare(Term o1, Term o2) {
				for(Ordering ordering:orderings) {
					int compare = ordering.property.compare(o1, o2);
					if(compare < 0) 
						return ordering.direction == Direction.ASC ? -1 : 1;
					else if(compare > 0)
						return ordering.direction == Direction.ASC ? 1 : -1;
				}
				return 0;
			}
		};
	}
	
	@Override
	public String toString() {
		return Joiner.on(", ").join(orderings);
	}
}
