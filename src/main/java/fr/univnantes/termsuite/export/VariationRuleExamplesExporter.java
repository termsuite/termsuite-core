package fr.univnantes.termsuite.export;

import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import fr.univnantes.termsuite.api.TermSuiteException;
import fr.univnantes.termsuite.engines.gatherer.VariantRule;
import fr.univnantes.termsuite.engines.gatherer.YamlRuleSet;
import fr.univnantes.termsuite.model.OccurrenceStore;
import fr.univnantes.termsuite.model.RelationProperty;
import fr.univnantes.termsuite.model.RelationType;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.TermOccurrence;
import fr.univnantes.termsuite.model.TermRelation;
import fr.univnantes.termsuite.model.Terminology;
import fr.univnantes.termsuite.utils.TermOccurrenceUtils;

public class VariationRuleExamplesExporter {

	private Terminology termino;
	private Writer writer;
	private YamlRuleSet yamlVariantRules;

	
	class TermPair implements Comparable<TermPair> {
		Term source;
		Term target;

		public TermPair(Term source, Term target) {
			super();
			this.source = source;
			this.target = target;
		}

		@Override
		public int compareTo(TermPair o) {
			return ComparisonChain.start().compare(o.target.getFrequency(), this.target.getFrequency()).result();
		}
	}


	private VariationRuleExamplesExporter(Terminology termino, Writer writer, YamlRuleSet yamlVariantRules) {
		this.termino = termino;
		this.writer = writer;
		this.yamlVariantRules = yamlVariantRules;
	}

	public static void export(Terminology termino, Writer writer, YamlRuleSet yamlVariantRules) {
		new VariationRuleExamplesExporter(termino, writer, yamlVariantRules).doExport();
	}

	private void doExport() {
		final Multimap<String, TermPair> pairs = HashMultimap.create();

		for (Term t : termino.getTerms()) {
			for (TermRelation v : termino.getOutboundRelations(t, RelationType.VARIATION))
				pairs.put(v.getPropertyStringValue(RelationProperty.VARIATION_RULE), new TermPair(t, v.getTo()));
		}

		// gets all variant rules (event size-0) and sorts them
		TreeSet<VariantRule> varianRules = new TreeSet<VariantRule>(new Comparator<VariantRule>() {
			@Override
			public int compare(VariantRule o1, VariantRule o2) {

				return ComparisonChain.start().compare(pairs.get(o2.getName()).size(), pairs.get(o1.getName()).size())
						.compare(o1.getName(), o2.getName()).result();
			}
		});
		varianRules.addAll(yamlVariantRules.getVariantRules());

		try {
			/*
			 * Display Summary
			 */
			int total = 0;
			int nbMatchingRules = 0;
			String summaryLine = "%-16s: %d\n";
			for (VariantRule rule : varianRules) {
				int nbPairs = pairs.get(rule.getName()).size();
				total += nbPairs;
				if (nbPairs > 0)
					nbMatchingRules++;
				writer.write(String.format(summaryLine, rule.getName(), nbPairs));
			}
			writer.write("---\n");
			writer.write(String.format(summaryLine, "TOTAL", total));
			writer.write(String.format("%-16s: %d / %d\n", "nb matching rules", nbMatchingRules, varianRules.size()));
			writer.write("\n---\n");

			/*
			 * Display variant rules' matches.
			 */
			for (VariantRule rule : varianRules) {
				List<TermPair> sortedPairs = Lists.newArrayList(pairs.get(rule.getName()));
				Collections.sort(sortedPairs);

				int nbOverlappingOccs = 0;
				int nbStrictOccs = 0;
				List<String> lines = Lists.newArrayList();
				for (TermPair pair : sortedPairs) {
					OccurrenceStore occStore = termino.getOccurrenceStore();
					List<TermOccurrence> targetStrictOccurrences = Lists.newLinkedList(occStore.getOccurrences(pair.target));
					TermOccurrenceUtils.removeOverlaps(occStore.getOccurrences(pair.source), targetStrictOccurrences);
					nbOverlappingOccs += pair.target.getFrequency();
					nbStrictOccs += targetStrictOccurrences.size();
					lines.add(String.format("%14d%14d%35s || %-35s\n", pair.target.getFrequency(),
							targetStrictOccurrences.size(), pair.source.getGroupingKey(),
							pair.target.getGroupingKey()));
				}

				writer.write(
						"\n----------------------------------------------------------------------------------------------------------------\n");
				writer.write(String.format(
						"---------------------  %s   [nb_terms: %d, total_occs: %d, total_strict_occurrences: %d] \n",
						rule.getName(), sortedPairs.size(), nbOverlappingOccs, nbStrictOccs));
				writer.write(
						"----------------------------------------------------------------------------------------------------------------\n");
				writer.write(String.format("%14s%14s\n", "fr_overlaps", "fr_strict"));
				for (String line : lines)
					writer.write(line);
				writer.write(String.format("TOTAL: %7s%14s\n", nbOverlappingOccs, nbStrictOccs));

			}
		} catch (IOException e) {
			throw new TermSuiteException(e);
		}

		
	}

	
	
}
