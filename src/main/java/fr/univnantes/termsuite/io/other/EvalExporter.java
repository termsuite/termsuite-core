package fr.univnantes.termsuite.io.other;

import static java.util.stream.Collectors.toSet;

import java.io.Writer;

import fr.univnantes.termsuite.api.TermSuiteException;
import fr.univnantes.termsuite.framework.Export;
import fr.univnantes.termsuite.framework.service.RelationService;
import fr.univnantes.termsuite.framework.service.TermService;
import fr.univnantes.termsuite.framework.service.TerminologyService;

public class EvalExporter {
	
	private boolean withVariants;
	
	public EvalExporter(boolean withVariants) {
		super();
		this.withVariants = withVariants;
	}

	@Export
	public void export(TerminologyService termino, Writer writer) {
		try {
			for(TermService t: termino.getTerms()) {
				if(this.withVariants) {
					for (RelationService v : t.variations().collect(toSet()))
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
