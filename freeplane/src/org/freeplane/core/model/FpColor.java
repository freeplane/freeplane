package org.freeplane.core.model;

import java.awt.Color;

/**
 * Defines a color with some utility methods.
 * 
 * @author robert.ladstaetter
 */
public class FpColor {
	final Color color;

	public Color getColor() {
    	return color;
    }

	public static String colorToXml(final Color col) {
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

	public FpColor(Color color) {
		super();
		assert color != null;
		this.color = color;
	}

	public FpColor(String color) {
		this(color == null ? null : new Color(Integer.parseInt(color.substring(1, 3), 16), Integer.parseInt(color
		    .substring(3, 5), 16), Integer.parseInt(color.substring(5, 7), 16)));
	}
}
