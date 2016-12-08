package fr.univnantes.termsuite.export;

import java.io.Writer;

import fr.univnantes.termsuite.api.TermSuiteException;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.Terminology;
import fr.univnantes.termsuite.model.TermRelation;

public class EvalExporter {
	
	private Terminology termIndex;
	private boolean withVariants;
	private Writer writer;
	
	private EvalExporter(Terminology termIndex, Writer writer, boolean withVariants) {
		super();
		this.termIndex = termIndex;
		this.writer = writer;
		this.withVariants = withVariants;
	}

	public static void export(Terminology termIndex, Writer writer, boolean withVariants) {
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
