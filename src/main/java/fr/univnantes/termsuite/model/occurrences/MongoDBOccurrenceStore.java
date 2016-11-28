
/*******************************************************************************
 * Copyright 2015-2016 - CNRS (Centre National de Recherche Scientifique)
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *******************************************************************************/

package fr.univnantes.termsuite.model.occurrences;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.BulkWriteOptions;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOneModel;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import com.mongodb.client.model.WriteModel;

import fr.univnantes.termsuite.model.Document;
import fr.univnantes.termsuite.model.Form;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.TermOccurrence;
import fr.univnantes.termsuite.model.termino.FrequencyUnderThreshholdSelector;
import fr.univnantes.termsuite.model.termino.TermSelector;

public class MongoDBOccurrenceStore extends AbstractMemoryOccStore {
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

	private static final String BEGIN = "ob";
	private static final String END = "oe";
	private static final String URL = "doc";


	protected static final String COVERED_TEXT = "t";

	private static final String FREQUENCY = "f";
	private static final String TERM_ID = "tid";

	private MongoClientURI mongoDBUri;

	private State state;
	
	private MongoCollection<org.bson.Document> termCollection;
	private MongoCollection<org.bson.Document> occurrenceCollection;
	
	private List<Object[]> occurrencesBuffer;
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

	private MongoClient mongoClient;


	private LoadingCache<Term, List<Form>> formCache = CacheBuilder.newBuilder()
			.maximumSize(1000)
			.build(new CacheLoader<Term, List<Form>>() {
				@Override
				public List<Form> load(Term key) throws Exception {
					Set<Form> forms = occurrenceCache.get(key).stream().map(TermOccurrence::getForm).collect(Collectors.toSet());
					List<Form> sortedForms = new ArrayList<>(forms);
					Collections.sort(sortedForms);
					return sortedForms;
				}
			});
			

	private LoadingCache<Term, List<TermOccurrence>> occurrenceCache = CacheBuilder.newBuilder()
			.maximumSize(1000)
			.build(new CacheLoader<Term, List<TermOccurrence>>() {
				@Override
				public List<TermOccurrence> load(Term term) throws Exception {
					List<Object[]> occurrences = Lists.newArrayList();
					FindIterable<org.bson.Document> find = occurrenceCollection.find(Filters.eq(TERM_ID,term.getGroupingKey()));
					
					Map<String, Form> forms = new HashMap<>();
					for(org.bson.Document occDoc:find) {
						occurrences.add(new Object[]{
							occDoc.getString(COVERED_TEXT), 
							MongoDBOccurrenceStore.this.protectedGetDocument(occDoc.getString(URL)), 
							occDoc.getInteger(BEGIN),
							occDoc.getInteger(END)
						});
					}
					
					List<TermOccurrence> list = occurrences.stream().map( occ -> {
						String text = (String)occ[0];
						int begin = (Integer)occ[1];
						int end = (Integer)occ[2];
						Document doc = (Document)occ[3];
						
						if(!forms.containsKey(text))
							forms.put(text, new Form(text));
						Form form = forms.get(text);
						form.setCount(1 + form.getCount());
						return new TermOccurrence(term, form, doc, begin, end);
					}).collect(Collectors.toList());

					return list;
				}

			});

	@Override
	public Collection<TermOccurrence> getOccurrences(Term term) {
		checkState(State.INDEXED);
		return occurrenceCache.getUnchecked(term);
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
		for(Object[] o:this.occurrencesBuffer) {
			
			occDocuments.add(new org.bson.Document()
					.append(TERM_ID, o[0])
					.append(URL, o[1])
					.append(BEGIN, o[2])
					.append(END, o[3])
					.append(COVERED_TEXT, o[4])
				);
		}
		if(!occurrencesBuffer.isEmpty())
			executor.execute(new Runnable(){
					public void run() {
						occurrenceCollection.insertMany(occDocuments);
				}
			});

		
		// bulk write terms
		final List<WriteModel<org.bson.Document>> termsOps = Lists.newArrayList();		
		for(Term t:termsBuffer.keySet()) {
			UpdateOneModel<org.bson.Document> w = new UpdateOneModel<org.bson.Document>(
					Filters.eq(_ID, t.getGroupingKey()), 
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
				termCollection.deleteOne(new org.bson.Document(_ID, t.getGroupingKey()));
				occurrenceCollection.deleteMany(Filters.eq(_ID, t.getGroupingKey()));
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


	@Override
	public List<Form> getForms(Term term) {
		return formCache.getUnchecked(term);
	}

	@Override
	public void addOccurrence(Term term, String documentUrl, int begin, int end, String coveredText) {
		checkState(State.COLLECTING);

		MutableInt mutableInt = termsBuffer.get(term);
		if(mutableInt == null)
			termsBuffer.put(term, new MutableInt(1));
		else
			mutableInt.increment();
		
		occurrencesBuffer.add(new Object[]{
				documentUrl,
				term.getGroupingKey(),
				begin,
				end,
				coveredText
		});

	}
}
