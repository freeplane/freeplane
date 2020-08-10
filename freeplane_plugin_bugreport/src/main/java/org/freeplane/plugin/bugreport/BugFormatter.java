/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2009 Dimitry
 *
 *  This file author is Dimitry
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
package org.freeplane.plugin.bugreport;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * @author Dimitry Polivaev
 * 13.06.2009
 */
public class BugFormatter extends Formatter {
	@Override
	public String format(final LogRecord record) {
		final String message = record.getMessage();
		final StringBuilder sb = new StringBuilder();
		sb.append(message);
		sb.append('\n');
		if (record.getThrown() != null) {
			try (final StringWriter sw = new StringWriter();
                final PrintWriter pw = new PrintWriter(sw);
			){
				record.getThrown().printStackTrace(pw);
				sb.append(sw.toString());
			}
			catch (final Exception ex) {/**/}
		}
		return sb.toString();
	}
}
