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
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.nio.charset.StandardCharsets;
import java.util.zip.CRC32;

import javax.swing.Icon;

import org.freeplane.features.mode.Controller;
import org.freeplane.features.ui.IMapViewManager;

public class TagIcon implements Icon {
    private final String tag;
    private final Font font;
    private final Color textColor;
    private final Color backgroundColor;
    private final int width;
    private final int height;
    public TagIcon(String tag, Font font) {
        super();
        this.tag = tag;
        this.font = font;
        long crc = computeCRC32(tag);
        this.backgroundColor = HSLColorConverter.generateColorFromLong(crc);
        this.textColor = UITools.getTextColorForBackground(backgroundColor);
        if(tag.isEmpty()) {
            width = 0;
            height = (int) (UITools.FONT_SCALE_FACTOR * font.getSize());
        }
        else {
            Rectangle2D rect = font.getStringBounds(tag, 0, tag.length(),
                    new FontRenderContext(new AffineTransform(),
                            true, true));
            double textWidth = rect.getWidth();
            double textHeight = rect.getHeight();
            width = (int) Math.ceil(textWidth + textHeight);
            height = (int)  Math.ceil(textHeight * 1.2);
        }

    }
    private static long computeCRC32(String input) {
        CRC32 crc = new CRC32();
        crc.update(input.getBytes(StandardCharsets.UTF_8));
        return crc.getValue();
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

    public Color getTextColor() {
        return textColor;
    }

    @Override
    public void paintIcon(Component c, Graphics prototypeGraphics, int x, int y) {
        if(tag.isEmpty())
            return;
        Graphics2D g = (Graphics2D) prototypeGraphics.create();
        g.setColor(backgroundColor);
        int r = (int) (UITools.FONT_SCALE_FACTOR * 10);
        IMapViewManager mapViewManager = Controller.getCurrentController().getMapViewManager();
        mapViewManager.setEdgesRenderingHint(g);
        g.fillRoundRect(x, y, width, height, r, r);
        g.setColor(textColor);
        g.drawRoundRect(x, y, width, height, r, r);
        g.setFont(font);
        mapViewManager.setTextRenderingHint(g);
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
