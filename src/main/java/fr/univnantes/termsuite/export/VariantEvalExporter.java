package fr.univnantes.termsuite.export;

import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;

import fr.univnantes.termsuite.api.TermSuiteException;
import fr.univnantes.termsuite.model.RelationProperty;
import fr.univnantes.termsuite.model.RelationType;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.TermIndex;
import fr.univnantes.termsuite.model.TermOccurrence;
import fr.univnantes.termsuite.model.TermRelation;
import fr.univnantes.termsuite.utils.TermOccurrenceUtils;

public class VariantEvalExporter {
	
	private TermIndex termIndex;
	private Writer writer;

	private int nbVariantsPerTerm;
	private int contextSize;
	private int nbExampleOccurrences;
	private int topN;
	
	private VariantEvalExporter(TermIndex termIndex, Writer writer, int nbVariantsPerTerm, int contextSize,
			int nbExampleOccurrences, int topN) {
		super();
		this.termIndex = termIndex;
		this.writer = writer;
		this.nbVariantsPerTerm = nbVariantsPerTerm;
		this.contextSize = contextSize;
		this.nbExampleOccurrences = nbExampleOccurrences;
		this.topN = topN;
	}
	
	
	public static void export(TermIndex termIndex, Writer writer, int nbVariantsPerTerm, int contextSize,
			int nbExampleOccurrences, int topN) {
		new VariantEvalExporter(termIndex, writer, nbVariantsPerTerm, contextSize, nbExampleOccurrences, topN).doExport();
	}

	private void doExport() {
		try {
			int rank = 0;
			int variantCnt = 0;
			for(Term t:termIndex.getTerms()) {
				if(!termIndex.getOutboundRelations(t).isEmpty())
					continue;
				printBase(++rank, t);
				int variantRank = 0;
				for(TermRelation variation:termIndex.getOutboundRelations(t,  RelationType.SYNONYMIC, RelationType.MORPHOLOGICAL, RelationType.SYNTACTICAL)) {
					if(variantRank >= nbVariantsPerTerm)
						break;
					variantCnt++;
					variantRank++;
					printVariation(rank, variantRank, variation);
					printTermOccurrences(variation.getTo());
				}
				
				if(variantCnt>this.topN)
					break;
			}

		} catch (IOException e) {
			throw new TermSuiteException(e);
		}
	}
	
	private void printVariation(int termRank, int variantRank, TermRelation variation) throws IOException {
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

	private void printBase(int rank, Term t) throws IOException {
		writer.write(Integer.toString(rank));
		writer.write("\t");
		writer.write("T");
		writer.write("\t");
		writer.write(t.getPilot());
		writer.write("\t");
		writer.write(String.format("[%s]", t.getGroupingKey()));
		writer.write("\n");
	}

	private void printTermOccurrences(Term term) throws IOException {
		List<TermOccurrence> occurrences = Lists.newArrayList(termIndex.getOccurrenceStore().getOccurrences(term));
		Collections.shuffle(occurrences);
		int occCnt = 0;
		for(TermOccurrence occurrence:occurrences) {
			if(occCnt > this.nbExampleOccurrences)
				break;
			printOccurrence(occurrence);
			occCnt++;
		}
	}

	private void printOccurrence(TermOccurrence occurrence) throws IOException {
		writer.write("#\t\t  ...");
		String textualContext = TermOccurrenceUtils.getTextualContext(occurrence, contextSize);
		writer.write(textualContext);
		writer.write("\n");
	}

}
