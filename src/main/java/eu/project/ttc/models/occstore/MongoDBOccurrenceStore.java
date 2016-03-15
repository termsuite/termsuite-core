package eu.project.ttc.models.occstore;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.assertj.core.util.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.WriteConcern;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.BulkWriteOptions;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.UpdateOneModel;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import com.mongodb.client.model.WriteModel;

import eu.project.ttc.models.Document;
import eu.project.ttc.models.OccurrenceStore;
import eu.project.ttc.models.Term;
import eu.project.ttc.models.TermOccurrence;

public class MongoDBOccurrenceStore implements OccurrenceStore {
	private static final Logger LOGGER = LoggerFactory.getLogger(MongoDBOccurrenceStore.class);

	
	/**
	 * A monitor for {@link Executor}.
	 * 
	 * @author Damien Cram
	 *
	 */
	public class MyMonitorThread implements Runnable
	{
	    private ThreadPoolExecutor executor;

	    private int seconds;

	    private boolean run=true;

	    public MyMonitorThread(ThreadPoolExecutor executor, int delay)
	    {
	        this.executor = executor;
	        this.seconds=delay;
	    }

	    public void shutdown(){
	        this.run=false;
	    }

	    @Override
	    public void run()
	    {
	        while(run){
	                display();
	                try {
	                    Thread.sleep(seconds*1000);
	                } catch (InterruptedException e) {
	                    e.printStackTrace();
	                }
	        }

	    }

		public void display() {
			System.out.println(
			    String.format("[ThreadPoolExecutor monitor] [%d/%d] Active: %d, Queued: %d, Completed: %d, Task: %d, isShutdown: %s, isTerminated: %s",
			        this.executor.getPoolSize(),
			        this.executor.getCorePoolSize(),
			        this.executor.getQueue().size(),
			        this.executor.getActiveCount(),
			        this.executor.getCompletedTaskCount(),
			        this.executor.getTaskCount(),
			        this.executor.isShutdown(),
			        this.executor.isTerminated()));
		}
	}
	
	
	private static final String TERM_ID = "tid";

	private static final String DOC_ID = "doc";

	private static final String BEGIN = "b";
	private static final String END = "e";

	private static final String OCCURRENCES = "occurrences";

	private static final String DOC_URL = "url";

	protected static final String COVERED_TEXT = "t";

	private static final String FREQUENCY = "f";

	private String uri;

	private State state;
	
	private MongoCollection<org.bson.Document> termCollection;
	private MongoCollection<org.bson.Document> documentUrlCollection;
	private Map<Integer, String> documentsUrls;
	private Multimap<Term, TermOccurrence> termsBuffer;

	private ThreadPoolExecutor executor;
	private MyMonitorThread monitor;
	
	public MongoDBOccurrenceStore(String dbName) {
		this(dbName, State.COLLECTING);
	}
	
	public MongoDBOccurrenceStore(String dbName, State state) {
		this("mongodb://localhost:27017", dbName, state);
	}
	

	public MongoDBOccurrenceStore(String mongoDBUrl, String dbName) {
		this(mongoDBUrl, dbName, State.COLLECTING);
	}
	
	public MongoDBOccurrenceStore(String mongoDbUri, String dbName, State state) {
		super();
		
		int blockingBound = 500; // the size of the blocking queue.
		int maximumPoolSize = 50; // the max number of threads to execute
		executor = new BlockingThreadPoolExecutor(
				5, 
				maximumPoolSize, 
				1, TimeUnit.SECONDS, 
				blockingBound);
				
		monitor = new MyMonitorThread(executor, 5);
		new Thread(monitor).start();
		
		Preconditions.checkState(
				state != State.INDEXING, 
				"Invalid occ store state for constructor. Only " + State.COLLECTING + " and " + State.INDEXED + " allowed"
				);
		this.state = state;
		this.uri =  mongoDbUri;
		
		MongoClientURI connectionString = new MongoClientURI(mongoDbUri);
		mongoClient = new MongoClient(connectionString);
		MongoDatabase db = mongoClient.getDatabase( dbName )
										.withWriteConcern(WriteConcern.JOURNALED);

		if(state == State.COLLECTING) {
			db.drop();
		}
		
		termCollection = db.getCollection("terms");
		documentUrlCollection = db.getCollection("documents");

		
		documentUrlCollection.createIndex(
				new org.bson.Document(DOC_ID, 1), 
				new IndexOptions().unique(true));
		termCollection.createIndex(
				new org.bson.Document(TERM_ID, 1), 
				new IndexOptions().unique(true));
		termCollection.createIndex(
				new org.bson.Document(FREQUENCY, -1), 
				new IndexOptions().sparse(true));
		
		resetBuffers();
	}

	private void resetBuffers() {
		this.termsBuffer = HashMultimap.create();
		this.documentsUrls = Maps.newHashMap();
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
			.concurrencyLevel(1)
			.build(new CacheLoader<Integer, Document>() {
				@Override
				public Document load(Integer documentId) throws Exception {
					org.bson.Document bsonDoc = documentUrlCollection.find(Filters.eq(DOC_ID,documentId)).first();
					return new Document(documentId, bsonDoc.getString(DOC_URL));
				}
			});
			
	private LoadingCache<Term, List<TermOccurrence>> occurrenceCache = CacheBuilder.newBuilder()
			.maximumSize(1000)
			.build(new CacheLoader<Term, List<TermOccurrence>>() {
				@Override
				public List<TermOccurrence> load(Term key) throws Exception {
					@SuppressWarnings("unchecked")
					ArrayList<org.bson.Document> list = (ArrayList<org.bson.Document>)termCollection.find(Filters.eq(TERM_ID,key.getId())).first().get(OCCURRENCES);
					List<TermOccurrence> occurrences = Lists.newArrayListWithCapacity(list.size());
					org.bson.Document bsonOccurrence = null;
					for(Object value:list) {
						bsonOccurrence = (org.bson.Document)value;
						occurrences.add(new TermOccurrence(
								key, 
								bsonOccurrence.getString(COVERED_TEXT), 
								documentCache.getUnchecked(bsonOccurrence.getInteger(DOC_ID)), 
								bsonOccurrence.getInteger(BEGIN),
								bsonOccurrence.getInteger(END)

							));
					}
					return occurrences;
				}
			});

	private MongoClient mongoClient;
	
	@Override
	public Collection<TermOccurrence> getOccurrences(Term term) {
		checkState(State.INDEXED);
		return occurrenceCache.getUnchecked(term);
	}
	
	@Override
	public void addOccurrence(Term term, TermOccurrence e) {
		checkState(State.COLLECTING);

		documentsUrls.put(e.getSourceDocument().getId(), e.getSourceDocument().getUrl());
		termsBuffer.put(term, e);
	}

	@Override
	public void addAllOccurrences(Term term, Collection<TermOccurrence> c) {
		for(TermOccurrence occ:c)
			addOccurrence(term, occ);
	}

	@Override
	public Type getStoreType() {
		return Type.MONGODB;
	}

	@Override
	public String getUrl() {
		return uri;
	}

	
	@Override
	public void flush() {
		
		final List<WriteModel<org.bson.Document>> documentUrlsOps = Lists.newArrayListWithCapacity(documentsUrls.size());
		for(Map.Entry<Integer, String> e:this.documentsUrls.entrySet()) {
			UpdateOneModel<org.bson.Document> w = new UpdateOneModel<org.bson.Document>(
					Filters.eq(DOC_ID, e.getKey()),
					Updates.set(DOC_URL, e.getValue()),
					new UpdateOptions().upsert(true));
			documentUrlsOps.add(w);
		}

		if(!documentUrlsOps.isEmpty())
			executor.execute(new Runnable(){
					public void run() {
						documentUrlCollection.bulkWrite(documentUrlsOps, new BulkWriteOptions().ordered(false));
				}
			});
		
		
		final List<WriteModel<org.bson.Document>> termsOps = Lists.newArrayList();
		
		for(Term t:termsBuffer.keySet()) {
			List<org.bson.Document> list = Lists.newArrayListWithCapacity(termsBuffer.get(t).size());
			for(TermOccurrence e:termsBuffer.get(t)) {
				list.add(new org.bson.Document()
						.append(DOC_ID, e.getSourceDocument().getId())
						.append(BEGIN, e.getBegin())
						.append(END, e.getEnd())
						.append(COVERED_TEXT, e.getCoveredText()));
			}
			UpdateOneModel<org.bson.Document> w = new UpdateOneModel<org.bson.Document>(
					Filters.eq(TERM_ID, t.getId()), 
					Updates.combine(Updates.inc(FREQUENCY, list.size()),Updates.pushEach(OCCURRENCES, list)),
					new UpdateOptions().upsert(true));
			termsOps.add(w);
		}
		
		if(!termsOps.isEmpty())
			executor.execute(new Runnable(){
				public void run() {
					termCollection.bulkWrite(termsOps, new BulkWriteOptions().ordered(false));
				}
			});
		
		
		resetBuffers();

	}

	@Override
	public State getCurrentState() {
		return this.state;
	}

	@Override
	public void makeIndex() {
		this.state = State.INDEXING;
		flush();
		LOGGER.info("Shutting down the executor");
//		executor.shutdown();
		LOGGER.info("Waiting for the executor to terminate");
		while(executor.getActiveCount() > 0) {
			// Waiting for all taks to terminate
		}
		LOGGER.info("Executor terminated");
		monitor.shutdown();
		this.state = State.INDEXED;
	}

	@Override
	public void removeTerm(final Term t) {
		executor.execute(new Runnable(){
			public void run() {
				termCollection.deleteOne(new org.bson.Document(TERM_ID, t.getId()));
			}
		});
	}
	
	public void close() {
		monitor.shutdown();
		mongoClient.close();
		executor.shutdown();
	}
}
