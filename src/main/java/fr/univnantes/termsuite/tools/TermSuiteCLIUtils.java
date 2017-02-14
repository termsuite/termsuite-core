
/*******************************************************************************
 * Copyright 2015-2016 - CNRS (Centre National de Recherche Scientifique)
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *******************************************************************************/

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
package fr.univnantes.termsuite.tools;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	/**
	 * Instances should NOT be constructed in standard programming.
	 */
	private TermSuiteCLIUtils() {}

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
	private static Option createOption(String opt, String longOpt,
			boolean hasArg, String description, boolean isMandatory) {
		Option option = new Option(opt, longOpt, hasArg, description);
		option.setRequired(isMandatory);
		return option;
	}

	public static Option createMandatoryOption(String opt, String longOpt,
			boolean hasArg, String description) {
		return createOption(opt, longOpt, hasArg, description, true);
	}

	public static Option createOptionalOption(String opt, String longOpt,
			boolean hasArg, String description) {
		return createOption(opt, longOpt, hasArg, description, false);
	}

	
	public static Option createMandatoryOption(String longOpt,
			boolean hasArg, String description) {
		return createOption(null, longOpt, hasArg, description, true);
	}

	public static Option createOptionalOption(String longOpt,
			boolean hasArg, String description) {
		return createOption(null, longOpt, hasArg, description, false);
	}

	
	private static String in = null;
	/**
	 * Read the standard input as a string.
	 * @return
	 * @throws IOException
	 */
	static String readIn(String encoding) throws IOException {
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
	static void exitWithErrorMessage(String aMessage) {
		LOGGER.error(aMessage);
		System.err.println(aMessage);
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
	static void printUsage(ParseException e, String cmdLine,
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
	static void logCommandLineOptions(CommandLine line) {
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
	public static Stopwatch startChrono() {
		stopwatch = Stopwatch.createStarted();
		return stopwatch;
	}
	public static void stopChrono() {
		stopwatch.stop();
		LOGGER.info("CLI stopped after " + stopwatch.toString());
	}

	static void logToFile(String path) {
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

	static void disableLogging() {
		LoggerContext loggerContext = (LoggerContext)LoggerFactory.getILoggerFactory();
		loggerContext.stop();
	}
	
	static void setGlobalLogLevel(String levelString) {
		setGlobalLogLevel(getLevel(levelString));
	}
	
	private static void setGlobalLogLevel(Level level) {
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

	private static Level getLevel(String logLevelString) {
		for(Level level:ImmutableList.of(Level.ALL, Level.ERROR, Level.WARN, Level.INFO, Level.DEBUG, Level.TRACE)) {
			if(level.toString().toLowerCase().equals(logLevelString.toLowerCase()))
				return level;
		}
		throw new IllegalArgumentException("Invalid log level: " + logLevelString);
	}

}
