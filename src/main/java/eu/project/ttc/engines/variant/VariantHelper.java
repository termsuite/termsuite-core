package eu.project.ttc.engines.variant;

import java.util.Iterator;

import com.google.common.base.Objects;

import eu.project.ttc.models.GroovyWord;
import eu.project.ttc.models.Term;
import eu.project.ttc.models.TermIndex;
import eu.project.ttc.models.TermVariation;
import eu.project.ttc.models.VariationType;
import eu.project.ttc.utils.TermUtils;

public class VariantHelper {

	private TermIndex termIndex;
	
	public VariantHelper() {
		super();
	}
	
	void setTermIndex(TermIndex termIndex) {
		this.termIndex = termIndex;
	}

	
	public boolean derivesInto(String derivationPattern, GroovyWord s, GroovyWord t) {
		Term sourceTerm = toTerm(s);
		if(sourceTerm == null)
			return false;
		Term targetTerm = toTerm(t);
		if(targetTerm == null)
			return false;
		
		TermVariation tv;
		for(Iterator<TermVariation> it = sourceTerm.getVariations(VariationType.DERIVES_INTO).iterator()
				; it.hasNext() 
				; ) {
			tv = it.next();
			if(tv.getVariant().equals(targetTerm)) {
				if(Objects.equal(tv.getInfo(), derivationPattern))
					return true;
			}
		}
		
		return false;
	}
	
	public boolean isPrefixOf(GroovyWord s, GroovyWord t) {
		Term sourceTerm = toTerm(s);
		if(sourceTerm == null)
			return false;
		Term targetTerm = toTerm(t);
		if(targetTerm == null)
			return false;
		
		TermVariation tv;
		for(Iterator<TermVariation> it = sourceTerm.getVariations(VariationType.IS_PREFIX_OF).iterator()
				; it.hasNext() 
				; ) {
			tv = it.next();
			if(tv.getVariant().equals(targetTerm)) {
				return true;
			}
		}
		
		return false;
	}

	private Term toTerm(GroovyWord s) {
		String sourceGroupingKey = TermUtils.toGroupingKey(s.getTermWord());
		Term sourceTerm = this.termIndex.getTermByGroupingKey(sourceGroupingKey);
		return sourceTerm;
	}
}
