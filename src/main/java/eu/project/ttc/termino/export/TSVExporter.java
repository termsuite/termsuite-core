package eu.project.ttc.termino.export;

import java.io.IOException;
import java.io.Writer;

import com.google.common.collect.Lists;

import eu.project.ttc.api.TSVOptions;
import eu.project.ttc.api.TermSuiteException;
import eu.project.ttc.api.Traverser;
import eu.project.ttc.models.Term;
import eu.project.ttc.models.TermIndex;
import eu.project.ttc.models.TermVariation;
import eu.project.ttc.tools.utils.IndexerTSVBuilder;

public class TSVExporter {
	
	private TermIndex termIndex;
	private Writer writer;
	private TSVOptions options;
	private Traverser traverser;
	
	private TSVExporter(TermIndex termIndex, Writer writer, Traverser traverser, TSVOptions options) {
		super();
		this.termIndex = termIndex;
		this.writer = writer;
		this.options = options;
		this.traverser = traverser;
	}

	public static void export(TermIndex termIndex, Writer writer) {
		export(termIndex, writer, Traverser.create(), new TSVOptions());
	}

	public static void export(TermIndex termIndex, Writer writer,  TSVOptions options) {
		new TSVExporter(termIndex, writer, Traverser.create(), options).doExport();
	}

	
	public static void export(TermIndex termIndex, Writer writer, Traverser traverser, TSVOptions options) {
		new TSVExporter(termIndex, writer, traverser, options).doExport();
	}

	private void doExport() {
		
		IndexerTSVBuilder tsv = new IndexerTSVBuilder(
				writer,
				Lists.newArrayList(options.properties()),
				options.showScores()
			);
		
		try {
			if(options.showHeaders())
				tsv.writeHeaders();
				
			for(Term t:traverser.toList(termIndex)) {
				tsv.startTerm(termIndex, t, "");
				
				if(options.showVariants())
					for(TermVariation tv:t.getVariations()) {
						tsv.addVariant(
								termIndex, 
								tv.getVariant(), 
								String.format("%.2f", tv.getScore()));
					}
			}
			tsv.close();
		} catch (IOException e) {
			throw new TermSuiteException(e);
		}
	}
}
