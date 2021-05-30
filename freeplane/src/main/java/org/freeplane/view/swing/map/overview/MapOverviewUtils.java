package org.freeplane.view.swing.map.overview;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Rectangle;

import javax.swing.JScrollBar;
import javax.swing.JScrollPane;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.view.swing.map.MapView;

public class MapOverviewUtils {

    enum MapOverviewAttachPoint {
        NORTH_WEST, NORTH_EAST, SOUTH_WEST, SOUTH_EAST
    }

    static Color complementaryColor(final Color color) {
        return new Color(0xFF - color.getRed(), 0xFF - color.getGreen(), 0xFF - color.getBlue());
    }

    static MapOverviewAttachPoint getMapOverviewAttachPoint() {
        ResourceController resourceController = ResourceController.getResourceController();
        String rawAttachPoint = resourceController.getProperty(MapOverviewConstants.ATTACH_POINT_PROPERTY);
        if (rawAttachPoint == null) {
            return MapOverviewAttachPoint.SOUTH_EAST;
        }
        try {
            return MapOverviewAttachPoint.valueOf(rawAttachPoint);
        } catch (IllegalArgumentException e) {
            return MapOverviewAttachPoint.SOUTH_EAST;
        }
    }

    static void convertOriginByAttachPoint(MapView mapView, Rectangle bounds) {
        final JScrollPane mapViewScrollPane = (JScrollPane) (mapView.getParent().getParent());
        Insets insets = mapViewScrollPane.getInsets();
        int bottom = mapViewScrollPane.getHeight() - insets.bottom;
        int right = mapViewScrollPane.getWidth() - insets.right;
        JScrollBar hsb = mapViewScrollPane.getHorizontalScrollBar();
        JScrollBar vsb = mapViewScrollPane.getVerticalScrollBar();
        int vsw = vsb.isVisible() ? vsb.getSize().width : 0;
        int hsw = hsb.isVisible() ? hsb.getSize().height : 0;

        switch (getMapOverviewAttachPoint()) {
        case SOUTH_EAST:
            bounds.setLocation(right - bounds.x - vsw - 1 - bounds.width, bottom - bounds.y - hsw - 1 - bounds.height);
            break;
        case SOUTH_WEST:
            bounds.setLocation(bounds.x, bottom - bounds.y - hsw - 1 - bounds.height);
            break;
        case NORTH_EAST:
            bounds.setLocation(right - bounds.x - vsw - 1 - bounds.width, bounds.y);
            break;
        case NORTH_WEST:
            break;
        }
    }

    static int getBoundedValue(int value, int min, int max) {
        return Math.max(Math.min(value, max), min);
    }

    public static Rectangle getMapOverviewBounds(MapView mapView) {
        ResourceController resourceController = ResourceController.getResourceController();
        String rawBoundsValue = resourceController.getProperty(MapOverviewConstants.BOUNDS_PROPERTY);
        int[] elements = null;
        if (rawBoundsValue != null) {
            String[] rawElements = rawBoundsValue.split(",");
            if (rawElements.length == 4) {
                elements = new int[4];
                for (int i = 0; i < 4; i++) {
                    try {
                        elements[i] = Integer.parseInt(rawElements[i]);
                    } catch (NumberFormatException e) {
                        elements = null;
                        break;
                    }
                }
            }
        }

        if (elements == null) {
            elements = new int[] { 0, 0, MapOverviewConstants.DEFAULT_SIZE, MapOverviewConstants.DEFAULT_SIZE };
        }
        int x = elements[0];
        int y = elements[1];
        int width = getBoundedValue(elements[2], MapOverviewConstants.MIN_SIZE, MapOverviewConstants.MAX_SIZE);
        int height = getBoundedValue(elements[3], MapOverviewConstants.MIN_SIZE, MapOverviewConstants.MAX_SIZE);
        Rectangle bounds = new Rectangle(x, y, width, height);
        convertOriginByAttachPoint(mapView, bounds);
        return bounds;
    }

    static double getBestScale(Dimension source, Dimension target, Dimension extentedSize) {
        double sourceWidth = source.getWidth();
        double sourceHeight = source.getHeight();
        double targetWidth = target.getWidth();
        double targetHeight = target.getHeight();
        double scaleX = targetWidth / sourceWidth;
        double scaleY = targetHeight / sourceHeight;
        double scale = ((sourceWidth / sourceHeight) > (targetWidth / targetHeight)) ? scaleX : scaleY;
        if (extentedSize != null) {
            double extendedWidth = (targetWidth / scale - sourceWidth) / 2d;
            double extendedHeight = (targetHeight / scale - sourceHeight) / 2d;
            extentedSize.setSize(extendedWidth, extendedHeight);
        }
        return scale;
    }

}
