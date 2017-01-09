package fr.univnantes.termsuite.model.occurrences;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	
	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractMemoryOccStore.class);
	
	private Lang lang;
	private Map<String, Document> documents = new ConcurrentHashMap<>();
	private Map<Term, Set<Document>> documentsByTerm = new ConcurrentHashMap<>();
	private Map<Term, Map<String, AtomicInteger>> forms = new ConcurrentHashMap<>();

	public AbstractMemoryOccStore(Lang lang) {
		super();
		this.lang = lang;
	}

	
	@Override
	public void log() {
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug(String.format("Nb documents: %d - Nb terms x documents: %d - Nb forms: %d", 
					documents.size(),
					documentsByTerm.entrySet().stream().map(e -> (long)e.getValue().size()).reduce(0L, (acc, el) -> acc +  el),
					forms.entrySet().stream().map(e -> (long)e.getValue().size()).reduce(0L, (acc, el) -> acc +  el)					
			));
		}
	}
	
	protected Set<Document> protectedGetDocuments(Term t) {
		if(!documentsByTerm.containsKey(t))
			documentsByTerm.put(t, new HashSet<>(4));
		return documentsByTerm.get(t);
	}

	protected Map<String, AtomicInteger> protectedGetForms(Term t) {
		if(!forms.containsKey(t))
			forms.put(t, new HashMap<>(2));
		return forms.get(t);
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
	
	@Override
	public void addOccurrence(Term term, String documentUrl, int begin, int end, String coveredText) {
		Document document = protectedGetDocument(documentUrl);

		// store documents
		protectedGetDocuments(term).add(document);

		// store forms
		Map<String, AtomicInteger> forms = protectedGetForms(term);
		String coveredTextForm = toForm(coveredText);
		if(forms.containsKey(coveredTextForm))
			forms.get(coveredTextForm).incrementAndGet();
		else
			forms.put(coveredTextForm, new AtomicInteger(1));
	}
	
	protected String toForm(String coveredText) {
		return coveredText.replaceAll("[\\s\r\n]+", " ");
	}

	@Override
	public void removeTerm(Term t) {
		documentsByTerm.remove(t);
		forms.remove(t);
	}
	
	
	@Override
	public String getMostFrequentForm(Term t) {
		if(forms.containsKey(t)) {
			Map<String, AtomicInteger> forms = protectedGetForms(t);
			Optional<Entry<String, AtomicInteger>> max = forms.entrySet()
				.stream()
				.max((e1,e2) -> {
					return Integer.compare(e1.getValue().intValue(), e2.getValue().intValue());
				});
			
			if(max.isPresent())
				return max.get().getKey();
		}
		return null;
	}

	@Override
	public Set<Document> getDocuments(Term t) {
		return protectedGetDocuments(t);
	}

}
