package fr.univnantes.termsuite.export.tsv;

import java.io.IOException;
import java.io.Writer;

import com.google.common.collect.Lists;

import fr.univnantes.termsuite.api.TermSuiteException;
import fr.univnantes.termsuite.framework.Export;
import fr.univnantes.termsuite.framework.service.TerminologyService;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.TermProperty;
import fr.univnantes.termsuite.uima.engines.export.IndexerTSVBuilder;

public class TsvExporter {
	
	private TsvOptions options;
	
	public TsvExporter(TsvOptions options) {
		super();
		this.options = options;
	}

	@Export
	public void export(Writer writer, TerminologyService termino) {
		final IndexerTSVBuilder tsv = new IndexerTSVBuilder(
				writer,
				Lists.newArrayList(options.properties())
			);
		
		try {
			if(options.showHeaders())
				tsv.writeHeaders();
				
			for(Term t:termino.getTerms(options.getTermOrdering())) {
				if(t.isPropertySet(TermProperty.FILTERED) && t.getPropertyBooleanValue(TermProperty.FILTERED))
					continue;
				
				tsv.startTerm(t);
				
				if(options.isShowVariants())
					termino.variationsFrom(t)
						.sorted(options.getVariantOrdering().toComparator())
						.forEach(tv -> {
							try {
								tsv.addVariant(
										termino, 
										tv,
										options.tagsTermsHavingVariants());
							} catch (IOException e) {
								throw new TermSuiteException(e);
							}
					});
			}
			tsv.close();
		} catch (IOException e) {
			throw new TermSuiteException(e);
		}
	}
}
