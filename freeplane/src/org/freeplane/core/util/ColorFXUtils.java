/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2014 home
 *
 *  This file author is home
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

import javafx.scene.paint.Color;

public class ColorFXUtils {
	public static final String BLACK = "#000000";

	public static String colorToString(final Color color) {
		if (color == null) {
			return null;
		}
		return String.format("#%02x%02x%02x", (int) color.getRed(), (int) color.getGreen(), (int) color.getBlue());
	}

	public static Color stringToColor(final String str) {
		if (str == null) {
			return null;
		}
		if (str.length() != 7 || str.charAt(0) != '#') {
			throw new NumberFormatException("wrong color format in " + str + ". Expecting #rrggbb");
		}
		double red = Integer.parseInt(str.substring(1, 3), 16);
		double green = Integer.parseInt(str.substring(3, 5), 16);
		double blue = Integer.parseInt(str.substring(5, 7), 16);
		return new Color(red, green, blue, 1);
	}

	public static Color createColor(final Color color, final int alpha) {
		if (color.getOpacity() == alpha)
			return color;
		return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
	}
}
