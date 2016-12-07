package fr.univnantes.termsuite.engines.gatherer;

import fr.univnantes.julestar.uima.resources.MultimapFlatResource;

public class SynonymVariantHelper extends GroovyHelper {

	private MultimapFlatResource synonyms;

	void setSynonyms(MultimapFlatResource dico) {
		this.synonyms = dico;
	}

	
	@Override
	public boolean areSynonym(GroovyWord s, GroovyWord t) {
		boolean b1 = this.synonyms.getValues(s.lemma).contains(t.lemma);
		boolean b2 = this.synonyms.getValues(t.lemma).contains(s.lemma);
		return b1 || b2;

	}
}
