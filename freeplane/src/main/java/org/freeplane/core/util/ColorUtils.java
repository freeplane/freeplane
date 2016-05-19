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
		final int alpha = col.getAlpha();
		if (alpha == 255)
			return colorToRGBString(col);
		else
			return String.format("#%02x%02x%02x%02x", alpha, col.getRed(), col.getGreen(), col.getBlue());
	}

	public static String colorToRGBString(final Color col) {
		if (col == null) {
			return null;
		}
		final String string = String.format("#%02x%02x%02x", col.getRed(), col.getGreen(), col.getBlue());
		return string;
	}

	public static Color stringToColor(final String str) {
		if (str == null) {
			return null;
		}
		if (str.charAt(0) != '#') {
			throw new NumberFormatException("wrong color format in " + str + ". Expecting #rrggbb or #aarrggbb");
		}
		final int stringLength = str.length();
		switch(stringLength){
		case 7: {		
			final int red = Integer.parseInt(str.substring(1, 3), 16);
			final int green = Integer.parseInt(str.substring(3, 5), 16);
			final int blue = Integer.parseInt(str.substring(5, 7), 16);
			return new Color(red, green, blue);
		}
		case 9:	{		
			final int alpha = Integer.parseInt(str.substring(1, 3), 16);
			final int red = Integer.parseInt(str.substring(3, 5), 16);
			final int green = Integer.parseInt(str.substring(5, 7), 16);
			final int blue = Integer.parseInt(str.substring(7, 9), 16);
			return new Color(red, green, blue, alpha);
		}	
		default:
			throw new NumberFormatException("wrong color format in " + str + ". Expecting #rrggbb or #aarrggbb");
		}
	}

	public static Color createColor(final Color color, final int alpha) {
        if(color.getAlpha() == alpha)
    		return color;
    	return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
    }
}
