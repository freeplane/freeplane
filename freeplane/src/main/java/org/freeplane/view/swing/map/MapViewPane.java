package org.freeplane.view.swing.map;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.EnumSet;
import java.util.Optional;

import javax.swing.BoundedRangeModel;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.event.MouseInputAdapter;
import javax.swing.event.MouseInputListener;

import org.freeplane.core.resources.IFreeplanePropertyListener;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.features.map.IMapChangeListener;
import org.freeplane.features.map.MapChangeEvent;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.ui.FrameController;
import org.freeplane.features.ui.IMapViewManager;
import org.freeplane.features.ui.ViewController;

public class MapViewPane extends JLayeredPane implements IFreeplanePropertyListener, IMapChangeListener {
    private static final long serialVersionUID = 8664710783654626093L;

    private static final Color THUMB_COLOR_BLUE = new Color(0x32_00_00_FF, true);
    private static final Color THUMB_COLOR_RED = new Color(0x92_FF_00_00, true);

    private static final int MAP_OVERVIEW_MIN_SIZE = 80;
    private final static int MAP_OVERVIEW_DEFAULT_SIZE = 320;
    private static final int MAP_OVERVIEW_MAX_SIZE = 640;

    private final static String MAP_OVERVIEW_VISIBLE = "mapOverviewVisible";
    private final static String MAP_OVERVIEW_VISIBLE_FS = "mapOverviewVisible.fullscreen";

    private final static String MAP_OVERVIEW_PREFIX = "map_overview_";
    private final static String MAP_OVERVIEW_ATTACH_POINT = MAP_OVERVIEW_PREFIX + "attach_point";
    private final static String MAP_OVERVIEW_BOUNDS = MAP_OVERVIEW_PREFIX + "bounds";

    private static final int RESIZE_PANEL_BORDER_SIZE = 4;

    private class JMapOverviewImage extends JComponent {
		private static final long serialVersionUID = 1L;
		private BufferedImage image;
		
		JMapOverviewImage(){
			MapOverviewMouseHandler handler = new MapOverviewMouseHandler();
            addMouseListener(handler);
            addMouseMotionListener(handler);
            addMouseWheelListener(handler);
		}

        private BufferedImage snapshot(Graphics2D g, Rectangle mapOverviewBounds) {
            Rectangle innerBounds = mapView.getInnerBounds();
            int x = innerBounds.x;
            int y = innerBounds.y;
            int width = innerBounds.width;
            int height = innerBounds.height;

            double scaledOverviewBoundWidth = mapOverviewBounds.width * g.getTransform().getScaleX();
            double scaledOverviewBoundHeight = mapOverviewBounds.height * g.getTransform().getScaleY();
            double scale = getScale(mapView.getInnerBounds(), scaledOverviewBoundWidth, scaledOverviewBoundHeight);

            double extendedWidth = (scaledOverviewBoundWidth / scale - width) / 2d;
            double extendedHeight = (scaledOverviewBoundHeight / scale - height) / 2d;

            AffineTransform translation = AffineTransform.getTranslateInstance(extendedWidth - x, extendedHeight - y);
            AffineTransform transformer = AffineTransform.getScaleInstance(scale, scale);
            transformer.concatenate(translation);

            BufferedImage image = new BufferedImage(mapOverviewBounds.width - RESIZE_PANEL_BORDER_SIZE * 2,
                    mapOverviewBounds.height - RESIZE_PANEL_BORDER_SIZE * 2, BufferedImage.TYPE_INT_ARGB);
            Graphics2D imageG2D = image.createGraphics();
            try {
                imageG2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                        RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                imageG2D.setColor(mapView.getBackground());
                imageG2D.fillRect(0, 0, mapOverviewBounds.width, mapOverviewBounds.height);
                imageG2D.transform(transformer);
                imageG2D.clip(innerBounds);
                mapView.paintOverview(imageG2D);
            } finally {
                imageG2D.dispose();
            }
            return image;
        }		
		

		@Override
        protected void paintComponent(Graphics g) {
			final Graphics2D g2 = (Graphics2D)g;
			final AffineTransform transform = g2.getTransform();
			final double scaleX = transform.getScaleX();
			Rectangle mapOverviewBounds = getMapOverviewBounds();
			if(image == null || image.getWidth() != (int)(mapOverviewBounds.width * scaleX)) {
				image = snapshot(g2, mapOverviewBounds);
			}
			if(scaleX == 1) {
				g2.drawImage(image, 0, 0, this);
			}
			else {
				AffineTransform newTransform = AffineTransform.getTranslateInstance(
						transform.getTranslateX(), 
						 transform.getTranslateY());
				g2.setTransform(newTransform);
				g2.drawImage(image, 0, 0, this);
				g2.setTransform(transform);
			}            
			JViewport viewPort = (JViewport) mapView.getParent();
            Point viewPortPosition = viewPort.getViewPosition();
            Rectangle innerBounds = mapView.getInnerBounds();
            Rectangle viewPortRect = viewPort.getBounds();
            Rectangle thumbRect = new Rectangle(viewPortRect);
            thumbRect.x = viewPortPosition.x - innerBounds.x;
            thumbRect.y = viewPortPosition.y - innerBounds.y;

            double scaledOverviewBoundWidth = mapOverviewBounds.width * g2.getTransform().getScaleX();
            double scaledOverviewBoundHeight = mapOverviewBounds.height * g2.getTransform().getScaleY();
            double scale = getScale(mapView.getInnerBounds(), scaledOverviewBoundWidth, scaledOverviewBoundHeight);

            double extendedWidth = (scaledOverviewBoundWidth / scale - innerBounds.width) / 2d;
            double extendedHeight = (scaledOverviewBoundHeight / scale - innerBounds.height) / 2d;
            thumbRect.x += extendedWidth;
            thumbRect.y += extendedHeight;

            AffineTransform transformer = AffineTransform.getScaleInstance(scale, scale);
            Rectangle scaledThumbRect = transformer.createTransformedShape(thumbRect).getBounds();
            Color color = scaledThumbRect.width * scaledThumbRect.height < 400 ? THUMB_COLOR_RED : THUMB_COLOR_BLUE;
            g2.setColor(color);
            g2.fillRect(scaledThumbRect.x, scaledThumbRect.y, scaledThumbRect.width, scaledThumbRect.height);
            g2.setColor(color.darker());
            g2.drawRect(scaledThumbRect.x, scaledThumbRect.y, scaledThumbRect.width - 1, scaledThumbRect.height - 1);

            String anchorSymbol = "\u2693";
            g2.setColor(complementaryColor(mapView.getBackground()));

            Point anchorPos = new Point();
            Point zoomTextPos = new Point(1, mapOverviewBounds.height - 12);
            switch (getMapOverviewAttachPoint()) {
            case SOUTH_EAST:
                anchorPos.setLocation(mapOverviewBounds.width - 22, mapOverviewBounds.height - 12);
                break;
            case SOUTH_WEST:
                anchorPos.setLocation(1, mapOverviewBounds.height - 12);
                zoomTextPos.setLocation(1, 12);
                break;
            case NORTH_EAST:
                anchorPos.setLocation(mapOverviewBounds.width - 22, 12);
                break;
            case NORTH_WEST:
                anchorPos.setLocation(1, 12);
                break;
            }
            g2.drawChars(anchorSymbol.toCharArray(), 0, anchorSymbol.length(), anchorPos.x, anchorPos.y);

            IMapViewManager mapViewManager = (MapViewController) Controller.getCurrentController().getMapViewManager();
            if (mapViewManager instanceof MapViewController && isFullScreenEnabled()) {
                String zoom = ((MapViewController) mapViewManager).getItemForZoom(mapView.getZoom());
                Color zoomColor = complementaryColor(mapView.getBackground());
                zoomColor = new Color(zoomColor.getRed(), zoomColor.getGreen(), zoomColor.getBlue(), 0x7F);
                g2.setColor(zoomColor);
                g2.drawChars(zoom.toCharArray(), 0, zoom.length(), zoomTextPos.x, zoomTextPos.y);
            }
        }

		private boolean isFullScreenEnabled() {
            final FrameController viewController = (FrameController) Controller.getCurrentController()
                    .getViewController();
            final Component component = viewController.getCurrentRootComponent();
            if (component instanceof JFrame) {
                JRootPane rootPane = ((JFrame) component).getRootPane();
                return rootPane != null
                        && Boolean.TRUE.equals(rootPane.getClientProperty(ViewController.FULLSCREEN_ENABLED_PROPERTY));
            }
            return false;
        }

		public void resetImage() {
			image = null;
		}
	}

	private class MapOverviewMouseHandler extends MouseInputAdapter {
        @Override
        public void mousePressed(MouseEvent e) {
            processMousePanEvent(e);
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            e.getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        }

        @Override
        public void mouseExited(MouseEvent e) {
            e.getComponent().setCursor(Cursor.getDefaultCursor());
        }

        @Override
        public void mouseWheelMoved(MouseWheelEvent e) {
            double oldZoom = mapView.getZoom();
            double amount = Math.pow(1.0345, e.getScrollAmount());
            float zoom = (float) (e.getWheelRotation() > 0 ? (oldZoom / amount) : (oldZoom * amount));
            zoom = Math.max(Math.min(zoom, 32f), 0.03f);
            IMapViewManager viewManager = Controller.getCurrentController().getMapViewManager();
            viewManager.changeToMapView(mapView);
            viewManager.setZoom(zoom);
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            processMousePanEvent(e);
        }

        protected final void processMousePanEvent(MouseEvent e) {
            Rectangle innerBounds = mapView.getInnerBounds();
            Rectangle mapOverviewBounds = getMapOverviewBounds();
            double scale = getScale(mapView.getInnerBounds(), mapOverviewBounds.width, mapOverviewBounds.height);

            double extendedWidth = (mapOverviewBounds.width / scale - innerBounds.width) / 2d;
            double extendedHeight = (mapOverviewBounds.height / scale - innerBounds.height) / 2d;

            Point pt = e.getPoint();
            setHorizontalRange((int) (pt.x - extendedWidth * scale), scale, innerBounds);
            setVerticalRange((int) (pt.y - extendedHeight * scale), scale, innerBounds);
        }

        private final void setVerticalRange(int y, double scale, Rectangle innerBounds) {
            BoundedRangeModel m = mapViewScrollPane.getVerticalScrollBar().getModel();
            int value = (int) (y / scale + innerBounds.y) - m.getExtent() / 2;
            m.setValue(Math.max(value, 0));
        }

        private final void setHorizontalRange(int x, double scale, Rectangle innerBounds) {
            BoundedRangeModel m = mapViewScrollPane.getHorizontalScrollBar().getModel();
            int value = (int) (x / scale + innerBounds.x) - m.getExtent() / 2;
            m.setValue(Math.max(value, 0));
        }
    };

    private class ResizeMouseListener extends MouseInputAdapter {
        private final Point startPos = new Point();
        private final Rectangle startingBounds = new Rectangle();
        private Cursor cursor;

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
        public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() != 2) {
                return;
            }

            int cursorType = Optional.ofNullable(cursor).map(Cursor::getType).orElse(Cursor.DEFAULT_CURSOR);
            Directions.getByCursorType(cursorType).ifPresent(dir -> {
                int ordinal = dir.ordinal();
                if (ordinal > Directions.EAST.ordinal() && ordinal < Directions.MOVE.ordinal()) {
                    final ResourceController resourceController = ResourceController.getResourceController();
                    resourceController.setProperty(MAP_OVERVIEW_ATTACH_POINT, dir.name());
                    Component c = e.getComponent();
                    setMapOverviewBounds(c.getBounds());
                }
            });
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
            Directions.getByCursorType(cursorType).ifPresent(dir -> {
                Point delta = getLimitedDelta(cursorType, parent.getBounds(), deltaX, deltaY);
                Rectangle bounds = dir.getBounds(startingBounds, delta);
                setMapOverviewBounds(bounds);
            });
        }

        private int getDeltaX(int dx) {
            int left = Math.min(MAP_OVERVIEW_MAX_SIZE - startingBounds.width, startingBounds.x);
            return Math.max(Math.min(dx, left), MAP_OVERVIEW_MIN_SIZE - startingBounds.width);
        }

        private int getDeltaX(int dx, Rectangle pr) {
            int right = Math.max(startingBounds.width - MAP_OVERVIEW_MAX_SIZE,
                    startingBounds.x + startingBounds.width - pr.width);
            return Math.min(Math.max(dx, right), startingBounds.width - MAP_OVERVIEW_MIN_SIZE);
        }

        private int getDeltaY(int dy) {
            int top = Math.min(MAP_OVERVIEW_MAX_SIZE - startingBounds.height, startingBounds.y);
            return Math.max(Math.min(dy, top), MAP_OVERVIEW_MIN_SIZE - startingBounds.height);
        }

        private int getDeltaY(int dy, Rectangle pr) {
            int bottom = Math.max(startingBounds.height - MAP_OVERVIEW_MAX_SIZE,
                    startingBounds.y + startingBounds.height - pr.height);
            return Math.min(Math.max(dy, bottom), startingBounds.height - MAP_OVERVIEW_MIN_SIZE);
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

    private enum Directions {
        NORTH(Cursor.N_RESIZE_CURSOR) {
            @Override
            Rectangle getBounds(Rectangle r, Point d) {
                return new Rectangle(r.x, r.y - d.y, r.width, r.height + d.y);
            }
        },
        SOUTH(Cursor.S_RESIZE_CURSOR) {
            @Override
            Rectangle getBounds(Rectangle r, Point d) {
                return new Rectangle(r.x, r.y, r.width, r.height - d.y);
            }
        },
        WEST(Cursor.W_RESIZE_CURSOR) {
            @Override
            Rectangle getBounds(Rectangle r, Point d) {
                return new Rectangle(r.x - d.x, r.y, r.width + d.x, r.height);
            }
        },
        EAST(Cursor.E_RESIZE_CURSOR) {
            @Override
            Rectangle getBounds(Rectangle r, Point d) {
                return new Rectangle(r.x, r.y, r.width - d.x, r.height);
            }
        },
        NORTH_WEST(Cursor.NW_RESIZE_CURSOR) {
            @Override
            Rectangle getBounds(Rectangle r, Point d) {
                return new Rectangle(r.x - d.x, r.y - d.y, r.width + d.x, r.height + d.y);
            }
        },
        NORTH_EAST(Cursor.NE_RESIZE_CURSOR) {
            @Override
            Rectangle getBounds(Rectangle r, Point d) {
                return new Rectangle(r.x, r.y - d.y, r.width - d.x, r.height + d.y);
            }
        },
        SOUTH_WEST(Cursor.SW_RESIZE_CURSOR) {
            @Override
            Rectangle getBounds(Rectangle r, Point d) {
                return new Rectangle(r.x - d.x, r.y, r.width + d.x, r.height - d.y);
            }
        },
        SOUTH_EAST(Cursor.SE_RESIZE_CURSOR) {
            @Override
            Rectangle getBounds(Rectangle r, Point d) {
                return new Rectangle(r.x, r.y, r.width - d.x, r.height - d.y);
            }
        },
        MOVE(Cursor.MOVE_CURSOR) {
            @Override
            Rectangle getBounds(Rectangle r, Point d) {
                return new Rectangle(r.x - d.x, r.y - d.y, r.width, r.height);
            }
        };

        private final int cursor;

        Directions(int cursor) {
            this.cursor = cursor;
        }

        abstract Rectangle getBounds(Rectangle rect, Point delta);

        public static Optional<Directions> getByCursorType(int cursor) {
            return EnumSet.allOf(Directions.class).stream().filter(d -> d.cursor == cursor).findFirst();
        }
    }

    private enum Locations {
        NORTH(Cursor.N_RESIZE_CURSOR) {
            @Override
            Point getPoint(Rectangle r) {
                return new Point(r.x + r.width / 2 - RESIZE_PANEL_BORDER_SIZE / 2, r.y);
            }
        },
        SOUTH(Cursor.S_RESIZE_CURSOR) {
            @Override
            Point getPoint(Rectangle r) {
                return new Point(r.x + r.width / 2 - RESIZE_PANEL_BORDER_SIZE / 2,
                        r.y + r.height - RESIZE_PANEL_BORDER_SIZE);
            }
        },
        WEST(Cursor.W_RESIZE_CURSOR) {
            @Override
            Point getPoint(Rectangle r) {
                return new Point(r.x, r.y + r.height / 2 - RESIZE_PANEL_BORDER_SIZE / 2);
            }
        },
        EAST(Cursor.E_RESIZE_CURSOR) {
            @Override
            public Point getPoint(Rectangle r) {
                return new Point(r.x + r.width - RESIZE_PANEL_BORDER_SIZE,
                        r.y + r.height / 2 - RESIZE_PANEL_BORDER_SIZE / 2);
            }
        },
        NORTH_WEST(Cursor.NW_RESIZE_CURSOR) {
            @Override
            Point getPoint(Rectangle r) {
                return new Point(r.x, r.y);
            }
        },
        NORTH_EAST(Cursor.NE_RESIZE_CURSOR) {
            @Override
            Point getPoint(Rectangle r) {
                return new Point(r.x + r.width - RESIZE_PANEL_BORDER_SIZE, r.y);
            }
        },
        SOUTH_WEST(Cursor.SW_RESIZE_CURSOR) {
            @Override
            Point getPoint(Rectangle r) {
                return new Point(r.x, r.y + r.height - RESIZE_PANEL_BORDER_SIZE);
            }
        },
        SOUTH_EAST(Cursor.SE_RESIZE_CURSOR) {
            @Override
            Point getPoint(Rectangle r) {
                return new Point(r.x + r.width - RESIZE_PANEL_BORDER_SIZE, r.y + r.height - RESIZE_PANEL_BORDER_SIZE);
            }
        };

        private final int cursor;

        Locations(int cursor) {
            this.cursor = cursor;
        }

        abstract Point getPoint(Rectangle r);

        public Cursor getCursor() {
            return Cursor.getPredefinedCursor(cursor);
        }
    }

    private interface ResizableBorder extends Border {
        Cursor getResizeCursor(MouseEvent e);
    }

    private class DefaultResizableBorder implements ResizableBorder, SwingConstants {
        private final Color BORDER_CORDER_SQUARE_COLOR = new Color(0xDF_BF_BF_BF, true);
        private final Color BORDER_CENTER_LINE_COLOR = new Color(0x7F_FF_FF_FF, true);

        @Override
        public Insets getBorderInsets(Component component) {
            return new Insets(RESIZE_PANEL_BORDER_SIZE, RESIZE_PANEL_BORDER_SIZE, RESIZE_PANEL_BORDER_SIZE,
                    RESIZE_PANEL_BORDER_SIZE);
        }

        @Override
        public boolean isBorderOpaque() {
            return false;
        }

        @Override
        public void paintBorder(Component component, Graphics g, int x, int y, int w, int h) {
            g.setColor(BORDER_CENTER_LINE_COLOR);
            g.drawRect(x + RESIZE_PANEL_BORDER_SIZE / 2, y + RESIZE_PANEL_BORDER_SIZE / 2, w - RESIZE_PANEL_BORDER_SIZE,
                    h - RESIZE_PANEL_BORDER_SIZE);
            Rectangle rect = new Rectangle(RESIZE_PANEL_BORDER_SIZE, RESIZE_PANEL_BORDER_SIZE);
            Rectangle r = new Rectangle(x, y, w, h);
            for (Locations loc : Locations.values()) {
                rect.setLocation(loc.getPoint(r));
                g.setColor(BORDER_CORDER_SQUARE_COLOR);
                g.fillRect(rect.x, rect.y, rect.width - 1, rect.height - 1);
                g.setColor(complementaryColor(BORDER_CORDER_SQUARE_COLOR));
                g.drawRect(rect.x, rect.y, rect.width - 1, rect.height - 1);
            }
        }

        @Override
        public Cursor getResizeCursor(MouseEvent e) {
            Component c = e.getComponent();
            int w = c.getWidth();
            int h = c.getHeight();
            Point pt = e.getPoint();

            Rectangle bounds = new Rectangle(w, h);
            if (! bounds.contains(pt)) {
                return Cursor.getDefaultCursor();
            }

            Rectangle actualBounds = new Rectangle(RESIZE_PANEL_BORDER_SIZE, RESIZE_PANEL_BORDER_SIZE,
                    w - 2 * RESIZE_PANEL_BORDER_SIZE, h - 2 * RESIZE_PANEL_BORDER_SIZE);
            if (actualBounds.contains(pt)) {
                return Cursor.getDefaultCursor();
            }
            Rectangle rect = new Rectangle(RESIZE_PANEL_BORDER_SIZE, RESIZE_PANEL_BORDER_SIZE);
            Rectangle r = new Rectangle(0, 0, w, h);
            for (Locations loc : Locations.values()) {
                rect.setLocation(loc.getPoint(r));
                if (rect.contains(pt)) {
                    return loc.getCursor();
                }
            }
            return Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR);
        }
    }

    private class JResizablePanel extends JPanel {
        private static final long serialVersionUID = 1L;

        protected JResizablePanel(LayoutManager layout) {
            super(layout);
        }

        @Override
        public void updateUI() {
            super.updateUI();
            MouseInputListener resizeListener = new ResizeMouseListener();
            addMouseListener(resizeListener);
            addMouseMotionListener(resizeListener);
            setBorder(new DefaultResizableBorder());
        }
    }

    private enum MapOverviewAttachPoint {
        SOUTH_EAST, SOUTH_WEST, NORTH_EAST, NORTH_WEST
    }

    private final JMapOverviewImage mapOverviewImage;

    private final JScrollPane mapViewScrollPane;
    private final JPanel mapOverviewPanel;
    private final MapView mapView;
    private boolean isMapOverviewVisible;

    public MapViewPane(JScrollPane mapViewScrollPane) {
        this.mapViewScrollPane = mapViewScrollPane;
        this.mapView = (MapView) mapViewScrollPane.getViewport().getView();
        setLayout(new BorderLayout(0, 0) {
            private static final long serialVersionUID = 3702408082745761647L;

            @Override
            public void layoutContainer(Container parent) {
                synchronized (parent.getTreeLock()) {
                    Insets insets = parent.getInsets();
                    int width = parent.getWidth();
                    int height = parent.getHeight();
                    int top = insets.top;
                    int bottom = height - insets.bottom;
                    int left = insets.left;
                    int right = width - insets.right;
                    mapViewScrollPane.setBounds(left, top, right - left, bottom - top);
                    mapOverviewPanel.setBounds(getMapOverviewBounds());
                }
            }
        });
        mapOverviewImage = new JMapOverviewImage();
        mapOverviewPanel = new JResizablePanel(new BorderLayout(0, 0));
        mapOverviewPanel.add(mapOverviewImage);
        final ViewController viewController = Controller.getCurrentController().getViewController();
        isMapOverviewVisible = viewController.isMapOverviewVisible();
        mapOverviewPanel.setVisible(isMapOverviewVisible);
        add(mapOverviewPanel, BorderLayout.EAST);
        add(mapViewScrollPane);

        mapView.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                updateMapOverview();
            };
        });
    }

    private MapOverviewAttachPoint getMapOverviewAttachPoint() {
        ResourceController resourceController = ResourceController.getResourceController();
        String rawAttachPoint = resourceController.getProperty(MAP_OVERVIEW_ATTACH_POINT,
                MapOverviewAttachPoint.SOUTH_EAST.name());
        try {
            return MapOverviewAttachPoint.valueOf(rawAttachPoint);
        } catch (IllegalArgumentException e) {
            return MapOverviewAttachPoint.SOUTH_EAST;
        }
    }

    private int getBoundedValue(int value, int min, int max) {
        return Math.max(Math.min(value, max), min);
    }

    private void convertOriginByAttachPoint(Rectangle bounds) {
        Insets insets = getInsets();
        int bottom = getHeight() - insets.bottom;
        int right = getWidth() - insets.right;
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

    private Rectangle getMapOverviewBounds() {
        ResourceController resourceController = ResourceController.getResourceController();
        String rawBoundsValue = resourceController.getProperty(MAP_OVERVIEW_BOUNDS);
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
            elements = new int[] { 0, 0, MAP_OVERVIEW_DEFAULT_SIZE, MAP_OVERVIEW_DEFAULT_SIZE };
        }
        int x = elements[0];
        int y = elements[1];
        int width = getBoundedValue(elements[2], MAP_OVERVIEW_MIN_SIZE, MAP_OVERVIEW_MAX_SIZE);
        int height = getBoundedValue(elements[3], MAP_OVERVIEW_MIN_SIZE, MAP_OVERVIEW_MAX_SIZE);
        Rectangle bounds = new Rectangle(x, y, width, height);
        convertOriginByAttachPoint(bounds);
        return bounds;
    }

    private void setMapOverviewBounds(Rectangle bounds) {
        convertOriginByAttachPoint(bounds);
        ResourceController.getResourceController().setProperty(MAP_OVERVIEW_BOUNDS,
                String.format("%s,%s,%s,%s", bounds.x, bounds.y, bounds.width, bounds.height));
    }

    private double getScale(Rectangle source, double scaledOverviewBoundWidth, double scaledOverviewBoundHeight) {
        double innerBoundsWidth = (double) source.width;
        double innerBoundsHeight = (double) source.height;
        double scaleX = scaledOverviewBoundWidth / innerBoundsWidth;
        double scaleY = scaledOverviewBoundHeight / innerBoundsHeight;
        return ((innerBoundsWidth / innerBoundsHeight) > (scaledOverviewBoundWidth / scaledOverviewBoundHeight))
                ? scaleX
                : scaleY;
    }

    @Override
    public void mapChanged(MapChangeEvent event) {
        if (event.getMap() != mapView.getModel()) {
            return;
        }
        updateMapOverview();
    }

    private void updateMapOverview() {
        if (isMapOverviewVisible) {
        	mapOverviewImage.resetImage();
        	SwingUtilities.invokeLater(mapOverviewPanel::repaint);
        }

    }

    private Color complementaryColor(final Color bgColor) {
        return new Color(255 - bgColor.getRed(), 255 - bgColor.getGreen(), 255 - bgColor.getBlue());
    }


    @Override
    public boolean isOptimizedDrawingEnabled() {
        return false; // enable overlap
    }

    @Override
    public void addNotify() {
        super.addNotify();
        Controller.getCurrentModeController().getMapController().addMapChangeListener(this);
        ResourceController.getResourceController().addPropertyChangeListener(this);
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        Controller.getCurrentModeController().getMapController().removeMapChangeListener(this);
        ResourceController.getResourceController().removePropertyChangeListener(this);
    }

    @Override
    public void propertyChanged(String propertyName, String newValue, String oldValue) {
        if (ViewController.FULLSCREEN_ENABLED_PROPERTY.equals(propertyName) || MAP_OVERVIEW_VISIBLE.equals(propertyName)
                || MAP_OVERVIEW_VISIBLE_FS.equals(propertyName)) {
            final ViewController viewController = Controller.getCurrentController().getViewController();
            if (isMapOverviewVisible != viewController.isMapOverviewVisible()) {
                isMapOverviewVisible = ! isMapOverviewVisible;
                mapOverviewPanel.setVisible(isMapOverviewVisible);
                updateMapOverview();
            }
        } else if (MAP_OVERVIEW_BOUNDS.equals(propertyName)) {
            mapOverviewPanel.revalidate();
            mapOverviewPanel.repaint();
        }
    }
}
