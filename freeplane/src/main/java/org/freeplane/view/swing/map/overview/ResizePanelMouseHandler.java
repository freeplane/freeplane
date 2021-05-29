package org.freeplane.view.swing.map.overview;

import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.EnumSet;
import java.util.Optional;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputAdapter;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.Compat;
import org.freeplane.view.swing.map.MapView;

public class ResizePanelMouseHandler extends MouseInputAdapter {
    final static int COMPAT_MOVE_CURSOR = Compat.isMacOsX() ? Cursor.HAND_CURSOR : Cursor.MOVE_CURSOR;

    private enum MouseDragAction {
        NORTH(Cursor.N_RESIZE_CURSOR) {
            @Override
            Rectangle getFinalBounds(Rectangle r, Point d) {
                return new Rectangle(r.x, r.y - d.y, r.width, r.height + d.y);
            }
        },
        SOUTH(Cursor.S_RESIZE_CURSOR) {
            @Override
            Rectangle getFinalBounds(Rectangle r, Point d) {
                return new Rectangle(r.x, r.y, r.width, r.height - d.y);
            }
        },
        WEST(Cursor.W_RESIZE_CURSOR) {
            @Override
            Rectangle getFinalBounds(Rectangle r, Point d) {
                return new Rectangle(r.x - d.x, r.y, r.width + d.x, r.height);
            }
        },
        EAST(Cursor.E_RESIZE_CURSOR) {
            @Override
            Rectangle getFinalBounds(Rectangle r, Point d) {
                return new Rectangle(r.x, r.y, r.width - d.x, r.height);
            }
        },
        NORTH_WEST(Cursor.NW_RESIZE_CURSOR) {
            @Override
            Rectangle getFinalBounds(Rectangle r, Point d) {
                return new Rectangle(r.x - d.x, r.y - d.y, r.width + d.x, r.height + d.y);
            }
        },
        NORTH_EAST(Cursor.NE_RESIZE_CURSOR) {
            @Override
            Rectangle getFinalBounds(Rectangle r, Point d) {
                return new Rectangle(r.x, r.y - d.y, r.width - d.x, r.height + d.y);
            }
        },
        SOUTH_WEST(Cursor.SW_RESIZE_CURSOR) {
            @Override
            Rectangle getFinalBounds(Rectangle r, Point d) {
                return new Rectangle(r.x - d.x, r.y, r.width + d.x, r.height - d.y);
            }
        },
        SOUTH_EAST(Cursor.SE_RESIZE_CURSOR) {
            @Override
            Rectangle getFinalBounds(Rectangle r, Point d) {
                return new Rectangle(r.x, r.y, r.width - d.x, r.height - d.y);
            }
        },
        MOVE(COMPAT_MOVE_CURSOR) {
            @Override
            Rectangle getFinalBounds(Rectangle r, Point d) {
                return new Rectangle(r.x - d.x, r.y - d.y, r.width, r.height);
            }
        };

        private final int cursor;

        MouseDragAction(int cursor) {
            this.cursor = cursor;
        }

        abstract Rectangle getFinalBounds(Rectangle rect, Point delta);

        public static Optional<MouseDragAction> getByCursorType(int cursor) {
            return EnumSet.allOf(MouseDragAction.class).stream().filter(d -> d.cursor == cursor).findFirst();
        }
    }

    private final Point startPos = new Point();
    private final Rectangle startingBounds = new Rectangle();
    private final MapView mapView;
    private Cursor cursor;

    public ResizePanelMouseHandler(MapView mapView) {
        this.mapView = mapView;
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        JComponent c = (JComponent) e.getComponent();
        ResizableBorder border = (ResizableBorder) c.getBorder();
        c.setCursor(border.getResizeCursor(e));
    }

    @Override
    public void mouseExited(MouseEvent e) {
        e.getComponent().setCursor(Cursor.getDefaultCursor());
    }

    @Override
    public void mousePressed(MouseEvent e) {
        JComponent c = (JComponent) e.getComponent();
        ResizableBorder border = (ResizableBorder) c.getBorder();
        cursor = border.getResizeCursor(e);
        startPos.setLocation(SwingUtilities.convertPoint(c, e.getX(), e.getY(), null));
        startingBounds.setBounds(c.getBounds());
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        startingBounds.setSize(0, 0);
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (startingBounds.isEmpty()) {
            return;
        }
        Component c = e.getComponent();
        Point p = SwingUtilities.convertPoint(c, e.getX(), e.getY(), null);
        int deltaX = startPos.x - p.x;
        int deltaY = startPos.y - p.y;
        Container parent = SwingUtilities.getUnwrappedParent(c);
        int cursorType = Optional.ofNullable(cursor).map(Cursor::getType).orElse(Cursor.DEFAULT_CURSOR);
        MouseDragAction.getByCursorType(cursorType).ifPresent(action -> {
            Point delta = getLimitedDelta(cursorType, parent.getBounds(), deltaX, deltaY);
            Rectangle bounds = action.getFinalBounds(startingBounds, delta);
            setMapOverviewBounds(bounds);
        });
    }

    private void setMapOverviewBounds(Rectangle bounds) {
        MapOverviewUtils.convertOriginByAttachPoint(mapView, bounds);
        ResourceController.getResourceController().setProperty(MapOverviewConstants.PROP_MAP_OVERVIEW_BOUNDS,
                String.format("%s,%s,%s,%s", bounds.x, bounds.y, bounds.width, bounds.height));
    }

    private int getDeltaX(int dx) {
        int left = Math.min(MapOverviewConstants.MAX_SIZE - startingBounds.width, startingBounds.x);
        return Math.max(Math.min(dx, left), MapOverviewConstants.MIN_SIZE - startingBounds.width);
    }

    private int getDeltaX(int dx, Rectangle pr) {
        int right = Math.max(startingBounds.width - MapOverviewConstants.MAX_SIZE,
                startingBounds.x + startingBounds.width - pr.width);
        return Math.min(Math.max(dx, right), startingBounds.width - MapOverviewConstants.MIN_SIZE);
    }

    private int getDeltaY(int dy) {
        int top = Math.min(MapOverviewConstants.MAX_SIZE - startingBounds.height, startingBounds.y);
        return Math.max(Math.min(dy, top), MapOverviewConstants.MIN_SIZE - startingBounds.height);
    }

    private int getDeltaY(int dy, Rectangle pr) {
        int bottom = Math.max(startingBounds.height - MapOverviewConstants.MAX_SIZE,
                startingBounds.y + startingBounds.height - pr.height);
        return Math.min(Math.max(dy, bottom), startingBounds.height - MapOverviewConstants.MIN_SIZE);
    }

    private Point getLimitedDelta(int cursorType, Rectangle pr, int deltaX, int deltaY) {
        switch (cursorType) {
        case Cursor.N_RESIZE_CURSOR:
            return new Point(0, getDeltaY(deltaY));
        case Cursor.S_RESIZE_CURSOR:
            return new Point(0, getDeltaY(deltaY, pr));
        case Cursor.W_RESIZE_CURSOR:
            return new Point(getDeltaX(deltaX), 0);
        case Cursor.E_RESIZE_CURSOR:
            return new Point(getDeltaX(deltaX, pr), 0);
        case Cursor.NW_RESIZE_CURSOR:
            return new Point(getDeltaX(deltaX), getDeltaY(deltaY));
        case Cursor.SW_RESIZE_CURSOR:
            return new Point(getDeltaX(deltaX), getDeltaY(deltaY, pr));
        case Cursor.NE_RESIZE_CURSOR:
            return new Point(getDeltaX(deltaX, pr), getDeltaY(deltaY));
        case Cursor.SE_RESIZE_CURSOR:
            return new Point(getDeltaX(deltaX, pr), getDeltaY(deltaY, pr));
        default:
            return new Point(deltaX, deltaY);
        }
    }
}
