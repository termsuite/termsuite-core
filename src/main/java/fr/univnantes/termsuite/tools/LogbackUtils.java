
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

import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.FileAppender;
import ch.qos.logback.core.OutputStreamAppender;



/**
 * 
 * Util class from configuring Logback programmatically
 * 
 * @author Damien Cram
 */
public final class LogbackUtils {
	/**
	 * Instances should NOT be constructed in standard programming.
	 */
	private LogbackUtils() {}
	
	public static final Level LEVEL_INFO = Level.INFO;
	public static final Level LEVEL_DEBUG = Level.DEBUG;

	static void activateLogging(OutputStreamAppender<ILoggingEvent> appender) {
		PatternLayoutEncoder ple = new PatternLayoutEncoder();
		ple.setPattern("%date %level [%thread] %logger{10} [%file:%line] %msg%n");
		ple.setContext(getContext());
		ple.start();
		appender.setContext(getContext());
		appender.setEncoder(ple);
		appender.start();
		
		// add appender to any Logger (here is root)
		ch.qos.logback.classic.Logger rootLogger = getRootLogger();
		rootLogger.addAppender(appender);
	}

	public static OutputStreamAppender<ILoggingEvent> createFileAppender(Path path) {
		FileAppender<ILoggingEvent> fa = new FileAppender<ILoggingEvent>();
		fa.setFile(path.toString());
		fa.setAppend(true);
		fa.setName("FileLogger");
		return fa;
	}
	
	public static OutputStreamAppender<ILoggingEvent> createConsoleAppender() {
		ConsoleAppender<ILoggingEvent> fa = new ConsoleAppender<ILoggingEvent>();
		fa.setName("ConsoleLogger");
		return fa;
	}
	
	
	public static void disableLogging() {
		getContext().stop();
	}

	public static LoggerContext getContext() {
		return (LoggerContext)LoggerFactory.getILoggerFactory();
	}
	
	static void setGlobalLogLevel(String levelString) {
		setGlobalLogLevel(getLevel(levelString));
	}
	
	public static void setGlobalLogLevel(Level level) {
		ch.qos.logback.classic.Logger logger = getRootLogger();
		logger.setLevel(level);
	}

	public static ch.qos.logback.classic.Logger getRootLogger() {
		return (ch.qos.logback.classic.Logger)LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
	}

	public static void setLogLevel(Object... loggerOverridings) {
		Preconditions.checkArgument(loggerOverridings.length%2==0);
		for(int i=0; i<loggerOverridings.length; i+=2) {
			Class<?> cls = (Class<?>)loggerOverridings[i];
			Level level = getLevel((String)loggerOverridings[i+1]);
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
