package org.freeplane.core.util;

import java.awt.Color;

import org.freeplane.core.io.ITreeWriter;
import org.freeplane.n3.nanoxml.XMLElement;

/**
 * Defines a color with some utility methods.
 * 
 * @author robert.ladstaetter
 */
public class ColorUtils {
	public static final int NON_TRANSPARENT_ALPHA = 255;
	public static final String BLACK = "#000000";

	public static String colorToString(final Color col) {
		if (col == null) {
			return null;
		}
		return String.format("#%02x%02x%02x", col.getRed(), col.getGreen(), col.getBlue());
	}

	public static String colorToRGBAString(final Color col) {
		if (col == null) {
			return null;
		}
		return String.format("#%02x%02x%02x%02x", col.getRed(), col.getGreen(), col.getBlue(), col.getAlpha());
	}

	public static Color rgbStringToColor(final String str) {
		if (str == null) {
			return null;
		}
		if (str.length() != 7 || str.charAt(0) != '#') {
			throw new NumberFormatException("wrong color format in " + str + ". Expecting #rrggbb");
		}
		final int r = Integer.parseInt(str.substring(1, 3), 16);
		final int g = Integer.parseInt(str.substring(3, 5), 16);
		final int b = Integer.parseInt(str.substring(5, 7), 16);
		return new Color(r, g, b);
	}

	public static Color stringToColor(final String str) {
		if (str == null || str.equals("none")) {
			return null;
		}
		if(str.length() == 7)
			return rgbStringToColor(str);
		
		if (str.length() != 9 || str.charAt(0) != '#') {
			throw new NumberFormatException("wrong color format in " + str + ". Expecting #aarrggbb");
		}
		final int r = Integer.parseInt(str.substring(1, 3), 16);
		final int g = Integer.parseInt(str.substring(3, 5), 16);
		final int b = Integer.parseInt(str.substring(5, 7), 16);
		final int a = Integer.parseInt(str.substring(7, 9), 16);
		return new Color(r,g,b,a);
	}

	public static Color stringToColor(final String str, final Color alphaHolder) {
		final Color rgb = stringToColor(str);
		if(alphaHolder == null || isNonTransparent(alphaHolder) || rgb == null)
			return rgb;
		else
			return new Color(rgb.getRed(), rgb.getGreen(), rgb.getBlue(), alphaHolder.getAlpha());
	}

	protected static boolean isNonTransparent(final Color alphaHolder) {
		final boolean isNonTransparent = alphaHolder.getAlpha() == NON_TRANSPARENT_ALPHA;
		return isNonTransparent;
	}
	
	static public Color alphaToColor(String value, Color color) {
		return alphaToColor(Integer.parseInt(value), color);
	}
	
	public static Color alphaToColor(final int alpha, final Color rgbHolder) {
		if(rgbHolder == null)
			return new Color(0, 0, 0, alpha);
        if(rgbHolder.getAlpha() == alpha)
    		return rgbHolder;
    	return new Color(rgbHolder.getRed(), rgbHolder.getGreen(), rgbHolder.getBlue(), alpha);
    }

	public static void setColorAttributes(final XMLElement element, String colorAttribute, String alphaAttribute, final Color color) {
		element.setAttribute(colorAttribute, colorToString(color));
		if(! isNonTransparent(color))
			element.setAttribute(alphaAttribute, Integer.toString(color.getAlpha()));
	}
	
	public static void addColorAttributes(final ITreeWriter writer, String colorAttribute, String alphaAttribute, final Color color) {
		writer.addAttribute(colorAttribute, colorToString(color));
		if(! isNonTransparent(color))
			writer.addAttribute(alphaAttribute, Integer.toString(color.getAlpha()));
	}
}
