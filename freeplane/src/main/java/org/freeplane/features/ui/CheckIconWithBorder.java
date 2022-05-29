package org.freeplane.features.ui;

import java.awt.BasicStroke;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.Icon;
import javax.swing.JMenuItem;

import org.freeplane.core.ui.components.UITools;

class CheckIconWithBorder implements Icon {
    
	private static final int BOX_SIZE = 14;
	private static final BasicStroke CHECK_STROKE = new BasicStroke(Math.max(1f, UITools.FONT_SCALE_FACTOR), BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND);
	private static final BasicStroke BOX_STROKE = new BasicStroke(1f, BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND);
	private final Icon fallbackIcon;
	private final int width;
	private final int height;
	private final int checkBoxHeight;

    public CheckIconWithBorder(Icon fallbackIcon, int checkBoxHeight, int gap) {
		this.fallbackIcon = fallbackIcon;
		this.checkBoxHeight = checkBoxHeight;
        width = Math.max(fallbackIcon.getIconWidth(), checkBoxHeight + gap);
        height = Math.max(fallbackIcon.getIconHeight(), checkBoxHeight);
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

        int margin = Math.max((height - BOX_SIZE)/2, 0);
		g.translate(x + margin, y + margin);
		double f = ((double)checkBoxHeight) / BOX_SIZE;
		g.scale(f, f);

        g.setColor(c.getForeground());

        if (c == null || ((JMenuItem)c).isSelected()) {
          g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
          g.setStroke(CHECK_STROKE);
          g.drawLine(3, 7, 6, 10);
          g.drawLine(6, 10, BOX_SIZE-2, 4);
          g.drawLine(3, 5, 6, 8);
          g.drawLine(6, 8, BOX_SIZE-2, 2);
        }
        g.setStroke(BOX_STROKE);
        g.drawRoundRect(0, 0, BOX_SIZE, BOX_SIZE - 1, 4, 4);
        g.drawRoundRect(0, 0, BOX_SIZE, BOX_SIZE - 1, 4, 4);
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
