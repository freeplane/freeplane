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

/**
 * A locale independent parser that uses the parsed input to decide between TYPE_DATE and TYPE_DATETIME.
 *
 * @author Volker Boerchers
 */
public class IsoDateParser extends Parser {
	public IsoDateParser() {
		super(Parser.STYLE_ISODATE, IFormattedObject.TYPE_DATE, null);
	}

	@Override
	Object parse(String string) {
		try {
			if (string == null)
				return null;
			final FormattedDate date = FormattedDate.toDateISO(string);
			if(date == null)
				return null;
			final String type = date.containsTime() ? IFormattedObject.TYPE_DATETIME : IFormattedObject.TYPE_DATE;
			return FormattedDate.createDefaultFormattedDate(date.getTime(), type);
		}
		catch (Exception e) {
			return null;
		}
	}
}
