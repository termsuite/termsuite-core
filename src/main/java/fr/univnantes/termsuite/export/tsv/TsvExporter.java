package fr.univnantes.termsuite.export.tsv;

import java.io.IOException;
import java.io.Writer;

import javax.inject.Inject;

import com.google.common.collect.Lists;

import fr.univnantes.termsuite.api.TermSuiteException;
import fr.univnantes.termsuite.export.TerminologyExporter;
import fr.univnantes.termsuite.framework.service.TerminologyService;
import fr.univnantes.termsuite.model.RelationProperty;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.TermProperty;
import fr.univnantes.termsuite.uima.engines.export.IndexerTSVBuilder;

public class TsvExporter implements TerminologyExporter {
	
	@Inject
	private TerminologyService termino;
	
	@Inject
	private Writer writer;
	
	@Inject
	private TsvOptions options;
	
	public void export() {
		final IndexerTSVBuilder tsv = new IndexerTSVBuilder(
				writer,
				Lists.newArrayList(options.properties())
			);
		
		try {
			if(options.showHeaders())
				tsv.writeHeaders();
				
			for(Term t:termino.getTermsBy(TermProperty.SPECIFICITY, true)) {
				if(t.isPropertySet(TermProperty.FILTERED) && t.getPropertyBooleanValue(TermProperty.FILTERED))
					continue;
				
				tsv.startTerm(t);
				
				if(options.isShowVariants())
					termino.variationsFrom(t)
						.sorted(RelationProperty.VARIANT_SCORE.getComparator(true))
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
