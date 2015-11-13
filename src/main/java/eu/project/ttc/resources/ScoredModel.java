package eu.project.ttc.resources;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.uima.resource.DataResource;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.SharedResourceObject;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import eu.project.ttc.models.Term;
import eu.project.ttc.models.TermIndex;
import eu.project.ttc.models.TermVariation;
import eu.project.ttc.models.scored.ScoredTerm;
import eu.project.ttc.models.scored.ScoredVariation;

public class ScoredModel implements SharedResourceObject {
	public static final String SCORED_MODEL = "ScoredModel";
	
	private List<ScoredTerm> terms;
	private Map<Term, ScoredTerm> adapters;
	private TermIndex termIndex;
	
	@Override
	public void load(DataResource aData) throws ResourceInitializationException {
	}
	
	public void importTermIndex(TermIndex termIndex) {
		adapters = Maps.newHashMap();
		this.terms = Lists.newLinkedList();
		for(Term t:termIndex.getTerms()) {
			List<ScoredVariation> scoredVariations  = Lists.newArrayListWithExpectedSize(t.getVariations().size());
			ScoredTerm st = new ScoredTerm(this, t);
			adapters.put(t, st);
			for(TermVariation tv:t.getVariations()) {
				ScoredVariation stv = new ScoredVariation(this, tv);
				scoredVariations.add(stv);
			}
			st.setVariations(scoredVariations);
			this.terms.add(st);
		}
		this.termIndex = termIndex;
	}

	public Collection<ScoredTerm> getTerms() {
		return Collections.unmodifiableCollection(this.terms);
	}

	public void sort(Comparator<ScoredTerm> compoarator) {
		Collections.sort(this.terms, compoarator);
	}

	public ScoredTerm getAdapter(Term t) {
		if(adapters.containsKey(t))
			return adapters.get(t);
		else
			throw new IllegalStateException("No such adapter for term: " + t);
	}

	public TermIndex getTermIndex() {
		return termIndex;
	}

	public void removeTerms(Set<ScoredTerm> rem) {
		Iterator<ScoredTerm> it = this.terms.iterator();
		ScoredTerm cur;
		while(it.hasNext()) {
			cur = it.next();
			if(rem.contains(cur))
				it.remove();
			else {
				Iterator<ScoredVariation> vIt = cur.getVariations().iterator();
				while(vIt.hasNext()) {
					if(rem.contains(vIt.next().getVariant()))
						vIt.remove();
				}
			}
		}
		for(ScoredTerm st:rem)
			adapters.remove(st.getTerm());
	}
}
