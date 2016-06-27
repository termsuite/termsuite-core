package eu.project.ttc.engines.morpho;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import eu.project.ttc.models.Component;
import eu.project.ttc.models.Word;

public class CompoundUtils {

	private static final String ERR_MSG_CANNOT_MERGE_AN_EMPTY_SET = "Cannot merge an empty set of component";
	private static final String ERR_MSG_COMPONENTS_ARE_NOT_CONSECUTIVE = "Cannot merge two components if they are not consecutive. Got [%s,%s] followed by [%s,%s].";
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
	public static Set<Component> allSizeComponents(Word word) {
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
		return components;
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
					cur.getBegin() == lastComponent.getEnd(),
					ERR_MSG_COMPONENTS_ARE_NOT_CONSECUTIVE,
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
			lastComponent = cur;
		}
		lemmaBuilder.append(lastComponent.getLemma());
		return new Component(lemmaBuilder.toString(), begin, lastComponent.getEnd());
	}

}
