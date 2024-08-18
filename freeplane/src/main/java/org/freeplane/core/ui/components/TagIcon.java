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

import javax.swing.Icon;

import org.freeplane.features.icon.Tag;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.ui.IMapViewManager;

public class TagIcon implements Icon {
    private final Tag tag;
    private final Font font;
    private final int width;
    private final int height;
    public TagIcon(Tag tag, Font font) {
        super();
        this.tag = tag;
        this.font = font;
        String content = tag.isEmpty() ? "*" : tag.getContent();
        Rectangle2D rect = font.getStringBounds(content , 0, content.length(),
        		new FontRenderContext(new AffineTransform(),
        				true, true));
        double textHeight = rect.getHeight();
        width = tag.isEmpty() ? 0 : (int) Math.ceil(rect.getWidth() + textHeight);
        height = (int)  Math.ceil(textHeight * 1.2);
    }

    @Override
    public void paintIcon(Component c, Graphics prototypeGraphics, int x, int y) {
        if(tag.isEmpty())
            return;
        Graphics2D g = (Graphics2D) prototypeGraphics.create();
        Color backgroundColor = tag.getColor();
        Color textColor = UITools.getTextColorForBackground(backgroundColor);
         g.setColor(backgroundColor);
        int r = (int) (UITools.FONT_SCALE_FACTOR * 10);
        IMapViewManager mapViewManager = Controller.getCurrentController().getMapViewManager();
        mapViewManager.setEdgesRenderingHint(g);
        g.fillRoundRect(x, y, width, height, r, r);
        g.setColor(textColor);
        g.drawRoundRect(x, y, width - 1, height - 1, r, r);
        g.setFont(font);
        mapViewManager.setTextRenderingHint(g);
        g.drawString(tag.getContent(), x + height / 2, y + height * 4 / 5);
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

    public Tag getTag() {
        return tag;
    }
}
