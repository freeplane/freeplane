/*
 * Created on 27 Dec 2023
 *
 * author dimitry
 */
package org.freeplane.core.ui.svgicons;

import java.awt.Component;
import java.awt.Graphics;

import javax.swing.Icon;

public class FixSizeIconWrapper implements Icon {
    private final int width;
    private final int height;
    private final Icon wrappedIcon;


    public FixSizeIconWrapper(int width, int height) {
        this(width, height, null);
    }

    public FixSizeIconWrapper(int width, int height, Icon wrappedIcon) {
        super();
        this.width = width;
        this.height = height;
        this.wrappedIcon = wrappedIcon;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        if(wrappedIcon != null)
            wrappedIcon.paintIcon(c, g, x + (width - wrappedIcon.getIconWidth())/ 2, y + (height - wrappedIcon.getIconHeight())/ 2);
    }

    @Override
    public int getIconWidth() {
        return width;
    }

    @Override
    public int getIconHeight() {
        return height;
    }

    public Icon getWrappedIcon() {
        return wrappedIcon;
    }

}