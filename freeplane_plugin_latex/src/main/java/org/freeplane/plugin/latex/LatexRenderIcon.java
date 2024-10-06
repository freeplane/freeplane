/*
 * Created on 28 Sept 2024
 *
 * author dimitry
 */
package org.freeplane.plugin.latex;

import static java.lang.Boolean.TRUE;
import static org.freeplane.core.awt.GraphicsHints.FORCE_TEXT_TO_SHAPE;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.Icon;

class LatexRenderIcon implements Icon {

    private final Icon icon;

    public LatexRenderIcon(Icon icon) {
        this.icon = icon;
    }

    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g;
        Object oldHint = g2.getRenderingHint(FORCE_TEXT_TO_SHAPE);
        g2.setRenderingHint(FORCE_TEXT_TO_SHAPE, TRUE);
        try {
            icon.paintIcon(c, g, x, y);
        }
        finally {
            if(oldHint != Boolean.TRUE)
                g2.setRenderingHint(FORCE_TEXT_TO_SHAPE, oldHint);
        }
    }

    public int getIconWidth() {
        return icon.getIconWidth();
    }

    public int getIconHeight() {
        return icon.getIconHeight();
    }



}
