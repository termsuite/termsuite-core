package fr.univnantes.termsuite.framework.service;

import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;

import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Sets;

import fr.univnantes.termsuite.api.TermSuiteException;
import fr.univnantes.termsuite.framework.InjectLogger;
import fr.univnantes.termsuite.index.TermIndex;
import fr.univnantes.termsuite.index.TermIndexType;
import fr.univnantes.termsuite.index.Terminology;
import fr.univnantes.termsuite.model.Term;

public class IndexService {

	@InjectLogger 
	Logger logger;
	
	Terminology terminology;
	
	private Stopwatch indexingSW = Stopwatch.createUnstarted();

	private ConcurrentMap<TermIndexType, TermIndex> termIndexes = new ConcurrentHashMap<>();
	
	public IndexService(Terminology terminology) {
		super();
		this.terminology = terminology;
	}

	public void addTerm(Term term) {
		for(TermIndex termIndex:termIndexes.values())
			termIndex.addToIndex(term);
	}

	public TermIndex getIndex(TermIndexType indexType) {
		if(!this.termIndexes.containsKey(indexType))
			createIndex(indexType);
		return this.termIndexes.get(indexType);
	}
	
	private TermIndex createIndex(TermIndexType indexType) {
		Preconditions.checkArgument(!termIndexes.containsKey(indexType),
				"TermIndex %s already created", indexType);
		logger.debug("Creating TermIndex {}", indexType);
		indexingSW.start();
		TermIndex termIndex;
		try {
			termIndex = new TermIndex(indexType.getProviderClass().newInstance());
		} catch (InstantiationException | IllegalAccessException e) {
			indexingSW.stop();
			throw new TermSuiteException(e);
		}
		this.termIndexes.put(indexType, termIndex);

		for(Term t:this.terminology.getTerms().values()) 
			termIndex.addToIndex(t);
		indexingSW.stop();
		return termIndex;
	}

	public void dropIndex(TermIndexType indexType) {
		logger.debug("Dropping TermIndex {}", indexType);
		this.termIndexes.remove(indexType);
	}
	
	public void removeTerm(Term term) {
		for(TermIndex termIndex:termIndexes.values())
			termIndex.removeTerm(term);
	}

	public Stopwatch getIndexingTime() {
		return indexingSW;
	}
	
	public void cleanEntriesByMaxSize(TermIndexType termIndexType, int maxSize) {
		TermIndex termIndex = checkIndexCreated(termIndexType);

		String msg = "Index entry {} had too many elements. Applied th={} filter. Before -> after filtering: {} -> {}";
		int th;
		Iterator<Term> it;
		Term t;
		int initialSize;
		for(String key:termIndex.getClasses().keySet()) {
			th = 1;
			initialSize = termIndex.getClasses().get(key).size();
			while (termIndex.getClasses().get(key).size() > maxSize) {
				th++;
				it = termIndex.getClasses().get(key).iterator();
				while(it.hasNext()) {
					t = it.next();
					if(t.getFrequency()<th)
						it.remove();
				}
			}
			if(th>1) {
				logger.warn(msg,
						key,
						th,
						initialSize,
						termIndex.getClasses().get(key).size()
						);
			}
		}
	}

	public void dropBiggerEntries(TermIndexType termIndexType, int threshholdSize) {
		TermIndex termIndex = checkIndexCreated(termIndexType);
		Set<String> toRemove = Sets.newHashSet();
		for(String key:termIndex.getClasses().keySet()) {
			if(termIndex.getClasses().get(key).size() >= threshholdSize)
				toRemove.add(key);
		}
		for(String rem:toRemove) {
			logger.warn("Removing key {} from custom index {} because its size {} is bigger than the threshhold {}",
					rem,
					termIndexType,
					termIndex.getClasses().get(rem).size(),
					threshholdSize);
			termIndex.getClasses().removeAll(rem);
		}
	}

	public TermIndex checkIndexCreated(TermIndexType termIndexType) {
		Preconditions.checkState(this.termIndexes.containsKey(termIndexType), "Index %s not created", termIndexType);
		return this.termIndexes.get(termIndexType);
	}

	public void cleanSingletonKeys(TermIndex termIndex) {
		Iterator<String> it = termIndex.getClasses().keySet().iterator();
		while(it.hasNext())
			if(termIndex.getClasses().get(it.next()).size() == 1)
				it.remove();
	}

}
