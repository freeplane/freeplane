package org.freeplane.view.swing.map;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.Icon;

public class ShortenedNodeIcon implements Icon {
	public ShortenedNodeIcon(Color color) {
	    super();
	    this.color = color;
    }

	private static final int SIZE = 16;
	final private Color color;

	public void paintIcon(Component c, Graphics g, int x, int y) {
		Graphics2D g2 = (Graphics2D) g.create();
		g2.translate(x, y);
		final int x1 = SIZE-1;
		g2.setColor(color);
		g2.setStroke(new BasicStroke());
		g2.drawLine(x1, 0, x1, x1);
		g2.drawLine(0, x1, x1, x1);
		g2.setStroke(new BasicStroke(2));
		final int x2 = x1-2;
		g2.drawLine(2, 2, x2-1, x2-1);
		g2.drawLine(x2 - 3, x2, x2, x2);
		g2.drawLine(x2, x2 - 3, x2, x2);
		g2.dispose();
	}

	public int getIconWidth() {
		return SIZE;
	}

	public int getIconHeight() {
		return SIZE;
	}
}
