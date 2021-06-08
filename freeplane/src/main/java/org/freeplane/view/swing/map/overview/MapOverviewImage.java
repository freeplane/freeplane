package org.freeplane.view.swing.map.overview;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.MenuItem;
import java.awt.Point;
import java.awt.PopupMenu;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import javax.swing.JComponent;
import javax.swing.JViewport;

import org.freeplane.api.LengthUnit;
import org.freeplane.api.Quantity;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.ui.IMapViewManager;
import org.freeplane.view.swing.map.MapView;
import org.freeplane.view.swing.map.MapViewController;

class MapOverviewImage extends JComponent {
    private static final long serialVersionUID = 1L;

    private static final Color VIEWPORT_THUMBNAIL_COLOR = new Color(0x32_00_00_FF, true);
    private static final float FONT_SCALE = 0.75F;
    private static final int FONT_RIGHT_MARGIN = new Quantity<LengthUnit>(3, LengthUnit.pt).toBaseUnitsRounded();

    private BufferedImage image;
    private MapView mapView;
    private PopupMenu popupMenu;

    MapOverviewImage(MapView mapView) {
        this.mapView = mapView;
        MapOverviewImageMouseHandler handler = new MapOverviewImageMouseHandler(mapView);
        addMouseListener(handler);
        addMouseMotionListener(handler);
        addMouseWheelListener(handler);
        popupMenu = new PopupMenu();
        MenuItem hideItem = new MenuItem(TextUtils.getText("map_overview_hide"));
        hideItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Controller.getCurrentController().getViewController().setMapOverviewVisible(false);
            }
        });
        popupMenu.add(hideItem);
        add(popupMenu);
    }

    void showPopupMenu(int x, int y) {
        popupMenu.show(this, x, y);
    }

    void resetImage() {
        image = null;
    }

    double getBestScale(Dimension source, Dimension target) {
        double tw = target.getWidth();
        double sw = source.getWidth();
        double th = target.getHeight();
        double sh = source.getHeight();
        double scale = ((sw / sh) > (tw / th)) ? tw / sw : th / sh;
        return scale;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Rectangle overviewBounds = getBounds();
        Rectangle mapInnerBounds = mapView.getInnerBounds();
        Graphics2D g2d = (Graphics2D) g;
        final AffineTransform transform = g2d.getTransform();
        double scaleX = transform.getScaleX();
        AffineTransform overviewTransform = AffineTransform.getScaleInstance(transform.getScaleX(),
                transform.getScaleY());
        overviewBounds = overviewTransform.createTransformedShape(overviewBounds).getBounds();
        Dimension source = mapInnerBounds.getSize();
        Dimension target = overviewBounds.getSize();
        double scale = getBestScale(source, target);
        int extendedWidth = (int)Math.ceil ((target.getWidth() / scale - source.getWidth()) / 2);
        int extendedHeight = (int)Math.ceil ((target.getHeight() / scale - source.getHeight()) / 2);
        Dimension extendedSize = new Dimension(extendedWidth, extendedHeight);
        if (image == null || image.getWidth() != (int) overviewBounds.width * scaleX) {
            image = snapshot(mapInnerBounds, overviewBounds, scale, extendedSize);
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

        drawViewportThumbnail(g2d, mapInnerBounds, scale, extendedSize);
        drawZoomLevel(g2d);
    }

    private void drawZoomLevel(Graphics2D g2d) {
    	String zoomLevel = null;
    	IMapViewManager mapViewManager = Controller.getCurrentController().getMapViewManager();
    	if (mapViewManager instanceof MapViewController) {
    		zoomLevel = ((MapViewController) mapViewManager).getItemForZoom(mapView.getZoom());
    	}
    	if (zoomLevel == null)
    		return;

    	Dimension imageSize = getSize();
        int borderSize = MapViewPane.MAP_OVERVIEW_BORDER_SIZE;
        Point zoomLevelLoc = new Point(borderSize, imageSize.height - borderSize);
        Color invBgColor = complementaryColor(mapView.getBackground());
        Color textColor = new Color(invBgColor.getRed(), invBgColor.getGreen(), invBgColor.getBlue(), 0x5F);
        g2d.setColor(textColor);
        final Font origFont = getFont();
        g2d.setFont(origFont.deriveFont(origFont.getSize() * FONT_SCALE));
        g2d.drawChars(zoomLevel.toCharArray(), 0, zoomLevel.length(), zoomLevelLoc.x, zoomLevelLoc.y);

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
