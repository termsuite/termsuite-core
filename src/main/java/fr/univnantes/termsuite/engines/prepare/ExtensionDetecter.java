package fr.univnantes.termsuite.engines.prepare;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;

import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;

import fr.univnantes.termsuite.framework.InjectLogger;
import fr.univnantes.termsuite.framework.TerminologyEngine;
import fr.univnantes.termsuite.model.RelationType;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.TermRelation;
import fr.univnantes.termsuite.model.termino.CustomTermIndex;
import fr.univnantes.termsuite.model.termino.TermIndexes;
import fr.univnantes.termsuite.model.termino.TermValueProviders;
import fr.univnantes.termsuite.utils.TermHistory;
import fr.univnantes.termsuite.utils.TermUtils;

public class ExtensionDetecter extends TerminologyEngine {
	private static final int WARNING_CRITICAL_SIZE = 10000;
	@InjectLogger Logger logger;

	private Optional<TermHistory> history = Optional.empty();
	
	public ExtensionDetecter setHistory(TermHistory history) {
		this.history = Optional.of(history);
		return this;
	}
	
	
	@Override
	public void execute() {
		logger.info("Detecting extensions on terminology");
		if(terminology.getTerms().isEmpty())
			return;

		Stopwatch sw = Stopwatch.createStarted();
		
		setSize1Extensions();
		setSize2Extensions();
		logger.debug("Extensions detected in {}", sw);

	}

	public void setSize1Extensions() {
		CustomTermIndex swtIndex = terminology.getTerminology().createCustomIndex(
				TermIndexes.SWT_GROUPING_KEYS,
				TermValueProviders.get(TermIndexes.SWT_GROUPING_KEYS));
		
		logger.debug("Detecting size-1 extensions");
		for (String swtGroupingKey : swtIndex.keySet()) {
			Term swt = terminology.getTerm(swtGroupingKey);
			for(Term term:swtIndex.getTerms(swtGroupingKey)) {
				if(swt.equals(term) || term.getWords().size() == 1)
					continue;
				else {
					addExtensionRelationIfNotExisting(swt, term);
				}
			}
		}
		
		
		terminology.getTerminology().dropCustomIndex(TermIndexes.SWT_GROUPING_KEYS);
	}


	private static final String MSG_BAD_EXTENSION = "Bad extension format. Require from.size < to.size. Got from: \"%s\" and to: \"%s\"";
	public void addExtensionRelationIfNotExisting(Term from, Term to) {
		if(!terminology.extensions(from, to)
				.findAny().isPresent()) {
			Preconditions.checkArgument(from.getWords().size() < to.getWords().size(), 
				MSG_BAD_EXTENSION,
				from, to
			);
			
			terminology.addRelation(new TermRelation(
					RelationType.HAS_EXTENSION,
					from, 
					to
					));
			watch(from, to);
		}
	}

	public void setSize2Extensions() {
		logger.debug("Detecting size-2 (and more) extensions");

		String gatheringKey = TermIndexes.ALLCOMP_PAIRS;
		CustomTermIndex customIndex = terminology.getTerminology().createCustomIndex(
				gatheringKey,
				TermValueProviders.get(gatheringKey));
		logger.debug("Rule-based gathering over {} classes", customIndex.size());

		// clean singleton classes
		logger.debug("Cleaning singleton keys");
		customIndex.cleanSingletonKeys();

		// clean biggest classes
		customIndex.dropBiggerEntries(WARNING_CRITICAL_SIZE, true);
		
		Term t1;
		Term t2;
		for (String cls : customIndex.keySet()) {
			List<Term> list = customIndex.getTerms(cls);
			for(int i = 0; i< list.size(); i++) {
				t1 = list.get(i);
				for(int j = i+1; j< list.size(); j++) {
					t2 = list.get(j);
					if(TermUtils.isIncludedIn(t1, t2)) {
						addExtensionRelationIfNotExisting(t1, t2);

					} else if(TermUtils.isIncludedIn(t2, t1)) {
						addExtensionRelationIfNotExisting(t2, t1);
					}
				}
			}
		}
		//finalize
		terminology.getTerminology().dropCustomIndex(gatheringKey);
	}

	private void watch(Term t1, Term t2) {
		if(history.isPresent()) {
			if(this.history.get().isGKeyWatched(t1.getGroupingKey()))
				this.history.get().saveEvent(
						t1.getGroupingKey(),
						this.getClass(), 
						"Term has a new extension: " + t2);
	
			if(this.history.get().isGKeyWatched(t2.getGroupingKey()))
				this.history.get().saveEvent(
						t2.getGroupingKey(),
						this.getClass(), 
						"Term is the extension of " + t1);
		}
	}

}
