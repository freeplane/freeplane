package org.freeplane.view.swing.map;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import javax.swing.BorderFactory;
import javax.swing.BoundedRangeModel;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputAdapter;

import org.freeplane.api.LengthUnit;
import org.freeplane.core.resources.IFreeplanePropertyListener;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.features.map.IMapChangeListener;
import org.freeplane.features.map.MapChangeEvent;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.styles.MapStyle;
import org.freeplane.features.ui.FrameController;
import org.freeplane.features.ui.IMapViewManager;
import org.freeplane.features.ui.ViewController;

public class MapOverview extends JPanel implements IFreeplanePropertyListener, IMapChangeListener {
    private static final long serialVersionUID = 8664710783654626093L;

    private static final Color THUMB_COLOR_BLUE = new Color(0x32_00_00_FF, true);
    private static final Color THUMB_COLOR_RED = new Color(0x92_FF_00_00, true);

    private static Cursor MAG_CURSOR;

    static {
        ImageIcon imageIcon = (ImageIcon) ResourceController.getResourceController()
                .getImageIcon("map_overview_cursor");
        if (imageIcon != null && imageIcon.getImage() != null) {
            MAG_CURSOR = Toolkit.getDefaultToolkit().createCustomCursor(imageIcon.getImage(), new Point(6, 6),
                    "Map Overview Cursor");
        }

    }

    private class MapOverviewMouseHandler extends MouseInputAdapter {
        private Cursor oldCursor;

        @Override
        public void mousePressed(MouseEvent e) {
            processMousePanEvent(e);
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            if (MAG_CURSOR == null) {
                return;
            }
            oldCursor = getCursor();
            setCursor(MAG_CURSOR);
        }

        @Override
        public void mouseExited(MouseEvent e) {
            if (oldCursor != null) {
                setCursor(oldCursor);
            }
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
            double scale = (double) mapOverviewSize / Math.max(innerBounds.width, innerBounds.height);
            Point pt = e.getPoint();
            setHorizontalRange(pt.x, scale, innerBounds);
            setVerticalRange(pt.y, scale, innerBounds);
        }

        private final void setVerticalRange(int y, double scale, Rectangle innerBounds) {
            if (y < 0 || y > mapOverviewSize) {
                return;
            }
            BoundedRangeModel m = mapViewScrollPane.getVerticalScrollBar().getModel();
            int value = (int) (y / scale + innerBounds.y) - m.getExtent() / 2
                    - (innerBounds.width > innerBounds.height ? (innerBounds.width - innerBounds.height) / 2 : 0);
            m.setValue(Math.max(value, 0));
        }

        private final void setHorizontalRange(int x, double scale, Rectangle innerBounds) {
            if (x < 0 || x > mapOverviewSize) {
                return;
            }
            BoundedRangeModel m = mapViewScrollPane.getHorizontalScrollBar().getModel();
            int value = (int) (x / scale + innerBounds.x) - m.getExtent() / 2
                    + (innerBounds.width < innerBounds.height ? (innerBounds.width - innerBounds.height) / 2 : 0);
            m.setValue(Math.max(value, 0));
        }
    };

    private JLabel mapOverviewLabel = new JLabel() {
        private static final long serialVersionUID = -3555087194678925L;
        private transient MouseInputAdapter handler;

        @Override
        public void updateUI() {
            removeMouseListener(handler);
            removeMouseMotionListener(handler);
            removeMouseWheelListener(handler);
            super.updateUI();
            handler = new MapOverviewMouseHandler();
            addMouseListener(handler);
            addMouseMotionListener(handler);
            addMouseWheelListener(handler);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            JViewport viewPort = (JViewport) mapView.getParent();
            Point viewPortPosition = viewPort.getViewPosition();
            Rectangle innerBounds = mapView.getInnerBounds();
            Rectangle viewPortRect = viewPort.getBounds();
            Rectangle thumbRect = new Rectangle(viewPortRect);
            thumbRect.x = viewPortPosition.x - innerBounds.x;
            thumbRect.y = viewPortPosition.y - innerBounds.y;
            int extend = Math.abs(innerBounds.width - innerBounds.height) / 2;
            if (innerBounds.width > innerBounds.height) {
                thumbRect.y += extend;
            } else {
                thumbRect.x += extend;
            }
            double scale = (double) mapOverviewSize / Math.max(innerBounds.getWidth(), innerBounds.getHeight());
            AffineTransform transformer = AffineTransform.getScaleInstance(scale, scale);
            Rectangle scaledThumbRect = transformer.createTransformedShape(thumbRect).getBounds();
            Graphics2D g2d = (Graphics2D) g.create();
            try {
                Color color = scaledThumbRect.width * scaledThumbRect.height < 400 ? THUMB_COLOR_RED : THUMB_COLOR_BLUE;
                g2d.setColor(color);
                g2d.fillRect(scaledThumbRect.x, scaledThumbRect.y, scaledThumbRect.width, scaledThumbRect.height);
                g2d.setColor(color.darker());
                g2d.drawRect(scaledThumbRect.x, scaledThumbRect.y, scaledThumbRect.width - 1,
                        scaledThumbRect.height - 1);

                IMapViewManager mapViewManager = (MapViewController) Controller.getCurrentController()
                        .getMapViewManager();
                if (mapViewManager instanceof MapViewController && isFullScreenEnabled()) {
                    String zoom = ((MapViewController) mapViewManager).getItemForZoom(mapView.getZoom());
                    Color zoomColor = complementaryColor(mapView.getBackground());
                    zoomColor = new Color(zoomColor.getRed(), zoomColor.getGreen(), zoomColor.getBlue(), 0x7F);
                    g2d.setColor(zoomColor);
                    g2d.drawChars(zoom.toCharArray(), 0, zoom.length(), 5, mapOverviewSize - 5);
                }

            } finally {
                g2d.dispose();
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
    };

    private JScrollPane mapViewScrollPane;
    private JPanel mapOverviewPanel;
    private MapView mapView;
    private int mapOverviewSize;
    private boolean isMapOverviewVisible;

    public MapOverview(JScrollPane mapViewScrollPane) {
        this.mapViewScrollPane = mapViewScrollPane;
        this.mapView = (MapView) mapViewScrollPane.getViewport().getView();
        this.mapOverviewSize = (int) ResourceController.getResourceController()
                .getLengthQuantityProperty("map_overview_size").in(LengthUnit.px).value;
        this.mapOverviewLabel.setIcon(
                new ImageIcon(new BufferedImage(mapOverviewSize, mapOverviewSize, BufferedImage.TYPE_INT_ARGB)));
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
                    Component cc = getLayoutComponent(parent, BorderLayout.CENTER);
                    if (cc != null) {
                        cc.setBounds(left, top, right - left, bottom - top);
                    }
                    Component ec = getLayoutComponent(parent, BorderLayout.EAST);
                    if (ec != null) {
                        Dimension d = ec.getPreferredSize();
                        JScrollBar hsb = mapViewScrollPane.getHorizontalScrollBar();
                        JScrollBar vsb = mapViewScrollPane.getVerticalScrollBar();
                        int vsw = vsb.isVisible() ? vsb.getSize().width : 0;
                        int hsw = hsb.isVisible() ? hsb.getSize().height : 0;
                        ec.setBounds(right - d.width - vsw - 1, height - d.height - hsw - 1, d.width, d.height);
                    }
                }
            }
        });
        mapOverviewPanel = new JPanel(new BorderLayout(0, 0));
        mapOverviewPanel.add(mapOverviewLabel);
        mapOverviewPanel.setBorder(BorderFactory.createLineBorder(complementaryColor(mapView.getBackground())));
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

    @Override
    public void mapChanged(MapChangeEvent event) {
        if (event.getMap() != mapView.getModel()) {
            return;
        }
        if (event.getProperty() == MapStyle.RESOURCES_BACKGROUND_COLOR) {
            mapOverviewPanel.setBorder(BorderFactory.createLineBorder(complementaryColor((Color) event.getNewValue())));
        }
        updateMapOverview();
    }

    private void updateMapOverview() {
        if (!isMapOverviewVisible) {
            return;
        }
        SwingUtilities.invokeLater(() -> {
            snapshot();
            mapOverviewLabel.repaint();
        });
    }

    private Color complementaryColor(final Color bgColor) {
        return new Color(255 - bgColor.getRed(), 255 - bgColor.getGreen(), 255 - bgColor.getBlue());
    }

    private void snapshot() {
        Rectangle innerBounds = mapView.getInnerBounds();
        int x = innerBounds.x;
        int y = innerBounds.y;
        int width = innerBounds.width;
        int height = innerBounds.height;
        int maxSize = Math.max(width, height);
        double scale = (double) mapOverviewSize / maxSize;

        AffineTransform translation = AffineTransform.getTranslateInstance((maxSize - width) / 2d - x,
                (maxSize - height) / 2d - y);
        AffineTransform transformer = AffineTransform.getScaleInstance(scale, scale);
        transformer.concatenate(translation);

        ImageIcon icon = (ImageIcon) mapOverviewLabel.getIcon();
        BufferedImage image = (BufferedImage) icon.getImage();
        Graphics2D imageG2D = image.createGraphics();
        try {
            imageG2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            imageG2D.setColor(mapView.getBackground());
            imageG2D.fillRect(0, 0, mapOverviewSize, mapOverviewSize);
            imageG2D.transform(transformer);
            imageG2D.clip(innerBounds);
            mapView.paint(imageG2D);
        } finally {
            imageG2D.dispose();
        }
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
        if (ViewController.FULLSCREEN_ENABLED_PROPERTY.equals(propertyName)
                || "mapOverviewVisible".equals(propertyName) 
                || "mapOverviewVisible.fullscreen".equals(propertyName)) {
            final ViewController viewController = Controller.getCurrentController().getViewController();
            if (isMapOverviewVisible != viewController.isMapOverviewVisible()) {
                isMapOverviewVisible = ! isMapOverviewVisible;
                mapOverviewPanel.setVisible(isMapOverviewVisible);
                updateMapOverview();
            }
        }
    }
}
