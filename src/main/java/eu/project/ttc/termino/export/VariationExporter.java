package eu.project.ttc.termino.export;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import eu.project.ttc.api.TermSuiteException;
import eu.project.ttc.engines.cleaner.TermProperty;
import eu.project.ttc.models.Term;
import eu.project.ttc.models.TermIndex;
import eu.project.ttc.models.TermVariation;
import eu.project.ttc.models.VariationType;

public class VariationExporter {
	
	private static final String SOURCE_LINE_FORMAT = "%-30s f=%-3d";
	private static final String EMPTY_LINE_FORMAT = "%-36s";

	private static final String TARGET_LINE_FORMAT = " %-25s %-30s f=%d %n";

	private TermIndex termIndex;
	private Writer writer;
	private List<VariationType> variationTypes;
	
	private VariationExporter(TermIndex termIndex, Writer writer, List<VariationType> variationTypes) {
		super();
		this.termIndex = termIndex;
		this.writer = writer;
		this.variationTypes = Lists.newArrayList(variationTypes);
	}

	public static void export(TermIndex termIndex, Writer writer, VariationType... variationTypes) {
		new VariationExporter(termIndex, writer, Lists.newArrayList(variationTypes)).doExport();
	}

	public static void export(TermIndex termIndex, Writer writer, List<VariationType> variationTypes) {
		new VariationExporter(termIndex, writer, variationTypes).doExport();
	}

	public void doExport() {
		try {
			Multimap<Term,TermVariation> acceptedVariations = HashMultimap.create();
			for(Term t:termIndex.getTerms()) {
				for(TermVariation v:termIndex.getOutboundTermVariations(t)) {
					if(this.variationTypes.isEmpty()
							|| this.variationTypes.contains(v.getVariationType()))
						acceptedVariations.put(t, v);
				}
			}
			
			Set<Term> sortedTerms = new TreeSet<Term>(TermProperty.SPECIFICITY.getComparator(
					this.termIndex, 
					true));
			sortedTerms.addAll(acceptedVariations.keySet());
			
			for(Term t:sortedTerms) {
				Set<TermVariation> variations = Sets.newHashSet(acceptedVariations.get(t));
				boolean first = true;
				for(TermVariation tv:variations) {
					if(first)
						writer.write(String.format(SOURCE_LINE_FORMAT, 
							t.getGroupingKey(),
							t.getFrequency()));
					else
						writer.write(String.format(EMPTY_LINE_FORMAT, ""));
					writer.write(String.format(TARGET_LINE_FORMAT,
							tv.getVariationType() + " ["+tv.getInfo()+"]",
							tv.getVariant().getGroupingKey(),
							tv.getVariant().getFrequency()
							));
					first = false;
				}
			}
		} catch (IOException e) {
			throw new TermSuiteException(e);
		}		

	}

}
