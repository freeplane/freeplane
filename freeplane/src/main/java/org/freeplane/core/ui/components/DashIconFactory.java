package org.freeplane.core.ui.components;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;

import javax.swing.Icon;

public class DashIconFactory {

	public static Icon createIcon(final int width, final int height, final int lineWidth, final int[] dash) {
		final BasicStroke stroke = UITools.createStroke(lineWidth, dash);
		return new Icon() {
			public void paintIcon(Component c, Graphics g, int x, int y) {
				Graphics2D g2 = (Graphics2D) g;
				g2.setColor(Color.BLACK);
				Stroke oldStroke = g2.getStroke();
				g2.setStroke(stroke);
				g2.drawLine(x, y+height / 2, x+width, y+height / 2);
				g2.setStroke(oldStroke);
			}
			
			public int getIconWidth() {
				return width;
			}
			
			public int getIconHeight() {
				return height;
			}
		};
	}

}
