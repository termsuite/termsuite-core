package fr.univnantes.termsuite.export;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;

import fr.univnantes.termsuite.api.TermSuiteException;
import fr.univnantes.termsuite.model.RelationProperty;
import fr.univnantes.termsuite.model.TermRelation;
import fr.univnantes.termsuite.model.Terminology;

public class VariantDistributionExporter {
	
	private Terminology termino;
	private Writer writer;
	private Predicate<TermRelation> selector;
	
	private List<RelationProperty> termProperties = Lists.newArrayList();
	
	private VariantDistributionExporter(Terminology termino, Writer writer, Predicate<TermRelation> selector, RelationProperty... properties) {
		super();
		this.termino = termino;
		this.writer = writer;
		this.selector = selector;
		this.termProperties = Lists.newArrayList(properties);
	}

	public static void export(Terminology termino, Writer writer, Predicate<TermRelation> selector, RelationProperty... properties) {
		new VariantDistributionExporter(termino, writer, selector, properties).doExport();
	}

	private void doExport() {
		try {
			writer.write("type");
			writer.write("\t");
			writer.write("from");
			writer.write("\t");
			writer.write("to");
			writer.write("\t");
			writer.write(termProperties.stream().map(RelationProperty::getShortName).collect(Collectors.joining("\t")));
			writer.write("\n");

			termino.getRelations()
				.filter(selector)
				.forEach( relation -> {
				try {
					if(relation.getFrom().getGroupingKey().contains("\""))
						return;
					if(relation.getTo().getGroupingKey().contains("\""))
						return;
					
					writer.write(relation.getType().getLetter());
					writer.write("\t");
					writer.write(relation.getFrom().getGroupingKey());
					writer.write("\t");
					writer.write(relation.getTo().getGroupingKey());
					for(RelationProperty p:termProperties) {
						writer.write("\t");
						writer.write(relation.isPropertySet(p) ?
								relation.getPropertyValue(p).toString() :
											"");
					}
					writer.write("\n");
				} catch (IOException e) {
					e.printStackTrace();
					System.exit(1);
				}
			});
			writer.flush();
			writer.close();
		} catch (Exception e) {
			throw new TermSuiteException(e);

		}

	}
}
