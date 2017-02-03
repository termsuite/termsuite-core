package fr.univnantes.termsuite.export.tsv;

import java.io.IOException;
import java.io.Writer;

import com.google.common.collect.Lists;

import fr.univnantes.termsuite.api.TermSuiteException;
import fr.univnantes.termsuite.framework.Export;
import fr.univnantes.termsuite.framework.service.TermService;
import fr.univnantes.termsuite.framework.service.TerminologyService;
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
				
			termino.terms(options.getTermOrdering())
				.filter(TermService::notFiltered)
				.forEach(t-> {
				
				try {
					tsv.startTerm(t.getTerm());
				} catch (IOException e1) {
					throw new TermSuiteException(e1);
				}
				
				if(options.isShowVariants())
					t.variations()
						.sorted(options.getVariantOrdering().toServiceComparator())
						.forEach(tv -> {
							try {
								tsv.addVariant(
										termino, 
										tv.getRelation(),
										options.tagsTermsHavingVariants());
							} catch (IOException e) {
								throw new TermSuiteException(e);
							}
					});
			});
			tsv.close();
		} catch (IOException e) {
		}
	}
}
