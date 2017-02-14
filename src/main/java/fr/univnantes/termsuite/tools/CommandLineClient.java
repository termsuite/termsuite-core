package fr.univnantes.termsuite.tools;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;

import fr.univnantes.termsuite.api.TermSuiteException;
import fr.univnantes.termsuite.model.Lang;
import fr.univnantes.termsuite.utils.TermHistory;

public abstract class CommandLineClient {
	private static final Logger LOGGER = LoggerFactory.getLogger(CommandLineClient.class);
	
	
	protected abstract void configureOpts();
	protected abstract void run() throws Exception;
	protected Options options = new Options();
	protected CommandLine line = null;
	protected Optional<TermHistory> history = Optional.empty();

	protected List<Set<CliOption>> atLeastOneOfBags= new ArrayList<>();
	protected List<Set<CliOption>> exactlyOneOfBags= new ArrayList<>();
	protected Set<CliOption> declaredOptions = new HashSet<>();

	protected void declareResourceOpts() {
		declareOptional(CliOption.RESOURCE_DIR);
		declareOptional(CliOption.RESOURCE_JAR);
		declareOptional(CliOption.RESOURCE_URL_PREFIX);
	}
	
	protected void declareAtLeastOneOf(CliOption first, CliOption second, CliOption... others) {
		atLeastOneOfBags.add(toBag(first, second, others));
	}
	
	protected void declareExactlyOneOf(CliOption first, CliOption second, CliOption... others) {
		exactlyOneOfBags.add(toBag(first, second, others));
	}
	
	private Set<CliOption> toBag(CliOption first, CliOption second, CliOption... others) {
		Set<CliOption> bag = new HashSet<>();
		bag.add(first);
		declareOptional(first);
		bag.add(second);
		declareOptional(second);
		for(CliOption opt:others) {
			bag.add(opt);
			declareOptional(opt);
		}
		return bag;
	}
	
	protected void declareMandatory(CliOption option) {
		Option opt = addDeclaredOption(option);
		opt.setRequired(true);
	}
	
	protected void declareOptional(CliOption option) {
		Option opt = addDeclaredOption(option);
		opt.setRequired(false);
	}
	private Option addDeclaredOption(CliOption option) {
		Preconditions.checkArgument(
				!declaredOptions.contains(option),
				"Option %s already declared.", 
				option);
		declaredOptions.add(option);
		Option opt = new Option(
				option.getOptShortName(), 
				option.getOptName(), 
				option.hasArg(), 
				option.getDescription());
		options.addOption(opt);
		return opt;
	}
	
	protected Optional<String> getOpt(CliOption opt) {
		if(isSet(opt))
			return Optional.of(line.getOptionValue(opt.getOptName()));
		else
			return Optional.empty();
	}

	protected boolean hasOpt(CliOption opt) {
		checkClientState(opt);
		return line.hasOption(opt.getOptName());
	}
	public void checkClientState(CliOption opt) {
		Preconditions.checkState(
				line != null, "Command line not parsed");
		Preconditions.checkArgument(
				!declaredOptions.contains(opt),
				"Option %s not declared", 
				opt);
	}
	
	protected Path asPath(CliOption opt) {
		Preconditions.checkState(opt.hasArg());
		Path path = Paths.get(getOpt(opt).get().trim());
		if(path.getParent() != null)
			path.getParent().toFile().mkdirs();
		return path;
	}
	
	protected Path asDir(CliOption opt) {
		Path path = asPath(opt);
		if(path.toFile().exists())
			Preconditions.checkArgument(path.toFile().isDirectory(), "Not a directory: ", path);
		else
			path.toFile().mkdirs();
		return path;
	}
	
	protected boolean asBoolean(CliOption opt) {
		Preconditions.checkState(!opt.hasArg());
		return hasOpt(opt);
	}

	protected int asInt(CliOption opt) {
		Preconditions.checkState(opt.hasArg());
		return Integer.parseInt(getOpt(opt).get().trim());
	}

	protected List<String> getList(CliOption opt) {
		Preconditions.checkState(opt.hasArg());
		return Splitter.on(",")
				.splitToList(getOpt(opt).get()
						.trim()
						.replaceAll("^\"", "")
						.replaceAll("\"$", "")
						);
	}

	
	protected boolean isSet(CliOption opt) {
		return line.hasOption(opt.getOptName());
	}

	protected long asLong(CliOption opt) {
		Preconditions.checkState(opt.hasArg());
		return Long.parseLong(getOpt(opt).get().trim());
	}
	
	protected String asString(CliOption opt) {
		Preconditions.checkState(opt.hasArg());
		return getOpt(opt).get().trim();
	}

	protected Lang getLang() {
		return Lang.forName(asString(CliOption.LANGUAGE));
	}


	protected double asDouble(CliOption opt) {
		Preconditions.checkState(opt.hasArg());
		return Double.parseDouble(getOpt(opt).get().trim());
	}
	
	protected void launch(String[] args) {
		configureOpts();
		try {
			this.line = new PosixParser().parse(options, args);
			checkAtLeastOneOf();
			checkExactlyOneOf();
			run();
			if(history.isPresent()) 
				LOGGER.info("Watched terms: {}", history.get());
		} catch (ParseException e) {
			LOGGER.error("Could not parse arguments {}", e.getMessage());
			throw new TermSuiteException(e);
		} catch (Exception e) {
			LOGGER.error("An unexpected error occurred", e);
			throw new TermSuiteException(e);
		}
	}

	private void checkExactlyOneOf() {
		for(Set<CliOption> bag:exactlyOneOfBags) {
			Set<CliOption> foundOpts = new HashSet<>();
			for(CliOption opt:bag) 
				if(getOpt(opt).isPresent())
					foundOpts.add(opt);
			Preconditions.checkState(
					foundOpts.size() == 1, 
					"Exactly one option in %s must be set. Got: %s", 
					bag,
					foundOpts
					);
		}
	}

	private void checkAtLeastOneOf() {
		for(Set<CliOption> bag:atLeastOneOfBags) {
			boolean found = false;
			for(CliOption opt:bag) 
				found |= getOpt(opt).isPresent();
			Preconditions.checkState(found, "At least one option of %s must be set.", bag);
		}
	}
	
	protected TermHistory createHistory() {
		Preconditions.checkState(isSet(CliOption.WATCH), "History not available when option %s is not set.", CliOption.WATCH);
		history = Optional.of(new TermHistory());
		return history.get();
	}
	

}
