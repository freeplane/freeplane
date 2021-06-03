package org.freeplane.view.swing.map.overview.resizable;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.EnumSet;
import java.util.Optional;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputAdapter;

import org.freeplane.core.util.Compat;
import org.freeplane.view.swing.map.overview.MapOverviewAttachPoint;
import org.freeplane.view.swing.map.overview.MapViewPane;

public class ResizePanelMouseHandler extends MouseInputAdapter {
    final static int COMPAT_MOVE_CURSOR = Compat.isMacOsX() ? Cursor.HAND_CURSOR : Cursor.MOVE_CURSOR;

    private enum MouseDragAction {
        RESIZE_NORTH(Cursor.N_RESIZE_CURSOR) {
            @Override
            Rectangle getTargetBounds(Rectangle startBounds, Dimension d) {
                return new Rectangle(startBounds.x, startBounds.y - d.height, startBounds.width,
                        startBounds.height + d.height);
            }
        },
        RESIZE_SOUTH(Cursor.S_RESIZE_CURSOR) {
            @Override
            Rectangle getTargetBounds(Rectangle startBounds, Dimension d) {
                return new Rectangle(startBounds.x, startBounds.y, startBounds.width, startBounds.height - d.height);
            }
        },
        RESIZE_WEST(Cursor.W_RESIZE_CURSOR) {
            @Override
            Rectangle getTargetBounds(Rectangle startBounds, Dimension d) {
                return new Rectangle(startBounds.x - d.width, startBounds.y, startBounds.width + d.width,
                        startBounds.height);
            }
        },
        RESIZE_EAST(Cursor.E_RESIZE_CURSOR) {
            @Override
            Rectangle getTargetBounds(Rectangle startBounds, Dimension d) {
                return new Rectangle(startBounds.x, startBounds.y, startBounds.width - d.width, startBounds.height);
            }
        },
        RESIZE_NORTH_WEST(Cursor.NW_RESIZE_CURSOR) {
            @Override
            Rectangle getTargetBounds(Rectangle startBounds, Dimension d) {
                return new Rectangle(startBounds.x - d.width, startBounds.y - d.height, startBounds.width + d.width,
                        startBounds.height + d.height);
            }
        },
        RESIZE_NORTH_EAST(Cursor.NE_RESIZE_CURSOR) {
            @Override
            Rectangle getTargetBounds(Rectangle startBounds, Dimension d) {
                return new Rectangle(startBounds.x, startBounds.y - d.height, startBounds.width - d.width,
                        startBounds.height + d.height);
            }
        },
        RESIZE_SOUTH_WEST(Cursor.SW_RESIZE_CURSOR) {
            @Override
            Rectangle getTargetBounds(Rectangle startBounds, Dimension d) {
                return new Rectangle(startBounds.x - d.width, startBounds.y, startBounds.width + d.width,
                        startBounds.height - d.height);
            }
        },
        RESIZE_SOUTH_EAST(Cursor.SE_RESIZE_CURSOR) {
            @Override
            Rectangle getTargetBounds(Rectangle startBounds, Dimension d) {
                return new Rectangle(startBounds.x, startBounds.y, startBounds.width - d.width,
                        startBounds.height - d.height);
            }
        },
        MOVE_COMPONENT(COMPAT_MOVE_CURSOR) {
            @Override
            Rectangle getTargetBounds(Rectangle startBounds, Dimension d) {
                return new Rectangle(startBounds.x - d.width, startBounds.y - d.height, startBounds.width,
                        startBounds.height);
            }
        };

        private final int cursor;

        MouseDragAction(int cursor) {
            this.cursor = cursor;
        }

        abstract Rectangle getTargetBounds(Rectangle startBounds, Dimension delta);

        public static Optional<MouseDragAction> getByCursorType(int cursor) {
            return EnumSet.allOf(MouseDragAction.class).stream().filter(d -> d.cursor == cursor).findFirst();
        }
    }

    private final Point startLocation = new Point();
    private final Rectangle startBounds = new Rectangle();
    private Cursor cursor;

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() != 2 || e.getButton() != 1) {
            return;
        }
        int cursorType = Optional.ofNullable(cursor).map(Cursor::getType).orElse(Cursor.DEFAULT_CURSOR);
        MouseDragAction.getByCursorType(cursorType).ifPresent(action -> {
            int ordinal = action.ordinal();
            if (ordinal > MouseDragAction.RESIZE_EAST.ordinal() && ordinal < MouseDragAction.MOVE_COMPONENT.ordinal()) {
                MapOverviewAttachPoint attachPoint = MapOverviewAttachPoint
                        .values()[(ordinal - MouseDragAction.RESIZE_NORTH_WEST.ordinal())];
                MapViewPane mapViewPane = (MapViewPane) e.getComponent().getParent();
                mapViewPane.updateMapOverviewAttachPoint(attachPoint);
            }
        });
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        JComponent c = (JComponent) e.getComponent();
        ResizablePanelBorder border = (ResizablePanelBorder) c.getBorder();
        c.setCursor(border.getResizeCursor(e));
    }

    @Override
    public void mouseExited(MouseEvent e) {
        e.getComponent().setCursor(Cursor.getDefaultCursor());
    }

    @Override
    public void mousePressed(MouseEvent e) {
        JComponent c = (JComponent) e.getComponent();
        ResizablePanelBorder border = (ResizablePanelBorder) c.getBorder();
        cursor = border.getResizeCursor(e);
        startLocation.setLocation(SwingUtilities.convertPoint(c, e.getX(), e.getY(), null));
        startBounds.setBounds(c.getBounds());
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        startBounds.setSize(0, 0);
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (startBounds.isEmpty()) {
            return;
        }
        Component resizedPanel = e.getComponent();
        Point targetPoint = SwingUtilities.convertPoint(resizedPanel, e.getX(), e.getY(), null);
        int deltaX = startLocation.x - targetPoint.x;
        int deltaY = startLocation.y - targetPoint.y;
        if (! resizedPanel.getCursor().equals(cursor)) {
            resizedPanel.setCursor(cursor); // restore the cursor in case mouseExited is triggered while dragging
        }

        MapViewPane mapViewPane = (MapViewPane) e.getComponent().getParent();
        int cursorType = Optional.ofNullable(cursor).map(Cursor::getType).orElse(Cursor.DEFAULT_CURSOR);
        MouseDragAction.getByCursorType(cursorType).ifPresent(action -> {
            Dimension delta = getLimitedDimensionDelta(cursorType, mapViewPane.getBounds(), deltaX, deltaY);
            mapViewPane.setMapOverviewBounds(action.getTargetBounds(startBounds, delta));
        });
    }

    private int getDeltaX(int dx) {
        int left = Math.min(MapViewPane.MAP_OVERVIEW_MAX_SIZE - startBounds.width, startBounds.x);
        return Math.max(Math.min(dx, left), MapViewPane.MAP_OVERVIEW_MIN_SIZE - startBounds.width);
    }

    private int getDeltaX(int dx, Rectangle pr) {
        int right = Math.max(startBounds.width - MapViewPane.MAP_OVERVIEW_MAX_SIZE,
                startBounds.x + startBounds.width - pr.width);
        return Math.min(Math.max(dx, right), startBounds.width - MapViewPane.MAP_OVERVIEW_MIN_SIZE);
    }

    private int getDeltaY(int dy) {
        int top = Math.min(MapViewPane.MAP_OVERVIEW_MAX_SIZE - startBounds.height, startBounds.y);
        return Math.max(Math.min(dy, top), MapViewPane.MAP_OVERVIEW_MIN_SIZE - startBounds.height);
    }

    private int getDeltaY(int dy, Rectangle pr) {
        int bottom = Math.max(startBounds.height - MapViewPane.MAP_OVERVIEW_MAX_SIZE,
                startBounds.y + startBounds.height - pr.height);
        return Math.min(Math.max(dy, bottom), startBounds.height - MapViewPane.MAP_OVERVIEW_MIN_SIZE);
    }

    private Dimension getLimitedDimensionDelta(int cursorType, Rectangle pr, int deltaX, int deltaY) {
        switch (cursorType) {
        case Cursor.N_RESIZE_CURSOR:
            return new Dimension(0, getDeltaY(deltaY));
        case Cursor.S_RESIZE_CURSOR:
            return new Dimension(0, getDeltaY(deltaY, pr));
        case Cursor.W_RESIZE_CURSOR:
            return new Dimension(getDeltaX(deltaX), 0);
        case Cursor.E_RESIZE_CURSOR:
            return new Dimension(getDeltaX(deltaX, pr), 0);
        case Cursor.NW_RESIZE_CURSOR:
            return new Dimension(getDeltaX(deltaX), getDeltaY(deltaY));
        case Cursor.SW_RESIZE_CURSOR:
            return new Dimension(getDeltaX(deltaX), getDeltaY(deltaY, pr));
        case Cursor.NE_RESIZE_CURSOR:
            return new Dimension(getDeltaX(deltaX, pr), getDeltaY(deltaY));
        case Cursor.SE_RESIZE_CURSOR:
            return new Dimension(getDeltaX(deltaX, pr), getDeltaY(deltaY, pr));
        default:
            return new Dimension(deltaX, deltaY);
        }
    }
}
