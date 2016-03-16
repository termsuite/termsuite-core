package eu.project.ttc.models.occstore;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import eu.project.ttc.models.Document;
import eu.project.ttc.models.OccurrenceStore;
import eu.project.ttc.models.Term;
import eu.project.ttc.models.TermOccurrence;
import eu.project.ttc.models.TermSelector;

public class FileOccurrenceStore implements OccurrenceStore {
	private static final Logger LOGGER = LoggerFactory.getLogger(FileOccurrenceStore.class);

	private String fileURL;

	private State state;
	
	private File storeFile;
	
	private DB db;
	
	private ConcurrentMap<Integer, List<List<Integer>>> occurrenceMap;
	private ConcurrentMap<Integer, String> documentUrlMap;
	
	public FileOccurrenceStore(String url) {
		super();
		this.fileURL = url;
		this.state = State.COLLECTING;
		this.storeFile = new File(url);
		db = DBMaker
				.fileDB(storeFile)
				.asyncWriteEnable()
//				.fileMmapEnableIfSupported()
				.make();
		occurrenceMap = db
				.hashMap("occurrences");
		documentUrlMap = db.hashMap("documentUrls");
	}
	
	private void checkState(State state) {
		if(state != this.state)
			throw new IllegalStateException("Current state is " + this.state + ". Expected state: " + state);
	}

	@Override
	public Iterator<TermOccurrence> occurrenceIterator(Term term) {
		return getOccurrences(term).iterator();
	}

	
	private LoadingCache<Integer, Document> documentCache = CacheBuilder.newBuilder()
			.build(new CacheLoader<Integer, Document>() {
				@Override
				public Document load(Integer documentId) throws Exception {
					return new Document(documentId, documentUrlMap.get(documentId));
				}
			});
			
	private LoadingCache<Term, List<TermOccurrence>> occurrenceCache = CacheBuilder.newBuilder()
			.maximumSize(1000)
			.build(new CacheLoader<Term, List<TermOccurrence>>() {
				@Override
				public List<TermOccurrence> load(Term key) throws Exception {
					List<List<Integer>> list = occurrenceMap.get(key.getId());
					List<TermOccurrence> occurrences = Lists.newArrayListWithCapacity(list.size());
					for(List<Integer> values:list) {
						occurrences.add(new TermOccurrence(
								key, 
								null, 
								documentCache.getUnchecked(values.get(1)), 
								values.get(2),
								values.get(3)
							));
					}
					return occurrences;
				}
			});
	
	@Override
	public Collection<TermOccurrence> getOccurrences(Term term) {
		checkState(State.INDEXED);
		return occurrenceCache.getUnchecked(term);
	}
	
	@Override
	public void addOccurrence(Term term, TermOccurrence e) {
		checkState(State.COLLECTING);
		documentUrlMap.put(e.getSourceDocument().getId(), e.getSourceDocument().getUrl());
		if(occurrenceMap.containsKey(term.getId())) {
			occurrenceMap.get(term.getId()).add(ImmutableList.of( 
			term.getId(), 
			e.getSourceDocument().getId(), 
			e.getBegin(), 
			e.getEnd()));
		} else {
			List<List<Integer>> values = Lists.newArrayList();
			values.add(ImmutableList.of( 
				term.getId(), 
				e.getSourceDocument().getId(), 
				e.getBegin(), 
				e.getEnd()));
			occurrenceMap.put(term.getId(), values);
		}
	}

	@Override
	public void addAllOccurrences(Term term, Collection<TermOccurrence> c) {
		for(TermOccurrence occ:c)
			addOccurrence(term, occ);
	}

	@Override
	public Type getStoreType() {
		return Type.FILE;
	}

	@Override
	public String getUrl() {
		return fileURL;
	}

	@Override
	public void flush() {
		db.commit();
	}

	@Override
	public State getCurrentState() {
		return this.state;
	}

	@Override
	public void makeIndex() {
		db.commit();
		LOGGER.info("Start indexing");
		this.state = State.INDEXED;
		LOGGER.info("Occurrence store indexed");
	}

	@Override
	public void removeTerm(Term t) {
		occurrenceMap.remove(t.getId());
	}

	@Override
	public void deleteMany(TermSelector selector) {
		throw new UnsupportedOperationException("not supported");
	}
}
