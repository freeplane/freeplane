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

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.Locale;

public class DecimalFormatParser extends Parser {
	private final DecimalFormat parser;

	public DecimalFormatParser(Locale locale) {
		super(Parser.STYLE_DECIMAL, IFormattedObject.TYPE_NUMBER, null);
		parser = (DecimalFormat) NumberFormat.getInstance(locale);
		parser.setGroupingUsed(false);
	}

	@Override
	Object parse(String string) {
		if (string == null)
			return null;
		final ParsePosition parsePosition = new ParsePosition(0);
		final Number result = parser.parse(string, parsePosition);
		if (parsePosition.getIndex() != string.length())
			return null;
		return new FormattedNumber(result);
	}
}
