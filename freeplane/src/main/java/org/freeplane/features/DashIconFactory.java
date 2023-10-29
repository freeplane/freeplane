/*
 * Created on 27 Oct 2023
 *
 * author dimitry
 */
package org.freeplane.features;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.util.EnumMap;

import javax.swing.Icon;

import org.freeplane.api.Dash;
import org.freeplane.core.ui.components.UITools;

public class DashIconFactory {
	private static EnumMap<Dash, Icon> icons = new EnumMap<>(Dash.class);
	private static Icon createIcon(Dash dash) {
		final int LINE_WIDTH = 2;
		final int ICON_HEIGHT = Math.round(12 * UITools.FONT_SCALE_FACTOR);
		final int ICON_WIDTH = ICON_HEIGHT * 5;
		Icon icon = DashIconFactory.createIcon(ICON_WIDTH, ICON_HEIGHT, LINE_WIDTH, dash.pattern);
		return icon;
	}

	public static Icon iconFor(Dash dash) {
		return icons.computeIfAbsent(dash, DashIconFactory::createIcon);
	}

    private static Icon createIcon(final int width, final int height, final int lineWidth, final int[] dash) {
        final BasicStroke stroke = UITools.createStroke(lineWidth, dash, BasicStroke.JOIN_ROUND);
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(Color.BLACK);
                Stroke oldStroke = g2.getStroke();
                g2.setStroke(stroke);
                g2.drawLine(x, y+height / 2, x+width, y+height / 2);
                g2.setStroke(oldStroke);
            }

            @Override
            public int getIconWidth() {
                return width;
            }

            @Override
            public int getIconHeight() {
                return height;
            }
        };
    }

}