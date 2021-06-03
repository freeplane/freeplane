package org.freeplane.view.swing.map.overview.resizable;

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

import org.freeplane.view.swing.map.overview.MapViewPane;

public class ResizablePanelBorder implements Border, SwingConstants {

    private enum MouseActionableSite {
        NORTH(Cursor.N_RESIZE_CURSOR) {
            @Override
            Point getSiteLocation(Dimension componentSize, int borderSize) {
                return new Point(componentSize.width / 2 - borderSize / 2, 0);
            }
        },
        SOUTH(Cursor.S_RESIZE_CURSOR) {
            @Override
            Point getSiteLocation(Dimension componentSize, int borderSize) {
                return new Point(componentSize.width / 2 - borderSize / 2, componentSize.height - borderSize);
            }
        },
        WEST(Cursor.W_RESIZE_CURSOR) {
            @Override
            Point getSiteLocation(Dimension componentSize, int borderSize) {
                return new Point(0, componentSize.height / 2 - borderSize / 2);
            }
        },
        EAST(Cursor.E_RESIZE_CURSOR) {
            @Override
            public Point getSiteLocation(Dimension componentSize, int borderSize) {
                return new Point(componentSize.width - borderSize, componentSize.height / 2 - borderSize / 2);
            }
        },
        NORTH_WEST(Cursor.NW_RESIZE_CURSOR) {
            @Override
            Point getSiteLocation(Dimension componentSize, int borderSize) {
                return new Point(0, 0);
            }
        },
        NORTH_EAST(Cursor.NE_RESIZE_CURSOR) {
            @Override
            Point getSiteLocation(Dimension componentSize, int borderSize) {
                return new Point(componentSize.width - borderSize, 0);
            }
        },
        SOUTH_WEST(Cursor.SW_RESIZE_CURSOR) {
            @Override
            Point getSiteLocation(Dimension componentSize, int borderSize) {
                return new Point(0, componentSize.height - borderSize);
            }
        },
        SOUTH_EAST(Cursor.SE_RESIZE_CURSOR) {
            @Override
            Point getSiteLocation(Dimension componentSize, int borderSize) {
                return new Point(componentSize.width - borderSize, componentSize.height - borderSize);
            }
        };

        private final int cursor;

        MouseActionableSite(int cursor) {
            this.cursor = cursor;
        }

        abstract Point getSiteLocation(Dimension componentSize, int borderSize);

        public Cursor getCursor() {
            return Cursor.getPredefinedCursor(cursor);
        }
    }

    private final int size;
    private final int extendedSize;

    public ResizablePanelBorder(int size, int extendedSize) {
        this.size = size;
        this.extendedSize = extendedSize;
    }

    @Override
    public Insets getBorderInsets(Component component) {
        return new Insets(size, size, size, size);
    }

    @Override
    public boolean isBorderOpaque() {
        return false;
    }

    @Override
    public void paintBorder(Component component, Graphics g, int x, int y, int w, int h) {
        MapViewPane mapViewPane = (MapViewPane) component.getParent();
        final Color mapViewBackground = mapViewPane.getMapViewBackground();
        final Color invertedColor = complementaryColor(mapViewBackground);
        g.setColor(mapViewBackground);
        g.fillRect(x, y, w, h);
        g.setColor(invertedColor);
        g.drawRect(x, y, w - 1, h - 1);

        Rectangle rect = new Rectangle(size, size);
        g.setColor(invertedColor);
        for (MouseActionableSite loc : MouseActionableSite.values()) {
            rect.setLocation(loc.getSiteLocation(new Dimension(w, h), size));
            g.fillRect(rect.x, rect.y, rect.width, rect.height);
        }
    }

    private Color complementaryColor(final Color color) {
        return new Color(0xFF - color.getRed(), 0xFF - color.getGreen(), 0xFF - color.getBlue());
    }

    public Cursor getResizeCursor(MouseEvent e) {
        Component c = e.getComponent();
        int width = c.getWidth();
        int height = c.getHeight();
        Point mouseLocation = e.getPoint();

        Rectangle bounds = new Rectangle(width, height);
        if (! bounds.contains(mouseLocation)) {
            return Cursor.getDefaultCursor();
        }

        Rectangle contentAreaBounds = new Rectangle(size, size, width - 2 * size, height - 2 * size);
        if (contentAreaBounds.contains(mouseLocation)) {
            return Cursor.getDefaultCursor();
        }

        Rectangle actionableRegion = new Rectangle(extendedSize, extendedSize);
        for (MouseActionableSite site : MouseActionableSite.values()) {
            actionableRegion.setLocation(site.getSiteLocation(c.getSize(), extendedSize));
            if (actionableRegion.contains(mouseLocation)) {
                return site.getCursor();
            }
        }

        return Cursor.getPredefinedCursor(ResizePanelMouseHandler.COMPAT_MOVE_CURSOR);
    }
}
