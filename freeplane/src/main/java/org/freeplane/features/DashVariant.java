package org.freeplane.features;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

import javax.swing.Icon;

import org.freeplane.core.ui.components.UITools;

public enum DashVariant{
	SOLID(new int[] {}),
	CLOSE_DOTS(new int[]{3, 3}),
	DASHES(new int[]{7, 7}),
	DISTANT_DOTS(new int[]{2, 7}),
	DOTS_AND_DASHES(new int[]{2, 7, 7, 7});

    private static class DashIconFactory {

        public static Icon createIcon(final int width, final int height, final int lineWidth, final int[] dash) {
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


	public static DashVariant DEFAULT = DashVariant.SOLID;
	public final int[] variant;
	public final Icon icon;

	static public Optional<DashVariant> of(int[] variant) {
	    return Stream.of(values())
	        .filter(self -> Arrays.equals(self.variant, variant))
	        .findAny();
	}

	private DashVariant(int[] variant) {
		this.variant = variant;
		final int LINE_WIDTH = 2;
		final int ICON_HEIGHT = Math.round(12 * UITools.FONT_SCALE_FACTOR);
		final int ICON_WIDTH = ICON_HEIGHT * 5;
		icon = DashIconFactory.createIcon(ICON_WIDTH, ICON_HEIGHT, LINE_WIDTH, variant);
	}
}
