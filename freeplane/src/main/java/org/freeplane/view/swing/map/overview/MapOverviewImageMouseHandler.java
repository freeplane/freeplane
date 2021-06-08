package org.freeplane.view.swing.map.overview;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import javax.swing.BoundedRangeModel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputAdapter;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.ui.IMapViewManager;
import org.freeplane.view.swing.map.MapView;

class MapOverviewImageMouseHandler extends MouseInputAdapter {
    private static final String ZOOM_AROUND_SELECTED_NODE_PROPERTY = "zoomAroundSelectedNode";

    private final MapView mapView;

    MapOverviewImageMouseHandler(MapView mapView) {
        this.mapView = mapView;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.isPopupTrigger()) {
            MapOverviewImage image = (MapOverviewImage) e.getComponent();
            image.showPopupMenu(e.getX(), e.getY());
        } else if (e.getButton() == 1) {
            processMousePanEvent(e);
        }
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
        double zoomFactor = 1.25;
        double zoom = e.getWheelRotation() > 0 ? (oldZoom / zoomFactor) : (oldZoom * zoomFactor);
        double x = Math.round(Math.log(zoom) / Math.log(1.25));
        zoom = Math.pow(1.25, x);
        zoom = Math.max(Math.min(zoom, 32f), 0.03f);
        IMapViewManager viewManager = Controller.getCurrentController().getMapViewManager();
        viewManager.changeToMapView(mapView);
        final ResourceController resourceController = ResourceController.getResourceController();
        if (! resourceController.getBooleanProperty(ZOOM_AROUND_SELECTED_NODE_PROPERTY)) {
            MapOverviewImage image = (MapOverviewImage) e.getComponent();
            final Point keptPoint = convertToMapViewPoint(image, e.getPoint());
            scrollTo(keptPoint);
            mapView.setZoom((float) zoom, keptPoint);
        }
        viewManager.setZoom((float) zoom);
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        processMousePanEvent(e);
    }

    private void processMousePanEvent(MouseEvent e) {
        MapOverviewImage image = (MapOverviewImage) e.getComponent();
        final Point newCenterPoint = convertToMapViewPoint(image, e.getPoint());
        scrollTo(newCenterPoint);
    }

	private void scrollTo(final Point newCenterPoint) {
		JScrollPane mapViewScrollPane = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, mapView);
        setScrollingValue(newCenterPoint.x, mapViewScrollPane.getHorizontalScrollBar().getModel());
        setScrollingValue(newCenterPoint.y, mapViewScrollPane.getVerticalScrollBar().getModel());
	}

    private void setScrollingValue(int value, BoundedRangeModel model) {
        model.setValue(Math.max(value - model.getExtent() / 2, 0));
    }

    private Point convertToMapViewPoint(MapOverviewImage image, Point overviewPoint) {
        Rectangle innerBounds = mapView.getInnerBounds();
        Rectangle mapOverviewBounds = image.getBounds();
        Dimension source = innerBounds.getSize();
        Dimension target = mapOverviewBounds.getSize();
        double scale = image.getBestScale(source, target);
        double extendedWidth = (target.getWidth() / scale - source.getWidth()) / 2d;
        double extendedHeight = (target.getHeight() / scale - source.getHeight()) / 2d;
        int x = (int) (overviewPoint.x / scale - extendedWidth + innerBounds.x);
        int y = (int) (overviewPoint.y / scale - extendedHeight + innerBounds.y);
        Point point = new Point(x, y);
//System.out.println("" + overviewPoint + ", " + innerBounds + ", " + mapOverviewBounds + ", " + source + ", " + 
//        target + ", " +  scale + ", " +  extendedWidth + ", " + extendedHeight + ", " + point);        
        return point;
    }
};
