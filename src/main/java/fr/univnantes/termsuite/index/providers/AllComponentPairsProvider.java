package fr.univnantes.termsuite.index.providers;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.Sets;

import fr.univnantes.termsuite.engines.splitter.CompoundUtils;
import fr.univnantes.termsuite.index.TermIndexValueProvider;
import fr.univnantes.termsuite.model.Component;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.TermWord;
import fr.univnantes.termsuite.model.Word;
import fr.univnantes.termsuite.utils.CollectionUtils;
import fr.univnantes.termsuite.utils.Pair;
import fr.univnantes.termsuite.utils.TermUtils;

/**
 * Provides all lemma-lemma pairs found in the term.
 * 
 * Ex1: offshore wind energy
 * 			--> {offshore+wind, energy+wind, offshore+energy}
 * 
 * Performs two iterations when there are compounds
 * 
 * Ex2: horizontal-axis wind turbine
 * 			it1 --> {horizontal-axis+turbine, horizontal-axis+turbine, wind+turbine}
 * 			it2 --> {axis+horizontal, axis+turbine, axis+wind, horizontal+turbine, horizontal+wind, wind+turbine}
 * 			total -->  it1 U it2
 * 
 * @author Damien Cram
 * 
 */
public class AllComponentPairsProvider implements TermIndexValueProvider {

	@Override
	public Collection<String> getClasses(Term term) {
		Set<Pair<Component>> componentPairs = Sets.newHashSet();
		Set<Word> significantWords = Sets.newHashSetWithExpectedSize(term.getWords().size());
		
		/*
		 * 1- select significant words
		 */
		for(TermWord w:term.getWords()) {
			if(w.isSwt())
				significantWords.add(w.getWord());
		}
		
		/*
		 * 2- Adds intra-compound component pairs (for compound words only)
		 */
		significantWords.stream()
			.filter(Word::isCompound)
			.forEach(w -> componentPairs.addAll(CompoundUtils.innerComponentPairs(w)));
		
		/*
		 * 3- Add inter-word component pairs
		 */
		componentPairs.addAll(CollectionUtils.combineAndProduct(TermUtils.toComponentSets(significantWords)));
		
		
		/*
		 * 4- transform each component pair to class
		 */
		Set<String> classes = new HashSet<>();
			
		componentPairs.stream()
			.forEach(pair -> classes.addAll(CompoundUtils.toIndexStrings(pair)));
		return classes;
	}

}
