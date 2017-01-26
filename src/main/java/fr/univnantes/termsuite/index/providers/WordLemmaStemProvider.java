package fr.univnantes.termsuite.index.providers;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;

import fr.univnantes.termsuite.index.AbstractTermIndexValueProvider;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.TermWord;
import fr.univnantes.termsuite.model.Terminology;
import fr.univnantes.termsuite.utils.TermSuiteConstants;

public class WordLemmaStemProvider extends AbstractTermIndexValueProvider {

	
	@Override
	public Collection<String> getClasses(Terminology termino, Term term) {
		List<String> lemmas = Lists.newArrayListWithCapacity(term.getWords().size());
		
		Map<String, String> stems = new HashMap<String, String>();
		for(TermWord w:term.getWords()) {
			if (w.getWord().getLemma() == null || w.getWord().getLemma().isEmpty()) {
				continue;
			} else if(TermSuiteConstants.TERM_MATCHER_LABELS.contains(w.getSyntacticLabel())) {
					lemmas.add(w.getWord().getNormalizedLemma());
				if(w.getWord().getStem() == null || w.getWord().getStem().isEmpty()) {
					// do nothing
				} else 
					stems.put(w.getWord().getNormalizedLemma(), w.getWord().getNormalizedStem());
			}
		}	
		Collections.sort(lemmas);
		List<String> keys = Lists.newArrayListWithCapacity(lemmas.size());
		
		for (int i = 0 ; i < lemmas.size(); i++) {
			for (int j = i + 1; j < lemmas.size(); j++) {
				StringBuilder sb = new StringBuilder();
				sb.append(lemmas.get(i));
				sb.append(TermSuiteConstants.PLUS);
				sb.append(stems.get(lemmas.get(j)));
				keys.add(sb.toString());
			}
		}
		

		return keys;
	}

}
