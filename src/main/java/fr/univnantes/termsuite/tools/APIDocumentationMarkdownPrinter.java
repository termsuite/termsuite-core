package fr.univnantes.termsuite.tools;

import static java.util.stream.Collectors.joining;

import java.util.List;
import java.util.Set;

import fr.univnantes.termsuite.api.TermSuite;
import fr.univnantes.termsuite.tools.opt.CliOption;
import fr.univnantes.termsuite.tools.opt.OptType;

/**
 * 
 * Prints the document of the API in Markdown.
 * 
 * 
 * @author Damien Cram
 *
 */
public class APIDocumentationMarkdownPrinter {

	public static void main(String[] args) {
		
		printOptions(new TerminologyExtractorCLI());
		printOptions(new PreprocessorCLI());
		printOptions(new AlignerCLI());
	}
	
	public static void doHelp(CommandLineClient client) {
		System.out.format("%n%n%n### %s%n", client.getClass().getSimpleName());
		printId(client.getClass().getSimpleName());
		
		System.out.format("#### Usage%n%n```%njava [-Xms256m -Xmx8g] -cp termsuite-core-%s.jar \\%n\t %s OPTIONS %n```%n",
				TermSuite.currentVersion(),
				client.getClass().getCanonicalName());
		
		System.out.format("%n#### Description%n%n%s%n", client.description);
		System.out.format("%n#### Mandatory options%n%n");
		for(CliOption opt:client.mandatoryOptions) 
			printOption2(client, opt);
		
		System.out.println();
		if(!(client.exactlyOneOfBags.isEmpty() && client.atLeastOneOfBags.isEmpty())) {
			for(Set<CliOption> bag:client.exactlyOneOfBags) {
				printConstraint(client, bag, "Exactly one option");
			}
			
			for(Set<CliOption> bag:client.atLeastOneOfBags) {
				printConstraint(client, bag, "At least one option");
			}
		}

		System.out.println();
		

		
		System.out.format("%n#### Other options%n%n");
		List<CliOption> otherOptions = client.getSortedDeclaredOptions();
		otherOptions.removeAll(client.mandatoryOptions);
		if(otherOptions.isEmpty())
			System.out.println("(none)");
		
		else {
			for(CliOption opt:otherOptions)
				printOption2(client, opt);
			
			for(Set<CliOption> bag:client.atMostOneOfBags) 
				printConstraint(client, bag, "At most one option");
			
		}
	}

	private static void printConstraint(CommandLineClient client, Set<CliOption> bag, String txt) {
		System.out.format("%n<div class=\"alert alert-warning\" role=\"alert\">%n%s in %s must be set.%n</div>%n", txt, bagToString(client, bag));
	}

	private static void printId(String id) {
		System.out.format("{: id=\"%s\"}%n%n", id);
	}

	private static String bagToString(CommandLineClient client, Set<CliOption> bag) {
		return bag.stream().map(opt -> optionLink(client, opt)).collect(joining(", "));
	}

	private static String optionLink(CommandLineClient client, CliOption opt) {
		return String.format("[`--%s`](#%s)", opt.getOptName(), getOptId(client, opt));
	}
	
	private static void printOptions(CommandLineClient client) {
		client.configureOpts();
		doHelp(client);
	}

	private static void printOption2(CommandLineClient client, CliOption opt) {
		System.out.format("###### %s %s%n", 
				getOptString(opt),
				getOptArgString(opt)
			);
		printId(getOptId(client, opt));
		
		System.out.format("%n > %s%n%n", 
				opt.getDescription()
			);
		
	}
	private static String getOptId(CommandLineClient client, CliOption opt) {
		return String.format("%s-%s", client.getClass().getSimpleName(), opt.getOptName());
	}

	private static String getOptString(CliOption opt) {
		String shortOpt;
		if(opt.getOptShortName() != null)
			shortOpt = ", `-" + opt.getOptShortName() + "`";
		else
			shortOpt = "";
		String longOpt = "`--" + opt.getOptName() + "`";
		return longOpt + shortOpt;
	}

	private static String getOptArgString(CliOption opt) {
		if(opt.getArgType() == OptType.T_NONE)
			return "*(no arg)*";
		else
			return opt.getArgType().getStr();
	}
}
