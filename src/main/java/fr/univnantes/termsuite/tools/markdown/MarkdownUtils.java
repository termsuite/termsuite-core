package fr.univnantes.termsuite.tools.markdown;

import java.util.Optional;

import fr.univnantes.termsuite.model.Property;
import fr.univnantes.termsuite.model.RelationProperty;
import fr.univnantes.termsuite.model.TermProperty;

public class MarkdownUtils {
	public static final String PROPERTY_PREFIX = "{{site.baseurl_root}}/documentation/properties/";

	public static void printId(String id) {
		System.out.format("{: id=\"%s\"}%n%n", id);
	}
	
	public static Optional<Property<?>> asProperty(Object name) {
		Property<?> property = null;
		if(TermProperty.forNameOptional(name.toString()).isPresent())
			property = TermProperty.forNameOptional(name.toString()).get();
		else if(RelationProperty.forNameOptional(name.toString()).isPresent())
			property = RelationProperty.forNameOptional(name.toString()).get();
		return Optional.ofNullable(property);
	}

	public static void printAlterBlock(String cls, String content) {
		System.out.format("%n<div class=\"alert alert-%s\" role=\"alert\">%n%s%n</div>%n%n", cls, content);
	}

	public static String toMarkdownPropertyLink(Property<?> property) {
		return String.format("[`%s`](%s)", property.getPropertyName(), toPropertyLink(property));
	}

	public static String toPropertyLink(Property<?> property) {
		return String.format("%s#%s", PROPERTY_PREFIX, getPropertyHTMLId(property));
	}
	
	public static String getPropertyHTMLId(Property<?> p) {
		return String.format("%s-%s", p.getClass().getSimpleName(), p.getJsonField());
	}

}
