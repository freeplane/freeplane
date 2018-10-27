/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file is modified by Dimitry Polivaev in 2008.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.freeplane.core.util;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.io.File;
import java.io.PrintStream;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.StreamHandler;

import org.freeplane.core.resources.ResourceController;

/**
 * Utilities for logging to the standard logfile.
 * <p>
 * In scripts this class can be accessed via the "global" variable <code>logger</code>,
 * so this is the way to log in scripts:
 * <pre>
 *  try {
 *      logger.info("this node as date: " + node.to.date)
 *  } catch (Exception ex) {
 *      logger.severe('error on conversion of "' + node.text + '" to date', ex)
 *  }
 * </pre>
 *
 * @author foltin
 */
public class LogUtils {
    private static final Logger LOGGER = Logger.global;
	private static final int MAX_LOG_SIZE = 1 * 1024 * 1024;
	static private boolean loggerCreated = false;

	public static void createLogger() {
		if (loggerCreated) {
			return;
		}
		loggerCreated = true;
		FileHandler mFileHandler = null;
		final Logger parentLogger = Logger.getAnonymousLogger().getParent();
		final Handler[] handlers = parentLogger.getHandlers();
		for (int i = 0; i < handlers.length; i++) {
			final Handler handler = handlers[i];
			if (handler instanceof ConsoleHandler) {
				parentLogger.removeHandler(handler);
			}
		}
		try {
			final String logDirectoryPath = getLogDirectory();
			final File logDirectory = new File(logDirectoryPath);
			logDirectory.mkdirs();
			if(logDirectory.isDirectory()){
				final String pathPattern = logDirectoryPath + File.separatorChar + "log";
				mFileHandler = new FileHandler(pathPattern, 1400000, 5, false);
				mFileHandler.setFormatter(new StdFormatter());
				parentLogger.addHandler(mFileHandler);
			}
			final StreamHandler stdConsoleHandler = new StreamHandler(System.out, new StdFormatter()) {
				{
					setLevel(Level.INFO);
				}
				@Override
				public void publish(LogRecord record) {
					super.publish(record);
					flush();
				}
			};

			if(System.getProperty("java.util.logging.config.file", null) == null){
				mFileHandler.setLevel(Level.INFO);
			}
			parentLogger.addHandler(stdConsoleHandler);
			LoggingOutputStream los;
			Logger logger = Logger.getLogger(StdFormatter.STDOUT.getName());
			los = new LoggingOutputStream(logger, StdFormatter.STDOUT, MAX_LOG_SIZE);
			System.setOut(new PrintStream(los, true));
			logger = Logger.getLogger(StdFormatter.STDERR.getName());
			los = new LoggingOutputStream(logger, StdFormatter.STDERR, MAX_LOG_SIZE);
			System.setErr(new PrintStream(los, true));
		}
		catch (final Exception e) {
			LogUtils.warn("Error creating logging File Handler", e);
		}
	}

	public static String getLogDirectory() {
	    final String logDirectory = ResourceController.getResourceController().getFreeplaneUserDirectory() + File.separatorChar + "logs";
	    return logDirectory;
    }

	public static void info(final String string) {
		LOGGER.log(Level.INFO, string);
	}

	public static void info(final Transferable t) {
		System.out.println();
		System.out.println("BEGIN OF Transferable:\t" + t);
		final DataFlavor[] dataFlavors = t.getTransferDataFlavors();
		for (int i = 0; i < dataFlavors.length; i++) {
			System.out.println("  Flavor:\t" + dataFlavors[i]);
			System.out.println("    Supported:\t" + t.isDataFlavorSupported(dataFlavors[i]));
			try {
				System.out.println("    Content:\t" + t.getTransferData(dataFlavors[i]));
			}
			catch (final Exception e) {
			}
		}
		System.out.println("END OF Transferable");
		System.out.println();
	}

	public static void severe(final String message) {
		LOGGER.log(Level.SEVERE, message);
	}

	public static void severe(final String comment, final Throwable e) {
		if(e instanceof SecurityException || e.getCause() instanceof SecurityException)
			warn(comment, e);
		else
			LOGGER.log(Level.SEVERE, comment, e);
	}

	public static void severe(final Throwable e) {
		LogUtils.severe("", e);
	}

	public static void warn(final String msg) {
		LOGGER.log(Level.WARNING, msg);
	}

	public static void warn(final String comment, final Throwable e) {
		LOGGER.log(Level.WARNING, comment, e);
	}

	public static void warn(final Throwable e) {
		LogUtils.warn("", e);
	}

	public static Logger getLogger() {
	    return LOGGER;
    }
}
