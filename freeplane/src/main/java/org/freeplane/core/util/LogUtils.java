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
import java.util.logging.Level;
import java.util.logging.Logger;

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
	private static final Logger LOGGER = Logger.getLogger("org.freeplane");
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
