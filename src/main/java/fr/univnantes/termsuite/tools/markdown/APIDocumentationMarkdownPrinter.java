package fr.univnantes.termsuite.tools.markdown;

import static java.util.stream.Collectors.joining;

import java.util.List;
import java.util.Set;

import fr.univnantes.termsuite.api.TermSuite;
import fr.univnantes.termsuite.tools.AlignerCLI;
import fr.univnantes.termsuite.tools.CommandLineClient;
import fr.univnantes.termsuite.tools.PreprocessorCLI;
import fr.univnantes.termsuite.tools.TerminologyExtractorCLI;
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
		MarkdownUtils.printId(client.getClass().getSimpleName());
		
		System.out.format("#### Usage%n%n```%njava [-Xms256m -Xmx8g] -cp termsuite-core-%s.jar \\%n\t %s OPTIONS %n```%n",
				TermSuite.currentVersion(),
				client.getClass().getCanonicalName());
		
		System.out.format("%n#### Description%n%n%s%n", client.getDescription());
		System.out.format("%n#### Mandatory options%n%n");
		for(CliOption opt:client.getMandatoryOptions()) 
			printOptionSection(client, opt);
		
		System.out.println();
		if(!(client.getExactlyOneOfBags().isEmpty() && client.getAtLeastOneOfBags().isEmpty())) {
			for(Set<CliOption> bag:client.getExactlyOneOfBags()) {
				System.out.format("%n###### %s%n%n", bagToString(client, bag));
				printConstraint(client, bag, "Exactly one option");
			}
			
			for(Set<CliOption> bag:client.getAtLeastOneOfBags()) {
				System.out.format("%n###### %s%n%n", bagToString(client, bag));
				printConstraint(client, bag, "At least one option");
			}
		}

		System.out.println();
		
		System.out.format("%n#### Other options%n%n");
		List<CliOption> otherOptions = client.getSortedDeclaredOptions();
		otherOptions.removeAll(client.getMandatoryOptions());
		if(otherOptions.isEmpty())
			System.out.println("(none)");
		
		else {
			for(CliOption opt:otherOptions)
				printOptionSection(client, opt);
		}
	}

	private static void printConstraint(CommandLineClient client, Set<CliOption> bag, String txt) {
		MarkdownUtils.printAlterBlock("warning", String.format("%s in %s must be set.", txt, bagToString(client, bag)));
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

	private static void printOptionSection(CommandLineClient client, CliOption opt) {
		System.out.format("###### %s %s%n", 
				getOptString(opt),
				getOptArgString(opt)
			);
		MarkdownUtils.printId(getOptId(client, opt));
		
		System.out.format("%n > %s%n%n", 
				getOptionDesctription(opt)
			);
		
		client.getConditionalOptions().entrySet().stream()
			.filter(entry -> entry.getValue().contains(opt))
			.forEach(entry -> {
				MarkdownUtils.printAlterBlock("warning", String.format("**Warning:** This option can only be set when option %s is already set.", optionLink(client, entry.getKey())));
			});
		
		client.getConditionalAbsentOptions().entrySet().stream()
			.filter(entry -> entry.getValue().contains(opt))
			.forEach(entry -> {
				MarkdownUtils.printAlterBlock("warning", String.format("**Warning:** This option **cannot be set** when option %s is already set.", optionLink(client, entry.getKey())));
			});

		client.getAtLeastOneOfBags().stream().filter(bag -> bag.contains(opt)).forEach(bag -> {
			printConstraint(client, bag, "**Warning:** At least one option");
		});
	
		client.getExactlyOneOfBags().stream().filter(bag -> bag.contains(opt)).forEach(bag -> {
			printConstraint(client, bag, "**Warning:** Exactly one option");
		});
		
		client.getAtMostOneOfBags().stream().filter(bag -> bag.contains(opt)).forEach(bag -> {
			printConstraint(client, bag, "**Warning:** At most one option");
		});
		

	}
	
	private static String getOptionDesctription(CliOption opt) {
		String suffix = "";
		if(!opt.getAllowedValues().isEmpty()) {
			boolean isProperty = areValuesAllProperties(opt);
			suffix = String.format(
					" Allowed values are: %s", 
					opt.getAllowedValues().stream()
						.map(v -> isProperty ? 
										// Adds an hyperlink to given property page
										MarkdownUtils.toMarkdownPropertyLink(MarkdownUtils.asProperty(v).get())
											: String.format("`%s`", v))
						.collect(joining(", ")));
		}
		return opt.getDescription() + suffix;
	}

	
	
	private static boolean areValuesAllProperties(CliOption opt) {
		return !opt.getAllowedValues().isEmpty()
				&& opt.getAllowedValues().stream().allMatch(v-> 
				MarkdownUtils.asProperty(v).isPresent()
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
