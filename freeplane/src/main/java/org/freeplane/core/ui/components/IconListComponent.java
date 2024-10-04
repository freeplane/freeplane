/*
 * Created on 18 May 2024
 *
 * author dimitry
 */
package org.freeplane.core.ui.components;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
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
    private int verticalAlignment;
    private boolean wrapsIcons;

    public IconListComponent() {
        this(Collections.emptyList());
    }

    public IconListComponent(List<? extends Icon> icons) {
        super();
        this.icons = icons;
        maximumWidth = Integer.MAX_VALUE;
        horizontalAlignment = SwingConstants.LEFT;
        verticalAlignment = SwingConstants.CENTER;
        setWrapIcons(false);
    }

    public List<? extends Icon> getIcons() {
        return icons;
    }

    public void setIcons(List<? extends Icon> icons) {
        this.icons = icons;
        revalidate();
        repaint();
    }

    public int getIconCount() {
        return icons.size();
    }

    public Icon removeIcon(int index){
        Icon removed = icons.remove(index);
        revalidate();
        repaint();
        return removed;
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (isOpaque()) {
            g.setColor(getBackground());
            g.fillRect(0, 0, getWidth(), getHeight());
        }
        super.paintComponent(g);
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

    public void setVerticalAlignment(int alignment) {
        if (alignment == verticalAlignment)
            return;
        int oldValue = verticalAlignment;
        verticalAlignment = alignment;
        firePropertyChange("verticalAlignment",
                           oldValue, verticalAlignment);
        repaint();
    }

    private void paintIcons(Graphics g, float zoom) {
        // Obtain insets
        Insets insets = getInsets();
        int x = insets.left; // Start drawing from the left inset
        int y = insets.top;  // Start drawing from the top inset
        int rowHeight = 0;
        int totalRowWidth = 0;
        List<Icon> rowIcons = new ArrayList<>(icons.size());
        final int availableWidth = (int) (getWidth() / zoom) - insets.left - insets.right;

        for (Icon icon : icons) {
            int iconWidth = icon.getIconWidth();
            if (wrapsIcons && x > insets.left && x + iconWidth > availableWidth + insets.left) {
                // Draw the current row
                int dx = availableWidth - totalRowWidth;
                drawIconsRow(g, rowIcons, dx, y, availableWidth, rowHeight, insets);
                // Move to next row
                x = insets.left;
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
            int dx = availableWidth - totalRowWidth;
            drawIconsRow(g, rowIcons, dx, y, availableWidth, rowHeight, insets);
        }
    }

    private void drawIconsRow(Graphics g, List<Icon> rowIcons, int dx, int y, int width, int height, Insets insets) {
        boolean isLeftToRight = getComponentOrientation().isLeftToRight();
        int x;
        if (horizontalAlignment == SwingConstants.CENTER) {
            x = insets.left + dx / 2;
        } else if (horizontalAlignment == SwingConstants.RIGHT) {
            x = isLeftToRight ? insets.left + dx : insets.left;
        } else { // SwingConstants.LEFT
            x = isLeftToRight ? insets.left : insets.left + dx;
        }
        for (Icon icon : rowIcons) {
            final int paintX = isLeftToRight ? x : width + insets.left - x - icon.getIconWidth();
            final int paintY;
            if (verticalAlignment == SwingConstants.CENTER) {
                paintY = y + (height - icon.getIconHeight()) / 2 + insets.top;
            } else if (verticalAlignment == SwingConstants.TOP) {
                paintY = y + insets.top;
            } else { // SwingConstants.BOTTOM
                paintY = y + height - icon.getIconHeight() + insets.top;
            }

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
        Insets insets = getInsets();
        int availableWidth = (int) (maximumWidth / zoom + 0.5) - insets.left - insets.right;
        int width = 0;
        int height = insets.top + insets.bottom;
        int maximumRowWidth = 0;
        int rowWidth = 0;
        int rowHeight = 0;

        for (Icon icon : icons) {
            if (wrapsIcons && rowWidth > 0 && rowWidth + icon.getIconWidth() > availableWidth) {
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
        width = maximumRowWidth + insets.left + insets.right;

        return new Dimension((int) (width * zoom + 0.5), (int)(height * zoom + 0.5));
    }

    public Icon getIcon(Point point) {
        final float zoom = getZoom();
        final Insets insets = getInsets();
        final int availableWidth = (int) (getWidth() / zoom) - insets.left - insets.right;
        int rowWidth = 0;
        int rowHeight = 0;
        int x = insets.left;
        int y = insets.top;

        for (Icon icon : icons) {
            if (wrapsIcons && rowWidth > 0 && rowWidth + icon.getIconWidth() > availableWidth) {
                x = insets.left;
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
        revalidate();
        repaint();
    }

    protected float getZoom() {
        return 1f;
    }

    protected boolean useFractionalMetrics() {
        return false;
    }

    public boolean wrapsIcons() {
        return wrapsIcons;
    }

    public void setWrapIcons(boolean wrapsIcons) {
        this.wrapsIcons = wrapsIcons;
        revalidate();
        repaint();
    }

}