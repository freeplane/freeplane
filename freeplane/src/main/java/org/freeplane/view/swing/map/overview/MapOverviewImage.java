package org.freeplane.view.swing.map.overview;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import javax.swing.JComponent;
import javax.swing.JViewport;

import org.freeplane.view.swing.map.MapView;

public class MapOverviewImage extends JComponent {
    private static final long serialVersionUID = 1L;

    private BufferedImage image;
    private MapView mapView;

    public MapOverviewImage(MapView mapView) {
        this.mapView = mapView;
        MapOverviewImageMouseHandler handler = new MapOverviewImageMouseHandler(mapView);
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
        double scale = MapOverviewUtils.getBestScale(mapView.getInnerBounds(), scaledOverviewBoundWidth,
                scaledOverviewBoundHeight);

        double extendedWidth = (scaledOverviewBoundWidth / scale - width) / 2d;
        double extendedHeight = (scaledOverviewBoundHeight / scale - height) / 2d;

        AffineTransform translation = AffineTransform.getTranslateInstance(extendedWidth - x, extendedHeight - y);
        AffineTransform transformer = AffineTransform.getScaleInstance(scale, scale);
        transformer.concatenate(translation);

        BufferedImage image = new BufferedImage(mapOverviewBounds.width, mapOverviewBounds.height,
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D imageG2D = image.createGraphics();
        try {
            imageG2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
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
        final Graphics2D g2 = (Graphics2D) g;
        final AffineTransform transform = g2.getTransform();
        final double scaleX = transform.getScaleX();
        Rectangle mapOverviewBounds = getBounds(); // getMapOverviewBounds();
        if (image == null || image.getWidth() != (int) (mapOverviewBounds.width * scaleX)) {
            image = snapshot(g2, mapOverviewBounds);
        }
        if (scaleX == 1) {
            g2.drawImage(image, 0, 0, this);
        } else {
            AffineTransform newTransform = AffineTransform.getTranslateInstance(transform.getTranslateX(),
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
        double scale = MapOverviewUtils.getBestScale(mapView.getInnerBounds(), scaledOverviewBoundWidth,
                scaledOverviewBoundHeight);

        double extendedWidth = (scaledOverviewBoundWidth / scale - innerBounds.width) / 2d;
        double extendedHeight = (scaledOverviewBoundHeight / scale - innerBounds.height) / 2d;
        thumbRect.x += extendedWidth;
        thumbRect.y += extendedHeight;

        AffineTransform transformer = AffineTransform.getScaleInstance(scale, scale);
        Rectangle scaledThumbRect = transformer.createTransformedShape(thumbRect).getBounds();
        g2.setColor(MapOverviewConstants.VIEWPORT_THUMBNAIL_COLOR);
        g2.fillRect(scaledThumbRect.x, scaledThumbRect.y, scaledThumbRect.width, scaledThumbRect.height);
        g2.setColor(MapOverviewConstants.VIEWPORT_THUMBNAIL_COLOR.darker());
        g2.drawRect(scaledThumbRect.x, scaledThumbRect.y, scaledThumbRect.width - 1, scaledThumbRect.height - 1);
    }

    public void resetImage() {
        image = null;
    }

}
