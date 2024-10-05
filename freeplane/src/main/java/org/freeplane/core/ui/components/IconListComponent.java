/*
 * Created on 18 May 2024
 *
 * author dimitry
 */
package org.freeplane.core.ui.components;

import java.awt.Color;
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

/**
 * IconListComponent is a custom JComponent that displays a list of icons with support for
 * wrapping, alignment, preferred size calculation, zooming, hit detection, and highlighting removed icons.
 */
public class IconListComponent extends JComponent {
    private static final long serialVersionUID = 1L;

    private final List<Icon> icons;
    private int horizontalAlignment;
    private int verticalAlignment;
    private boolean wrapsIcons;
    private Icon removedIcon;
    private Color removalColor = Color.RED;

    // Cached layout information
    private IconBounds iconBoundsCache;
    private float currentZoom = 1f;

    /**
     * Constructs an empty IconListComponent.
     */
    public IconListComponent() {
        this(Collections.emptyList());
    }

    /**
     * Constructs an IconListComponent with the specified list of icons.
     *
     * @param icons List of icons to display.
     */
    public IconListComponent(List<? extends Icon> icons) {
        super();
        this.icons = new ArrayList<>(icons);
        this.horizontalAlignment = SwingConstants.LEFT;
        this.verticalAlignment = SwingConstants.CENTER;
        this.wrapsIcons = false;
        this.iconBoundsCache = null; // Initialize cache as null
    }

    /**
     * Returns an unmodifiable list of icons.
     *
     * @return Unmodifiable list of icons.
     */
    public List<Icon> getIcons() {
        return Collections.unmodifiableList(icons);
    }

    /**
     * Sets the list of icons to be displayed.
     *
     * @param icons New list of icons.
     */
    public void setIcons(List<? extends Icon> icons) {
        this.icons.clear();
        this.icons.addAll(icons);
        invalidateIconBoundsCache();
        revalidate();
        repaint();
    }

    /**
     * Returns the number of icons.
     *
     * @return Icon count.
     */
    public int getIconCount() {
        return icons.size();
    }

    /**
     * Removes the icon at the specified index.
     *
     * @param index Index of the icon to remove.
     * @return The removed Icon.
     * @throws IndexOutOfBoundsException if the index is out of range.
     */
    public Icon removeIcon(int index) {
        Icon removed = icons.remove(index);
        invalidateIconBoundsCache();
        revalidate();
        repaint();
        return removed;
    }

    /**
     * Sets the horizontal alignment of icons within each row.
     *
     * @param alignment Horizontal alignment (SwingConstants.LEFT, CENTER, RIGHT).
     */
    public void setHorizontalAlignment(int alignment) {
        if (alignment == horizontalAlignment) {
            return;
        }
        int oldValue = horizontalAlignment;
        horizontalAlignment = alignment;
        firePropertyChange("horizontalAlignment", oldValue, horizontalAlignment);
        invalidateIconBoundsCache();
        revalidate();
        repaint();
    }

    /**
     * Sets the vertical alignment of icons within each row.
     *
     * @param alignment Vertical alignment (SwingConstants.TOP, CENTER, BOTTOM).
     */
    public void setVerticalAlignment(int alignment) {
        if (alignment == verticalAlignment) {
            return;
        }
        int oldValue = verticalAlignment;
        verticalAlignment = alignment;
        firePropertyChange("verticalAlignment", oldValue, verticalAlignment);
        invalidateIconBoundsCache();
        revalidate();
        repaint();
    }




    @Override
    public void invalidate() {
        invalidateIconBoundsCache();
        super.invalidate();
    }

    /**
     * Sets whether icons should wrap to the next line when exceeding the available width.
     *
     * @param wrapsIcons True to enable wrapping; false otherwise.
     */
    public void setWrapIcons(boolean wrapsIcons) {
        if (this.wrapsIcons != wrapsIcons) {
            this.wrapsIcons = wrapsIcons;
            invalidateIconBoundsCache();
            revalidate();
            repaint();
        }
    }

    /**
     * Sets the color used to highlight a removed icon.
     *
     * @param color The color to use for highlighting.
     */
    public void setRemovalColor(Color color) {
        if (color != null && !color.equals(this.removalColor)) {
            this.removalColor = color;
            repaint();
        }
    }

    /**
     * Highlights the specified removed icon by drawing a colored cross over it.
     *
     * @param icon The icon to highlight.
     */
    public void highlightRemovedIcon(Icon icon) {
        removedIcon = icon;
        repaint();
    }

    /**
     * Returns the current zoom factor.
     *
     * @return The zoom factor.
     */
    protected float getZoom() {
        return currentZoom;
    }

    /**
     * Determines whether to use fractional metrics during layout and painting.
     *
     * @return True to use fractional metrics; false otherwise.
     */
    protected boolean useFractionalMetrics() {
        return false;
    }

    /**
     * Invalidates the cached icon bounds, forcing a recalculation on the next layout.
     */
    private void invalidateIconBoundsCache() {
        this.iconBoundsCache = null;
    }

    /**
     * Calculates the layout bounds of all icons, utilizing caching for efficiency.
     *
     * @return The IconBounds containing layout information.
     */
    private IconBounds calculateIconBounds() {
        if (iconBoundsCache == null) {
            Insets insets = getInsets();
            float zoom = getZoom();
            int width = getWidth();
            int availableWidth = (int)(((width > 0 ? width : Integer.MAX_VALUE) - insets.left - insets.right) / zoom);
            iconBoundsCache = calculateIconBounds((int)(insets.left / zoom), (int)(insets.top / zoom), availableWidth);
        }
        return iconBoundsCache;
    }

    private IconBounds calculateIconBounds(int leftInset, int topInset, int availableWidth) {
        List<IconLayout> iconLayouts = new ArrayList<>();
        int currentY = topInset;
        List<Row> rows = organizeIconsIntoRows(availableWidth);
        int maximumRowWidth = IconBounds.calculateMaximumRowWidth(rows);
        int panelLeftInset = leftInset;

        for (Row row : rows) {
            int rowWidth = row.getTotalWidth();
            int rowHeight = row.getMaxHeight();
            int startingX = determineStartingX(rowWidth, availableWidth);
            boolean isLeftToRight = getComponentOrientation().isLeftToRight();
            int currentX = isLeftToRight ? panelLeftInset + startingX : panelLeftInset + availableWidth - startingX;

            for (Icon icon : row.getIcons()) {
                int iconWidth = icon.getIconWidth();
                int iconHeight = icon.getIconHeight();
                int paintX = isLeftToRight ? currentX : currentX - iconWidth;
                int paintY = calculatePaintY(currentY, rowHeight, iconHeight);

                iconLayouts.add(new IconLayout(icon, paintX, paintY));

                currentX += isLeftToRight ? iconWidth : -iconWidth;
            }

            currentY += rowHeight;
        }

        int totalHeight = currentY - topInset;
        return new IconBounds(iconLayouts, totalHeight, maximumRowWidth);
    }
    /**
     * Organizes icons into rows based on wrapping and available width.
     *
     * @param availableWidth The width available for laying out icons.
     * @return List of Rows containing grouped icons.
     */
    private List<Row> organizeIconsIntoRows(int availableWidth) {
        List<Row> rows = new ArrayList<>();
        Row currentRow = new Row();

        for (Icon icon : icons) {
            int iconWidth = icon.getIconWidth();

            if (wrapsIcons && !currentRow.isEmpty() && (currentRow.getTotalWidth() + iconWidth) > availableWidth) {
                rows.add(currentRow);
                currentRow = new Row();
            }

            currentRow.addIcon(icon);
        }

        if (!currentRow.isEmpty()) {
            rows.add(currentRow);
        }

        return rows;
    }

    /**
     * Paints the component by rendering all icons based on their calculated positions.
     *
     * @param g Graphics context.
     */
    @Override
    protected void paintComponent(Graphics g) {
        // Paint background if opaque
        if (isOpaque()) {
            g.setColor(getBackground());
            g.fillRect(0, 0, getWidth(), getHeight());
        }

        super.paintComponent(g);
        IconBounds iconBounds = calculateIconBounds();

        if (!useFractionalMetrics()) {
            // Paint without scaling
            renderIcons(g, iconBounds.getIconLayouts());
            return;
        }

        // Paint with fractional metrics and zoom scaling
        if (!(g instanceof Graphics2D)) {
            renderIcons(g, iconBounds.getIconLayouts());
            return;
        }

        Graphics2D g2 = (Graphics2D) g;
        Object oldRenderingHintFM = g2.getRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS);
        Object newRenderingHintFM = RenderingHints.VALUE_FRACTIONALMETRICS_ON;
        if (!newRenderingHintFM.equals(oldRenderingHintFM)) {
            g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, newRenderingHintFM);
        }

        AffineTransform originalTransform = g2.getTransform();
        float factor = 0.97f;
        float zoom = getZoom() * factor;
        g2.scale(zoom, zoom);

        renderIcons(g2, iconBounds.getIconLayouts());

        g2.setTransform(originalTransform);
        if (!newRenderingHintFM.equals(oldRenderingHintFM)) {
            g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
                    oldRenderingHintFM != null ? oldRenderingHintFM : RenderingHints.VALUE_FRACTIONALMETRICS_DEFAULT);
        }
    }

    /**
     * Renders all icons based on their layout positions.
     *
     * @param g           Graphics context.
     * @param iconLayouts List of IconLayout with positions.
     */
    private void renderIcons(Graphics g, List<IconLayout> iconLayouts) {
        for (IconLayout layout : iconLayouts) {
            paintIcon(g, layout.getIcon(), layout.getX(), layout.getY());
        }
    }

    /**
     * Paints an icon at the specified position and highlights it if it's the removed icon.
     *
     * @param g      Graphics context.
     * @param icon   The Icon to paint.
     * @param paintX The X position.
     * @param paintY The Y position.
     */
    private void paintIcon(Graphics g, Icon icon, final int paintX, final int paintY) {
        icon.paintIcon(this, g, paintX, paintY);
        if (removedIcon == icon) {
            g.setColor(removalColor);
            g.drawLine(paintX, paintY, paintX + icon.getIconWidth(), paintY + icon.getIconHeight());
            g.drawLine(paintX + icon.getIconWidth(), paintY, paintX, paintY + icon.getIconHeight());
        }
    }

    /**
     * Returns the Icon at the specified point, or null if none.
     *
     * @param point The point to check.
     * @return The Icon at the point or null if none.
     */
    public Icon getIconAt(Point point) {
        IconBounds iconBounds = calculateIconBounds();
        float zoom = getZoom();
        return findIconAtPoint(zoom == 1f ? point : new Point((int)(point.x / zoom), (int)(point.y / zoom)), iconBounds.getIconLayouts());
    }

    /**
     * Finds the icon at the specified point by checking the bounds of each icon.
     *
     * @param point      The point to check.
     * @param iconLayouts List of IconLayout with positions.
     * @return The Icon at the point or null if none.
     */
    private Icon findIconAtPoint(Point point, List<IconLayout> iconLayouts) {
        for (IconLayout layout : iconLayouts) {
            if (layout.contains(point)) {
                return layout.getIcon();
            }
        }
        return null;
    }

    /**
     * Calculates the preferred size of the component based on the icons, maximum width, and zoom.
     *
     * @return The preferred Dimension.
     */
    @Override
    public Dimension getPreferredSize() {
        IconBounds iconBounds = calculateIconBounds();
        int preferredWidth = iconBounds.getMaximumRowWidth();
        int preferredHeight = iconBounds.getTotalHeight();

        Insets insets = getInsets();
        // Apply zoom to preferred size
        float zoom = getZoom();
        int scaledWidth = Math.round((preferredWidth + insets.left + insets.right) * zoom);
        int scaledHeight = Math.round((preferredHeight + insets.top + insets.bottom) * zoom);

        return new Dimension(scaledWidth, scaledHeight);
    }

    /**
     * Inner class representing the layout bounds of all icons.
     */
    private static class IconBounds {
        private final List<IconLayout> iconLayouts;
        private final int totalHeight;
        private final int maximumRowWidth;



        /**
         * Constructs IconBounds by calculating icon positions based on rows.
         *
         * @param rows           List of Rows containing icons and row heights.
         * @param leftInset      Left inset.
         * @param topInset       Top inset.
         * @param availableWidth Available width for icons.
         */

        public IconBounds(List<IconLayout> iconLayouts, int totalHeight, int maximumRowWidth) {
            super();
            this.iconLayouts = iconLayouts;
            this.totalHeight = totalHeight;
            this.maximumRowWidth = maximumRowWidth;
        }

        /**
         * Calculates the maximum row width from all rows.
         *
         * @param rows           List of Rows.
         * @param availableWidth Available width for icons.
         * @return The maximum row width.
         */
        static int calculateMaximumRowWidth(List<Row> rows) {
            int max = 0;
            for (Row row : rows) {
                max = Math.max(max, row.getTotalWidth());
            }
            return max;
        }

        /**
         * Returns the list of IconLayout with positions.
         *
         * @return List of IconLayout.
         */
        public List<IconLayout> getIconLayouts() {
            return iconLayouts;
        }

        /**
         * Returns the total height required to display all rows.
         *
         * @return Total height.
         */
        public int getTotalHeight() {
            return totalHeight;
        }

        /**
         * Returns the maximum row width.
         *
         * @return Maximum row width.
         */
        public int getMaximumRowWidth() {
            return maximumRowWidth;
        }
    }

    /**
     * Helper class to associate an Icon with its bounds.
     */
    private static class IconLayout {
        private final Icon icon;
        private final int x;
        private final int y;

        public IconLayout(Icon icon, int x, int y) {
            this.icon = icon;
            this.x = x;
            this.y = y;
        }

        public boolean contains(Point point) {
            return x <= point.x && y <= point.y && point.x < x + icon.getIconWidth() && point.y < y + icon.getIconHeight();
        }

        public Icon getIcon() {
            return icon;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }
    }

    /**
     * Represents a row of icons.
     */
    private static class Row {
        private final List<Icon> icons;
        private int totalWidth;
        private int maxHeight;

        public Row() {
            this.icons = new ArrayList<>();
            this.totalWidth = 0;
            this.maxHeight = 0;
        }

        /**
         * Adds an icon to the row and updates row metrics.
         *
         * @param icon The icon to add.
         */
        public void addIcon(Icon icon) {
            icons.add(icon);
            totalWidth += icon.getIconWidth();
            maxHeight = Math.max(maxHeight, icon.getIconHeight());
        }

        public List<Icon> getIcons() {
            return icons;
        }

        public int getTotalWidth() {
            return totalWidth;
        }

        public int getMaxHeight() {
            return maxHeight;
        }

        public boolean isEmpty() {
            return icons.isEmpty();
        }
    }

    /**
     * Determines the starting X position for a row based on horizontal alignment.
     *
     * @param rowWidth       Total width of the row.
     * @param availableWidth Available width for icons.
     * @return Starting X offset.
     */
    private int determineStartingX(int rowWidth, int availableWidth) {
        int dx = availableWidth - rowWidth;
        switch (horizontalAlignment) {
            case SwingConstants.CENTER:
                return dx / 2;
            case SwingConstants.RIGHT:
                return getComponentOrientation().isLeftToRight() ? dx : 0;
            case SwingConstants.LEFT:
            default:
                return getComponentOrientation().isLeftToRight() ? 0 : dx;
        }
    }

    /**
     * Calculates the Y position for an icon based on vertical alignment.
     *
     * @param rowY        Y position of the row.
     * @param rowHeight   Height of the row.
     * @param iconHeight  Height of the icon.
     * @return Calculated Y position for the icon.
     */
    private int calculatePaintY(int rowY, int rowHeight, int iconHeight) {
        switch (verticalAlignment) {
            case SwingConstants.CENTER:
                return rowY + (rowHeight - iconHeight) / 2;
            case SwingConstants.TOP:
                return rowY;
            case SwingConstants.BOTTOM:
                return rowY + rowHeight - iconHeight;
            default:
                return rowY;
        }
    }
}