package fr.univnantes.termsuite.tools;

import static java.util.stream.Collectors.joining;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Stream;

import fr.univnantes.termsuite.tools.opt.CliOption;

public class CliUtil {
	public static void throwAtLeast(Collection<CliOption> opts) {
		throwAtLeast(asArray(opts));
	}

	public static void throwAtLeast(CliOption... opts) {
		throwException("At least one option in %s must be set", nameStream(opts).collect(joining(", ")));
	}

	public static void throwAtMost(Collection<CliOption> opts) {
		throwAtMost(asArray(opts));
	}
	
	public static void throwAtMost(CliOption... opts) {
		throwException("At most one option in %s must be set", nameStream(opts).collect(joining(", ")));
	}

	public static void throwExactlyOne(Collection<CliOption> opts) {
		throwExactlyOne(asArray(opts));
	}

	private static CliOption[] asArray(Collection<CliOption> opts) {
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
}
