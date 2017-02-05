/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2016 jberry
 *
 *  This file author is jberry
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
package org.freeplane.features.styles.mindmapmode.styleeditorpanel;

/**
 * @author Joe Berry
 * Dec 1, 2016
 */
public class EnumToStringMapper {

	private EnumToStringMapper() {}
	
	public static <U extends Enum<U>> String[] getStringValuesOf(Class<U> enumerationClass) {
		return getStringValuesOf(enumerationClass, enumerationClass.getEnumConstants().length);
	}

	public static <U extends Enum<U>> String[] getStringValuesOf(Class<U> enumerationClass, int length) {
		final U[] enumConstants = enumerationClass.getEnumConstants();
		final String[] strings = new String[length];
		for (int i = 0; i < length; i++) {
			strings[i] = enumConstants[i].toString();
		}
		return strings;
	}

}
