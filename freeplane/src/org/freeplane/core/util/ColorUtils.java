package org.freeplane.core.util;

import java.awt.Color;

/**
 * Defines a color with some utility methods.
 * 
 * @author robert.ladstaetter
 */
public class ColorUtils {
	public static final String BLACK = "#000000";

	public static String colorToString(final Color col) {
		if (col == null) {
			return null;
		}
		String red = Integer.toHexString(col.getRed());
		if (col.getRed() < 16) {
			red = "0" + red;
		}
		String green = Integer.toHexString(col.getGreen());
		if (col.getGreen() < 16) {
			green = "0" + green;
		}
		String blue = Integer.toHexString(col.getBlue());
		if (col.getBlue() < 16) {
			blue = "0" + blue;
		}
		return "#" + red + green + blue;
	}

	public static Color stringToColor(final String str) {
		if (str == null) {
			return null;
		}
		if (str.length() != 7 || str.charAt(0) != '#') {
			throw new NumberFormatException("wrong color format in " + str);
		}
		return new Color(Integer.parseInt(str.substring(1, 3), 16), Integer.parseInt(str.substring(3, 5), 16), Integer
		    .parseInt(str.substring(5, 7), 16));
	}
}
