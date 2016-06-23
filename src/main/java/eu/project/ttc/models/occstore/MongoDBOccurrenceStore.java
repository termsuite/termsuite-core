package eu.project.ttc.models.occstore;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.mutable.MutableInt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.WriteConcern;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.BulkWriteOptions;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOneModel;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import com.mongodb.client.model.WriteModel;

import eu.project.ttc.models.Document;
import eu.project.ttc.models.OccurrenceStore;
import eu.project.ttc.models.Term;
import eu.project.ttc.models.TermOccurrence;
import eu.project.ttc.models.index.selectors.FrequencyUnderThreshholdSelector;
import eu.project.ttc.models.index.selectors.TermSelector;

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
	    private BlockingThreadPoolExecutor executor;

	    private int seconds;

	    private boolean run=true;

	    public MyMonitorThread(BlockingThreadPoolExecutor executor, int delay)
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
	                log();
	                try {
	                    Thread.sleep(seconds*1000);
	                } catch (InterruptedException e) {
	                    e.printStackTrace();
	                }
	        }

	    }

		public void log() {
			LOGGER.info(
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
	
	
//	private static final String TERM_ID = "tid";

	
	private static final String _ID = "_id";
	private static final String DOC_ID = "did";

	private static final String BEGIN = "ob";
	private static final String END = "oe";

	private static final String DOC_URL = "url";

	protected static final String COVERED_TEXT = "t";

	private static final String FREQUENCY = "f";
	private static final String TERM_ID = "tid";

	private MongoClientURI mongoDBUri;

	private State state;
	
	private MongoCollection<org.bson.Document> termCollection;
	private MongoCollection<org.bson.Document> occurrenceCollection;
	private MongoCollection<org.bson.Document> documentUrlCollection;
	
	
	private Map<Integer, String> documentsUrls;
	private List<TermOccurrence> occurrencesBuffer;
	private Map<Term, MutableInt> termsBuffer;

	private BlockingThreadPoolExecutor executor;
	private MyMonitorThread monitor;
	
	public MongoDBOccurrenceStore(String dbURI) {
		this(dbURI, State.COLLECTING);
	}
	

	public MongoDBOccurrenceStore(String mongoDbUri, State state) {
		super();
		
		Preconditions.checkNotNull(mongoDbUri, "MongoDB dadabase's URI must not be null");
		Preconditions.checkState(
				state != State.INDEXING, 
				"Invalid occ store state for constructor. Only " + State.COLLECTING + " and " + State.INDEXED + " allowed"
				);

		this.mongoDBUri = getMongoDBUri(mongoDbUri);
		this.state = state;
		
		initThreadExecutor();
		
		MongoClientURI connectionString = new MongoClientURI(mongoDbUri);
		this.mongoClient = new MongoClient(connectionString);
		MongoDatabase db = mongoClient.getDatabase(this.mongoDBUri.getDatabase())
										.withWriteConcern(WriteConcern.ACKNOWLEDGED);
		db.runCommand(new org.bson.Document("profile", 1));

		if(state == State.COLLECTING)
			db.drop();
		
		this.termCollection = db.getCollection("terms");
		this.occurrenceCollection = db.getCollection("occurrences");
		this.documentUrlCollection = db.getCollection("documents");

		resetBuffers();
	}


	private void initThreadExecutor() {
		int blockingBound = 15; // the size of the blocking queue.
		int maximumPoolSize = 10; // the max number of threads to execute
		executor = new BlockingThreadPoolExecutor(
				0, 
				maximumPoolSize, 
				1, TimeUnit.SECONDS, 
				blockingBound);
				
		monitor = new MyMonitorThread(executor, 5);
		new Thread(monitor).start();
	}

	private MongoClientURI getMongoDBUri(String mongoDbUri) {
		if(mongoDbUri.startsWith("mongodb://"))
			return new MongoClientURI(mongoDbUri);
		else
			// mongoDbUri is a db name
			return new MongoClientURI("mongodb://localhost:27017/" + mongoDbUri);
	}


	private void resetBuffers() {
		this.termsBuffer = Maps.newHashMap();
		this.documentsUrls = Maps.newHashMap();
		this.occurrencesBuffer = Lists.newArrayList();
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
			.maximumSize(10000)
			.build(new CacheLoader<Integer, Document>() {
				@Override
				public Document load(Integer documentId) throws Exception {
					org.bson.Document bsonDoc = documentUrlCollection.find(Filters.eq(_ID,documentId)).first();
					return new Document(documentId, bsonDoc.getString(DOC_URL));
				}
			});
			
	private LoadingCache<Term, List<TermOccurrence>> occurrenceCache = CacheBuilder.newBuilder()
			.maximumSize(1000)
			.build(new CacheLoader<Term, List<TermOccurrence>>() {
				@Override
				public List<TermOccurrence> load(Term term) throws Exception {
					List<TermOccurrence> occurrences = Lists.newArrayList();
					for(org.bson.Document occDoc:occurrenceCollection.find(Filters.eq(TERM_ID,term.getId()))) {
						occurrences.add(new TermOccurrence(
								term, 
								occDoc.getString(COVERED_TEXT), 
								documentCache.getUnchecked(occDoc.getInteger(DOC_ID)), 
								occDoc.getInteger(BEGIN),
								occDoc.getInteger(END)
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
		MutableInt mutableInt = termsBuffer.get(term);
		if(mutableInt == null)
			termsBuffer.put(term, new MutableInt(1));
		else
			mutableInt.increment();
		occurrencesBuffer.add(e);
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
		return mongoDBUri.getURI();
	}

	
	@Override
	public void flush() {

		// bulk write occurrences
		final List<org.bson.Document> occDocuments = Lists.newArrayListWithCapacity(occurrencesBuffer.size());
		for(TermOccurrence o:this.occurrencesBuffer) {
			
			occDocuments.add(new org.bson.Document()
					.append(TERM_ID, o.getTerm().getId())
					.append(DOC_ID, o.getSourceDocument().getId())
					.append(BEGIN, o.getBegin())
					.append(END, o.getEnd())
					.append(COVERED_TEXT, o.getCoveredText())
				);
		}
		if(!occurrencesBuffer.isEmpty())
			executor.execute(new Runnable(){
					public void run() {
						occurrenceCollection.insertMany(occDocuments);
				}
			});

		
		// bulk write documents
		final List<WriteModel<org.bson.Document>> documentUrlsOps = Lists.newArrayListWithCapacity(documentsUrls.size());
		for(Map.Entry<Integer, String> e:this.documentsUrls.entrySet()) {
			UpdateOneModel<org.bson.Document> w = new UpdateOneModel<org.bson.Document>(
					Filters.eq(_ID, e.getKey()),
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
		
		
		// bulk write terms
		final List<WriteModel<org.bson.Document>> termsOps = Lists.newArrayList();		
		for(Term t:termsBuffer.keySet()) {
			UpdateOneModel<org.bson.Document> w = new UpdateOneModel<org.bson.Document>(
					Filters.eq(_ID, t.getId()), 
					Updates.inc(FREQUENCY, termsBuffer.get(t).intValue()),
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
		LOGGER.info("Indexing the occurrence store");
		this.state = State.INDEXING;
		flush();
		sync();
		LOGGER.debug("Removing orphan occurrences");
		Set<Integer> tids = Sets.newHashSet();
		for(org.bson.Document term:termCollection.find()) 
			tids.add(term.getInteger(_ID));
		occurrenceCollection.deleteMany(Filters.nin(TERM_ID, tids));
		LOGGER.debug("creating index occurrences.{}", TERM_ID);
		occurrenceCollection.createIndex(new org.bson.Document().append(TERM_ID, 1));
		LOGGER.debug("Created");
		monitor.shutdown();
		this.state = State.INDEXED;
	}

	@Override
	public void removeTerm(final Term t) {
		executor.execute(new Runnable(){
			public void run() {
				termCollection.deleteOne(new org.bson.Document(_ID, t.getId()));
				occurrenceCollection.deleteMany(Filters.eq(_ID, t.getId()));
			}
		});
	}
	
	@Override
	public void close() {
		sync();
		monitor.shutdown();
		mongoClient.close();
		executor.shutdown();
	}

	@Override
	public void deleteMany(TermSelector selector) {
		if (selector instanceof FrequencyUnderThreshholdSelector) {
			FrequencyUnderThreshholdSelector selector2 = (FrequencyUnderThreshholdSelector) selector;
			sync();
			Stopwatch sw = Stopwatch.createStarted();
			termCollection.deleteMany(Filters.lt(FREQUENCY, selector2.getThreshhold()));
			LOGGER.debug("Terms deleted in MongoDB in {}ms", sw.elapsed(TimeUnit.MILLISECONDS));
			
		}
	}

	private void sync() {
		LOGGER.info("Synchronizing with executor and mongoDB server");
		monitor.log();
		LOGGER.debug("Waiting for executor to finished queued tasks");
		Stopwatch sw = Stopwatch.createStarted();
		executor.sync();
		LOGGER.debug("Executor synchronized in {}ms", sw.elapsed(TimeUnit.MILLISECONDS));
		monitor.log();
		sw = Stopwatch.createStarted();
		LOGGER.debug("Synchronizing with MongoDB server");
		mongoClient.fsync(false);
		LOGGER.debug("MongoDB synchronized in {}ms", sw.elapsed(TimeUnit.MILLISECONDS));
	}
}
