/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright 2, 2015nership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package eu.project.ttc.tools.utils;

import java.text.Collator;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

import com.google.common.collect.ComparisonChain;

import eu.project.ttc.models.Term;
import eu.project.ttc.models.index.TermMeasure;

/**
 * Consists exclusively of terms that operate or create {@link TermPredicate}s.
 * 
 * @author Sebastián Peña Saldarriaga
 */
public class TermPredicates {

	/** No instances */
	private TermPredicates() {
	};

	/** A predicate that accepts every term */
	public static final TermPredicate TRIVIAL_ACCEPTOR = new TermPredicate() {

		@Override
		public boolean accept(Term term) {
			return true;
		}
	};

	/**
	 * Returns a {@link TermPredicate} the accepts terms whose occurrences are
	 * bigger than the specified <code>threshold</code>.
	 * 
	 * @param threshold
	 *            The occurrences threshold
	 * @return The term predicate
	 */
	public static TermPredicate createOccurrencesPredicate(final int threshold) {
		return new TermPredicate() {

			@Override
			public boolean accept(Term term) {
				return term.getFrequency() >= threshold;
			}
		};
	}

	/**
	 * Returns a {@link TermPredicate} the accepts terms whose measure is
	 * bigger than the specified <code>threshold</code>.
	 * 
	 * @param threshold
	 *            The measure threshold
	 * @return The term predicate
	 */
	public static TermPredicate createMeasurePredicate(
			final double threshold, final TermMeasure termMeasure) {
		return new TermPredicate() {

			@Override
			public boolean accept(Term term) {
				return termMeasure.getValue(term) >= threshold;
			}
		};
	}


	/**
	 * Returns a {@link TermPredicate} that accepts terms whose grammatical
	 * categories are <i>noun</i> or <i>adjective</i>.
	 * 
	 * @return The term predicate
	 */
	public static TermPredicate createNounAdjectivePredicate() {
		return new TermPredicate() {

			@Override
			public boolean accept(Term term) {
				throw new UnsupportedOperationException("Terms do not support categories yet");
			}
		};
	}

	/**
	 * Returns a {@link TermPredicate} that accepts terms whose grammatical
	 * categories are <i>verb</i> or <i>adverb</i>.
	 * 
	 * @return The term predicate
	 */
	public static TermPredicate createVerbAdverbPredicate() {
		return new TermPredicate() {
			@Override
			public boolean accept(Term term) {
				throw new UnsupportedOperationException("Terms do not support categories yet");
			}
		};
	}

	/**
	 * Returns a {@link TermPredicate} that accepts a term that is accepted by
	 * <code>pred1</code> or <code>pred2</code>.
	 * 
	 * @param pred1
	 *            The first predicate
	 * @param pred2
	 *            The second predicate
	 * @return The resulting term predicate
	 */
	public static TermPredicate createOrPredicate(final TermPredicate pred1,
			final TermPredicate pred2) {
		return new TermPredicate() {
			@Override
			public boolean accept(Term term) {
				return pred1.accept(term) || pred2.accept(term);
			}
		};
	}

	/**
	 * Returns a {@link TermPredicate} that accepts a term that is accepted by
	 * <code>pred1</code> and <code>pred2</code>.
	 * 
	 * @param pred1
	 *            The first predicate
	 * @param pred2
	 *            The second predicate
	 * @return The resulting term predicate
	 */
	public static TermPredicate createAndPredicate(final TermPredicate pred1,
			final TermPredicate pred2) {
		return new TermPredicate() {
			@Override
			public boolean accept(Term term) {
				return pred1.accept(term) && pred2.accept(term);
			}
		};
	}

	/**
	 * Returns a {@link TermPredicate} that accepts terms in sorted occurrence
	 * order up to the specified <code>cutoffRank</code>.
	 * 
	 * @param cutoffRank
	 *            The rank cutoff
	 * @return The term predicate object
	 */
	public static TermPredicate createTopNByOccurrencesPredicate(int cutoffRank) {
		return createSortedTermPredicate(cutoffRank,
				DESCENDING_OCCURRENCE_ORDER);
	}

	/**
	 * Returns a {@link TermPredicate} that accepts terms in sorted measure
	 * order up to the specified <code>cutoffRank</code>.
	 * 
	 * @param cutoffRank
	 *            The rank cutoff
	 * @return The term predicate object
	 */
	public static TermPredicate createTopNByTermMeasurePredicate(int cutoffRank, TermMeasure termMeasure) {
		return createSortedTermPredicate(cutoffRank, termMeasure.getTermComparator(true));
	}

	/**
	 * Returns a {@link TermPredicate} that accepts terms sorted by the
	 * specified <code>comparator</code> up to the <code>cutoffRank</code>.
	 * 
	 * @param cutoffRank
	 *            The rank cutoff
	 * @param comparator
	 *            The comparator
	 * @return The term predicate instance
	 */
	private static TermPredicate createSortedTermPredicate(
			final int cutoffRank, final Comparator<Term> comparator) {
		return new ListBasedTermPredicate() {

			@Override
			public void initialize(List<Term> termList) {
				
				// Sort annotations
				TreeSet<Term> annotations = new TreeSet<Term>(comparator);
				annotations.addAll(termList);			
				System.out.println("Terms in original list : " + termList.size());
				System.out.println("Terms in tree set list : " + annotations.size());
				System.out.println("Cutoff rank : " + cutoffRank);
				
				// Copy annotation ids up to the cutoff rank
				selectedIds = new String[cutoffRank];
				int i = 0;
				for (Term term : annotations) {
					if (i >= cutoffRank) {
						break;
					}
					selectedIds[i++] = term.getGroupingKey();
				}
				System.out.println("Terms in final list : " + selectedIds.length);
				Arrays.sort(selectedIds);
			}
		};
	}

	/**
	 * Creates a {@link TermPredicate} that accepts only terms contained in the
	 * specified <code>collection</code>
	 * 
	 * @param collection
	 *            The containment collection
	 * @return A term predicate instance
	 */
	public static TermPredicate createContainsPredicate(
			final Collection<Term> collection) {
		return new TermPredicate() {
			@Override
			public boolean accept(Term term) {
				return collection.contains(term);
			}
		};
	}
	
	/**
	 * A term predicate that can be initialized with a specific term list.
	 * 
	 * @author Sebastián Peña Saldarriaga
	 */
	private static abstract class ListBasedTermPredicate implements
			TermPredicate {

		/** Stores ids that are accepted by the predicate */
		protected String[] selectedIds;

		/**
		 * Initializes the predicate with the initial term list.
		 * 
		 * @param termList
		 *            The initial term list.
		 */
		public abstract void initialize(List<Term> termList);

		@Override
		public boolean accept(Term term) {
			return Arrays.binarySearch(selectedIds, term.getGroupingKey()) >= 0;
		}

	}

	// ////////////////////////////////////////////////////////////////////////
	// Comparators

	/**
	 * A comparator that imposes a descending occurrence order on
	 * {@link Term}s.
	 */
	public static final Comparator<Term> DESCENDING_OCCURRENCE_ORDER = new Comparator<Term>() {

		@Override
		public int compare(Term o1, Term o2) {
			return ComparisonChain.start()
					.compare(o2.getFrequency(), o1.getFrequency())
					.compare(o1.getGroupingKey(), o2.getGroupingKey())
					.result();
		}
	};

//	/**
//	 * A comparator that imposes a descending specificity order on
//	 * {@link Term}s.
//	 */
//	public static final Comparator<Term> DESCENDING_SPECIFICITY_ORDER = new Comparator<Term>() {
//
//		@Override
//		public int compare(Term o1, Term o2) {
//			return ComparisonChain.start()
//					.compare(o2.getWR(), o1.getWR())
//					.compare(o1.getGroupingKey(), o2.getGroupingKey())
//					.result();
//		}
//	};
	
	
	/**
	 * A comparator that imposes an ascending covered text order on
	 * {@link Term}s.
	 */
	public static final Comparator<Term> ASCENDING_TEXT_ORDER = new Comparator<Term>() {

		/** Compare names in a locale sensitive manner */
		private final Collator TextCollator = Collator.getInstance();

		@Override
		public int compare(Term o1, Term o2) {
			return TextCollator.compare(o1.getGroupingKey(), o2.getGroupingKey());
		}
	};
}
