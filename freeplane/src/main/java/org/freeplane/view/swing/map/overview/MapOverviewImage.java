package org.freeplane.view.swing.map.overview;

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
import org.freeplane.view.swing.map.overview.MapOverviewUtils.MapOverviewAttachPoint;

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
        double scale = MapOverviewUtils.getBestScale(mapInnerBounds.getSize(), overviewBounds.getSize(), extentedSize);
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
        drawAnchorAndZoomLevel(g);
    }

    private void drawAnchorAndZoomLevel(Graphics g) {
        Dimension overviewSize = getSize();

        String anchorSymbol = Compat.isMacOsX() ? "+" : "\u2693"; // "⚓"
        Point anchorLocation = new Point();
        Point zoomLevelLocation = new Point(4, overviewSize.height - 8);
        MapOverviewAttachPoint anchorPoint = MapOverviewUtils.getMapOverviewAttachPoint();
        switch (anchorPoint) {
        case SOUTH_EAST:
            anchorLocation.setLocation(overviewSize.width - 16, overviewSize.height - 8);
            break;
        case SOUTH_WEST:
            anchorLocation.setLocation(4, overviewSize.height - 8);
            break;
        case NORTH_EAST:
            anchorLocation.setLocation(overviewSize.width - 16, 16);
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
        g.setColor(MapOverviewUtils.complementaryColor(mapView.getBackground()));

        if (anchorPoint == MapOverviewAttachPoint.SOUTH_WEST && zoomLevelString != null) {
            anchorSymbol += " " + zoomLevelString;
        } else if (zoomLevelString != null) {
            g.drawChars(zoomLevelString.toCharArray(), 0, zoomLevelString.length(), zoomLevelLocation.x,
                    zoomLevelLocation.y);
        }
        g.drawChars(anchorSymbol.toCharArray(), 0, anchorSymbol.length(), anchorLocation.x, anchorLocation.y);
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
        g2d.setColor(MapOverviewConstants.VIEWPORT_THUMBNAIL_COLOR);
        g2d.fillRect(scaledThumbRect.x, scaledThumbRect.y, scaledThumbRect.width, scaledThumbRect.height);
        g2d.setColor(MapOverviewConstants.VIEWPORT_THUMBNAIL_COLOR.darker());
        g2d.drawRect(scaledThumbRect.x, scaledThumbRect.y, scaledThumbRect.width - 1, scaledThumbRect.height - 1);
    }

    public void resetImage() {
        image = null;
    }

}
