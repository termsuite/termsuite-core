package fr.univnantes.termsuite.io.other;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;

import fr.univnantes.termsuite.api.TermSuiteException;
import fr.univnantes.termsuite.framework.Export;
import fr.univnantes.termsuite.framework.service.RelationService;
import fr.univnantes.termsuite.framework.service.TerminologyService;
import fr.univnantes.termsuite.model.RelationProperty;

public class VariantDistributionExporter  {
	
	private Predicate<RelationService> selector;
	
	private List<RelationProperty> relationProperties = Lists.newArrayList();
	
	public VariantDistributionExporter(List<RelationProperty> relationProperties, Predicate<RelationService> selector) {
		super();
		this.selector = selector;
		this.relationProperties = relationProperties;
	}

	@Export
	public void export(Writer writer, TerminologyService termino) {
		try {
			writer.write("type");
			writer.write("\t");
			writer.write("from");
			writer.write("\t");
			writer.write("to");
			writer.write("\t");
			writer.write(relationProperties.stream().map(RelationProperty::getJsonField).collect(Collectors.joining("\t")));
			writer.write("\n");

			termino.relations()
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
					for(RelationProperty p:relationProperties) {
						writer.write("\t");
						writer.write(relation.isPropertySet(p) ?
								relation.get(p).toString() :
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
