/*
 * Created on 19 Oct 2024
 *
 * author dimitry
 */
package org.freeplane.view.swing.ui;

import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import org.freeplane.view.swing.map.MapView;

class RectangleMemorizer extends MouseAdapter {
    private Point startPoint;
    private MapView mapView;

    public RectangleMemorizer() {
    }


    @Override
    public void mousePressed(MouseEvent e) {
        Component component = e.getComponent();
        if(component instanceof MapView) {
            startPoint = e.getPoint();
            mapView = (MapView) component;
            e.consume();
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (startPoint == null)
            return;
        e.consume();
        mapView.selectNodeViewBySelectionRectangle();
        mapView.setSelectionRectangle(null);
        startPoint = null;
        mapView = null;
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (startPoint == null)
            return;
        e.consume();
        Point endPoint = e.getPoint();
        Rectangle newRectangle = new Rectangle(
                Math.min(startPoint.x, endPoint.x),
                Math.min(startPoint.y, endPoint.y),
                Math.abs(startPoint.x - endPoint.x),
                Math.abs(startPoint.y - endPoint.y)
        );
        mapView.setSelectionRectangle(newRectangle);
    }

}
