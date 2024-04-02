/*
 * Created on 27 Jan 2024
 *
 * author dimitry
 */
package org.freeplane.core.ui.components;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

import javax.swing.Icon;

public class TagIcon implements Icon {
    private final String tag;
    private final Font font;
    private final Color backgroundColor;
    private final int width;
    private final int height;
    public TagIcon(String tag, Font font, Color backgroundColor) {
        super();
        this.tag = tag;
        this.font = font;
        this.backgroundColor = backgroundColor;
        Rectangle2D rect = font.getStringBounds(tag, 0, tag.length(),
                             new FontRenderContext(new AffineTransform(),
                             true, true));
        double textWidth = rect.getWidth();
        double textHeight = rect.getHeight();
        width = (int) Math.ceil(textWidth + textHeight);
        height = (int)  Math.ceil(textHeight * 1.2);

    }
    public String getTag() {
        return tag;
    }
    public Font getFont() {
        return font;
    }
    public Color getBackgroundColor() {
        return backgroundColor;
    }

    @Override
    public void paintIcon(Component c, Graphics prototypeGraphics, int x, int y) {
        Graphics g = prototypeGraphics.create();
        g.setColor(backgroundColor);
        int r = (int) (UITools.FONT_SCALE_FACTOR * 10);
        g.fillRoundRect(x, y, width, height, r, r);
        Color textColor = UITools.getTextColorForBackground(backgroundColor);
        g.setColor(textColor);
        g.drawRoundRect(x, y, width, height, r, r);
        g.setFont(font);
        g.drawString(tag, x + height / 2, y + height * 4 / 5);
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
