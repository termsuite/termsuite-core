package fr.univnantes.termsuite.export;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import fr.univnantes.termsuite.api.TermSuiteException;
import fr.univnantes.termsuite.engines.gatherer.VariationType;
import fr.univnantes.termsuite.model.RelationProperty;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.TermProperty;
import fr.univnantes.termsuite.model.TermRelation;
import fr.univnantes.termsuite.model.Terminology;

public class VariationExporter {
	
	private static final String SOURCE_LINE_FORMAT = "%-30s f=%-3d";
	private static final String EMPTY_LINE_FORMAT = "%-36s";

	private static final String TARGET_LINE_FORMAT = " [%s] %-30s {f=%d,%s}%n";

	private Terminology termino;
	private Writer writer;
	private List<VariationType> variationTypes;
	
	private VariationExporter(Terminology termino, Writer writer, List<VariationType> variationTypes) {
		super();
		this.termino = termino;
		this.writer = writer;
		this.variationTypes = Lists.newArrayList(variationTypes);
	}

	public static void export(Terminology termino, Writer writer, VariationType... variationTypes) {
		new VariationExporter(termino, writer, Lists.newArrayList(variationTypes)).doExport();
	}

	public static void export(Terminology termino, Writer writer, List<VariationType> variationTypes) {
		new VariationExporter(termino, writer, variationTypes).doExport();
	}

	public void doExport() {
		try {
			Multimap<Term,TermRelation> acceptedVariations = HashMultimap.create();
			for(Term t:termino.getTerms().values()) {
				for(TermRelation v:termino.getOutboundRelations(t)) {
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
				}
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
