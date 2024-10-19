/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Dimitry Polivaev
 *
 *  This file author is Dimitry Polivaev
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.freeplane.view.swing.ui;

import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import org.freeplane.core.ui.ControllerPopupMenuListener;
import org.freeplane.core.ui.IMouseListener;
import org.freeplane.core.ui.components.PopupDialog;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.Compat;
import org.freeplane.features.link.Connectors;
import org.freeplane.features.link.LinkController;
import org.freeplane.features.map.IMapSelection;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.view.swing.map.MapView;

/**
 * @author Dimitry Polivaev
 * 06.01.2009
 */
/**
 * The MouseListener which belongs to MapView
 */
public class DefaultMapMouseListener implements IMouseListener {
    protected int originX = -1;
    protected int originY = -1;
    private RectangleMemorizer rectangleMemorizer;


	public DefaultMapMouseListener() {
	    rectangleMemorizer = new RectangleMemorizer();
	}

	protected void handlePopup(final MouseEvent e) {
		if (e.isPopupTrigger()) {
			Component popup = null;
			final Component popupForModel;
			final MapView mapView = (MapView) Controller.getCurrentController().getMapViewManager().getMapViewComponent();
			final ModeController modeController = Controller.getCurrentController().getModeController();
			if(isMapViewWithOriginalConectosAvailable(mapView)){
				final java.lang.Object obj = mapView.detectObject(e.getPoint());
				popupForModel= LinkController.getController(modeController).getPopupForModel(obj);
			}
			else{
				popupForModel = null;
			}
			if (popupForModel != null) {
				final ControllerPopupMenuListener popupListener = new ControllerPopupMenuListener();
				popupForModel.addHierarchyListener(popupListener);
				popup = popupForModel;
			}
			else {
				popup = modeController.getUserInputListenerFactory().getMapPopup();
			}
            Component component = e.getComponent();
			if(popup instanceof JPopupMenu) {
                ((JPopupMenu)popup).show(component, e.getX(), e.getY());
            }
			else {
			    final Component window;
			    if(popup instanceof Window){
			        window= popup;
			    }
			    else{
			    	window = PopupDialog.createOptionPanelPopupDialog(popup);
			    }
			    Point eventLocation = e.getPoint();
			    SwingUtilities.convertPointToScreen(eventLocation, e.getComponent());
			    UITools.setBounds(window, eventLocation.x, eventLocation.y, window.getWidth(), window.getHeight());
			    window.setVisible(true);
			}
			e.consume();
		}
	}

	private boolean isMapViewWithOriginalConectosAvailable(MapView mapView) {
	    return mapView != null && mapView.getClientProperty(Connectors.class) == null;
	}

	@Override
	public void mouseClicked(final MouseEvent e) {
		final Object source = e.getSource();
		if(! (source instanceof MapView))
			return;
		final MapView map = (MapView) source;
		final Controller controller = map.getModeController().getController();
		final IMapSelection selection = controller.getSelection();
		if(selection != null){
			final NodeModel selected = selection.getSelected();
			if(selected != null)
				controller.getMapViewManager().getComponent(selected).requestFocusInWindow();
		}
	}

	@Override
	public void mouseEntered(final MouseEvent e) {
	}

	@Override
	public void mouseExited(final MouseEvent e) {
	}

	@Override
	public void mouseMoved(final MouseEvent e) {
	}

	@Override
	public void mousePressed(final MouseEvent e) {
		final MapView mapView = MapView.getMapView(e.getComponent());
		if(mapView != null)
			mapView.select();
		if (e.isPopupTrigger()) {
			handlePopup(e);
            e.consume();
            return;
		}
		if((e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) != 0)
		    rectangleMemorizer.mousePressed(e);
        if(! e.isConsumed() && e.getButton() == MouseEvent.BUTTON1){
			if(mapView != null){
				mapView.setMoveCursor(true);
				originX = e.getX();
				originY = e.getY();
			}
			e.consume();
		}
	}

    @Override
	public void mouseReleased(final MouseEvent e) {
        rectangleMemorizer.mouseReleased(e);
        if(e.isConsumed())
            return;
		final MapView mapView = MapView.getMapView(e.getComponent());
		if(mapView != null)
			mapView.setMoveCursor(false);
		originX = -1;
		originY = -1;
		handlePopup(e);
	}
	/**
	 *
	 */
	@Override
	public void mouseDragged(final MouseEvent e) {
        rectangleMemorizer.mouseDragged(e);
        if(e.isConsumed())
            return;
		final JComponent component = (JComponent) e.getComponent();
		final MapView mapView = MapView.getMapView(component);
		if(mapView == null)
			return;
		if (originX >= 0) {
			final int dx = originX - e.getX();
			final int dy = originY - e.getY();
			final Rectangle visibleRect = component.getVisibleRect();
			final Rectangle r = new Rectangle(e.getX(), e.getY(), 1, 1);
			final boolean isEventPointVisible = visibleRect.contains(r);
			if (isEventPointVisible)
	            mapView.scrollBy(dx, dy);
            else {
				mapView.scrollBy(dx/3, dy/3);
				originX += dx/3;
				originY += dy/3;
			}
			e.consume();
		}
	}
}
