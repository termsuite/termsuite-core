package fr.univnantes.termsuite.engines;

import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.TermOccurrence;
import fr.univnantes.termsuite.model.TermProperty;
import fr.univnantes.termsuite.model.Terminology;

/**
 * 
 * Sets {@link TermProperty#PILOT} and {@link TermProperty#DOCUMENT_FREQUENCY} properties.
 * 
 * @author Damien Cram
 *
 */
public class CorpusWidePropertiesSetter {
	public void set(Terminology index) {
		for(Term t:index.getTerms()) {
			Multiset<String> texts = HashMultiset.create();
			Set<String> documentUrls = new HashSet<>();
			for(TermOccurrence occurrence : index.getOccurrenceStore().getOccurrences(t)) {
				texts.add(occurrence.getCoveredText());
				documentUrls.add(occurrence.getSourceDocument().getUrl());
			}
			String pilot = null;
			int maxCount = -1;
			for(String distinctText:texts.elementSet()) {
				if(texts.count(distinctText) > maxCount) {
					maxCount = texts.count(distinctText);
					pilot = distinctText;
				}
			}
			
			t.setPilot(pilot == null ? t.getGroupingKey() : pilot);
			t.setDocumentFrequency(documentUrls.size());
		}
	}
}
