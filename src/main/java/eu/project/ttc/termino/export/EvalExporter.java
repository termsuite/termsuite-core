package eu.project.ttc.termino.export;

import java.io.Writer;

import eu.project.ttc.api.TermSuiteException;
import eu.project.ttc.models.Term;
import eu.project.ttc.models.TermIndex;
import eu.project.ttc.models.TermRelation;

public class EvalExporter {
	
	private TermIndex termIndex;
	private boolean withVariants;
	private Writer writer;
	
	private EvalExporter(TermIndex termIndex, Writer writer, boolean withVariants) {
		super();
		this.termIndex = termIndex;
		this.writer = writer;
		this.withVariants = withVariants;
	}

	public static void export(TermIndex termIndex, Writer writer, boolean withVariants) {
		new EvalExporter(termIndex, writer, withVariants).doExport();
	}

	private void doExport() {
		try {
			for(Term t: termIndex.getTerms()) {
				if(this.withVariants) {
					for (TermRelation v : termIndex.getOutboundRelations(t))
							writer.write(v.getTo().getGroupingKey() + "#");
				}
				writer.write(t.getGroupingKey());
				writer.write("\t");
				writer.write(Double.toString(t.getSpecificity()));
				writer.write("\n");
			}
			writer.flush();
			writer.close();
		} catch (Exception e) {
			throw new TermSuiteException(e);

		}

	}
}
