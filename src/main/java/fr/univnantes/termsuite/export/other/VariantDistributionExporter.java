package fr.univnantes.termsuite.export.other;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import com.google.inject.Inject;

import fr.univnantes.termsuite.api.TermSuiteException;
import fr.univnantes.termsuite.export.TerminologyExporter;
import fr.univnantes.termsuite.framework.service.TerminologyService;
import fr.univnantes.termsuite.model.RelationProperty;
import fr.univnantes.termsuite.model.TermRelation;

public class VariantDistributionExporter implements TerminologyExporter {
	
	@Inject
	private TerminologyService termino;
	
	@Inject
	private Writer writer;
	
	@Inject
	private Predicate<TermRelation> selector;
	
	@Inject
	private List<RelationProperty> termProperties = Lists.newArrayList();
	
	public void export() {
		try {
			writer.write("type");
			writer.write("\t");
			writer.write("from");
			writer.write("\t");
			writer.write("to");
			writer.write("\t");
			writer.write(termProperties.stream().map(RelationProperty::getShortName).collect(Collectors.joining("\t")));
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
