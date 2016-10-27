package eu.project.ttc.termino.export;

import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;

import eu.project.ttc.api.TermSuiteException;
import eu.project.ttc.models.Term;
import eu.project.ttc.models.TermIndex;
import eu.project.ttc.models.TermOccurrence;
import eu.project.ttc.models.TermRelation;
import eu.project.ttc.models.RelationType;

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
				for(TermRelation variation:termIndex.getOutboundRelations(t, RelationType.MORPHOLOGICAL, RelationType.SYNTACTICAL)) {
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
		String pilot = variant.getForms().iterator().next();
		writer.write(Integer.toString(termRank));
		writer.write("\t");
		writer.write("V_" + Integer.toString(variantRank));
		writer.write("\t");
		writer.write(String.format("<%s>", variation.getInfo().toString()));
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
		writer.write(t.getForms().iterator().next());
		writer.write("\t");
		writer.write(String.format("[%s]", t.getGroupingKey()));
		writer.write("\n");
	}

	private void printTermOccurrences(Term term) throws IOException {
		List<TermOccurrence> occurrences = Lists.newArrayList(term.getOccurrences());
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
		String textualContext = occurrence.getTextualContext(contextSize);
		writer.write(textualContext);
		writer.write("\n");
	}

}
