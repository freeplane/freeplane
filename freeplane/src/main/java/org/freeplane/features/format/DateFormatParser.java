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

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateFormatParser extends Parser {
	private final SimpleDateFormat parser;
	private final String missingFields;
	private boolean forbidLeadingSpaces;

    public DateFormatParser(final String format, final String type) {
        super(Parser.STYLE_DATE, type, format);
        forbidLeadingSpaces = (format.charAt(0) != ' ');
        parser = new SimpleDateFormat(format.replaceFirst("^\\s", ""));
        parser.setLenient(false);
        missingFields = (format.contains("y") ? "" : "y") //
                + (format.contains("M") ? "" : "M") //
                + (format.contains("d") ? "" : "d");
    }
    
    public DateFormatParser(final String format) {
        this(format, getTypeDependingOnFormat(format));
    }

	private static String getTypeDependingOnFormat(final String format) {
		// if it contains minute format -> datetime
		return format.contains("m") ? IFormattedObject.TYPE_DATETIME : IFormattedObject.TYPE_DATE;
	}

    @Override
    Object parse(String string) {
        if (string == null || (forbidLeadingSpaces && string.charAt(0) == ' '))
            return null;
        final ParsePosition parsePosition = new ParsePosition(0);
        Date date = parser.parse(string, parsePosition);
        if (parsePosition.getIndex() != string.length())
            return null;
        if (missingFields.length() != 0) {
            final Calendar calendar = Calendar.getInstance();
            final int year = calendar.get(Calendar.YEAR);
            final int month = calendar.get(Calendar.MONTH);
            final int day = calendar.get(Calendar.DAY_OF_MONTH);
            calendar.setTime(date);
            if (missingFields.contains("y"))
                calendar.set(Calendar.YEAR, year);
            if (missingFields.contains("M"))
                calendar.set(Calendar.MONTH, month);
            if (missingFields.contains("d"))
                calendar.set(Calendar.DAY_OF_MONTH, day);
            date = calendar.getTime();
        }
        return FormattedDate.createDefaultFormattedDate(date.getTime(), getType());
    }
}
