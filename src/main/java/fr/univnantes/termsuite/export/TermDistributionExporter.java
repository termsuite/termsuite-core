package fr.univnantes.termsuite.export;

import java.io.Writer;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;

import fr.univnantes.termsuite.api.TermSuiteException;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.TermProperty;
import fr.univnantes.termsuite.model.Terminology;

public class TermDistributionExporter {
	
	private Terminology termino;
	private Writer writer;
	private Predicate<Term> selector;
	
	private List<TermProperty> termProperties = Lists.newArrayList();
	
	private TermDistributionExporter(Terminology termino, Writer writer, Predicate<Term> selector, TermProperty... properties) {
		super();
		this.termino = termino;
		this.writer = writer;
		this.selector = selector;
		this.termProperties = Lists.newArrayList(properties);
	}

	public static void export(Terminology termino, Writer writer, Predicate<Term> selector, TermProperty... properties) {
		new TermDistributionExporter(termino, writer, selector, properties).doExport();
	}

	private void doExport() {
		try {
			writer.write("term");
			writer.write("\t");
			writer.write(termProperties.stream().map(TermProperty::getShortName).collect(Collectors.joining("\t")));
			writer.write("\n");

			for(Term t: termino.getTerms()) {
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
