package eu.project.ttc.engines.morpho;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import eu.project.ttc.models.Component;
import eu.project.ttc.models.Word;
import eu.project.ttc.models.index.TermValueProviders;
import eu.project.ttc.utils.Pair;
import eu.project.ttc.utils.TermSuiteConstants;

/**
 * 
 * A set of helper methods for compound words and for iteration
 * over word components (see {@link TermValueProviders}).
 * 
 * @author Damien Cram
 *
 */
public class CompoundUtils {

	private static final String ERR_MSG_CANNOT_MERGE_AN_EMPTY_SET = "Cannot merge an empty set of component";
	private static final String ERR_MSG_COMPONENTS_OVERLAP = "Cannot merge two components if they overlap. Got [%s,%s] followed by [%s,%s].";
	private static final String ERR_MSG_COMPONENT_OFFSET_ARE_TOO_BIG = "Component %s does not belong to word %s (length=%s), because offsets [%s,%s] are too big.";
	private static final String ERR_WMSG_WORD_LEMMA_NULL = "Word lemma needs to not be null";
	
	
	/**
	 * Returns all possible components for a compound word 
	 * by combining its atomic components.
	 * 
	 * E.g. ab|cd|ef returns
	 * 		abcdef,
	 * 		ab, cdef,
	 * 		abcd, ef,
	 * 		cd
	 * 
	 * 
	 * @param word the compound word
	 * @return
	 * 			the list of all possible component lemmas
	 */
	public static List<Component> allSizeComponents(Word word) {
		Set<Component> components = Sets.newHashSet();
		for(int nbComponents=word.getComponents().size();
				nbComponents > 0 ;
				nbComponents--) {
			
			for(int startIndex = 0;
					startIndex <= word.getComponents().size() - nbComponents;
					startIndex++) {
				List<Component> toMerge = Lists.newArrayListWithExpectedSize(nbComponents);
				
				for(int i = 0; i<nbComponents; i++) 
					toMerge.add(word.getComponents().get(startIndex + i));
				
				components.add(merge(word, toMerge));
			}
		}
		return Lists.newArrayList(components);
	}

	/**
	 * 
	 * Merges <code>n</code> consecutive components of a compound
	 * word into a single {@link Component} object. 
	 * 
	 * The <code>lemma</code> of the returned {@link Component} is
	 * the concatenation of the 1st to n-1-th param components' substring 
	 * and the last param component's <code>lemma</code>.
	 * 
	 * 
	 * @param word
	 * 			The compound word
	 * @param components
	 * 			The list of consecutive components of the word to merge
	 * @return
	 * 			The merged component
	 * 
	 * @throws IllegalArgumentException
	 * 				when the <code>components</code> param is empty
	 * @throws IllegalArgumentException
	 * 				when the <code>components</code> are not consecutive
	 * @throws IllegalArgumentException
	 * 				when the components offsets do not match with the <code>word</code> size.
	 */
	public static Component merge(Word word, Iterable<? extends Component> components) {
		Preconditions.checkNotNull(word.getLemma(), ERR_WMSG_WORD_LEMMA_NULL);
		 
		
		Iterator<? extends Component> it = components.iterator();
		Preconditions.checkArgument(it.hasNext(), ERR_MSG_CANNOT_MERGE_AN_EMPTY_SET);
		
		Component lastComponent = it.next();
		int begin = lastComponent.getBegin();
		StringBuilder lemmaBuilder = new StringBuilder();
		while (it.hasNext()) {
			Component cur = it.next();
			Preconditions.checkArgument(
					cur.getBegin() >= lastComponent.getEnd(),
					ERR_MSG_COMPONENTS_OVERLAP,
					lastComponent.getBegin(), lastComponent.getEnd(),
					cur.getBegin(), cur.getEnd()
				);
			
			
			Preconditions.checkArgument(
					cur.getEnd() <= word.getLemma().length(),
					ERR_MSG_COMPONENT_OFFSET_ARE_TOO_BIG,
					cur, word, word.getLemma().length(),
					cur.getBegin(),cur.getEnd()
					);
			lemmaBuilder.append(word.getLemma().substring(lastComponent.getBegin(), lastComponent.getEnd()));
			
			if(lastComponent.getEnd() < cur.getBegin())
				/*
				 * Fills the gap with the lemma substring
				 */
				lemmaBuilder.append(word.getLemma().substring(lastComponent.getEnd(), cur.getBegin()));
			
			lastComponent = cur;
		}
		lemmaBuilder.append(lastComponent.getLemma());
		return new Component(lemmaBuilder.toString(), begin, lastComponent.getEnd());
	}

	
	/**
	 * 
	 * Produces the set of all pairs of non-overlapping components
	 * for a given word.
	 * 
	 * E.g. ab|cd|ef returns:
	 * 		ab+cd, ab+ef, cd+ef, ab+cdef, abcd+ef
	 * 			
	 * 
	 * @param word
	 * 			the compound word
	 * @return
	 * 			the exhaustive list of pairs.
	 */
	public static List<Pair<Component>> innerComponentPairs(Word word) {
		Set<Pair<Component>> pairs = Sets.newHashSet();
		List<Component> components = allSizeComponents(word);
		Component c1,c2;
		Pair<Component> pair;
		for(int i=0; i<components.size(); i++) {
			c1 = components.get(i);
			for(int j=i+1; j<components.size(); j++) {
				c2 = components.get(j);
				pair = new Pair<Component>(c1, c2);
				if(pair.getElement1().getEnd() <= pair.getElement2().getBegin())
					// no overlap
					pairs.add(pair);
			}
		}
		return Lists.newArrayList(pairs);
	}
	
	public static String toIndexString(Pair<Component> pair) {
		boolean ordered = pair.getElement1().getLemma().compareTo(pair.getElement2().getLemma()) <= 0;
		StringBuilder sb = new StringBuilder();
		sb.append(ordered ? pair.getElement1().getLemma() : pair.getElement2().getLemma());
		sb.append(TermSuiteConstants.PLUS);
		sb.append(ordered ? pair.getElement2().getLemma() : pair.getElement1().getLemma());
		return sb.toString();
		
	}

}
