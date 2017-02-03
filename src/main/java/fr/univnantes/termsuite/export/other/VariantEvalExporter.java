package fr.univnantes.termsuite.export.other;

import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.collect.Lists;

import fr.univnantes.termsuite.api.TermSuiteException;
import fr.univnantes.termsuite.framework.Export;
import fr.univnantes.termsuite.framework.service.TermService;
import fr.univnantes.termsuite.framework.service.TerminologyService;
import fr.univnantes.termsuite.model.OccurrenceStore;
import fr.univnantes.termsuite.model.Relation;
import fr.univnantes.termsuite.model.RelationProperty;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.TermOccurrence;
import fr.univnantes.termsuite.utils.TermOccurrenceUtils;

public class VariantEvalExporter {
	
	private VariantEvalExporterOptions options;
	
	public VariantEvalExporter(VariantEvalExporterOptions options) {
		super();
		this.options = options;
	}

	@Export
	public void export(TerminologyService termino, Writer writer,OccurrenceStore occurrenceStore) {
		try {
			AtomicInteger rank = new AtomicInteger(0);
			AtomicInteger variantCnt = new AtomicInteger(0);
			for(TermService t:termino.getTerms()) {
				if(!t.outboundRelations().findAny().isPresent())
					continue;
				printBase(writer, rank.incrementAndGet(), t.getTerm());
				AtomicInteger variantRank = new AtomicInteger(0);
				t.variations().forEach(variation -> {
					if(variantRank.intValue() >= options.getNbVariantsPerTerm())
						return;
					variantCnt.getAndIncrement();
					variantRank.getAndIncrement();
					try {
						printVariation(writer, rank.intValue(), variantRank.intValue(), variation.getRelation());
						printTermOccurrences(writer, occurrenceStore, variation.getTo().getTerm());
					} catch(IOException e) {
						throw new TermSuiteException(e);
					}
				});
				
				if(variantCnt.intValue()>this.options.getTopN())
					break;
			}

		} catch (IOException e) {
			throw new TermSuiteException(e);
		}
	}
	
	private void printVariation(Writer writer, int termRank, int variantRank, Relation variation) throws IOException {
		Term variant = variation.getTo();
		String pilot = variant.getPilot();
		writer.write(Integer.toString(termRank));
		writer.write("\t");
		writer.write("V_" + Integer.toString(variantRank));
		writer.write("\t");
		writer.write(String.format("<%s>", variation.getPropertyStringValue(RelationProperty.VARIATION_RULE, "[]")));
		writer.write("\t");
		writer.write(String.format("%s (%d)", pilot, variant.getFrequency()));
		writer.write("\t");
		writer.write(String.format("[%s]", variant.getGroupingKey()));
		writer.write("\t");
		writer.write("{is_variant: _0_or_1_, variant_type: _syn_termino_other_}");
		writer.write("\n");		
	}

	private void printBase(Writer writer, int rank, Term t) throws IOException {
		writer.write(Integer.toString(rank));
		writer.write("\t");
		writer.write("T");
		writer.write("\t");
		writer.write(t.getPilot());
		writer.write("\t");
		writer.write(String.format("[%s]", t.getGroupingKey()));
		writer.write("\n");
	}

	private void printTermOccurrences(Writer writer, OccurrenceStore occurrenceStore, Term term) throws IOException {
		List<TermOccurrence> occurrences = Lists.newArrayList(occurrenceStore.getOccurrences(term));
		Collections.shuffle(occurrences);
		int occCnt = 0;
		for(TermOccurrence occurrence:occurrences) {
			if(occCnt > this.options.getNbExampleOccurrences())
				break;
			printOccurrence(writer, occurrence);
			occCnt++;
		}
	}

	private void printOccurrence(Writer writer, TermOccurrence occurrence) throws IOException {
		writer.write("#\t\t  ...");
		String textualContext = TermOccurrenceUtils.getTextualContext(occurrence, options.getContextSize());
		writer.write(textualContext);
		writer.write("\n");
	}

}
