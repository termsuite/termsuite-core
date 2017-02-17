package fr.univnantes.termsuite.index.providers;

import java.util.Collection;

import com.google.common.collect.ImmutableList;

import fr.univnantes.termsuite.index.TermIndexValueProvider;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.TermWord;
import fr.univnantes.termsuite.model.Word;
import fr.univnantes.termsuite.utils.StringUtils;
import fr.univnantes.termsuite.utils.TermSuiteConstants;

public class FirstLettersValueProvider implements TermIndexValueProvider {
	
	private int nbLetters;
	
	public FirstLettersValueProvider(int nbLetters) {
		this.nbLetters = nbLetters;
	}

	@Override
	public Collection<String> getClasses(Term term) {
		if(term.getWords().size() == 1) {
			Word word = term.getWords().get(0).getWord();
			if(word.getLemma().length() < 5)
				return ImmutableList.of();
			else {
				String substring = StringUtils.replaceAccents(word.getLemma().toLowerCase().substring(0, 4));
				return ImmutableList.of(substring);
			}
		}
		StringBuilder builder = new StringBuilder();
		String normalizedStem;
		int i = 0;
		for(TermWord tw:term.getWords()) {
			if(i>0) {
				builder.append(TermSuiteConstants.COLONS);
			}
			normalizedStem = tw.getWord().getNormalizedStem();
			if(normalizedStem.length() > nbLetters)
				builder.append(normalizedStem.substring(0, nbLetters).toLowerCase());
			else
				builder.append(normalizedStem.toLowerCase());
			i++;
		}
		if(builder.length() >= nbLetters)
			return ImmutableList.of(builder.toString());
		else
			return ImmutableList.of();
	}
}