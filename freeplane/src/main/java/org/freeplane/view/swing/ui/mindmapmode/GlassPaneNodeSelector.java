/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2011 dimitry
 *
 *  This file author is dimitry
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
package org.freeplane.view.swing.ui.mindmapmode;

import java.awt.Component;
import java.awt.Container;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JScrollBar;
import javax.swing.SwingUtilities;

import org.freeplane.core.ui.IMouseListener;
import org.freeplane.features.map.MapController;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.view.swing.map.MainView;
import org.freeplane.view.swing.map.MapView;
import org.freeplane.view.swing.map.NodeView;

/**
 * @author Dimitry Polivaev
 * Mar 3, 2011
 */
class GlassPaneNodeSelector extends MouseAdapter implements IMouseListener{

    /**
     * 
     */
    private final INodeSelector nodeSelector;
	private Component activeComponent;
	/**
     * @param nodeSelector
     */
    GlassPaneNodeSelector(INodeSelector nodeSelector) {
        this.nodeSelector = nodeSelector;
    }

	public void mouseMoved(MouseEvent e) {
    }

    public void mouseDragged(MouseEvent e) {
        final Component component = findMapComponent(e);
        if(canRedispatchEventFor(component)){
        	redispatchMouseEvent(e, component);
        }
    }

    public void mouseClicked(MouseEvent e) {
    	if(e.getButton() != 1){
    		return;
    	}
        final Component component = findMapComponent(e);
        if(! (component instanceof MainView)){
        	return;
        }
        MainView mainView = (MainView) component;
        NodeView nodeView = mainView.getNodeView();
		final NodeModel node = nodeView.getModel();
        switch(e.getClickCount()){
        	case 1:
        		final MapController mapController = Controller.getCurrentModeController().getMapController();
				mapController.toggleFolded(node);
        		break;
        	case 2:
    	        nodeSelector.nodeSelected(node);
        		break;
        }
    }

	public boolean canRedispatchEventFor(final Component component) {
        if (component instanceof MapView)
        	return true;
        if (component instanceof JScrollBar || SwingUtilities.getAncestorOfClass(JScrollBar.class, component) != null)
        	return true;
        return false;
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
    	if(e.getButton() != 1){
    		return;
    	}
        final Component component = findMapComponent(e);
        if(canRedispatchEventFor(component)){
        	redispatchMouseEvent(e, component);
        	activeComponent = component;
        }
    }

    public void mouseReleased(MouseEvent e) {
    	if(e.getButton() != 1){
    		return;
    	}
        if(activeComponent != null){
        	redispatchMouseEvent(e, activeComponent);
        	activeComponent = null;
        }
    }

    //A basic implementation of redispatching events.
    private Component findMapComponent(MouseEvent e) {
    	final Component glassPane = e.getComponent();
    	final Point glassPanePoint = e.getPoint();
    	final Container container = SwingUtilities.getRootPane(glassPane).getContentPane();
    	Point containerPoint = SwingUtilities.convertPoint(
    		glassPane,
    		glassPanePoint,
    		container);
    	Component component = 
    		SwingUtilities.getDeepestComponentAt(
    			container,
    			containerPoint.x,
    			containerPoint.y);
    	if(component instanceof MainView || component instanceof MapView || component instanceof JScrollBar){
	    	return component;
    	}
    	return SwingUtilities.getAncestorOfClass(MapView.class, component);
    }
	    public void redispatchMouseEvent(MouseEvent e, final Component component) {
        final Component glassPane = e.getComponent();
        final Point glassPanePoint = e.getPoint();
        Point componentPoint = SwingUtilities.convertPoint(
        	glassPane,
        	glassPanePoint,
        	component);
        component.dispatchEvent(new MouseEvent(component,
        	e.getID(),
        	e.getWhen(),
        	e.getModifiers(),
        	componentPoint.x,
        	componentPoint.y,
        	e.getClickCount(),
        	e.isPopupTrigger()));
    }

}