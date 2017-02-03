package fr.univnantes.termsuite.engines.prepare;

import fr.univnantes.termsuite.engines.SimpleEngine;
import fr.univnantes.termsuite.model.RelationProperty;
import fr.univnantes.termsuite.model.RelationType;

public class IsExtensionPropertySetter extends SimpleEngine {

	@Override
	public void execute() {
		terminology
			.relations()
			.forEach(relation -> {
				if(relation.getType() == RelationType.HAS_EXTENSION)
					relation.setProperty(RelationProperty.IS_EXTENSION, true);
				else {
					boolean isExtension = terminology
						.extensions(relation.getFrom().getTerm(), relation.getTo().getTerm())
						.findAny().isPresent();
					relation.setProperty(
							RelationProperty.IS_EXTENSION,
							isExtension);
				}
			});
	}
}
