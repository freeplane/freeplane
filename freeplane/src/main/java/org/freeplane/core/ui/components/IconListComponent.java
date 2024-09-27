/*
 * Created on 18 May 2024
 *
 * author dimitry
 */
package org.freeplane.core.ui.components;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.SwingConstants;

public class IconListComponent extends JComponent {
    private static final long serialVersionUID = 1L;
    private List<? extends Icon> icons;
    private int maximumWidth;
    private int horizontalAlignment;

    public IconListComponent() {
        this(Collections.emptyList());
    }

    public IconListComponent(List<? extends Icon> icons) {
        super();
        this.icons = icons;
        maximumWidth = Integer.MAX_VALUE;
        horizontalAlignment = SwingConstants.LEFT;
    }

    public List<? extends Icon> getIcons() {
        return icons;
    }

    public void setIcons(List<? extends Icon> icons) {
        this.icons = icons;
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (!useFractionalMetrics()) {
            paintIcons(g, 1f);
            return;
        }
        final Graphics2D g2 = (Graphics2D) g;
        final Object oldRenderingHintFM = g2.getRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS);
        final Object newRenderingHintFM = RenderingHints.VALUE_FRACTIONALMETRICS_ON;
        if (oldRenderingHintFM != newRenderingHintFM) {
            g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, newRenderingHintFM);
        }
        final AffineTransform transform = g2.getTransform();
        final float factor = 0.97f;
        final float zoom = getZoom() * factor;
        g2.scale(zoom, zoom);
        paintIcons(g2, zoom);
        g2.setTransform(transform);
        if (oldRenderingHintFM != newRenderingHintFM) {
            g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, oldRenderingHintFM != null ? oldRenderingHintFM
                    : RenderingHints.VALUE_FRACTIONALMETRICS_DEFAULT);
        }
    }

    public void setHorizontalAlignment(int alignment) {
        if (alignment == horizontalAlignment)
            return;
        int oldValue = horizontalAlignment;
        horizontalAlignment = alignment;
        firePropertyChange("horizontalAlignment",
                           oldValue, horizontalAlignment);
        repaint();
    }

    private void paintIcons(Graphics g, float zoom) {
        super.paintComponent(g);
        int x = 0;
        int y = 0;
        int rowHeight = 0;
        int totalRowWidth = 0;
        List<Icon> rowIcons = new ArrayList<>(icons.size());
        final int width = (int) (getWidth() / zoom);

        for (Icon icon : icons) {
            int iconWidth = icon.getIconWidth();
            if (x > 0 && x + iconWidth > width) {
                int dx = width - totalRowWidth;
                drawIconsRow(g, rowIcons, dx, y, width);
                x = 0;
                y += rowHeight;
                rowHeight = 0;
                rowIcons.clear();
                totalRowWidth = 0;
            }
            rowIcons.add(icon);
            x += iconWidth;
            totalRowWidth += iconWidth;
            rowHeight = Math.max(rowHeight, icon.getIconHeight());
        }
        if (!rowIcons.isEmpty()) {
            int dx = width - totalRowWidth;
            drawIconsRow(g, rowIcons, dx, y, width);
        }
    }

    private void drawIconsRow(Graphics g, List<Icon> rowIcons, int dx, int y, int width) {
        boolean isLeftToRight = getComponentOrientation().isLeftToRight();
        int x;
        if (horizontalAlignment == SwingConstants.CENTER) {
            x = dx / 2;
        } else if (horizontalAlignment == SwingConstants.RIGHT) {
            x = isLeftToRight ? dx : 0;
        } else {
            x = isLeftToRight ? 0 : dx;
        }
        for (Icon icon : rowIcons) {
            final int paintX = isLeftToRight ?  x  : width - x - icon.getIconWidth();
            final int paintY = y ;
            icon.paintIcon(this, g, paintX, paintY);
            x += icon.getIconWidth();
        }
    }

    @Override
    public Dimension getPreferredSize() {
        if (isPreferredSizeSet()) {
            Dimension preferredSize = super.getPreferredSize();
            return preferredSize;
        }
        final float zoom = getZoom();
        int width = (int) (maximumWidth / zoom + 0.5);
        int height = 0;
        int maximumRowWidth = 0;
        int rowWidth = 0;
        int rowHeight = 0;

        for (Icon icon : icons) {
            if (rowWidth > 0 && rowWidth + icon.getIconWidth() > width) {
                height += rowHeight;
                maximumRowWidth = Math.max(rowWidth, maximumRowWidth);
                rowWidth = 0;
                rowHeight = 0;
            }
            rowWidth += icon.getIconWidth();
            rowHeight = Math.max(rowHeight, icon.getIconHeight());
        }

        height += rowHeight;
        maximumRowWidth = Math.max(rowWidth, maximumRowWidth);
        return new Dimension((int) (maximumRowWidth * zoom + 0.5), (int)(height * zoom + 0.5));
    }

    public Icon getIcon(Point point) {
        final float zoom = getZoom();
        final int width = (int) (getWidth() / zoom);
        int rowWidth = 0;
        int rowHeight = 0;
        int x = 0;
        int y = 0;

        for (int i = 0; i < icons.size(); i++) {
            Icon icon = icons.get(i);

            if (rowWidth > 0 && rowWidth + icon.getIconWidth() > width) {
                x = 0;
                y += rowHeight;
                rowWidth = 0;
                rowHeight = 0;
            }

            int iconWidth = icon.getIconWidth();
            int iconHeight = icon.getIconHeight();

            if (point.x >= x && point.x < x + iconWidth && point.y >= y && point.y < y + iconHeight) {
                return icon;
            }

            x += iconWidth;
            rowWidth += iconWidth;
            rowHeight = Math.max(rowHeight, iconHeight);
        }

        return null;
    }

    public void setMaximumWidth(int maximumWidth) {
        this.maximumWidth = maximumWidth;

    }

    protected float getZoom() {
        return 1f;
    }

    protected boolean useFractionalMetrics() {
        return false;
    }

}