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
		return String.format("#%02x%02x%02x", col.getRed(), col.getGreen(), col.getBlue());
	}

	public static Color stringToColor(final String str) {
		if (str == null) {
			return null;
		}
		if (str.length() != 7 || str.charAt(0) != '#') {
			throw new NumberFormatException("wrong color format in " + str + ". Expecting #rrggbb");
		}
		return new Color(Integer.parseInt(str.substring(1, 3), 16), Integer.parseInt(str.substring(3, 5), 16), Integer
		    .parseInt(str.substring(5, 7), 16));
	}

	public static Color createColor(final Color color, final int alpha) {
        if(color.getAlpha() == alpha)
    		return color;
    	return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
    }
}
