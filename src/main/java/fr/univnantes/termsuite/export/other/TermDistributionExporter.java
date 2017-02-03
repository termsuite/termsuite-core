package fr.univnantes.termsuite.export.other;

import static java.util.stream.Collectors.toList;

import java.io.Writer;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;

import fr.univnantes.termsuite.api.TermSuiteException;
import fr.univnantes.termsuite.framework.Export;
import fr.univnantes.termsuite.framework.service.TermService;
import fr.univnantes.termsuite.framework.service.TerminologyService;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.TermProperty;

public class TermDistributionExporter {
	
	private Predicate<Term> selector;
	
	private List<TermProperty> termProperties = Lists.newArrayList();
	
	public TermDistributionExporter(List<TermProperty> termProperties, Predicate<Term> selector) {
		super();
		this.selector = selector;
		this.termProperties = termProperties;
	}

	@Export
	public void export(TerminologyService termino, Writer writer) {
		try {
			writer.write("term");
			writer.write("\t");
			writer.write(termProperties.stream().map(TermProperty::getShortName).collect(Collectors.joining("\t")));
			writer.write("\n");

			for(Term t: termino.terms().map(TermService::getTerm).collect(toList())) {
				if(!selector.test(t))
					continue;
				
				if(t.getGroupingKey().contains("\""))
					continue;
				
				writer.write(t.getGroupingKey());
				for(TermProperty p:termProperties) {
					writer.write("\t");
					writer.write(t.isPropertySet(p) ?
									t.getPropertyValue(p).toString() :
										"");
				}
				writer.write("\n");
			}
			writer.flush();
			writer.close();
		} catch (Exception e) {
			throw new TermSuiteException(e);

		}

	}
}
