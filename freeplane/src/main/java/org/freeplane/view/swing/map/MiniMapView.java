package org.freeplane.view.swing.map;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import javax.swing.BorderFactory;
import javax.swing.BoundedRangeModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputAdapter;

import org.freeplane.api.LengthUnit;
import org.freeplane.core.resources.IFreeplanePropertyListener;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.ui.ViewController;

public class MiniMapView extends JPanel implements IFreeplanePropertyListener {
    private static final long serialVersionUID = 8664710783654626093L;

    private static final Color THUMB_COLOR_BLUE = new Color(0x32_00_00_FF, true);
    private static final Color THUMB_COLOR_RED = new Color(0x92_FF_00_00, true);

    private int miniMapSize;

    private class MiniMapMouseHandler extends MouseInputAdapter {
        @Override
        public void mousePressed(MouseEvent e) {
            processMiniMapMouseEvent(e);
        }

        @Override
        public void mouseWheelMoved(MouseWheelEvent e) {
            double oldZoom = mapView.getZoom();
            double amount = Math.pow(1.1, e.getScrollAmount());
            float zoom = (float) (e.getWheelRotation() > 0 ? (oldZoom / amount) : (oldZoom * amount));
            if (zoom > 32 || zoom <= 0.03f) {
                return;
            }
            Controller.getCurrentController().getMapViewManager().setZoom(zoom);
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            processMiniMapMouseEvent(e);
        }

        protected final void processMiniMapMouseEvent(MouseEvent e) {
            Rectangle innerBounds = mapView.getInnerBounds();
            double scale = (double) miniMapSize / Math.max(innerBounds.width, innerBounds.height);
            Point pt = e.getPoint();
            setHorizontalRange(pt.x, scale, innerBounds);
            setVerticalRange(pt.y, scale, innerBounds);
        }

        private final void setVerticalRange(int y, double scale, Rectangle innerBounds) {
            if (y < 0 || y > miniMapSize) {
                return;
            }
            BoundedRangeModel m = mapViewScrollPane.getVerticalScrollBar().getModel();
            int value = (int) (y / scale + innerBounds.y) - m.getExtent() / 2
                    - (innerBounds.width > innerBounds.height ? (innerBounds.width - innerBounds.height) / 2 : 0);
            m.setValue(Math.max(value, 0));
        }

        private final void setHorizontalRange(int x, double scale, Rectangle innerBounds) {
            if (x < 0 || x > miniMapSize) {
                return;
            }
            BoundedRangeModel m = mapViewScrollPane.getHorizontalScrollBar().getModel();
            int value = (int) (x / scale + innerBounds.x) - m.getExtent() / 2
                    + (innerBounds.width < innerBounds.height ? (innerBounds.width - innerBounds.height) / 2 : 0);
            m.setValue(Math.max(value, 0));
        }
    };

    private JLabel miniMap = new JLabel() {
        private static final long serialVersionUID = -3555087194678925L;
        private transient MouseInputAdapter handler;

        @Override
        public void updateUI() {
            removeMouseListener(handler);
            removeMouseMotionListener(handler);
            removeMouseWheelListener(handler);
            super.updateUI();
            handler = new MiniMapMouseHandler();
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
            double scale = (double) miniMapSize / Math.max(innerBounds.getWidth(), innerBounds.getHeight());
            AffineTransform transformer = AffineTransform.getScaleInstance(scale, scale);
            Rectangle scaledThumbRect = transformer.createTransformedShape(thumbRect).getBounds();
            Graphics2D g2d = (Graphics2D) g.create();
            Color color = scaledThumbRect.width * scaledThumbRect.height < 400 ? THUMB_COLOR_RED : THUMB_COLOR_BLUE;
            g2d.setColor(color);
            g2d.fillRect(scaledThumbRect.x, scaledThumbRect.y, scaledThumbRect.width, scaledThumbRect.height);
            g2d.setColor(color.darker());
            g2d.drawRect(scaledThumbRect.x, scaledThumbRect.y, scaledThumbRect.width - 1, scaledThumbRect.height - 1);
            g2d.dispose();
        }
    };

    private JScrollPane mapViewScrollPane;
    private JPanel miniMapPanel;
    private MapView mapView;

    public MiniMapView(JScrollPane mapViewScrollPane) {
        this.mapViewScrollPane = mapViewScrollPane;
        this.mapView = (MapView) mapViewScrollPane.getViewport().getView();
        this.miniMapSize = (int) ResourceController.getResourceController().getLengthQuantityProperty("minimap_size")
                .in(LengthUnit.px).value;
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
                        ec.setBounds(right - d.width - vsw, height - d.height - hsw, d.width, d.height);
                    }
                }
            }
        });
        mapViewScrollPane.getVerticalScrollBar().getModel().addChangeListener(e -> miniMap.repaint());
        miniMapPanel = new JPanel(new BorderLayout(0, 0));
        miniMapPanel.add(miniMap);
        miniMapPanel.setBorder(BorderFactory.createLineBorder(complementaryColor(mapView.getBackground())));
        final ViewController viewController = Controller.getCurrentController().getViewController();
        miniMapPanel.setVisible(viewController.isMinimapVisible());
        add(miniMapPanel, BorderLayout.EAST);
        add(mapViewScrollPane);

        mapView.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                SwingUtilities.invokeLater(() -> {
                    miniMap.setIcon(makeMiniMap());
                });
            };
        });
    }

    private Color complementaryColor(final Color bgColor) {
        return new Color(255 - bgColor.getRed(), 255 - bgColor.getGreen(), 255 - bgColor.getBlue());
    }

    // scale to restrict image memory
    private final long MAX_IMAGE_PIXELS = 16 * 1024 * 1024;

    private double computeMemGuardScale() {
        Dimension viewSize = mapView.getSize();
        long size = (long) viewSize.width * viewSize.height;
        return size > MAX_IMAGE_PIXELS ? ((double) MAX_IMAGE_PIXELS / size) : 1.0d;
    }

    private Icon makeMiniMap() {
        Dimension viewSize = mapView.getSize();
        double memGuardScale = computeMemGuardScale();
        int newImageW = (int) (viewSize.width * memGuardScale);
        int newImageH = (int) (viewSize.height * memGuardScale);

        // make a square image
        int newImageSize = Math.max(newImageW, newImageH);
        BufferedImage image = new BufferedImage(newImageSize, newImageSize, BufferedImage.TYPE_INT_ARGB);
        Graphics2D imageG2D = image.createGraphics();
        imageG2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        imageG2D.setColor(mapView.getBackground());
        imageG2D.fillRect(0, 0, newImageSize, newImageSize);

        AffineTransform transformer = AffineTransform.getScaleInstance(memGuardScale, memGuardScale);
        AffineTransform translation = newImageW > newImageH
                ? AffineTransform.getTranslateInstance(0d, (viewSize.width - viewSize.height) / 2d)
                : AffineTransform.getTranslateInstance((viewSize.height - viewSize.width) / 2d, 0d);
        transformer.concatenate(translation);
        imageG2D.transform(transformer);
        mapView.paint(imageG2D);
        imageG2D.dispose();

        // crop the desired square
        Rectangle transformedBounds = transformer.createTransformedShape(mapView.getInnerBounds()).getBounds();
        int scaledImageSize = Math.max(1, Math.max(transformedBounds.width, transformedBounds.height));
        // This assumes the MapView always centers its
        // contents, i.e innerBounds is centered.
        int extend = Math.abs(transformedBounds.width - transformedBounds.height) / 2;
        if (transformedBounds.width > transformedBounds.height) {
            transformedBounds.y -= extend;
        } else {
            transformedBounds.x -= extend;
        }

        return new ImageIcon(
                image.getSubimage(transformedBounds.x, transformedBounds.y, scaledImageSize, scaledImageSize)
                        .getScaledInstance(miniMapSize, miniMapSize, Image.SCALE_SMOOTH));
    }

    @Override
    public boolean isOptimizedDrawingEnabled() {
        return false; // enable overlap
    }

    @Override
    public void addNotify() {
        super.addNotify();
        ResourceController.getResourceController().addPropertyChangeListener(this);
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        ResourceController.getResourceController().removePropertyChangeListener(this);
    }

    @Override
    public void propertyChanged(String propertyName, String newValue, String oldValue) {
        if (!"minimapVisible".equals(propertyName)) {
            return;
        }
        final ViewController viewController = Controller.getCurrentController().getViewController();
        boolean minimapVisible = viewController.isMinimapVisible();
        miniMapPanel.setVisible(minimapVisible);
    }
}
