/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collection;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.freeplane.core.util.logging.LogHandlers;

/**
 * An OutputStream that writes contents to a Logger upon each call to flush()
 * See http://blogs.sun.com/nickstephen/entry/java_redirecting_system_out_and
 */
class LoggingOutputStream extends ByteArrayOutputStream {
	final private Level level;
	final private String lineSeparator;
	private int availableSpace;
	private final PrintStream out;

	/**
	 * Constructor
	 * @param out
	 *            Logger to write to
	 * @param stdout
	 * @param mFileHandler
	 *            Level at which to write the log message
	 */
	public LoggingOutputStream(Level level, final PrintStream out, int maximumLogSize) {
		super();
		this.out = out;
		this.level = level;
		this.availableSpace = maximumLogSize;
		lineSeparator = System.getProperty("line.separator");
	}

	/**
	 * upon flush() write the existing contents of the OutputStream to the
	 * logger as a log record.
	 *
	 * @throws java.io.IOException
	 *             in case of error
	 */
	@Override
	public void flush() throws IOException {
		out.flush();
		String record;
		synchronized (this) {
			super.flush();
			record = this.toString();
			super.reset();
		}
		if (record.length() == 0 || record.equals(lineSeparator)) {
			return;
		}
		final Collection<Handler> handlers = LogHandlers.getHandlers();
		final LogRecord logRecord = new LogRecord(level, record);
		logRecord.setLoggerName(LoggingOutputStream.class.getName());
		for(Handler handler : handlers) {
				handler.publish(logRecord);
		}
	}

	@Override
	public synchronized void write(int b) {
		out.write(b);
		if(availableSpace > 0) {
			availableSpace--;
			super.write(b);
		}
	}

	@Override
	public synchronized void write(byte[] b, int off, int len) {
		out.write(b, off, len);
		if(availableSpace > 0) {
			availableSpace-=len;
			super.write(b, off, len);
		}
	}


}
