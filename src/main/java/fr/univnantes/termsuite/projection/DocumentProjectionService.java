package fr.univnantes.termsuite.projection;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import com.google.common.util.concurrent.AtomicDouble;

import fr.univnantes.termsuite.framework.service.RelationService;
import fr.univnantes.termsuite.framework.service.TermService;
import fr.univnantes.termsuite.framework.service.TerminologyService;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.TermProperty;

public class DocumentProjectionService {

	private DocumentProjection projection;
	
	public DocumentProjectionService(DocumentProjection projection) {
		super();
		this.projection = projection;
	}

	public double getProjectionScore(TerminologyService referenceTerminology) {
		AtomicDouble sum = new AtomicDouble(0);
		AtomicDouble total = new AtomicDouble(0);
		List<Term> top100 = topN(100).collect(toList());
		for(Term docTerm :top100) {
			total.addAndGet(docTerm.getSpecificity());
			int baseRank = getBaseRankInRefTermino(referenceTerminology, docTerm);
			if(baseRank > 0 && baseRank < 500)
				sum.addAndGet(docTerm.getSpecificity());
		}
		return sum.doubleValue() / total.doubleValue();
	}

	public Stream<Term> topN(int n) {
		return getTerms().stream().limit(n);
	}
	
	public int getRankInRefTermino(TerminologyService referenceTerminology, Term documentTerm) {
		if(referenceTerminology.containsTerm(documentTerm.getGroupingKey())) {
			return referenceTerminology.getTerm(documentTerm.getGroupingKey()).getRank();
		} else 
			return -1;
	}

	public int getBaseRankInRefTermino(TerminologyService referenceTerminology, Term documentTerm) {
		int baseRank = getRankInRefTermino(referenceTerminology, documentTerm);
		if(baseRank != -1) {
			return referenceTerminology.getTerm(documentTerm.getGroupingKey())
				.variationBases()
				.map(RelationService::getFrom)
				.mapToInt(TermService::getRank)
				.min()
				.orElse(baseRank);
		} else
			return baseRank;
	}
	
	
	private List<Term> terms = null;
	public List<Term> getTerms() {
		if(terms == null) {
			terms = new ArrayList<>(this.projection.getTerms().size());
			terms.addAll(projection.getTerms().values());
			Collections.sort(terms, TermProperty.SPECIFICITY.getComparator(true));
		}
		return terms;
	}

	public Stream<Term> documentTerms() {
		return getTerms().stream();
	}

	public TerminologyService projectedTerminology(TerminologyService referenceTerminology) {
		TerminologyService projection = referenceTerminology.cloneTerminology();
		projection.removeTerms(getTerms());
		return projection;
	}
	public Stream<Term> documentTermsFoundInTerminology(TerminologyService referenceTerminology) {
		return getTerms().stream()
					.filter(docTerm -> referenceTerminology.containsTerm(docTerm.getGroupingKey()));
	}

	public Stream<Term> documentTermsNotFoundInTerminology(TerminologyService referenceTerminology) {
		return getTerms().stream()
					.filter(docTerm -> !referenceTerminology.containsTerm(docTerm.getGroupingKey()));
	}
}
