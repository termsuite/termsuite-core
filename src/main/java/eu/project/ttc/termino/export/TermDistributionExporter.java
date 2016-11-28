package eu.project.ttc.termino.export;

import java.io.Writer;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.assertj.core.util.Lists;

import eu.project.ttc.api.TermSuiteException;
import eu.project.ttc.models.Term;
import eu.project.ttc.models.TermIndex;
import eu.project.ttc.models.TermProperty;

public class TermDistributionExporter {
	
	private TermIndex termIndex;
	private Writer writer;
	private Predicate<Term> selector;
	
	private List<TermProperty> termProperties = Lists.newArrayList();
	
	private TermDistributionExporter(TermIndex termIndex, Writer writer, Predicate<Term> selector, TermProperty... properties) {
		super();
		this.termIndex = termIndex;
		this.writer = writer;
		this.selector = selector;
		this.termProperties = Lists.newArrayList(properties);
	}

	public static void export(TermIndex termIndex, Writer writer, Predicate<Term> selector, TermProperty... properties) {
		new TermDistributionExporter(termIndex, writer, selector, properties).doExport();
	}

	private void doExport() {
		try {
			writer.write("term");
			writer.write("\t");
			writer.write(termProperties.stream().map(TermProperty::getShortName).collect(Collectors.joining("\t")));
			writer.write("\n");

			for(Term t: termIndex.getTerms()) {
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
