package fr.univnantes.termsuite.model.occurrences;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

import fr.univnantes.termsuite.model.Document;
import fr.univnantes.termsuite.model.Form;
import fr.univnantes.termsuite.model.Lang;
import fr.univnantes.termsuite.model.OccurrenceStore;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.TermOccurrence;

public abstract class AbstractMemoryOccStore  implements OccurrenceStore {
	private static final String ERR_DOC_DOES_NOT_EXIST = "Document %s does not exists";
	
	private Lang lang;
	private Map<String, Document> documents = new ConcurrentHashMap<>();

	public AbstractMemoryOccStore(Lang lang) {
		super();
		this.lang = lang;
	}

	protected Document protectedGetDocument(String documentUrl) {
		if(!documents.containsKey(documentUrl))
			documents.put(documentUrl, new Document(lang, documentUrl));
		return documents.get(documentUrl);
	}

	@Override
	public Document getDocument(String url) {
		Preconditions.checkArgument(documents.containsKey(url), ERR_DOC_DOES_NOT_EXIST, url);
		return documents.get(url);
	}

	@Override
	public Collection<Document> getDocuments() {
		return documents.values();
	}
	
	@Override
	public List<Form> getForms(Term term) {
		Multiset<String> texts = HashMultiset.create();
		for(TermOccurrence o:getOccurrences(term)) {
			if(o.getCoveredText() != null)
				texts.add(o.getCoveredText());
		}
		List<Form> forms = new ArrayList<>();
		for(String distinctText:texts.elementSet())
			forms.add(new Form(distinctText).setCount(texts.count(distinctText)));
		Collections.sort(forms);
		return forms;
	}
}
