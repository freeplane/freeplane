package org.freeplane.view.swing.map.overview;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.event.MouseInputAdapter;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.ui.IMapViewManager;
import org.freeplane.view.swing.map.MapView;

class MapOverviewImageMouseHandler extends MouseInputAdapter {
    private static final String ZOOM_AROUND_SELECTED_NODE_PROPERTY = "zoomAroundSelectedNode";

    private final MapView mapView;

    private final JScrollPane mapViewScrollPane;

    MapOverviewImageMouseHandler(MapView mapView, JScrollPane mapViewScrollPane) {
        this.mapView = mapView;
        this.mapViewScrollPane = mapViewScrollPane;
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
            scrollTo(image, e.getPoint());
            Dimension viewportSize = mapViewScrollPane.getViewport().getExtentSize();
            Point mapViewLocation = mapView.getLocation();
            Point keptPoint = new Point(-mapViewLocation.x + viewportSize.width / 2, 
                    -mapViewLocation.y + viewportSize.height / 2);
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
        scrollTo(image, e.getPoint());
    }

    private void scrollTo(MapOverviewImage overview, Point newCenterPointOnOverview) {
        Rectangle mapBounds = mapView.getRoot().getBounds();
        Dimension mapSize = mapBounds.getSize();
        Rectangle overviewBounds = overview.getBounds();
        Dimension overviewSize = overviewBounds.getSize();
        double scale = overview.getBestScale(mapSize, overviewSize);
        System.out.println(mapBounds.width + "/" + scale);
        int minimumValidX = (int) ((overviewSize.width  - mapSize.width * scale) / 2);
        if(newCenterPointOnOverview.x < minimumValidX)
            newCenterPointOnOverview.x = minimumValidX;
        else if (0 < minimumValidX){
            int maximumValidX = overviewSize.width - minimumValidX;
            if (newCenterPointOnOverview.x > maximumValidX)
                newCenterPointOnOverview.x = maximumValidX;
        }
        int minimumValidY = (int) ((overviewSize.height  - mapSize.height * scale) / 2);
        if(newCenterPointOnOverview.y < minimumValidY)
            newCenterPointOnOverview.y = minimumValidY;
        else if (0 < minimumValidY){
            int maximumValidY = overviewSize.height - minimumValidY;
            if (newCenterPointOnOverview.y > maximumValidY)
                newCenterPointOnOverview.y = maximumValidY;
        }
        Point mapLocation = mapBounds.getLocation();
        JViewport viewport = mapViewScrollPane.getViewport();
        Dimension viewportSize = viewport.getExtentSize();
        Point newViewPosition = new Point(
                mapLocation.x
                    + (int)((newCenterPointOnOverview.x - overviewSize.width / 2) / scale)
                    + (mapSize.width - viewportSize.width) / 2,
                mapLocation.y 
                    + (int)((newCenterPointOnOverview.y - overviewSize.height / 2) / scale)
                    + (mapSize.height -viewportSize.height) / 2
                );
        viewport.setViewPosition(newViewPosition);
    }
};
