package org.freeplane.view.swing.map.overview;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import javax.swing.BoundedRangeModel;
import javax.swing.JScrollPane;
import javax.swing.event.MouseInputAdapter;

import org.freeplane.features.mode.Controller;
import org.freeplane.features.ui.IMapViewManager;
import org.freeplane.view.swing.map.MapView;

class MapOverviewImageMouseHandler extends MouseInputAdapter {
    private final MapView mapView;

    MapOverviewImageMouseHandler(MapView mapView) {
        this.mapView = mapView;
    }

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

    private void processMousePanEvent(MouseEvent e) {
        Rectangle innerBounds = mapView.getInnerBounds();
        Rectangle mapOverviewBounds = e.getComponent().getBounds();
        Dimension extension = new Dimension();
        MapOverviewImage image = (MapOverviewImage) e.getComponent();
        double scale = image.getBestScale(innerBounds.getSize(), mapOverviewBounds.getSize(), extension);
        Point target = e.getPoint();
        setHorizontalRange((int) (target.x - extension.width * scale), scale, innerBounds);
        setVerticalRange((int) (target.y - extension.height * scale), scale, innerBounds);
    }

    private final void setVerticalRange(int y, double scale, Rectangle innerBounds) {
        JScrollPane mapViewScrollPane = (JScrollPane) (mapView.getParent().getParent());
        BoundedRangeModel model = mapViewScrollPane.getVerticalScrollBar().getModel();
        int value = (int) (y / scale + innerBounds.y) - model.getExtent() / 2;
        model.setValue(Math.max(value, 0));
    }

    private final void setHorizontalRange(int x, double scale, Rectangle innerBounds) {
        JScrollPane mapViewScrollPane = (JScrollPane) (mapView.getParent().getParent());
        BoundedRangeModel model = mapViewScrollPane.getHorizontalScrollBar().getModel();
        int value = (int) (x / scale + innerBounds.x) - model.getExtent() / 2;
        model.setValue(Math.max(value, 0));
    }
};
