/*
 * Created on 18 May 2024
 *
 * author dimitry
 */
package org.freeplane.view.swing.map;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.util.Collections;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

public class IconListComponent extends JComponent {
    private static final long serialVersionUID = 1L;
    private List<? extends Icon> icons;
    private int maximumWidth;

    public IconListComponent() {
        this(Collections.emptyList());
    }

    public IconListComponent(List<? extends Icon> icons) {
        super();
        this.icons = icons;
        maximumWidth = Integer.MAX_VALUE;
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

    private void paintIcons(Graphics g, float zoom) {
        super.paintComponent(g);
        int x = 0;
        int y = 0;
        int rowHeight = 0;

        final int width = (int) (getWidth() / zoom + 0.5f);
        for (Icon icon : icons) {
            if (x > 0 && x + icon.getIconWidth() > width) {
                x = 0;
                y += rowHeight;
                rowHeight = 0;
            }

            icon.paintIcon(this, g, x, y);
            x += icon.getIconWidth();
            rowHeight = Math.max(rowHeight, icon.getIconHeight());
        }
    }

    @Override
    public Dimension getPreferredSize() {
        if (isPreferredSizeSet()) {
            Dimension preferredSize = super.getPreferredSize();
            return preferredSize;
        }
        int width = maximumWidth;
        int height = 0;
        int rowWidth = 0;
        int rowHeight = 0;

        for (Icon icon : icons) {
            if (rowWidth > 0 && rowWidth + icon.getIconWidth() > width) {
                height += rowHeight;
                rowWidth = 0;
                rowHeight = 0;
            }
            rowWidth += icon.getIconWidth();
            rowHeight = Math.max(rowHeight, icon.getIconHeight());
        }

        height += rowHeight;
        final float zoom = getZoom();
        return new Dimension((int) (width * zoom + 0.5), (int)(height * zoom + 0.5));
    }

    protected float getZoom() {
        final float zoom = getMap().getZoom();
        return zoom;
    }

    protected MapView getMap() {
        return getNodeView().getMap();
    }

    protected NodeView getNodeView() {
        return (NodeView) SwingUtilities.getAncestorOfClass(NodeView.class, this);
    }

    protected boolean useFractionalMetrics() {
        final MapView map = getMap();
        if (map.isPrinting()) {
            return true;
        }
        final float zoom = map.getZoom();
        return 1f != zoom;
    }

    public void setMaximumWidth(int maximumWidth) {
        this.maximumWidth = maximumWidth;

    }

}