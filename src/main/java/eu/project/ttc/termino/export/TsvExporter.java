package eu.project.ttc.termino.export;

import java.io.IOException;
import java.io.Writer;

import com.google.common.collect.Lists;

import eu.project.ttc.api.TsvOptions;
import eu.project.ttc.api.TermSuiteException;
import eu.project.ttc.api.Traverser;
import eu.project.ttc.models.Term;
import eu.project.ttc.models.TermIndex;
import eu.project.ttc.models.TermVariation;
import eu.project.ttc.tools.utils.IndexerTSVBuilder;

public class TsvExporter {
	
	private TermIndex termIndex;
	private Writer writer;
	private TsvOptions options;
	private Traverser traverser;
	
	private TsvExporter(TermIndex termIndex, Writer writer, Traverser traverser, TsvOptions options) {
		super();
		this.termIndex = termIndex;
		this.writer = writer;
		this.options = options;
		this.traverser = traverser;
	}

	public static void export(TermIndex termIndex, Writer writer) {
		export(termIndex, writer, Traverser.create(), new TsvOptions());
	}

	public static void export(TermIndex termIndex, Writer writer,  TsvOptions options) {
		new TsvExporter(termIndex, writer, Traverser.create(), options).doExport();
	}

	
	public static void export(TermIndex termIndex, Writer writer, Traverser traverser, TsvOptions options) {
		new TsvExporter(termIndex, writer, traverser, options).doExport();
	}

	private void doExport() {
		
		IndexerTSVBuilder tsv = new IndexerTSVBuilder(
				writer,
				Lists.newArrayList(options.properties()),
				options.isShowScores()
			);
		
		try {
			if(options.showHeaders())
				tsv.writeHeaders();
				
			for(Term t:traverser.toList(termIndex)) {
				tsv.startTerm(termIndex, t, "");
				
				if(options.isShowVariants())
					for(TermVariation tv:termIndex.getOutboundTermVariations(t)) {
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
