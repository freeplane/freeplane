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
        if (e.getButton() == 3) {
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
        double zoomFactor = Math.pow(1.25, e.getScrollAmount());
        float zoom = (float) (e.getWheelRotation() > 0 ? (oldZoom / zoomFactor) : (oldZoom * zoomFactor));
        zoom = Math.max(Math.min(zoom, 32f), 0.03f);
        IMapViewManager viewManager = Controller.getCurrentController().getMapViewManager();
        viewManager.changeToMapView(mapView);
        final ResourceController resourceController = ResourceController.getResourceController();
        if (! resourceController.getBooleanProperty(ZOOM_AROUND_SELECTED_NODE_PROPERTY)) {
            MapOverviewImage image = (MapOverviewImage) e.getComponent();
            final Point keptPoint = convertToMapViewPoint(image, e.getPoint());
            mapView.setZoom(zoom, keptPoint);
        }
        viewManager.setZoom(zoom);
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        processMousePanEvent(e);
    }

    private void processMousePanEvent(MouseEvent e) {
        MapOverviewImage image = (MapOverviewImage) e.getComponent();
        final Point keptPoint = convertToMapViewPoint(image, e.getPoint());
        JScrollPane mapViewScrollPane = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, mapView);
        setScrollingValue(keptPoint.x, mapViewScrollPane.getHorizontalScrollBar().getModel());
        setScrollingValue(keptPoint.y, mapViewScrollPane.getVerticalScrollBar().getModel());
    }

    private void setScrollingValue(int value, BoundedRangeModel model) {
        model.setValue(Math.max(value - model.getExtent() / 2, 0));
    }

    private Point convertToMapViewPoint(MapOverviewImage image, Point overviewPoint) {
        Rectangle innerBounds = mapView.getInnerBounds();
        Rectangle mapOverviewBounds = image.getBounds();
        Dimension extension = new Dimension();
        double scale = image.getBestScale(innerBounds.getSize(), mapOverviewBounds.getSize(), extension);
        int x = (int) (overviewPoint.x / scale - extension.width + innerBounds.x);
        int y = (int) (overviewPoint.y / scale - extension.height + innerBounds.y);
        return new Point(x, y);
    }
};
