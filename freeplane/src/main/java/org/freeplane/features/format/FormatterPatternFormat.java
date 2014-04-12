/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2011 Volker Boerchers
 *
 *  This file author is Volker Boerchers
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
package org.freeplane.features.format;

import java.text.FieldPosition;
import java.util.Formatter;

/**
 * @author Volker Boerchers
 */
class FormatterPatternFormat extends PatternFormat {
	private final Formatter formatter;
	
	public FormatterPatternFormat(String pattern, String type) {
		super(pattern, type);
		this.formatter = new Formatter();
	}

	@Override
	public final String formatObject (Object obj) {
		return formatter.format(getPattern(), obj).toString();
	}
	/**
	 * Formats an object via {@link Formatter#format(String, Object...)}.
	 * 
	 * @param obj  the object to format
	 * @param toAppendTo  the buffer to append to
	 * @param pos  the position - ignored
	 * @return the buffer passed in
	 */
	public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
		final String formatted = formatObject(obj);
		toAppendTo.append(formatted);
		return toAppendTo;
	}

	@Override
    public String getStyle() {
	    return PatternFormat.STYLE_FORMATTER;
    }
}
