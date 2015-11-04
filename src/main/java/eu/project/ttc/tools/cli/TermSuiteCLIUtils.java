/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright 2, 2015nership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package eu.project.ttc.tools.cli;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableList;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.FileAppender;



/**
 * This class consists of static methods used by the CLI.
 * 
 * @author Sebastián Peña Saldarriaga
 */
public final class TermSuiteCLIUtils {
	private static final Logger LOGGER = LoggerFactory.getLogger(TermSuiteCLIUtils.class);
	
	/** Name of the parameter that must be set to the input directory */
	public static final String P_INPUT_DIR = "directory";
	
	/** Name of the parameter that must be set to the input files' encoding */
	public static final String P_ENCODING = "encoding";
	
	/** Name of the parameter that must be set to the input files' language */
	public static final String P_LANGUAGE = "language";
	
	/** Name of the parameter that must be set to the configuration file */
	public static final String P_PROPERTIES_FILE = "configFile";
	
	/* The corpus format: txt or tei */
	public static final String CORPUS_FORMAT = "corpus-format";

	/* Possible values for argument corpus-format */
	public static final String CORPUS_FORMAT_TXT = "txt";
	public static final String CORPUS_FORMAT_TEI = "tei";

	/**
	 * Instances should NOT be constructed in standard programming.
	 */
	private TermSuiteCLIUtils() {}

	/**
	 * Unconditionally close an <code>InputStream</code>. Equivalent to
	 * {@link InputStream#close()}, except any exceptions will be ignored.
	 * <p>
	 * Code from apache IOUtils.
	 * 
	 * @param input
	 *            A (possibly null) InputStream
	 */
	public static void closeQuietly(InputStream input) {
		if (input == null) {
			return;
		}

		try {
			input.close();
		} catch (IOException ioe) {
		}
	}

	/**
	 * Unconditionally close an <code>OutputStream</code>. Equivalent to
	 * {@link OutputStream#close()}, except any exceptions will be ignored.
	 * <p>
	 * Code from apache IOUtils.
	 * 
	 * @param output
	 *            A (possibly null) OutputStream
	 */
	public static void closeQuietly(OutputStream output) {
		if (output == null) {
			return;
		}

		try {
			output.close();
		} catch (IOException ioe) {
		}
	}

	
	/**
	 * Reads a list of {@link Properties} from the specified
	 * <code>fileName</code> in the user preferences folder.
	 * 
	 * @param fileName
	 *            The name of the file to read
	 * @return The properties read, or <code>null</code> if the file cannot be
	 *         read.
	 */
	@SuppressWarnings("resource")
	public static Properties readPropertiesFileName(String fileName) {
		File preFile = new File(fileName);
		FileInputStream input = null;
		try {
			input = new FileInputStream(preFile);
			Properties props = new Properties();
			props.load(input);
			return props;
		} catch (IOException e) {
			return null;
		} finally {
			closeQuietly(input);
		}
	}


	/**
	 * Creates a mandatory {@link Option} using the specified arguments.
	 * 
	 * @param opt
	 *            The name of the option
	 * @param longOpt
	 *            The long representation of the option
	 * @param hasArg
	 *            Specifies whether the Option takes an argument or not
	 * @param description
	 *            Describes the function of the option
	 * @param isMandatory
	 *            whether the Option is mandatory
	 * @return The option instance
	 */
	public static Option createOption(String opt, String longOpt,
			boolean hasArg, String description, boolean isMandatory) {
		Option option = new Option(opt, longOpt, hasArg, description);
		option.setRequired(isMandatory);
		return option;
	}

	/**
	 * Returns the key of the given option
	 * 
	 * @param opt
	 *            The option
	 * @return {@link Option#getOpt()} if the value returned is not empty, or
	 *         {@link Option#getLongOpt()} otherwise.
	 */
	public static String getOptionKey(Option opt) {
		String key = opt.getOpt();
		if (key == null || key.isEmpty())
			key = opt.getLongOpt();
		return key;
	}

	/**
	 * Sets the <code>value</code> of the specified <code>property</code> if no
	 * value exists for it in the given <code>properties</code>.
	 * 
	 * @param properties
	 *            The property list
	 * @param property
	 *            The property name
	 * @param value
	 *            The value to set
	 */
	public static void setToValueIfNotExists(Properties properties,
			String property, String value) {
		if (properties.getProperty(property) == null)
			properties.setProperty(property, value);
	}

	/**
	 * Determines whether the specified <code>property</code> value is
	 * <code>null</code>.
	 * 
	 * @param properties
	 *            The property list
	 * @param property
	 *            The property name
	 * @return <code>true</code> if the value of <code>property</code> is
	 *         <code>null</code>, or <code>false</code> otherwise.
	 */
	public static boolean isNull(Properties properties, String property) {
		return properties.getProperty(property) == null;
	}
		
	
		
	/**
	 * Reads a file path from a command line option if set and 
	 * return the absolute file path as an optional, or exits with a log message if
	 * the file does not exist. 
	 * @param line the command line
	 * @param optionName the name of the file path option
	 * @return the string optional set with absolute file path if the option is set.
	 */
	public static Optional<String> readFileOption(CommandLine line, String optionName) {
		if(line.getOptionValue(optionName) != null) {
			if(!new File(line.getOptionValue(optionName)).exists()) 
				exitWithErrorMessage("File does not exist: " + line.getOptionValue(optionName));
			else 
				return Optional.of("file:" + new File(line.getOptionValue(optionName)).getAbsolutePath());
		}
		return Optional.absent();
	}
	
	
	private static String in = null;
	/**
	 * Read the standard input as a string.
	 * @return
	 * @throws IOException
	 */
	public static String readIn(String encoding) throws IOException {
		if(in == null) {
			InputStreamReader reader = new InputStreamReader(System.in, encoding);
			List<Character> chars = new LinkedList<Character>();
			int c;
			while(reader.ready() && ((c = reader.read())!= -1)) 
				chars.add((char)c);
			
			char[] charAry = new char[chars.size()];
			for(int i=0; i<chars.size(); i++)
				charAry[i] = chars.get(i);
			in = new String(charAry);
		}
		return in;
	}
	
	/**
	 * Prints an error message to the log and exits.
	 * @param aMessage
	 */
	public static void exitWithErrorMessage(String aMessage) {
		LOGGER.error(aMessage);
		System.exit(1);
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
	public static void logCommandLineOptions(CommandLine line) {
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
				
			
			LOGGER.info("with option: " + message);
		}
	}
	
	
	
	private static Stopwatch stopwatch;
	public static void startChrono() {
		stopwatch = Stopwatch.createStarted();
	}
	public static void stopChrono() {
		stopwatch.stop();
		LOGGER.info("CLI stopped after " + stopwatch.toString());
	}

	public static void logToFile(String path) {
		PatternLayoutEncoder ple = new PatternLayoutEncoder();

		ple.setPattern("%date %level [%thread] %logger{10} [%file:%line] %msg%n");
		ple.setContext((LoggerContext) LoggerFactory.getILoggerFactory());
		ple.start();
		FileAppender<ILoggingEvent> fa = new FileAppender<ILoggingEvent>();
		fa.setName("FileLogger");
		fa.setFile(path);
		fa.setContext((LoggerContext) LoggerFactory.getILoggerFactory());
		fa.setEncoder(ple);
//		fa.setThreshold(ch.qos.logback.classic.Level.DEBUG);
		fa.setAppend(true);
		fa.start();
		
		// add appender to any Logger (here is root)
		ch.qos.logback.classic.Logger rootLogger = (ch.qos.logback.classic.Logger)LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
		rootLogger.addAppender(fa);
	}

	public static void disableLogging() {
		LoggerContext loggerContext = (LoggerContext)LoggerFactory.getILoggerFactory();
		loggerContext.stop();
	}
	
	
	public static void setGlobalLogLevel(String levelString) {
		setGlobalLogLevel(getLevel(levelString));
	}
	
	public static void setGlobalLogLevel(Level level) {
		ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger)LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
		logger.setLevel(level);
	}


	public static void setLogLevel(Object... loggerOverridings) {
		Preconditions.checkArgument(loggerOverridings.length%2==0);
		for(int i=0; i<loggerOverridings.length; i+=2) {
			Class<?> cls = (Class<?>)loggerOverridings[i];
			Level level = getLevel((String)loggerOverridings[i+1]);
			LOGGER.debug("Setting log level {} for class {}", level, cls);
			ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger)LoggerFactory.getLogger(cls);
			logger.setLevel(level);
		}
	}

	public static Level getLevel(String logLevelString) {
		for(Level level:ImmutableList.of(Level.ALL, Level.ERROR, Level.WARN, Level.INFO, Level.DEBUG, Level.TRACE)) {
			if(level.toString().toLowerCase().equals(logLevelString.toLowerCase()))
				return level;
		}
		throw new IllegalArgumentException("Invalid log level: " + logLevelString);
	}
}
