package fr.univnantes.termsuite.engines.prepare;

import fr.univnantes.termsuite.framework.TerminologyEngine;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.TermProperty;
import fr.univnantes.termsuite.model.Terminology;

/**
 * 
 * Sets {@link TermProperty#PILOT} and {@link TermProperty#DOCUMENT_FREQUENCY} properties.
 * 
 * @author Damien Cram
 *
 */
public class CorpusWidePropertiesSetter extends TerminologyEngine {
	public void set(Terminology index) {
		for(Term t:index.getTerms()) {
//			Multiset<String> texts = HashMultiset.create();
//			Set<String> documentUrls = new HashSet<>();
//			for(TermOccurrence occurrence : index.getOccurrenceStore().getOccurrences(t)) {
//				texts.add(occurrence.getCoveredText());
//				documentUrls.add(occurrence.getSourceDocument().getUrl());
//			}
//			String pilot = null;
//			int maxCount = -1;
//			for(String distinctText:texts.elementSet()) {
//				if(texts.count(distinctText) > maxCount) {
//					maxCount = texts.count(distinctText);
//					pilot = distinctText;
//				}
//			}
			String pilot = index.getOccurrenceStore().getMostFrequentForm(t);
			t.setPilot(pilot == null ? t.getGroupingKey() : pilot);
			t.setDocumentFrequency(index.getOccurrenceStore().getDocuments(t).size());
		}
	}
}
