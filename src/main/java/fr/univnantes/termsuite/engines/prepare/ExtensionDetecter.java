package fr.univnantes.termsuite.engines.prepare;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;

import com.google.common.base.Stopwatch;

import fr.univnantes.termsuite.engines.SimpleEngine;
import fr.univnantes.termsuite.framework.Index;
import fr.univnantes.termsuite.framework.InjectLogger;
import fr.univnantes.termsuite.framework.service.TermService;
import fr.univnantes.termsuite.index.TermIndex;
import fr.univnantes.termsuite.index.TermIndexType;
import fr.univnantes.termsuite.model.Relation;
import fr.univnantes.termsuite.model.RelationType;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.TermWord;
import fr.univnantes.termsuite.utils.TermHistory;
import fr.univnantes.termsuite.utils.TermUtils;

public class ExtensionDetecter extends SimpleEngine {
	private static final int WARNING_CRITICAL_SIZE = 10000;
	
	@InjectLogger Logger logger;

	@Index(type=TermIndexType.ALLCOMP_PAIRS)
	TermIndex allCompPairsIndex;

	private Optional<TermHistory> history = Optional.empty();
	
	public ExtensionDetecter setHistory(TermHistory history) {
		this.history = Optional.of(history);
		return this;
	}
	
	@Override
	public void execute() {
		if(terminology.getTerms().isEmpty())
			return;
		
		logger.debug("Detecting size-1 extensions");
		Stopwatch sw1 = Stopwatch.createStarted();
		AtomicInteger cnt = new AtomicInteger(0);
		setSize1Extensions(cnt);
		sw1.stop();
		logger.debug("{} size-1 extensions detected in {}ms", cnt, sw1.elapsed(TimeUnit.MILLISECONDS));
		
		logger.debug("Detecting size-2 extensions");
		Stopwatch sw2 = Stopwatch.createStarted();
		setSize2Extensions();
		sw2.stop();
		logger.debug("Size-2 extensions detected in {}ms", sw2.elapsed(TimeUnit.MILLISECONDS));
//		logger.debug("Total nb of extension relations: {}", terminology.relations(RelationType.HAS_EXTENSION).count());
	}
	
	public void setSize1Extensions(AtomicInteger cnt) {
		Set<Relation> relations = new HashSet<>();
		terminology.terms()
			.filter(t-> t.isMultiWord())
			.forEach(t-> {
				Optional<TermService> swt = Optional.empty(); 
				for(TermWord tw:t.getWords()) {
					if(tw.isSwt()) {
						swt = terminology.getSwt(tw);
						if(swt.isPresent()) {
							cnt.incrementAndGet();
							relations.add(new Relation(RelationType.HAS_EXTENSION, swt.get().getTerm(),t.getTerm()));
						}
					}
				}
			});
		watchAndAdd(relations);
	}
	
	public void setSize2Extensions() {
		logger.debug("Detecting size-2 (and more) extensions");

		logger.debug("Rule-based gathering over {} classes", allCompPairsIndex.size());
		Set<Relation> relations = new HashSet<>();

		Term t1;
		Term t2;
		for (String cls : allCompPairsIndex.keySet()) {
			List<Term> list = allCompPairsIndex.getTerms(cls);
			if(list.size() <= 1)
				continue;
			if(list.size() >= WARNING_CRITICAL_SIZE)
				continue;
			for(int i = 0; i< list.size(); i++) {
				t1 = list.get(i);
				for(int j = i+1; j< list.size(); j++) {
					t2 = list.get(j);
					if(TermUtils.isIncludedIn(t1, t2)) {
						relations.add(new Relation(RelationType.HAS_EXTENSION, t1,t2));
					} else if(TermUtils.isIncludedIn(t2, t1)) {
						relations.add(new Relation(RelationType.HAS_EXTENSION, t2,t1));
					}
				}
			}
		}
		watchAndAdd(relations);
	}


	public void watchAndAdd(Set<Relation> relations) {
		for(Relation r:relations)
			watch(r.getFrom(), r.getTo());
		terminology.addRelations(relations);
	}

	private void watch(Term t1, Term t2) {
		if(history.isPresent()) {
			if(this.history.get().isTermWatched(t1))
				this.history.get().saveEvent(
						t1,
						this.getClass(), 
						"Term has a new extension: " + t2);
	
			if(this.history.get().isTermWatched(t2))
				this.history.get().saveEvent(
						t2,
						this.getClass(), 
						"Term is the extension of " + t1);
		}
	}

}
