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
import javax.swing.JTable;
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

	@Override
	public void mouseMoved(MouseEvent e) {
    }

    @Override
	public void mouseDragged(MouseEvent e) {
        final Component component = findMapComponent(e);
        if(canRedispatchEventFor(component)){
        	redispatchMouseEvent(e, component);
        }
    }

    @Override
	public void mouseClicked(MouseEvent e) {
    	if(e.getButton() != 1){
    		return;
    	}
        final Component component = findMapComponent(e);

        if(component instanceof JTable) {
        	mouseClickedOnTable((JTable)component, e);
        }

        else if(component instanceof MainView){
        	mouseClickedOnNode(e, (MainView) component);
        }
    }

	private void mouseClickedOnNode(MouseEvent e, MainView mainView) {
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

	private void mouseClickedOnTable(JTable table, MouseEvent e) {
		if(e.getClickCount() != 2)
			return;
		NodeView nodeView = (NodeView) SwingUtilities.getAncestorOfClass(NodeView.class, table);
		if(nodeView == null)
			return;
		Point pointAtTable = getPointAtComponent(e, table);
		final int selectedRow = table.rowAtPoint(pointAtTable );
		if(selectedRow < 0 || selectedRow >= table.getRowCount())
			return;
		String rowName = (String) table.getValueAt(selectedRow, 0);
		final NodeModel node = nodeView.getModel();
		nodeSelector.tableRowSelected(node, rowName);
	}

	public boolean canRedispatchEventFor(final Component component) {
        if (component instanceof MapView)
        	return true;
        if (component instanceof JScrollBar || SwingUtilities.getAncestorOfClass(JScrollBar.class, component) != null)
        	return true;
        return false;
    }

    @Override
	public void mouseEntered(MouseEvent e) {
    }

    @Override
	public void mouseExited(MouseEvent e) {
    }

    @Override
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

    @Override
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
    	if(component instanceof MainView
    			|| component instanceof JTable
    			|| component instanceof MapView
    			|| component instanceof JScrollBar){
	    	return component;
    	}
    	Container table = SwingUtilities.getAncestorOfClass(JTable.class, component);
    	if(table != null)
    		return table;
    	return SwingUtilities.getAncestorOfClass(MapView.class, component);
    }
	    private void redispatchMouseEvent(MouseEvent e, final Component component) {
        final MouseEvent componentEvent = convertToComponentEvent(e, component);
		component.dispatchEvent(componentEvent);
    }

		private MouseEvent convertToComponentEvent(MouseEvent e, final Component component) {
			Point componentPoint = getPointAtComponent(e, component);
			final MouseEvent componentEvent = new MouseEvent(component,
				e.getID(),
				e.getWhen(),
				e.getModifiers(),
				componentPoint.x,
				componentPoint.y,
				e.getClickCount(),
				e.isPopupTrigger());
			return componentEvent;
		}

		private Point getPointAtComponent(MouseEvent e, final Component component) {
			final Component glassPane = e.getComponent();
			final Point glassPanePoint = e.getPoint();
			Point componentPoint = SwingUtilities.convertPoint(
				glassPane,
				glassPanePoint,
				component);
			return componentPoint;
		}

}