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

import org.freeplane.core.resources.IFreeplanePropertyListener;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.ui.ViewController;

public class MiniMapView extends JPanel implements IFreeplanePropertyListener {
    private static final long serialVersionUID = 8664710783654626093L;

    private static final Color THUMB_COLOR = new Color(0x32_00_00_FF, true);
    private static final double VIEWPORT_SCALE = .15d;

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
            Controller.getCurrentController().getMapViewManager().setZoom(zoom);
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            processMiniMapMouseEvent(e);
        }

        protected final void processMiniMapMouseEvent(MouseEvent e) {
            setHorizontalRange(e);
            setVerticalRange(e);
        }

        private final void setVerticalRange(MouseEvent e) {
            Point pt = e.getPoint();
            Component c = e.getComponent();
            BoundedRangeModel m = mapViewScrollPane.getVerticalScrollBar().getModel();
            int iv = Math.round(pt.y * (m.getMaximum() - m.getMinimum()) / (float) c.getHeight() - m.getExtent() / 2f);
            m.setValue(iv);
        }

        private final void setHorizontalRange(MouseEvent e) {
            Point pt = e.getPoint();
            Component c = e.getComponent();
            BoundedRangeModel m = mapViewScrollPane.getHorizontalScrollBar().getModel();
            int iv = Math.round(pt.x * (m.getMaximum() - m.getMinimum()) / (float) c.getWidth() - m.getExtent() / 2f);
            m.setValue(iv);
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
            Container c = SwingUtilities.getAncestorOfClass(JViewport.class, mapView);
            if (!(c instanceof JViewport)) {
                return;
            }
            JViewport vport = (JViewport) c;
            Rectangle viewPortRect = vport.getBounds();
            Rectangle mapViewRect = mapView.getBounds();
            Rectangle crect = SwingUtilities.calculateInnerArea(this, null);

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            double sx = crect.getWidth() / mapViewRect.getWidth();
            double sy = crect.getHeight() / mapViewRect.getHeight();
            AffineTransform at = AffineTransform.getScaleInstance(sx, sy);

            Rectangle thumbRect = new Rectangle(viewPortRect);
            thumbRect.x = vport.getViewPosition().x;
            thumbRect.y = vport.getViewPosition().y;
            Rectangle r = at.createTransformedShape(thumbRect).getBounds();
            int x = crect.x + r.x;
            int y = crect.y + r.y;

            g2.setColor(THUMB_COLOR);
            g2.fillRect(x, y, r.width, r.height);
            g2.setColor(THUMB_COLOR.darker());
            g2.drawRect(x, y, r.width - 1, r.height - 1);
            g2.dispose();
        }
    };

    private JScrollPane mapViewScrollPane;
    private JPanel miniMapPanel;
    private MapView mapView;

    public MiniMapView(JScrollPane mapViewScrollPane) {
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

    private double computeProperScale() {
        Dimension d = mapView.getSize();
        float zoom = mapView.getZoom();
        if (zoom <= 1) {
            return 1.0d;
        }
        double scale = 1.0d / zoom;
        int newImageW = (int) Math.round(d.width * scale);
        int newImageH = (int) Math.round(d.height * scale);
        long size = newImageW * newImageH;
        if (size > 2500 * 2500) {
            scale *= (2500 * 2500d / size);
        }
        return scale;
    }

    private Icon makeMiniMap() {
        Dimension d = mapView.getSize();
        double scale = computeProperScale();
        int newImageW = (int) Math.round(d.width * scale);
        int newImageH = (int) Math.round(d.height * scale);
        BufferedImage image = new BufferedImage(newImageW, newImageH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D imageG2D = image.createGraphics();
        imageG2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        imageG2D.scale(scale, scale);
        mapView.paint(imageG2D);
        imageG2D.dispose();

        Rectangle mvInnerRect = mapView.getInnerBounds();
        AffineTransform transformer = AffineTransform.getScaleInstance(scale, scale);
        Rectangle scaled = transformer.createTransformedShape(mvInnerRect).getBounds();

        Dimension vpd = mapViewScrollPane.getSize();
        int scaledViewPortWidth = Math.max((int) Math.round(vpd.width * VIEWPORT_SCALE), 1);
        int scaledViewPortHeight = Math.max((int) Math.round(vpd.height * VIEWPORT_SCALE), 1);
        return new ImageIcon(image.getSubimage(scaled.x, scaled.y, scaled.width, scaled.height)
                .getScaledInstance(scaledViewPortWidth, scaledViewPortHeight, Image.SCALE_FAST));
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
