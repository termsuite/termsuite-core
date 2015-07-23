package eu.project.ttc.utils;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import eu.project.ttc.models.Document;
import eu.project.ttc.models.TermOccurrence;

/**
 * A utililty class for {@link TermOccurrence} objects and collections.
 * 
 * @author Damien Cram
 *
 */
public class TermOccurrenceUtils {
	
	public static Comparator<TermOccurrence> specificityComparator = new Comparator<TermOccurrence>() {
		@Override
		public int compare(TermOccurrence o1, TermOccurrence o2) {
			return ComparisonChain.start()
					.compare(o2.getTerm().getWR(), o1.getTerm().getWR())
					.compare(o1.getSourceDocument().getUrl(), o2.getSourceDocument().getUrl())
					.compare(o1.getBegin(), o2.getBegin())
					.compare(o2.getEnd(), o1.getEnd())
					.result();
		}
	};


	public static Comparator<TermOccurrence> uimaNaturalOrder = new Comparator<TermOccurrence>() {
		@Override
		public int compare(TermOccurrence o1, TermOccurrence o2) {
			return ComparisonChain.start()
					.compare(o1.getSourceDocument().getUrl(), o2.getSourceDocument().getUrl())
					.compare(o1.getBegin(), o2.getBegin())
					.compare(o2.getEnd(), o1.getEnd())
					.result();
		}
	};

	
	public static final int STRATEGY_MOST_SPECIFIC_FIRST = 1;
	
	/**
	 * Given a strategy, detects all primary occurrences in a collection 
	 * of {@link TermOccurrence}.
	 * 
	 * What defines an occurrence's primary/secondary status is the fact
	 * that in a {@link Document}, two primary occurrences cannot overlap.
	 * 
	 * E.g. in text "offshore wind energy", the sequence or term occurrences "offshore"
	 * and "wind energy" is a set of primary sequence, but the set of term occurrences 
	 * "offshore wind" and "wind energy" is not a primary sequence, because occurrences 
	 * overlap.
	 * 
	 * 
	 * @see TermOccurrenceUtils#STRATEGY_MOST_SPECIFIC_FIRST
	 * @see TermOccurrenceUtils#markPrimaryOccurrenceMostSpecificFirst(Collection)
	 * @see TermOccurrence#isPrimaryOccurrence()
	 * @param occs
	 * 			the occurrence collection
	 * @param strategy
	 * 			the strategy for detecting primary occurrences 
	 * @throw {@link IllegalArgumentException} if unknown strategy
	 * 			
	 */
	public static void markPrimaryOccurrence(Collection<TermOccurrence> occs, int strategy) {
		switch (strategy) {
		case STRATEGY_MOST_SPECIFIC_FIRST:
			markPrimaryOccurrenceMostSpecificFirst(occs);
			break;
		default:
			throw new IllegalArgumentException("Unkown strategy: " + strategy);
		}
	}

	/**
	 * Detects all primary occurrences in an occurrence set 
	 * with the "most-specific" first strategy.
	 * 
	 * @param occs
	 */
	public static void markPrimaryOccurrenceMostSpecificFirst(
			Collection<TermOccurrence> occs) {
		
		
		for(Iterator<List<TermOccurrence>> it = occurrenceChunkIterator(occs);it.hasNext();) {
			List<TermOccurrence> chunk = it.next();
			Set<TermOccurrence> primaryOccs = Sets.newHashSet();
			
			Collections.sort(chunk, specificityComparator);
			for(TermOccurrence o:chunk) {
				o.setPrimaryOccurrence(!hasOverlappingOffsets(o, primaryOccs));
				if(o.isPrimaryOccurrence())
					primaryOccs.add(o);
			}
			
		}
		
	}

	/**
	 * Returns a virtual iterator on chunks of an occurrence collection.
	 * 
	 * A occurrence collection's chunk is a list of overlapping {@link TermOccurrence}. Every time
	 * there is a gap between two occurrences (i.e. there do not overlap),
	 * a new chunk is created.
	 * 
	 * @param occurrences
	 * @return
	 */
	public static Iterator<List<TermOccurrence>> occurrenceChunkIterator(Collection<TermOccurrence> occurrences) {
		List<TermOccurrence> asList = Lists.newArrayList(occurrences);
		Collections.sort(asList, TermOccurrenceUtils.uimaNaturalOrder);
		final Iterator<TermOccurrence> it = asList.iterator();
		return new AbstractIterator<List<TermOccurrence>>() {
			private List<TermOccurrence> currentChunk = Lists.newArrayList();
			
			@Override
			protected List<TermOccurrence> computeNext() {
				while(it.hasNext()) {
					TermOccurrence next = it.next();
					if(currentChunk.isEmpty() || hasOverlappingOffsets(next, currentChunk))
						currentChunk.add(next);
					else {
						List<TermOccurrence> ret = copyAndReinit();
						currentChunk.add(next);
						return ret;	
					}
				} 
				if(!currentChunk.isEmpty()) {
					return copyAndReinit();
				} else 
					return endOfData();
			}

			private List<TermOccurrence> copyAndReinit() {
				List<TermOccurrence> copy = Lists.newArrayList(currentChunk);
				currentChunk = Lists.newArrayList();
				return copy;
			}
		};
	}

	/**
	 * True if an occurrence set contains any element overlapping 
	 * with the param occurrence.
	 * 
	 * @param theOcc
	 * @param theOccCollection
	 * @return
	 */
	public static boolean hasOverlappingOffsets(TermOccurrence theOcc, Collection<TermOccurrence> theOccCollection) {
		for(TermOccurrence o:theOccCollection)
			if(areOffsetsOverlapping(theOcc, o))
				return true;
		return false;
	}
	
	/**
	 * True if two {@link TermOccurrence} offsets overlap strictly. Sharing exactly
	 * one offset (e.g. <code>a.end == b.begin</code>) is not considered as overlap.
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public static boolean areOffsetsOverlapping(TermOccurrence a, TermOccurrence b) {
		if(a.getBegin() <= b.getBegin()) 
			return !(a.getBegin() <= b.getEnd() && a.getEnd() <= b.getBegin());
		else
			return !(b.getBegin() <= a.getEnd() && b.getEnd() <= a.getBegin());
			
	}

}
