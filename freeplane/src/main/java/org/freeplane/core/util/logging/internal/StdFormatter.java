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
package org.freeplane.core.util.logging.internal;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

class StdFormatter extends SimpleFormatter {
	private static class StdOutErrLevel extends Level {
		/**
		 *
		 */
		private static final long serialVersionUID = 1L;

		public StdOutErrLevel(final String name, final int value) {
			super(name, value);
		}
	}

	/**
	 * Level for STDERR activity
	 */
	public final static Level STDERR = new StdOutErrLevel("STDERR", Level.SEVERE.intValue());
	/**
	 * Level for STDOUT activity.
	 */
	public final static Level STDOUT = new StdOutErrLevel("STDOUT", Level.WARNING.intValue());
	final private String lineSeparator = System.getProperty("line.separator");

	/**
	 * Format the given LogRecord.
	 *
	 * @param record
	 *            the log record to be formatted.
	 * @return a formatted log record
	 */
	@Override
	public synchronized String format(final LogRecord record) {
		if (!StdFormatter.STDERR.getName().equals(record.getLevel().getName())
		        && !StdFormatter.STDOUT.getName().equals(record.getLevel().getName())) {
			return super.format(record);
		}
		final StringBuilder sb = new StringBuilder();
		final String message = formatMessage(record);
		sb.append(record.getLevel().getLocalizedName());
		sb.append(": ");
		sb.append(message.trim());
		sb.append(lineSeparator);
		return sb.toString();
	}
}
