package fr.univnantes.termsuite.export.other;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import fr.univnantes.termsuite.api.TermSuiteException;
import fr.univnantes.termsuite.engines.gatherer.VariationType;
import fr.univnantes.termsuite.export.TerminologyExporter;
import fr.univnantes.termsuite.framework.service.TerminologyService;
import fr.univnantes.termsuite.model.RelationProperty;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.TermProperty;
import fr.univnantes.termsuite.model.TermRelation;

public class VariationExporter implements TerminologyExporter {
	
	private static final String SOURCE_LINE_FORMAT = "%-30s f=%-3d";
	private static final String EMPTY_LINE_FORMAT = "%-36s";

	private static final String TARGET_LINE_FORMAT = " [%s] %-30s {f=%d,%s}%n";

	@Inject
	private TerminologyService termino;
	
	@Inject
	private Writer writer;
	
	@Inject
	@Named(value="variationTypes")
	private List<VariationType> variationTypes;
	
	public void export() {
		try {
			Multimap<Term,TermRelation> acceptedVariations = HashMultimap.create();
			for(Term t:termino.getTerms()) {
				termino.outboundRelations(t).forEach(v -> {
					if(this.variationTypes.isEmpty())
							acceptedVariations.put(t, v);
					else {
						if(variationTypes
							.stream()
							.filter(vType -> 
								v.isPropertySet(vType.getRelationProperty()) && v.getPropertyBooleanValue(vType.getRelationProperty())
								|| v.isPropertySet(RelationProperty.VARIATION_TYPE) && v.get(RelationProperty.VARIATION_TYPE) == vType
							).findAny()
							.isPresent()) 
						acceptedVariations.put(t, v);
					}
				});
			}
			
			Set<Term> sortedTerms = new TreeSet<Term>(TermProperty.SPECIFICITY.getComparator(
					true));
			sortedTerms.addAll(acceptedVariations.keySet());
			
			for(Term t:sortedTerms) {
				Set<TermRelation> variations = Sets.newHashSet(acceptedVariations.get(t));
				boolean first = true;
				for(TermRelation tv:variations) {
					if(first)
						writer.write(String.format(SOURCE_LINE_FORMAT,
							t.getGroupingKey(),
							t.getFrequency()));
					else
						writer.write(String.format(EMPTY_LINE_FORMAT, ""));
					writer.write(String.format(TARGET_LINE_FORMAT,
							tv.getType(),
							tv.getTo().getGroupingKey(),
							tv.getTo().getFrequency(),
							propertiesToString(tv)
							));
					first = false;
				}
			}
		} catch (IOException e) {
			throw new TermSuiteException(e);
		}		

	}

	public String propertiesToString(TermRelation tv) {
		return tv.getProperties().entrySet().stream()
				.map(e -> String.format("%s=%s", e.getKey().getShortName(), e.getValue()))
				.collect(Collectors.joining(", "));
	}

}
