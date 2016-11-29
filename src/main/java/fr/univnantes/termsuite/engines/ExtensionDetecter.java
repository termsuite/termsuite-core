package fr.univnantes.termsuite.engines;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.univnantes.termsuite.model.RelationProperty;
import fr.univnantes.termsuite.model.RelationType;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.TermIndex;
import fr.univnantes.termsuite.model.TermRelation;
import fr.univnantes.termsuite.model.termino.CustomTermIndex;
import fr.univnantes.termsuite.model.termino.TermIndexes;
import fr.univnantes.termsuite.model.termino.TermValueProviders;
import fr.univnantes.termsuite.utils.TermHistory;
import fr.univnantes.termsuite.utils.TermUtils;

public class ExtensionDetecter {
	private static final Logger LOGGER = LoggerFactory.getLogger(ExtensionDetecter.class);
	private static final int WARNING_CRITICAL_SIZE = 10000;

	private Optional<TermHistory> history = Optional.empty();
	
	public ExtensionDetecter setHistory(TermHistory history) {
		this.history = Optional.of(history);
		return this;
	}
	
	
	public void detectExtensions(TermIndex termIndex) {
		LOGGER.info("Detecting extensions on term index {}", termIndex.getName());
		if(termIndex.getTerms().isEmpty())
			return;

		setSize1Extensions(termIndex);
		setSize2Extensions(termIndex);
		setIsExtensionProperty(termIndex);
	}

	private void setIsExtensionProperty(TermIndex termIndex) {
		termIndex
			.getRelations()
			.forEach(relation -> {
				if(relation.getType() == RelationType.HAS_EXTENSION)
					relation.setProperty(RelationProperty.IS_EXTENSION, true);
				else {
					boolean isExtension = termIndex
						.getRelations(relation.getFrom(), relation.getTo(), RelationType.HAS_EXTENSION)
						.findAny().isPresent();
					relation.setProperty(
							RelationProperty.IS_EXTENSION,
							isExtension);
				}
			});
		
	}


	public void setSize1Extensions(TermIndex termIndex) {
		CustomTermIndex swtIndex = termIndex.createCustomIndex(
				TermIndexes.SWT_GROUPING_KEYS,
				TermValueProviders.get(TermIndexes.SWT_GROUPING_KEYS));
		
		for (String swtGroupingKey : swtIndex.keySet()) {
			Term swt = termIndex.getTermByGroupingKey(swtGroupingKey);
			for(Term term:swtIndex.getTerms(swtGroupingKey)) {
				if(swt.equals(term))
					continue;
				else {
					addExtensionRelationIfNotExisting(termIndex, swt, term);
				}
			}
		}
		
		
		termIndex.dropCustomIndex(TermIndexes.SWT_GROUPING_KEYS);
		
	}


	public void addExtensionRelationIfNotExisting(TermIndex termIndex, Term from, Term to) {
		if(!termIndex.getRelations(from, to, RelationType.HAS_EXTENSION).findAny().isPresent())
			termIndex.addRelation(new TermRelation(
					RelationType.HAS_EXTENSION,
					from, 
					to
				));
	}

	public void setSize2Extensions(TermIndex termIndex) {
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
						addExtensionRelationIfNotExisting(termIndex, t1, t2);
						watch(t1, t2);

					} else if(TermUtils.isIncludedIn(t2, t1)) {
						addExtensionRelationIfNotExisting(termIndex, t2, t1);
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
