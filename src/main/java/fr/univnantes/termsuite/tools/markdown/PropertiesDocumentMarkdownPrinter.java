package fr.univnantes.termsuite.tools.markdown;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fr.univnantes.termsuite.model.Property;
import fr.univnantes.termsuite.model.RelationProperty;
import fr.univnantes.termsuite.model.TermProperty;

public class PropertiesDocumentMarkdownPrinter {

	public static void main(String[] args) {
		System.out.format("### Term properties%n%n");
		for(TermProperty p:TermProperty.values()) 
			printProperty(p);
		
		System.out.format("%n%n### Relation properties%n%n");
		for(RelationProperty p:RelationProperty.values()) 
			printProperty(p);
	}

	private static void printProperty(Property<?> p) {
		System.out.format("#### %s %n", toMarkownTitle(p));
		MarkdownUtils.printId(MarkdownUtils.getPropertyHTMLId(p));
		System.out.format(" > %s%n%n", markdownifyProperties(p.getDescription()));
	}

	private static Object markdownifyProperties(String txt) {
		Set<String> allMatches = new HashSet<>(); 
		Matcher m = Pattern.compile("`(\\S+)`").matcher(txt);
		 while (m.find()) {
		   allMatches.add(m.group(1));
		}
		 for(String match:allMatches) {
			 Optional<Property<?>> p = MarkdownUtils.asProperty(match);
			 if(p.isPresent()) {
				 txt = txt.replaceAll(
						 String.format("`%s`", match), 
						 MarkdownUtils.toMarkdownPropertyLink(p.get())
						 );
			 }
		 }
		return txt;
	}

	private static String toMarkownTitle(Property<?> p) {
		if(p.getPropertyName().equals(p.getJsonField()))
			return String.format("`%s` [%s]", p.getJsonField(), p.getRange().getSimpleName());
		else
			return String.format("`%s`, `%s` [%s]", p.getPropertyName(), p.getJsonField(), p.getRange().getSimpleName());
	}
}
