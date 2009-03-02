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
import java.util.logging.Logger;

import org.freeplane.core.resources.ResourceController;

/**
 * @author foltin
 */
public class LogTool {
//	private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
	private static final Logger LOGGER = Logger.global;

	public static void logException(final Throwable e) {
		LogTool.logException(e, "");
	}

	public static void logException(final Throwable e, final String comment) {
		LOGGER.log(Level.SEVERE, "An exception occured: " + comment, e);
	}

	public static void logTransferable(final Transferable t) {
		System.err.println();
		System.err.println("BEGIN OF Transferable:\t" + t);
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
		System.err.println("END OF Transferable");
		System.err.println();
	}

	public static void warn(String msg) {
		LOGGER.log(Level.WARNING, msg);
	}
	
	static private boolean loggerCreated = false;

	public static void createLogger() {
		if (loggerCreated) {
			return;
		}
		loggerCreated = true;
		final ResourceController resourceController = ResourceController.getResourceController();
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
			mFileHandler = new FileHandler(resourceController.getFreeplaneUserDirectory() + File.separator + "log",
			    1400000, 5, false);
			mFileHandler.setFormatter(new StdFormatter());
			mFileHandler.setLevel(Level.INFO);
			parentLogger.addHandler(mFileHandler);
			final ConsoleHandler stdConsoleHandler = new ConsoleHandler();
			stdConsoleHandler.setFormatter(new StdFormatter());
			stdConsoleHandler.setLevel(Level.WARNING);
			parentLogger.addHandler(stdConsoleHandler);
			LoggingOutputStream los;
			Logger logger = Logger.getLogger(StdFormatter.STDOUT.getName());
			los = new LoggingOutputStream(logger, StdFormatter.STDOUT);
			System.setOut(new PrintStream(los, true));
			logger = Logger.getLogger(StdFormatter.STDERR.getName());
			los = new LoggingOutputStream(logger, StdFormatter.STDERR);
			System.setErr(new PrintStream(los, true));
		}
		catch (final Exception e) {
			System.err.println("Error creating logging File Handler");
			e.printStackTrace();
		}
	}

	public static void info(String string) {
		LOGGER.log(Level.INFO, string);	    
    }

}
