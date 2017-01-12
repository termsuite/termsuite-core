package fr.univnantes.termsuite.engines.prepare;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;

import fr.univnantes.termsuite.framework.TerminologyEngine;
import fr.univnantes.termsuite.framework.TerminologyService;
import fr.univnantes.termsuite.model.RelationProperty;
import fr.univnantes.termsuite.model.RelationType;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.TermRelation;
import fr.univnantes.termsuite.model.termino.CustomTermIndex;
import fr.univnantes.termsuite.model.termino.TermIndexes;
import fr.univnantes.termsuite.model.termino.TermValueProviders;
import fr.univnantes.termsuite.utils.TermHistory;
import fr.univnantes.termsuite.utils.TermUtils;

public class ExtensionDetecter extends TerminologyEngine {
	private static final Logger LOGGER = LoggerFactory.getLogger(ExtensionDetecter.class);
	private static final int WARNING_CRITICAL_SIZE = 10000;

	private Optional<TermHistory> history = Optional.empty();
	
	public ExtensionDetecter setHistory(TermHistory history) {
		this.history = Optional.of(history);
		return this;
	}
	
	public void detectExtensions(TerminologyService terminologyService) {
		LOGGER.info("Detecting extensions on terminology");
		if(terminologyService.getTerms().isEmpty())
			return;

		Stopwatch sw = Stopwatch.createStarted();
		
		setSize1Extensions(terminologyService);
		setSize2Extensions(terminologyService);
		setIsExtensionProperty(terminologyService);
		LOGGER.debug("Extensions detected in {}", sw);

	}

	public void setIsExtensionProperty(TerminologyService termino) {
		termino
			.relations()
			.forEach(relation -> {
				if(relation.getType() == RelationType.HAS_EXTENSION)
					relation.setProperty(RelationProperty.IS_EXTENSION, true);
				else {
					boolean isExtension = termino
						.extensions(relation.getFrom(), relation.getTo())
						.findAny().isPresent();
					relation.setProperty(
							RelationProperty.IS_EXTENSION,
							isExtension);
				}
			});
		
	}


	public void setSize1Extensions(TerminologyService termino) {
		CustomTermIndex swtIndex = termino.getTerminology().createCustomIndex(
				TermIndexes.SWT_GROUPING_KEYS,
				TermValueProviders.get(TermIndexes.SWT_GROUPING_KEYS));
		
		LOGGER.debug("Detecting size-1 extensions");
		for (String swtGroupingKey : swtIndex.keySet()) {
			Term swt = termino.getTerm(swtGroupingKey);
			for(Term term:swtIndex.getTerms(swtGroupingKey)) {
				if(swt.equals(term) || term.getWords().size() == 1)
					continue;
				else {
					addExtensionRelationIfNotExisting(termino, swt, term);
				}
			}
		}
		
		
		termino.getTerminology().dropCustomIndex(TermIndexes.SWT_GROUPING_KEYS);
	}


	private static final String MSG_BAD_EXTENSION = "Bad extension format. Require from.size < to.size. Got from: \"%s\" and to: \"%s\"";
	public void addExtensionRelationIfNotExisting(TerminologyService termino, Term from, Term to) {
		if(!termino.extensions(from, to)
				.findAny().isPresent()) {
			Preconditions.checkArgument(from.getWords().size() < to.getWords().size(), 
				MSG_BAD_EXTENSION,
				from, to
			);
			
			termino.addRelation(new TermRelation(
					RelationType.HAS_EXTENSION,
					from, 
					to
					));
			watch(from, to);
		}
	}

	public void setSize2Extensions(TerminologyService termino) {
		LOGGER.debug("Detecting size-2 (and more) extensions");

		String gatheringKey = TermIndexes.ALLCOMP_PAIRS;
		CustomTermIndex customIndex = termino.getTerminology().createCustomIndex(
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
						addExtensionRelationIfNotExisting(termino, t1, t2);

					} else if(TermUtils.isIncludedIn(t2, t1)) {
						addExtensionRelationIfNotExisting(termino, t2, t1);
					}
				}
			}
		}
		//finalize
		termino.getTerminology().dropCustomIndex(gatheringKey);
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
