package fr.univnantes.termsuite.tools;

import static java.util.stream.Collectors.joining;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Stream;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;

import fr.univnantes.termsuite.tools.opt.CliOption;

public class CliUtil {
	public static void throwAtLeast(Collection<? extends CliOption> opts) {
		throwAtLeast(asArray(opts));
	}

	public static void throwAtLeast(CliOption... opts) {
		throwException("At least one option in %s must be set", nameStream(opts).collect(joining(", ")));
	}

	public static void throwAtMost(Collection<? extends CliOption> opts) {
		throwAtMost(asArray(opts));
	}
	
	public static void throwAtMost(CliOption... opts) {
		throwException("At most one option in %s must be set", nameStream(opts).collect(joining(", ")));
	}

	public static void throwExactlyOne(Collection<? extends CliOption> opts) {
		throwExactlyOne(asArray(opts));
	}

	private static CliOption[] asArray(Collection<? extends CliOption> opts) {
		return opts.toArray(new CliOption[opts.size()]);
	}

	public static void throwExactlyOne(CliOption... opts) {
		throwException("Exactly one exception in %s must be set", nameStream(opts).collect(joining(", ")));
	}

	private static Stream<CliOption> stream(CliOption... opts) {
		return Arrays.stream(opts);
	}

	private static Stream<String> nameStream(CliOption... opts) {
		return stream(opts).map(opt -> {
			String str = "--" + opt.getOptName();
			if(opt.getOptShortName() != null)
				str+="[-" + opt.getOptShortName() + "]"; 
			return str;
		}).sorted();
	}

	public static void throwException(String format, Object... args) {
		throw new TermSuiteCliException(String.format(format, args));
	}
	
	
	/**
	 * Prints the command line usage to the std error output
	 * 
	 * @param e
	 *            The error that raised the help message
	 * @param cmdLine
	 *            The command line usage
	 * @param options
	 *            The options expected
	 */
	public static void printUsage(ParseException e, String cmdLine,
			Options options) {
		System.err.println(e.getMessage());
		// automatically generate the help statement
		HelpFormatter formatter = new HelpFormatter();
		PrintWriter pw = new PrintWriter(System.err);
		formatter.printUsage(pw, cmdLine.length() + 7, cmdLine, options);
		pw.flush();
	}
	
	
	/**
	 * Displays all command line options in log messages.
	 * @param line
	 */
	public static void logCommandLineOptions(Logger logger, CommandLine line) {
		for (Option myOption : line.getOptions()) {
			String message;
			String opt = "";
			if(myOption.getOpt() != null) {
				opt+="-"+myOption.getOpt();
				if(myOption.getLongOpt() != null) 
					opt+=" (--"+myOption.getLongOpt()+")";
			} else
				opt+="--"+myOption.getLongOpt()+"";
				
			if(myOption.hasArg()) 
			    message = opt + " " + myOption.getValue();
			else
				message = opt;
				
			
			logger.info("with option: " + message);
		}
	}

}
