package fr.univnantes.termsuite.export.other;

import java.io.Writer;

import fr.univnantes.termsuite.api.TermSuiteException;
import fr.univnantes.termsuite.index.Terminology;
import fr.univnantes.termsuite.model.Relation;
import fr.univnantes.termsuite.model.Term;

public class EvalExporter {
	
	private Terminology termino;
	private boolean withVariants;
	private Writer writer;
	
	private EvalExporter(Terminology termino, Writer writer, boolean withVariants) {
		super();
		this.termino = termino;
		this.writer = writer;
		this.withVariants = withVariants;
	}

	public static void export(Terminology termino, Writer writer, boolean withVariants) {
		new EvalExporter(termino, writer, withVariants).doExport();
	}

	private void doExport() {
		try {
			for(Term t: termino.getTerms().values()) {
				if(this.withVariants) {
					for (Relation v : termino.getOutboundRelations().get(t))
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
