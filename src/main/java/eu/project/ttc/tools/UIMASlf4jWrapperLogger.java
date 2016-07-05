
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

package eu.project.ttc.tools;

import java.io.OutputStream;
import java.io.PrintStream;

import org.apache.uima.UIMAFramework;
import org.apache.uima.resource.ResourceManager;
import org.apache.uima.util.Level;
import org.apache.uima.util.Logger;
import org.slf4j.LoggerFactory;

public class UIMASlf4jWrapperLogger implements org.apache.uima.util.Logger {
	org.slf4j.Logger slf4jLogger = null;

	private UIMASlf4jWrapperLogger(Class<?> component) {
		this.slf4jLogger = LoggerFactory.getLogger(component);
	}

	public static synchronized Logger getInstance(Class<?> component) {
		return new UIMASlf4jWrapperLogger(component);
	}

	private static UIMASlf4jWrapperLogger defaultLogger = null;

	public static synchronized Logger getInstance() {
		if (defaultLogger == null)
			defaultLogger = new UIMASlf4jWrapperLogger(UIMAFramework.class);
		return defaultLogger;
	}

	@Override
	public void log(String aMessage) {
		this.slf4jLogger.info(aMessage);
	}

	@Override
	public void log(String aResourceBundleName, String aMessageKey, Object[] aArguments) {
		log(org.apache.uima.util.Level.INFO, "[UIMA resource: " + aResourceBundleName + "] " + aMessageKey, aArguments);
	}

	@Override
	public void logException(Exception aException) {
		this.slf4jLogger.error(aException.getMessage(), aException);
	}

	@Override
	public void setOutputStream(PrintStream aStream) {
	}

	@Override
	public void setOutputStream(OutputStream aStream) {
	}

	@Override
	public void log(Level level, String msg) {
		switch (level.toInteger()) {
		case org.apache.uima.util.Level.OFF_INT:
			return;
		case org.apache.uima.util.Level.SEVERE_INT:
			this.slf4jLogger.error(msg);
		case org.apache.uima.util.Level.WARNING_INT:
			this.slf4jLogger.warn(msg);
		case org.apache.uima.util.Level.INFO_INT:
			this.slf4jLogger.info(msg);
		case org.apache.uima.util.Level.CONFIG_INT:
			this.slf4jLogger.debug(msg);
		case org.apache.uima.util.Level.FINE_INT:
			this.slf4jLogger.debug(msg);
		case org.apache.uima.util.Level.FINER_INT:
			this.slf4jLogger.trace(msg);
		case org.apache.uima.util.Level.FINEST_INT:
			this.slf4jLogger.trace(msg);
		default: // for all other cases return Level.ALL
			this.slf4jLogger.trace(msg);
		}
	}

	@Override
	public void log(Level level, String msg, Object param1) {
		switch (level.toInteger()) {
		case org.apache.uima.util.Level.OFF_INT:
			return;
		case org.apache.uima.util.Level.SEVERE_INT:
			this.slf4jLogger.error(msg, param1);
		case org.apache.uima.util.Level.WARNING_INT:
			this.slf4jLogger.warn(msg, param1);
		case org.apache.uima.util.Level.INFO_INT:
			this.slf4jLogger.info(msg, param1);
		case org.apache.uima.util.Level.CONFIG_INT:
			this.slf4jLogger.debug(msg, param1);
		case org.apache.uima.util.Level.FINE_INT:
			this.slf4jLogger.debug(msg, param1);
		case org.apache.uima.util.Level.FINER_INT:
			this.slf4jLogger.trace(msg, param1);
		case org.apache.uima.util.Level.FINEST_INT:
			this.slf4jLogger.trace(msg, param1);
		default: // for all other cases return Level.ALL
			this.slf4jLogger.trace(msg, param1);
		}
	}

	@Override
	public void log(Level level, String msg, Object[] params) {
		switch (level.toInteger()) {
		case org.apache.uima.util.Level.OFF_INT:
			return;
		case org.apache.uima.util.Level.SEVERE_INT:
			this.slf4jLogger.error(msg, params);
		case org.apache.uima.util.Level.WARNING_INT:
			this.slf4jLogger.warn(msg, params);
		case org.apache.uima.util.Level.INFO_INT:
			this.slf4jLogger.info(msg, params);
		case org.apache.uima.util.Level.CONFIG_INT:
			this.slf4jLogger.debug(msg, params);
		case org.apache.uima.util.Level.FINE_INT:
			this.slf4jLogger.debug(msg, params);
		case org.apache.uima.util.Level.FINER_INT:
			this.slf4jLogger.trace(msg, params);
		case org.apache.uima.util.Level.FINEST_INT:
			this.slf4jLogger.trace(msg, params);
		default: // for all other cases return Level.ALL
			this.slf4jLogger.trace(msg, params);
		}
	}

	@Override
	public void log(Level level, String msg, Throwable throwable) {
		switch (level.toInteger()) {
		case org.apache.uima.util.Level.OFF_INT:
			return;
		case org.apache.uima.util.Level.SEVERE_INT:
			this.slf4jLogger.error(msg, throwable);
		case org.apache.uima.util.Level.WARNING_INT:
			this.slf4jLogger.warn(msg, throwable);
		case org.apache.uima.util.Level.INFO_INT:
			this.slf4jLogger.info(msg, throwable);
		case org.apache.uima.util.Level.CONFIG_INT:
			this.slf4jLogger.debug(msg, throwable);
		case org.apache.uima.util.Level.FINE_INT:
			this.slf4jLogger.debug(msg, throwable);
		case org.apache.uima.util.Level.FINER_INT:
			this.slf4jLogger.trace(msg, throwable);
		case org.apache.uima.util.Level.FINEST_INT:
			this.slf4jLogger.trace(msg, throwable);
		default: // for all other cases return Level.ALL
			this.slf4jLogger.trace(msg, throwable);
		}
	}

	@Override
	public void logrb(Level level, String sourceClass, String sourceMethod, String bundleName, String msgKey) {
		log(level, String.format("[sourceMethod: %s] %s", sourceClass, sourceMethod,
				bundleName, msgKey));
	}

	@Override
	public void logrb(Level level, String sourceClass, String sourceMethod, String bundleName, String msgKey,
			Object param1) {
		log(level, String.format("[sourceClass: %s, sourceMethod: %s, bundleName: %s] %s", sourceClass, sourceMethod,
				bundleName, msgKey), param1);
	}

	@Override
	public void logrb(Level level, String sourceClass, String sourceMethod, String bundleName, String msgKey,
			Object[] params) {
		log(level, String.format("[sourceClass: %s, sourceMethod: %s, bundleName: %s] %s", sourceClass, sourceMethod,
				bundleName, msgKey), params);
	}

	@Override
	public void logrb(Level level, String sourceClass, String sourceMethod, String bundleName, String msgKey,
			Throwable thrown) {
		log(level, String.format("[sourceClass: %s, sourceMethod: %s, bundleName: %s] %s", sourceClass, sourceMethod,
				bundleName, msgKey), thrown);

	}

	@Override
	public void log(String wrapperFQCN, Level level, String msg, Throwable throwable) {
		log(level, msg, throwable);
	}

	@Override
	public boolean isLoggable(Level level) {
		return true;
	}

	@Override
	public void setLevel(Level level) {
	}

	@Override
	public void setResourceManager(ResourceManager resourceManager) {
	}

}
