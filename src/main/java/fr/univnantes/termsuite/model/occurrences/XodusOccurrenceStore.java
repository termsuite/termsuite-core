package fr.univnantes.termsuite.model.occurrences;

import static java.util.stream.Collectors.toList;

import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;
import java.util.concurrent.Semaphore;

import com.google.common.collect.Lists;

import fr.univnantes.termsuite.model.Lang;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.TermOccurrence;
import jetbrains.exodus.entitystore.Entity;
import jetbrains.exodus.entitystore.EntityIterator;
import jetbrains.exodus.entitystore.PersistentEntityStore;
import jetbrains.exodus.entitystore.PersistentEntityStores;
import jetbrains.exodus.entitystore.StoreTransaction;

public class XodusOccurrenceStore extends AbstractMemoryOccStore {

	public static final String ENTITY_OCCURRENCE = "Occurrence";
	public static final String P_DOCUMENT_URL = "docUrl";
	public static final String P_TERM_KEY = "termKey";
	public static final String P_COVERED_TEXT = "coveredText";
	public static final String P_BEGIN = "begin";
	public static final String P_END = "end";
	
	private String url;
	private PersistentEntityStore occStore;
	
	public XodusOccurrenceStore(Lang lang, String url) {
		super(lang);
		this.url = url;
		occStore = PersistentEntityStores.newInstance(url);
	}

	@Override
	public Iterator<TermOccurrence> occurrenceIterator(Term term) {
		return getOccurrences(term).iterator();
	}

	@Override
	public Collection<TermOccurrence> getOccurrences(Term term) {
		return occStore.computeInReadonlyTransaction(txn -> 
			Lists.newArrayList(
						txn.find(ENTITY_OCCURRENCE, P_TERM_KEY, term.getGroupingKey()).iterator())
						.stream()
						.map( e -> new TermOccurrence(
								term,
								(String)e.getProperty(P_COVERED_TEXT), 
								protectedGetDocument((String)e.getProperty(P_DOCUMENT_URL)),
								(int)e.getProperty(P_BEGIN),
								(int)e.getProperty(P_END)
							))
						.collect(toList())
		);
	}

	@Override
	public Type getStoreType() {
		return Type.DISK;
	}

	@Override
	public void flush() {
		try {
			transactionMutex.acquire();
			if(collectingTransaction.isPresent())  {
				collectingTransaction.get().commit();
				collectingTransaction = Optional.empty();
			}
			transactionMutex.release();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
	

	private Semaphore transactionMutex = new Semaphore(1);
	
	private StoreTransaction getTransaction() {
		if(!collectingTransaction.isPresent()) 
			collectingTransaction = Optional.of(occStore.beginTransaction());
		return collectingTransaction.get();
	}
	
	@Override
	public String getUrl() {
		return url;
	}

	@Override
	public void removeTerm(Term t) {
		occStore.executeInTransaction(txn -> {
			EntityIterator it = txn.find(ENTITY_OCCURRENCE, P_TERM_KEY, t.getGroupingKey()).iterator();
			while (it.hasNext()) 
				it.next().delete();
		});
	}

	@Override
	public void close() {
		try {
			transactionMutex.acquire();
			getTransaction().abort();
			transactionMutex.release();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		occStore.close();
	}

	private Optional<StoreTransaction> collectingTransaction = Optional.empty();
	
	@Override
	public void addOccurrence(Term term, String documentUrl, int begin, int end, String coveredText) {
		try {
			transactionMutex.acquire();
			Entity entity = getTransaction().newEntity(ENTITY_OCCURRENCE);
			entity.setProperty(P_BEGIN, begin);
			entity.setProperty(P_END, end);
			entity.setProperty(P_DOCUMENT_URL, protectedGetDocument(documentUrl).getUrl());
			entity.setProperty(P_TERM_KEY, term.getGroupingKey());
			if(coveredText != null)
				entity.setProperty(P_COVERED_TEXT, coveredText);
			transactionMutex.release();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}

	}
}
