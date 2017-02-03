package fr.univnantes.termsuite.engines.contextualizer;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;

import fr.univnantes.termsuite.model.TermOccurrence;

public class DocumentView {

	/**
	 * The ordered LinkedList of occurrences.
	 */
	private List<TermOccurrence> _occurrences = Lists.newArrayList();

	/**
	 * The position in occurrence list of each occurrence.
	 */
	private Map<TermOccurrence, Integer> occurrenceIndexes;  

	/**
	 * A flag that tells of occurrences must be sorted again
	 */
	private boolean occurrencesDirty = true;
	
	/**
	 * 
	 * Produce an iterator over all single-word term occurrences in the scope 
	 * of a given occurrence.
	 * 
	 * @see OccurrenceType
	 * @param occurrence
	 * 			the occurrence whose context iterator will be computed by the method
	 * @param coTermsType
	 * 			the type of occurrence to put in context
	 * @param contextSize
	 * 			the radius (i.e. half the maximum number of wingle-word terms) of the context window size
	 * @return
	 * 			an iterator over the occurrences
	 */
	public Iterator<TermOccurrence> getOccurrenceContext(final TermOccurrence occurrence, final int contextSize) {
		computeOccurrences();
		return Iterators.concat(
				new LeftContextIterator(occurrence, contextSize),
				new RightContextIterator(occurrence, contextSize)
			);
	}
	
	private abstract class DirectionalContextIterator extends AbstractIterator<TermOccurrence> {
		protected TermOccurrence occurrence;
		private int radius;

		protected int index;
		private int returnedOccCnt = 0;
		private TermOccurrence current;
		
		private DirectionalContextIterator(TermOccurrence occurrence, int radius) {
			super();
			this.occurrence = occurrence;
			this.radius = radius;
			this.index = DocumentView.this.occurrenceIndexes.get(occurrence);
			moveCursor();
		}

		@Override
		protected TermOccurrence computeNext() {
			while (returnedOccCnt < radius && index >=0 && index < DocumentView.this.getOccurrences().size()) {
				this.current = DocumentView.this.getOccurrences().get(this.index);
				
				if(keepOccurrence(this.current)) {
					this.returnedOccCnt++;
					moveCursor();
					return this.current;
				} else 
					moveCursor();

			}
			return endOfData();
		}

		private boolean keepOccurrence(TermOccurrence o) {
			if(overlap(o))
				return false;
			return o.getTerm().getWords().size() == 1;
		}

		protected abstract boolean overlap(TermOccurrence o);
		protected abstract void moveCursor();
	};
	
	/*
	 * Iterates from a given occurrence over its closest left-side 
	 * single-word neighbour term occurrences in the document.
	 */
	private class LeftContextIterator extends DirectionalContextIterator {
		private LeftContextIterator(TermOccurrence occurrence, int radius) {
			super(occurrence, radius);
		}
		@Override
		protected void moveCursor() {
			this.index--;
		}
		@Override
		protected boolean overlap(TermOccurrence o) {
			return o.getEnd() > this.occurrence.getBegin();
		}
	}

	/*
	 * Iterates from a given occurrence over its closest right-side 
	 * single-word neighbour term occurrences in the document.
	 */
	private class RightContextIterator extends DirectionalContextIterator {
		private RightContextIterator(TermOccurrence occurrence, int radius) {
			super(occurrence, radius);
		}
		@Override
		protected void moveCursor() {
			this.index++;
		}
		@Override
		protected boolean overlap(TermOccurrence o) {
			return o.getBegin() < this.occurrence.getEnd();
		}
	}
	
	/**
	 * 
	 * @param termOccurrence
	 * @return
	 */
	public int indexTermOccurrence(TermOccurrence termOccurrence) {
		this.occurrencesDirty = true;
		_occurrences.add(termOccurrence);
		return _occurrences.size();
	}
	
	public List<TermOccurrence> getOccurrences() {
		computeOccurrences();
		return this._occurrences;
	}

	private void computeOccurrences() {
		if(this.occurrencesDirty) {
			occurrenceIndexes = new HashMap<>();
			java.util.Collections.sort(this._occurrences);
			int index = 0;
			for(TermOccurrence o:this._occurrences) {
				occurrenceIndexes.put(o, index);
				index++;
			}
			this.occurrencesDirty = false;
		}
	}
	
	/**
	 * Nullifies the inner occurrence list so as to frees memory.
	 */
	public void clearOccurrenceIndex() {
		this._occurrences = Lists.newArrayList();
		this.occurrenceIndexes = new HashMap<>();
		this.occurrencesDirty = true;
	}
	
	public Iterator<TermOccurrence> contextIterator(TermOccurrence o, int contextSize) {
		return getOccurrenceContext(o, contextSize);
	}
}
