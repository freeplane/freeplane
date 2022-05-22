package org.freeplane.features.ui;

import java.awt.BasicStroke;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.Icon;
import javax.swing.JMenuItem;

class CheckIconWithBorder implements Icon {
    
    private static final int SIZE = 14;
    private static final int GAP = 3;
	private final Icon fallbackIcon;
	private final int width;
	private final int height;

    public CheckIconWithBorder(Icon fallbackIcon) {
		this.fallbackIcon = fallbackIcon;
        width = Math.max(fallbackIcon.getIconWidth(), SIZE + GAP);
        height = Math.max(fallbackIcon.getIconHeight(), SIZE);
	}

	@Override
    public void paintIcon(Component c, Graphics g2, int x, int y) {
        if(! (c instanceof JMenuItem)) {
        	fallbackIcon.paintIcon(c, g2, x, y);
			return;
		}
        Graphics2D g = (Graphics2D) g2.create();;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_DEFAULT);

        final int l = SIZE;
        g.translate(x + Math.max((width - GAP - l)/2, 0), y + Math.max((height - l)/2, 0));

        g.setColor(c.getForeground());

        if (c == null || ((JMenuItem)c).isSelected()) {
          g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
          g.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
          g.drawLine(3, 7, 6, 10);
          g.drawLine(6, 10, l-2, 4);
          g.drawLine(3, 5, 6, 8);
          g.drawLine(6, 8, l-2, 2);
        }
        g.drawRoundRect(0, 0, l, l - 1, 4, 4);
        g.drawRoundRect(0, 0, l, l - 1, 4, 4);
        g.dispose();
    }

    @Override
    public int getIconWidth() {
		return width;
    }

    @Override
    public int getIconHeight() {
		return height;
    }
}
