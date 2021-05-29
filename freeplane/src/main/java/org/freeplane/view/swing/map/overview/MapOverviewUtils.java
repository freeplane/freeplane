package org.freeplane.view.swing.map.overview;

import java.awt.Color;
import java.awt.Insets;
import java.awt.Rectangle;

import javax.swing.JScrollBar;
import javax.swing.JScrollPane;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.view.swing.map.MapView;

public class MapOverviewUtils {
    static Color complementaryColor(final Color color) {
        return new Color(0xFF - color.getRed(), 0xFF - color.getGreen(), 0xFF - color.getBlue());
    }

    private static MapOverviewAttachPoint getMapOverviewAttachPoint() {
        ResourceController resourceController = ResourceController.getResourceController();
        String rawAttachPoint = resourceController.getProperty(MapOverviewConstants.PROP_MAP_OVERVIEW_ATTACH_POINT,
                MapOverviewAttachPoint.SOUTH_EAST.name());
        try {
            return MapOverviewAttachPoint.valueOf(rawAttachPoint);
        } catch (IllegalArgumentException e) {
            return MapOverviewAttachPoint.SOUTH_EAST;
        }
    }

    public static void convertOriginByAttachPoint(MapView mapView, Rectangle bounds) {
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

    public static int getBoundedValue(int value, int min, int max) {
        return Math.max(Math.min(value, max), min);
    }

    public static Rectangle getMapOverviewBounds(MapView mapView) {
        ResourceController resourceController = ResourceController.getResourceController();
        String rawBoundsValue = resourceController.getProperty(MapOverviewConstants.PROP_MAP_OVERVIEW_BOUNDS);
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

    public static double getBestScale(Rectangle source, double scaledOverviewBoundWidth,
            double scaledOverviewBoundHeight) {
        double innerBoundsWidth = (double) source.width;
        double innerBoundsHeight = (double) source.height;
        double scaleX = scaledOverviewBoundWidth / innerBoundsWidth;
        double scaleY = scaledOverviewBoundHeight / innerBoundsHeight;
        return ((innerBoundsWidth / innerBoundsHeight) > (scaledOverviewBoundWidth / scaledOverviewBoundHeight))
                ? scaleX
                : scaleY;
    }

}
