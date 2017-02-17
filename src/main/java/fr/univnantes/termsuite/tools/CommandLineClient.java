package fr.univnantes.termsuite.tools;

import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.OutputStreamAppender;
import fr.univnantes.termsuite.api.IndexedCorpusIO;
import fr.univnantes.termsuite.api.TermSuite;
import fr.univnantes.termsuite.model.IndexedCorpus;
import fr.univnantes.termsuite.model.Lang;
import fr.univnantes.termsuite.model.Property;
import fr.univnantes.termsuite.model.RelationProperty;
import fr.univnantes.termsuite.model.TermProperty;
import fr.univnantes.termsuite.tools.opt.CliOption;
import fr.univnantes.termsuite.tools.opt.ClientHelper;
import fr.univnantes.termsuite.tools.opt.OptType;

public abstract class CommandLineClient {
	private static final Logger LOGGER = LoggerFactory.getLogger(CommandLineClient.class);
	
	protected ClientHelper clientHelper = new ClientHelper(this);
	
	protected String description;
	public abstract void configureOpts();
	protected abstract void run() throws Exception;
	protected Options options = new Options();
	protected CommandLine line = null;

	protected List<Set<CliOption>> atLeastOneOfBags= new ArrayList<>();
	protected List<Set<CliOption>> atMostOneOfBags= new ArrayList<>();
	protected List<Set<CliOption>> exactlyOneOfBags= new ArrayList<>();
	protected Set<CliOption> declaredOptions = new HashSet<>();
	protected Set<CliOption> facultativeOptions = new HashSet<>();
	protected Set<CliOption> mandatoryOptions = new HashSet<>();
	protected Map<CliOption, Set<CliOption>> conditionalOptions = new HashMap<>();
	protected Map<CliOption, Set<CliOption>> conditionalAbsentOptions = new HashMap<>();

	public CommandLineClient(String description) {
		super();
		this.description = description;
	}
	public void declareAtMostOneOf(CliOption first, CliOption second, CliOption... others) {
		atMostOneOfBags.add(toBag(first, second, others));
	}

	public void declareAtLeastOneOf(CliOption first, CliOption second, CliOption... others) {
		atLeastOneOfBags.add(toBag(first, second, others));
	}
	
	public void declareExactlyOneOf(CliOption first, CliOption second, CliOption... others) {
		exactlyOneOfBags.add(toBag(first, second, others));
	}
	
	private Set<CliOption> toBag(CliOption first, CliOption second, CliOption... others) {
		Set<CliOption> bag = new HashSet<>();
		bag.add(first);
		if(!declaredOptions.contains(first))
			declareFacultative(first);
		bag.add(second);
		if(!declaredOptions.contains(second))
			declareFacultative(second);
		for(CliOption opt:others) {
			bag.add(opt);
			if(!declaredOptions.contains(opt))
				declareFacultative(opt);
		}
		return bag;
	}
	
	public void declareMandatory(CliOption option) {
		Option opt = addDeclaredOption(option);
		opt.setRequired(true);
		mandatoryOptions.add(option);
	}
	
	/**
	 * 
	 * Declare some option mandatory only when another 
	 * facultative (optional) option is present.
	 * 
	 * @param conditionalOpt
	 * @param mandatoryOptsIfPresent
	 */
	public void declareConditional(CliOption conditionalOpt, CliOption... mandatoryOptsIfPresent) {
		Preconditions.checkArgument(declaredOptions.contains(conditionalOpt), "%s must be declared as optional", conditionalOpt);
		Set<CliOption> set = Sets.newHashSet(mandatoryOptsIfPresent);
		for(CliOption opt:set)
			if(!declaredOptions.contains(opt))
				declareFacultative(opt);
		if(conditionalOptions.containsKey(conditionalOpt))
			conditionalOptions.get(conditionalOpt).addAll(set);
		else
			conditionalOptions.put(conditionalOpt, set);
	}
	
	/**
	 * 
	 * Declares a list of options that cannot appear when
	 * a conditional option is set.
	 * 
	 * @param conditionalOpt
	 * @param mustBeAbsentOnCondition
	 */
	public void declareCannotAppearWhenCondition(CliOption conditionalOpt, CliOption... mustBeAbsentOnCondition) {
		Preconditions.checkArgument(declaredOptions.contains(conditionalOpt), "%s must be declared as optional", conditionalOpt);
		Set<CliOption> set = Sets.newHashSet(mustBeAbsentOnCondition);
		if(conditionalAbsentOptions.containsKey(conditionalOpt))
			conditionalAbsentOptions.get(conditionalOpt).addAll(set);
		else
			conditionalAbsentOptions.put(conditionalOpt, set);

	}

	
	public void declareFacultative(CliOption option) {
		Option opt = addDeclaredOption(option);
		opt.setRequired(false);
		facultativeOptions.add(option);
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
		if(option.getArgType() == OptType.T_TERM_LIST)
			opt.setArgs(Option.UNLIMITED_VALUES);
		options.addOption(opt);
		return opt;
	}
	
	public Optional<String> getOpt(CliOption opt) {
		if(isSet(opt))
			return Optional.of(line.getOptionValue(opt.getOptName()));
		else
			return Optional.empty();
	}

	public boolean hasOpt(CliOption opt) {
		Preconditions.checkState(
				line != null, "Command line not parsed");
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
	
	public Path asPath(CliOption opt) {
		Preconditions.checkState(opt.hasArg());
		Path path = Paths.get(getOpt(opt).get().trim());
		if(path.getParent() != null)
			path.getParent().toFile().mkdirs();
		return path;
	}

	protected IndexedCorpus asIndexedCorpus(CliOption path) {
		return IndexedCorpusIO.fromJson(asPath(path));
	}

	public Path asDir(CliOption opt) {
		Path path = asPath(opt);
		if(path.toFile().exists())
			Preconditions.checkArgument(path.toFile().isDirectory(), "Not a directory: ", path);
		else
			path.toFile().mkdirs();
		return path;
	}
	
	public boolean asBoolean(CliOption opt) {
		Preconditions.checkState(!opt.hasArg());
		return hasOpt(opt);
	}

	public int asInt(CliOption opt) {
		Preconditions.checkState(opt.hasArg());
		return Integer.parseInt(getOpt(opt).get().trim());
	}

	public List<String> getList(CliOption opt) {
		Preconditions.checkState(opt.hasArg());
		return Splitter.on(",")
				.splitToList(getOpt(opt).get()
						.trim()
						.replaceAll("^\"", "")
						.replaceAll("\"$", "")
						);
	}
	
	public boolean isSet(CliOption opt) {
		return line.hasOption(opt.getOptName());
	}

	public long asLong(CliOption opt) {
		Preconditions.checkState(opt.hasArg());
		return Long.parseLong(getOpt(opt).get().trim());
	}
	
	public List<String> asList(CliOption opt) {
		return Splitter.on(",").splitToList(asString(opt));
	}
	
	public String[] asArray(CliOption opt) {
		List<String> list = asList(opt);
		return list.toArray(new String[list.size()]);
	}

	public String asString(CliOption opt) {
		Preconditions.checkState(opt.hasArg());
		return getOpt(opt).get().trim();
	}
	public List<String> asTermString(CliOption opt) {
		Preconditions.checkState(opt.hasArg());
		Preconditions.checkArgument(isSet(opt));
		String[] optionValues = line.getOptionValues(opt.getOptName());
		String paramStr = Joiner.on(" ").join(optionValues);
		List<String> termStrings = new ArrayList<>();
		for(String termStr:paramStr.split(","))
			termStrings.add(termStr.trim().replaceAll("\\s+", " "));
		return termStrings;
	}

	public Lang getLang() {
		return Lang.forName(asString(CliOption.LANGUAGE));
	}

	public double asDouble(CliOption opt) {
		Preconditions.checkState(opt.hasArg());
		return Double.parseDouble(getOpt(opt).get().trim());
	}
	
	public void launch(String... args) {
		configureGeneralOpts();
		configureOpts();
		try {
			this.line = new PosixParser().parse(options, args);
			applyLogConfig();
			if(isSet(CliOption.HELP)) {
				doHelp(System.out);
				return;
			}
			CliUtil.logCommandLineOptions(LOGGER, line);
			checkAtLeastOneOf();
			checkAtMostOneOf();
			checkExactlyOneOf();
			checkConditionals();
			checkConditionalAbsent();
			run();
			if(clientHelper.getHistory().isPresent()) 
				LOGGER.info("Watched terms: {}", clientHelper.getHistory());
		} catch (ParseException e) {
			LOGGER.error("Could not parse arguments {}", e.getMessage());
			throw new TermSuiteCliException("Could not parse arguments. Error: " + e.getMessage(), e);
		} catch (Exception e) {
			LOGGER.error("An unexpected error occurred: " + e.getMessage(), e);
			throw new TermSuiteCliException("An unexpected error occurred: " + e.getMessage(), e);
		}
	}
	
	private void applyLogConfig() {
		boolean loggingActivated = isSet(CliOption.LOG_INFO) 
				|| isSet(CliOption.LOG_DEBUG) 
				|| isSet(CliOption.LOG_TO_FILE);
		
		if(loggingActivated) {
			OutputStreamAppender<ILoggingEvent> appender = 
					isSet(CliOption.LOG_TO_FILE) ?
							LogbackUtils.createFileAppender(asPath(CliOption.LOG_TO_FILE)) :
								LogbackUtils.createConsoleAppender();
			LogbackUtils.activateLogging(appender);
			
			Level level = Level.WARN;
			if(isSet(CliOption.LOG_INFO))
				level = Level.INFO;
			else if(isSet(CliOption.LOG_DEBUG))
				level = Level.DEBUG;
			else if(isSet(CliOption.LOG_TO_FILE))
				level = Level.INFO;
			LogbackUtils.setGlobalLogLevel(level);
		}
	}
	private void configureGeneralOpts() {
		declareFacultative(CliOption.HELP);
		declareFacultative(CliOption.LOG_TO_FILE);
		declareFacultative(CliOption.LOG_DEBUG);
		declareFacultative(CliOption.LOG_INFO);
		declareAtMostOneOf(CliOption.LOG_INFO, CliOption.LOG_DEBUG);
	}
	
	protected void runClient(String[] args) {
		try {
			launch(args);
		} catch(TermSuiteCliException e) {
			System.out.flush();
			System.err.flush();
			System.err.format("ERROR: %s%n---%nRun again with option --%s for more information", e.getMessage());
			System.exit(1);
		}
	}

	public void doHelp(PrintStream out) {
		out.format("Usage:%n\tjava [-Xms256m -Xmx8g] -cp termsuite-core-%s.jar %s OPTIONS %n",
				TermSuite.currentVersion(),
				this.getClass().getCanonicalName());
		out.format("%nDescription:%n\t%s%n", description);
		out.format("%nMandatory options:%n");
		for(CliOption opt:mandatoryOptions) {
			out.format("\t%-3s --%-35s %s%n", 
					opt.getOptShortName() == null ? "" : ("-"+opt.getOptShortName()+""),
					opt.getOptName() + " " + (opt.hasArg() ? opt.getArgType().getStr() : ""),
					opt.getDescription()
							
				);
		}
		out.format("%nOther options:%n");
		List<CliOption> otherOptions = getSortedDeclaredOptions();
		otherOptions.removeAll(mandatoryOptions);
		for(CliOption opt:otherOptions) {
			out.format("\t%-3s --%-35s %s%n", 
					opt.getOptShortName() == null ? "" : ("-"+opt.getOptShortName()+""),
					opt.getOptName() + " " + (opt.hasArg() ? opt.getArgType().getStr() : ""),
					opt.getDescription()
							
				);
		}

	}
	
	private List<CliOption> getSortedDeclaredOptions() {
		ArrayList<CliOption> sorted = Lists.newArrayList(declaredOptions);
		sorted.sort(new Comparator<CliOption>() {
			@Override
			public int compare(CliOption o1, CliOption o2) {
				if(mandatoryOptions.contains(o1) && !mandatoryOptions.contains(o2))
					return -1;
				else if(mandatoryOptions.contains(o2) && !mandatoryOptions.contains(o1))
					return 1;
				else
					return o1.compareTo(o2);
			}
		});
		return sorted;
	}
	
	private void checkConditionals() {
		for(CliOption condition:conditionalOptions.keySet()) {
			if(!isSet(condition)) {
				for(CliOption dependingOpt:conditionalOptions.get(condition)) {
					if(isSet(dependingOpt)) {
						CliUtil.throwException("Option --%s can be set only when option --%s is present", 
								dependingOpt.getOptName(), 
								condition.getOptName());
					}
				}
			}
		}
	}

	private void checkConditionalAbsent() {
		for(CliOption condition:conditionalAbsentOptions.keySet()) {
			if(isSet(condition)) {
				for(CliOption dependingOpt:conditionalAbsentOptions.get(condition)) {
					if(isSet(dependingOpt)) {
						CliUtil.throwException("Option --%s cannot be set when option --%s is present", 
								dependingOpt.getOptName(), 
								condition.getOptName());
					}
				}
			}
		}
	}
	

	public void checkExactlyOneOf() {
		for(Set<CliOption> bag:exactlyOneOfBags) {
			Set<CliOption> foundOpts = new HashSet<>();
			for(CliOption opt:bag) 
				if(isSet(opt))
					foundOpts.add(opt);
			if(foundOpts.size() != 1)
				CliUtil.throwExactlyOne(bag);
		}
	}
	
	public void checkAtMostOneOf() {
		for(Set<CliOption> bag:atMostOneOfBags) {
			Set<CliOption> foundOpts = new HashSet<>();
			for(CliOption opt:bag) 
				if(isSet(opt))
					foundOpts.add(opt);
			if(foundOpts.size() > 1)
				CliUtil.throwAtMost(bag);
		}
	}
	
	public void checkAtLeastOneOf() {
		for(Set<CliOption> bag:atLeastOneOfBags) {
			boolean found = false;
			for(CliOption opt:bag) 
				found |= isSet(opt);
			if(!found)
				CliUtil.throwAtLeast(bag);
		}
	}
	
	public Property<?> asProperty(CliOption opt) {
		String propertyName = asString(opt);
		return asProperty(propertyName);
	}
	
	public Property<?> asProperty(String propertyName) {
		try {
			return TermProperty.forName(propertyName);
		} catch(IllegalArgumentException e) {
			try {
				return RelationProperty.forName(propertyName);
			} catch(IllegalArgumentException e2) {
				throw new TermSuiteCliException("No such term or relation property");
			}
		}
	}

	
	public TermProperty asTermProperty(String pName) {
		try {
			return TermProperty.forName(pName);
		} catch(IllegalArgumentException e) {
			throw new TermSuiteCliException(e);
		}
	}
	
	public TermProperty asTermProperty(CliOption opt) {
		return asTermProperty(asString(opt));
	}
	
	public RelationProperty asRelationProperty(CliOption opt) {
		return asRelationProperty(asString(opt));
	}
	private RelationProperty asRelationProperty(String pName) {
		try {
			return RelationProperty.forName(pName);
		} catch(IllegalArgumentException e) {
			throw new TermSuiteCliException(e);
		}
	}

	public Class<?> asClass(CliOption opt) {
		try {
			return (Class<?>)Class.forName(asString(opt));
		} catch (ClassNotFoundException e) {
			CliUtil.throwException("No suitable class found for option %s", opt);
			return null;
		}
	}
}
