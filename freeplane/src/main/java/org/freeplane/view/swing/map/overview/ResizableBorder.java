package org.freeplane.view.swing.map.overview;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;

import javax.swing.SwingConstants;
import javax.swing.border.Border;

import org.freeplane.view.swing.map.MapView;

public class ResizableBorder implements Border, SwingConstants {
    static final int SIZE = 6;

    private enum MouseActionableSite {
        NORTH(Cursor.N_RESIZE_CURSOR) {
            @Override
            Point getSiteLocation(Dimension componentSize) {
                return new Point(componentSize.width / 2 - SIZE / 2, 0);
            }
        },
        SOUTH(Cursor.S_RESIZE_CURSOR) {
            @Override
            Point getSiteLocation(Dimension componentSize) {
                return new Point(componentSize.width / 2 - SIZE / 2, componentSize.height - SIZE);
            }
        },
        WEST(Cursor.W_RESIZE_CURSOR) {
            @Override
            Point getSiteLocation(Dimension componentSize) {
                return new Point(0, componentSize.height / 2 - SIZE / 2);
            }
        },
        EAST(Cursor.E_RESIZE_CURSOR) {
            @Override
            public Point getSiteLocation(Dimension componentSize) {
                return new Point(componentSize.width - SIZE, componentSize.height / 2 - SIZE / 2);
            }
        },
        NORTH_WEST(Cursor.NW_RESIZE_CURSOR) {
            @Override
            Point getSiteLocation(Dimension componentSize) {
                return new Point(0, 0);
            }
        },
        NORTH_EAST(Cursor.NE_RESIZE_CURSOR) {
            @Override
            Point getSiteLocation(Dimension componentSize) {
                return new Point(componentSize.width - SIZE, 0);
            }
        },
        SOUTH_WEST(Cursor.SW_RESIZE_CURSOR) {
            @Override
            Point getSiteLocation(Dimension componentSize) {
                return new Point(0, componentSize.height - SIZE);
            }
        },
        SOUTH_EAST(Cursor.SE_RESIZE_CURSOR) {
            @Override
            Point getSiteLocation(Dimension componentSize) {
                return new Point(componentSize.width - SIZE, componentSize.height - SIZE);
            }
        };

        private final int cursor;

        MouseActionableSite(int cursor) {
            this.cursor = cursor;
        }

        abstract Point getSiteLocation(Dimension componentSize);

        public Cursor getCursor() {
            return Cursor.getPredefinedCursor(cursor);
        }
    }

    private final MapView mapView;

    public ResizableBorder(MapView mapView) {
        this.mapView = mapView;
    }

    @Override
    public Insets getBorderInsets(Component component) {
        final int size = SIZE;
        return new Insets(size, size, size, size);
    }

    @Override
    public boolean isBorderOpaque() {
        return false;
    }

    @Override
    public void paintBorder(Component component, Graphics g, int x, int y, int w, int h) {
        System.out.println("x = " + x + ", y = " + y);
        final Color mapViewBackground = mapView.getBackground();
        final Color invertedColor = MapOverviewUtils.complementaryColor(mapViewBackground);
        g.setColor(mapViewBackground);
        g.fillRect(x, y, w, h);
        g.setColor(invertedColor);
        g.drawRect(x + SIZE / 2, y + SIZE / 2, w - SIZE, h - SIZE);
        Rectangle rect = new Rectangle(SIZE, SIZE);
        for (MouseActionableSite loc : MouseActionableSite.values()) {
            rect.setLocation(loc.getSiteLocation(new Dimension(w, h)));
            g.setColor(invertedColor);
            g.fillRect(rect.x, rect.y, rect.width - 1, rect.height - 1);
            g.setColor(mapViewBackground);
            g.drawRect(rect.x, rect.y, rect.width - 1, rect.height - 1);
        }
    }

    public Cursor getResizeCursor(MouseEvent e) {
        Component c = e.getComponent();
        int width = c.getWidth();
        int height = c.getHeight();
        Point pt = e.getPoint();

        Rectangle bounds = new Rectangle(width, height);
        if (! bounds.contains(pt)) {
            return Cursor.getDefaultCursor();
        }

        Rectangle contentAreaBounds = new Rectangle(SIZE, SIZE, width - 2 * SIZE, height - 2 * SIZE);
        if (contentAreaBounds.contains(pt)) {
            return Cursor.getDefaultCursor();
        }

        Rectangle actionableRegion = new Rectangle(SIZE, SIZE);
        for (MouseActionableSite site : MouseActionableSite.values()) {
            actionableRegion.setLocation(site.getSiteLocation(c.getSize()));
            if (actionableRegion.contains(pt)) {
                return site.getCursor();
            }
        }

        return Cursor.getPredefinedCursor(ResizePanelMouseHandler.COMPAT_MOVE_CURSOR);
    }
}
