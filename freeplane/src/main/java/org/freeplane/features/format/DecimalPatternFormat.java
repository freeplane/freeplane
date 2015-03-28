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
 * @author vboerchers
 */
class DecimalPatternFormat extends PatternFormat {

	public DecimalPatternFormat(final String pattern) {
		super(pattern, IFormattedObject.TYPE_NUMBER);
	}

	@Override
	public Object formatObject(final Object obj) {
		if (obj instanceof Number) {
		    final Number number = (obj instanceof FormattedNumber) ? ((FormattedNumber) obj).getNumber() : (Number) obj;
		    return new FormattedNumber(number, getPattern());
		}
		return obj;
	}

	@Override
	public String getStyle() {
		return PatternFormat.STYLE_DECIMAL;
	}
}
