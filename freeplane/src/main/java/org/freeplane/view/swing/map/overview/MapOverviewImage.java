package org.freeplane.view.swing.map.overview;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import javax.swing.JComponent;
import javax.swing.JViewport;

import org.freeplane.core.util.Compat;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.ui.IMapViewManager;
import org.freeplane.view.swing.map.MapView;
import org.freeplane.view.swing.map.MapViewController;

class MapOverviewImage extends JComponent {
    private static final long serialVersionUID = 1L;

    static final Color VIEWPORT_THUMBNAIL_COLOR = new Color(0x32_00_00_FF, true);

    private BufferedImage image;
    private MapView mapView;

    MapOverviewImage(MapView mapView) {
        this.mapView = mapView;
        MapOverviewImageMouseHandler handler = new MapOverviewImageMouseHandler(mapView);
        addMouseListener(handler);
        addMouseMotionListener(handler);
        addMouseWheelListener(handler);
    }

    void resetImage() {
        image = null;
    }

    double getBestScale(Dimension source, Dimension target, Dimension extentedSize) {
        double sourceWidth = source.getWidth();
        double sourceHeight = source.getHeight();
        double targetWidth = target.getWidth();
        double targetHeight = target.getHeight();
        double scaleX = targetWidth / sourceWidth;
        double scaleY = targetHeight / sourceHeight;
        double scale = ((sourceWidth / sourceHeight) > (targetWidth / targetHeight)) ? scaleX : scaleY;
        if (extentedSize != null) {
            double extendedWidth = (targetWidth / scale - sourceWidth) / 2d;
            double extendedHeight = (targetHeight / scale - sourceHeight) / 2d;
            extentedSize.setSize(extendedWidth, extendedHeight);
        }
        return scale;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Rectangle overviewBounds = getBounds();
        Rectangle mapInnerBounds = mapView.getInnerBounds();
        Dimension extentedSize = new Dimension();
        Graphics2D g2d = (Graphics2D) g;
        final AffineTransform transform = g2d.getTransform();
        double scaleX = transform.getScaleX();
        AffineTransform overviewTransform = AffineTransform.getScaleInstance(transform.getScaleX(),
                transform.getScaleY());
        overviewBounds = overviewTransform.createTransformedShape(overviewBounds).getBounds();
        double scale = getBestScale(mapInnerBounds.getSize(), overviewBounds.getSize(), extentedSize);
        if (image == null || image.getWidth() != (int) overviewBounds.width * scaleX) {
            image = snapshot(mapInnerBounds, overviewBounds, scale, extentedSize);
        }
        if (scaleX == 1) {
            g2d.drawImage(image, 0, 0, this);
        } else {
            AffineTransform newTransform = AffineTransform.getTranslateInstance(transform.getTranslateX(),
                    transform.getTranslateY());
            g2d.setTransform(newTransform);
            g2d.drawImage(image, 0, 0, this);
            g2d.setTransform(transform);
        }

        drawViewportThumbnail(g2d, mapInnerBounds, scale, extentedSize);
        drawAnchorAndZoomLevel(g2d);
    }

    private void drawAnchorAndZoomLevel(Graphics2D g2d) {
        Dimension imageSize = getSize();

        String anchorSymbol = Compat.isMacOsX() ? "+" : "\u2693"; // "⚓"
        Point anchorLocation = new Point();
        Point zoomLevelLocation = new Point(4, imageSize.height - 8);
        final MapViewPane mapViewPane = (MapViewPane) getParent().getParent();
        MapOverviewAttachPoint anchorPoint = mapViewPane.getMapOverviewAttachPoint();
        switch (anchorPoint) {
        case SOUTH_EAST:
            anchorLocation.setLocation(imageSize.width - 16, imageSize.height - 8);
            break;
        case SOUTH_WEST:
            anchorLocation.setLocation(4, imageSize.height - 8);
            break;
        case NORTH_EAST:
            anchorLocation.setLocation(imageSize.width - 16, 16);
            break;
        case NORTH_WEST:
            anchorLocation.setLocation(4, 16);
            break;
        }
        String zoomLevelString = null;
        IMapViewManager mapViewManager = Controller.getCurrentController().getMapViewManager();
        if (mapViewManager instanceof MapViewController) {
            zoomLevelString = ((MapViewController) mapViewManager).getItemForZoom(mapView.getZoom());
        }

        Color invBgColor = complementaryColor(mapView.getBackground());
        Color textColor = new Color(invBgColor.getRed(), invBgColor.getGreen(), invBgColor.getBlue(), 0x4F);
        g2d.setColor(textColor);
        if (anchorPoint == MapOverviewAttachPoint.SOUTH_WEST && zoomLevelString != null) {
            anchorSymbol += " " + zoomLevelString;
        } else if (zoomLevelString != null) {
            g2d.drawChars(zoomLevelString.toCharArray(), 0, zoomLevelString.length(), zoomLevelLocation.x,
                    zoomLevelLocation.y);
        }
        g2d.drawChars(anchorSymbol.toCharArray(), 0, anchorSymbol.length(), anchorLocation.x, anchorLocation.y);
    }

    private Color complementaryColor(final Color color) {
        return new Color(0xFF - color.getRed(), 0xFF - color.getGreen(), 0xFF - color.getBlue());
    }

    private BufferedImage snapshot(Rectangle mapInnerBounds, Rectangle overviewBounds, double scale, Dimension extent) {
        AffineTransform translation = AffineTransform.getTranslateInstance(extent.width - mapInnerBounds.x,
                extent.height - mapInnerBounds.y);
        AffineTransform transformer = AffineTransform.getScaleInstance(scale, scale);
        transformer.concatenate(translation);

        BufferedImage image = new BufferedImage(overviewBounds.width, overviewBounds.height,
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D imageG2D = image.createGraphics();
        try {
            imageG2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            imageG2D.setColor(mapView.getBackground());
            imageG2D.fillRect(0, 0, overviewBounds.width, overviewBounds.height);
            imageG2D.transform(transformer);
            imageG2D.clip(mapInnerBounds);
            mapView.paintOverview(imageG2D);
        } finally {
            imageG2D.dispose();
        }
        return image;
    }

    private void drawViewportThumbnail(Graphics2D g2d, Rectangle mapInnerBounds, double scale, Dimension extent) {
        JViewport viewPort = (JViewport) mapView.getParent();
        Point viewPortPosition = viewPort.getViewPosition();
        Rectangle viewPortRect = viewPort.getBounds();
        Rectangle thumbRect = new Rectangle(viewPortRect);
        thumbRect.x = viewPortPosition.x - mapInnerBounds.x + extent.width;
        thumbRect.y = viewPortPosition.y - mapInnerBounds.y + extent.height;
        AffineTransform transformed = g2d.getTransform();
        AffineTransform transformer = AffineTransform.getScaleInstance(scale / transformed.getScaleX(),
                scale / transformed.getScaleY());
        Rectangle scaledThumbRect = transformer.createTransformedShape(thumbRect).getBounds();
        g2d.setColor(VIEWPORT_THUMBNAIL_COLOR);
        g2d.fillRect(scaledThumbRect.x, scaledThumbRect.y, scaledThumbRect.width, scaledThumbRect.height);
        g2d.setColor(VIEWPORT_THUMBNAIL_COLOR.darker());
        g2d.drawRect(scaledThumbRect.x, scaledThumbRect.y, scaledThumbRect.width - 1, scaledThumbRect.height - 1);
    }

}
