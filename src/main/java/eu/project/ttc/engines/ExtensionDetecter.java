package eu.project.ttc.engines;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.project.ttc.history.TermHistory;
import eu.project.ttc.models.RelationType;
import eu.project.ttc.models.Term;
import eu.project.ttc.models.TermIndex;
import eu.project.ttc.models.index.CustomTermIndex;
import eu.project.ttc.models.index.TermIndexes;
import eu.project.ttc.models.index.TermValueProviders;
import eu.project.ttc.utils.TermSuiteConstants;
import eu.project.ttc.utils.TermUtils;

public class ExtensionDetecter {
	private static final Logger LOGGER = LoggerFactory.getLogger(ExtensionDetecter.class);
	private static final int WARNING_CRITICAL_SIZE = 10000;

	private Optional<TermHistory> history = Optional.empty();
	
	public ExtensionDetecter setHistory(TermHistory history) {
		this.history = Optional.of(history);
		return this;
	}
	
	
	public void detectExtensions(TermIndex termIndex) {
		
		if(termIndex.getTerms().isEmpty())
			return;

		String gatheringKey = TermIndexes.WORD_COUPLE_LEMMA_LEMMA;
		CustomTermIndex customIndex = termIndex.createCustomIndex(
				gatheringKey,
				TermValueProviders.get(gatheringKey));
		LOGGER.debug("Rule-based gathering over {} classes", customIndex.size());

		// clean singleton classes
		LOGGER.debug("Cleaning singleton keys");
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
						termIndex.addRelation(t1, t2, RelationType.HAS_EXTENSION, TermSuiteConstants.EMPTY_STRING);
						watch(t1, t2);

					} else if(TermUtils.isIncludedIn(t2, t1)) {
						termIndex.addRelation(t2, t1, RelationType.HAS_EXTENSION, TermSuiteConstants.EMPTY_STRING);
						watch(t2, t1);
					}
				}
			}
		}
		
		//finalize
		termIndex.dropCustomIndex(gatheringKey);

	}

	private void watch(Term t1, Term t2) {
		if(history.isPresent()) {
			if(this.history.get().isWatched(t1.getGroupingKey()))
				this.history.get().saveEvent(
						t1.getGroupingKey(),
						this.getClass(), 
						"Term has a new extension: " + t2);
	
			if(this.history.get().isWatched(t2.getGroupingKey()))
				this.history.get().saveEvent(
						t2.getGroupingKey(),
						this.getClass(), 
						"Term is the extension of " + t1);
		}
	}

}
