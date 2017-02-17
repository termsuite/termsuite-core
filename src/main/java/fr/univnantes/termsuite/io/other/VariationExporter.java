package fr.univnantes.termsuite.io.other;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import fr.univnantes.termsuite.api.TermSuiteException;
import fr.univnantes.termsuite.engines.gatherer.VariationType;
import fr.univnantes.termsuite.framework.Export;
import fr.univnantes.termsuite.framework.service.TermService;
import fr.univnantes.termsuite.framework.service.TerminologyService;
import fr.univnantes.termsuite.model.Relation;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.TermProperty;

public class VariationExporter {
	
	private static final String SOURCE_LINE_FORMAT = "%-30s f=%-3d";
	private static final String EMPTY_LINE_FORMAT = "%-36s";

	private static final String TARGET_LINE_FORMAT = " [%s] %-30s {f=%d,%s}%n";
	
	private EnumSet<VariationType> variationTypes;
	
	public VariationExporter(Collection<VariationType> variationTypes) {
		super();
		this.variationTypes = EnumSet.copyOf(variationTypes);
	}

	@Export
	public void export(TerminologyService termino, Writer writer) {
		try {
			Multimap<Term,Relation> acceptedVariations = HashMultimap.create();
			for(TermService t:termino.getTerms()) {
				t.outboundRelations().forEach(v -> {
					if(this.variationTypes.isEmpty())
						acceptedVariations.put(t.getTerm(), v.getRelation());
					else {
						if(variationTypes
							.stream()
							.filter(vType -> v.isVariationOfType(vType))
							.findAny()
							.isPresent()) 
						acceptedVariations.put(t.getTerm(), v.getRelation());
					}
				});
			}
			
			Set<Term> sortedTerms = new TreeSet<Term>(TermProperty.SPECIFICITY.getComparator(
					true));
			sortedTerms.addAll(acceptedVariations.keySet());
			
			for(Term t:sortedTerms) {
				Set<Relation> variations = Sets.newHashSet(acceptedVariations.get(t));
				boolean first = true;
				for(Relation tv:variations) {
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

	public String propertiesToString(Relation tv) {
		return tv.getProperties().entrySet().stream()
				.map(e -> String.format("%s=%s", e.getKey().getShortName(), e.getValue()))
				.collect(Collectors.joining(", "));
	}

}
